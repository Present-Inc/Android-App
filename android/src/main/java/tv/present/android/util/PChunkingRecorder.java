package tv.present.android.util;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.opengl.EGLContext;
import android.opengl.GLSurfaceView;
import android.os.Trace;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Present Chunking Recorder
 *
 * This class manages recording and chunking the resulting video data so that it can be uploaded
 * to the server.
 *
 * June 18, 2014
 * @author Kyle Weisel (kyle@present.tv)
 *
 */
public final class PChunkingRecorder {

    private static final String TAG = "tv.present.android.util.PChunkingRecorder";

    private static final boolean VERBOSE = false;
    private static final boolean TRACE = true;
    
    // Multimedia Settings
    private static final long MULTIMEDIA_CHUNK_DURATION_SEC = 10;
    private final int FRAMES_PER_CHUNK = (int) MULTIMEDIA_CHUNK_DURATION_SEC * VIDEO_FRAME_RATE;
    
    // Audio Settings
    private static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";
    private static final int AUDIO_SAMPLE_RATE = 44100;
    private static final int AUDIO_SAMPLES_PER_FRAME = 1024;
    private static final int AUDIO_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    
    // Video settings
    private static final int VIDEO_HEIGHT = 480;
    private static final int VIDEO_FRAME_RATE = 30;
    private static final int VIDEO_IFRAME_INTERVAL = 2;
    private static final String VIDEO_MIME_TYPE = "video/avc";
    private static final int VIDEO_OUTPUT_FORMAT = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4;
    private static final int VIDEO_WIDTH = 480;
    private static final int VIDEO_ENCODER_BIT_RATE = 1000000;

    // Display Surface
    private GLSurfaceView displaySurface;
    private MediaCodec videoEncoder;
    private MediaCodec audioEncoder;
    private CodecInputSurface codecInputSurface;
    private MediaMuxerWrapper muxerWrapper1;
    private MediaMuxerWrapper muxerWrapper2;
    private SurfaceTextureManager surfaceTextureManager;
    private TrackInfo audioTrackInfo;
    private TrackInfo videoTrackInfo;

    // Camera
    private Camera camera;

    // Audio
    private AudioRecord audioRecord;
    private long lastEncodedAudioTimeStamp = 0;

    // Audio/video buffering
    private MediaCodec.BufferInfo videoBufferInfo;
    private MediaCodec.BufferInfo audioBufferInfo;
    
    // Audio/video formats
    private MediaFormat videoFormat;
    private MediaFormat audioFormat;
    private MediaFormat videoOutputFormat;
    private MediaFormat audioOutputFormat;

    // Markers & IDs
    private int leadingChunk = 1;
    private long startWhen = 0;
    private int frameCount = 0;
    
    // Control booleans
    private boolean audioEosRequested = false;
    private boolean eosSentToAudioEncoder = false;
    private boolean eosSentToVideoEncoder = false;
    private boolean fullStopPerformed = false;
    private boolean fullStopReceived = false;
    private boolean firstFrameReady;
    private boolean eosReceived;

    // Debug values
    private int totalFrameCount = 0;
    private long startTime;
    private long endTime;

    private Context context;

    /**
     * Constructs a PChunkingRecorder.
     * @param context is a Context to create the recorder with.
     */
    public PChunkingRecorder(Context context) {
        this.context = context;
        this.firstFrameReady = false;
        this.eosReceived = false;
    }

    public void setDisplaySurface(GLSurfaceView displaySurface) {
        this.displaySurface = displaySurface;
    }

    public void setDisplayEGLContext(EGLContext context) {
        this.codecInputSurface.mEGLDisplayContext = context;
    }

    /**
     * Starts the recording process.
     */
    public void startRecording() {

        PLog.logDebug(TAG, "startRecording() -> " + VIDEO_MIME_TYPE + " output " + VIDEO_WIDTH + "x" + VIDEO_HEIGHT + " @" + VIDEO_ENCODER_BIT_RATE);

        try {
            this.prepareCamera(VIDEO_WIDTH, VIDEO_HEIGHT, Camera.CameraInfo.CAMERA_FACING_BACK);
            this.prepareEncoder(VIDEO_WIDTH, VIDEO_HEIGHT, VIDEO_ENCODER_BIT_RATE);
            this.codecInputSurface.makeEncodeContextCurrent();
            this.prepareSurfaceTexture();
            this.setupAudioRecord();
            this.startAudioRecord();
            this.startWhen = System.nanoTime();
            this.camera.startPreview();
            SurfaceTexture surfaceTexture = this.surfaceTextureManager.getSurfaceTexture();
            this.eosReceived = false;

            // Feed any pending encoder output to the muxer and chunk the media if necessary.
            while (!(this.fullStopReceived && this.eosSentToVideoEncoder)) {

                this.eosReceived = ((this.frameCount % FRAMES_PER_CHUNK) == 0 && this.frameCount != 0);

                if (this.eosReceived) {
                    PLog.logDebug(TAG, "Chunkpoint on frame " + this.frameCount);
                }

                this.audioEosRequested = this.eosReceived;

                // Drain the video from the encoder
                synchronized (videoTrackInfo.muxerWrapper.sync) {
                    drainEncoder(this.videoEncoder, this.videoBufferInfo, this.videoTrackInfo, (this.eosReceived || this.fullStopReceived));
                }

                // Jump out of the while loop when a full stop is requested.
                if (this.fullStopReceived) {
                    break;
                }

                // Update frame count and total frame count
                this.frameCount++;
                this.totalFrameCount++;

                // Acquire a new frame of input, and render it to the Surface.  If we had a
                // GLSurfaceView we could switch EGL contexts and call drawImage() a second
                // time to render it on screen.  The texture can be shared between contexts by
                // passing the GLSurfaceView's EGLContext as eglCreateContext()'s share_context
                // argument.
                this.surfaceTextureManager.awaitNewImage();
                this.surfaceTextureManager.drawImage();

                // Set the presentation time stamp from the SurfaceTexture's time stamp.  This
                // will be used by MediaMuxer to set the PTS in the video.
                this.codecInputSurface.setPresentationTime(surfaceTexture.getTimestamp() - startWhen);

                // Submit it to the encoder.  The eglSwapBuffers call will block if the input
                // is full, which would be bad if it stayed full until we dequeued an output
                // buffer (which we can't do, since we're stuck here).  So long as we fully drain
                // the encoder before supplying additional input, the system guarantees that we
                // can supply another frame without blocking.
                PLog.logDebug(TAG, "startRecording() -> sending frame to encoder.");
                this.codecInputSurface.swapBuffers();

                if (! this.firstFrameReady) {
                    startTime = System.nanoTime();
                    this.firstFrameReady = true;
                }

            }

            PLog.logDebug(TAG, "startRecording() -> Exiting video encode loop.");

        } catch (Exception e) {
            PLog.logError(TAG, "Encoding loop exception!");
            e.printStackTrace();
        }

    }

    /**
     * Stops the recording process.
     */
    public void stopRecording() {
        PLog.logDebug(TAG, "stopRecording() -> Method called.");
        this.fullStopReceived = true;
        final double recordingDurationSec = (System.nanoTime() - startTime) / 1000000000.0;
        PLog.logDebug(TAG, "Recorded " + recordingDurationSec + " seconds. Expected " + (VIDEO_FRAME_RATE * recordingDurationSec) + " frames. Got " + totalFrameCount + " for " + (totalFrameCount / recordingDurationSec) + " fps");
    }

    /**
     * Called internally to finalize HQ and last chunk
     */
    private void _stopRecording() {
        fullStopPerformed = true;
        //mMediaRecorderWrapper.stopRecording();
        releaseCamera();
        releaseEncodersAndMuxer();
        releaseSurfaceTexture();
        if (codecInputSurface != null) {
            codecInputSurface.release();
            codecInputSurface = null;
        }
    }

    private void setupAudioRecord() {
        int min_buffer_size = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AUDIO_CHANNEL_CONFIG, AUDIO_FORMAT);
        int buffer_size = AUDIO_SAMPLES_PER_FRAME * 10;
        if (buffer_size < min_buffer_size)
            buffer_size = ((min_buffer_size / AUDIO_SAMPLES_PER_FRAME) + 1) * AUDIO_SAMPLES_PER_FRAME * 2;

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,       // source
                AUDIO_SAMPLE_RATE,                         // sample rate, hz
                AUDIO_CHANNEL_CONFIG,                      // channels
                AUDIO_FORMAT,                        // audio format
                buffer_size);                        // buffer size (bytes)
    }

    private void startAudioRecord() {
        if(audioRecord != null) {

            new Thread(new Runnable() {

                @Override
                public void run() {
                    audioRecord.startRecording();
                    boolean audioEosRequestedCopy = false;
                    while(true) {

                        if(!firstFrameReady)
                            continue;
                        audioEosRequestedCopy = audioEosRequested; // make sure audioEosRequested doesn't change value mid loop
                        if (audioEosRequestedCopy || fullStopReceived) { // TODO post eosReceived message with Handler?
                            PLog.logDebug(TAG, "Audio loop caught audioEosRequested / fullStopReceived " + audioEosRequestedCopy + " " + fullStopReceived);
                            if (TRACE) Trace.beginSection("sendAudio");
                            sendAudioToEncoder(true);
                            if (TRACE) Trace.endSection();
                        }
                        if (fullStopReceived) {
                            PLog.logDebug(TAG, "Stopping AudioRecord");
                            audioRecord.stop();
                        }

                        synchronized (audioTrackInfo.muxerWrapper.sync) {
                            if (TRACE) Trace.beginSection("drainAudio");
                            drainEncoder(audioEncoder, audioBufferInfo, audioTrackInfo, audioEosRequestedCopy || fullStopReceived);
                            if (TRACE) Trace.endSection();
                        }

                        if (audioEosRequestedCopy) audioEosRequested = false;

                        if (!fullStopReceived) {
                            if (TRACE) Trace.beginSection("sendAudio");
                            sendAudioToEncoder(false);
                            if (TRACE) Trace.endSection();
                        }else{
                            break;
                        }
                    } // end while
                }
            }).start();

        }

    }

    public void sendAudioToEncoder(boolean endOfStream) {
        // send current frame data to encoder
        try {
            ByteBuffer[] inputBuffers = audioEncoder.getInputBuffers();
            int inputBufferIndex = audioEncoder.dequeueInputBuffer(-1);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                long presentationTimeNs = System.nanoTime();
                int inputLength =  audioRecord.read(inputBuffer, AUDIO_SAMPLES_PER_FRAME );
                presentationTimeNs -= (inputLength / AUDIO_SAMPLE_RATE ) / 1000000000;
                if(inputLength == AudioRecord.ERROR_INVALID_OPERATION)
                    Log.e(TAG, "Audio read error");

                //long presentationTimeUs = (presentationTimeNs - startWhen) / 1000;
                long presentationTimeUs = (presentationTimeNs - startWhen) / 1000;
                if (VERBOSE) PLog.logDebug(TAG, "queueing " + inputLength + " audio bytes with pts " + presentationTimeUs);
                if (endOfStream) {
                    PLog.logDebug(TAG, "EOS received in sendAudioToEncoder");
                    audioEncoder.queueInputBuffer(inputBufferIndex, 0, inputLength, presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    eosSentToAudioEncoder = true;
                } else {
                    audioEncoder.queueInputBuffer(inputBufferIndex, 0, inputLength, presentationTimeUs, 0);
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "_offerAudioEncoder exception");
            t.printStackTrace();
        }
    }


    /**
     * Attempts to find a preview size that matches the provided width and height (which
     * specify the dimensions of the encoded video).  If it fails to find a match it just
     * uses the default preview size.
     * <p/>
     * TODO: should do a best-fit match.
     */
    private static void choosePreviewSize(Camera.Parameters parms, int width, int height) {
        // We should make sure that the requested MPEG size is less than the preferred
        // size, and has the same aspect ratio.
        Camera.Size ppsfv = parms.getPreferredPreviewSizeForVideo();
        if (ppsfv != null) {
            PLog.logDebug(TAG, "Camera preferred preview size for video is " + ppsfv.width + "x" + ppsfv.height);
        }

        for (Camera.Size size : parms.getSupportedPreviewSizes()) {
            if (size.width == width && size.height == height) {
                parms.setPreviewSize(width, height);
                return;
            }
        }

        PLog.logWarning(TAG, "Unable to set preview size to " + width + "x" + height);
        if (ppsfv != null) {
            parms.setPreviewSize(ppsfv.width, ppsfv.height);
        }
    }

    /**
     * Configures Camera for video capture.  Sets camera.
     * <p/>
     * Opens a Camera and sets parameters.  Does not start preview.
     */
    private void prepareCamera(int encWidth, int encHeight, int cameraType) {
        if (cameraType != Camera.CameraInfo.CAMERA_FACING_FRONT && cameraType != Camera.CameraInfo.CAMERA_FACING_BACK) {
            throw new RuntimeException("Invalid cameraType");
        }

        Camera.CameraInfo info = new Camera.CameraInfo();

        // Try to find a front-facing camera (e.g. for videoconferencing).
        int nucameras = Camera.getNumberOfCameras();
        for (int i = 0; i < nucameras; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == cameraType) {
                camera = Camera.open(i);
                break;
            }
        }
        if (camera == null && cameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            PLog.logDebug(TAG, "No front-facing camera found; opening default");
            camera = Camera.open();    // opens first back-facing camera
        }
        if (camera == null) {
            throw new RuntimeException("Unable to open camera");
        }

        Camera.Parameters parms = camera.getParameters();
        List<int[]> fpsRanges = parms.getSupportedPreviewFpsRange();
        int[] maxFpsRange = fpsRanges.get(fpsRanges.size() - 1);
        parms.setPreviewFpsRange(maxFpsRange[0], maxFpsRange[1]);

        choosePreviewSize(parms, encWidth, encHeight);
        // leave the frame rate set to default
        camera.setParameters(parms);
        camera.setDisplayOrientation(90);

        Camera.Size size = parms.getPreviewSize();
        PLog.logDebug(TAG, "Camera preview size is " + size.width + "x" + size.height);
    }

    /**
     * Stops camera preview, and releases the camera to the system.
     */
    private void releaseCamera() {
        if (VERBOSE) PLog.logDebug(TAG, "releasing camera");
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    /**
     * Configures SurfaceTexture for camera preview.  Initializes surfaceTextureManager, and sets the
     * associated SurfaceTexture as the Camera's "preview texture".
     * <p/>
     * Configure the EGL surface that will be used for output before calling here.
     */
    private void prepareSurfaceTexture() {
        surfaceTextureManager = new SurfaceTextureManager();
        SurfaceTexture st = surfaceTextureManager.getSurfaceTexture();
        try {
            camera.setPreviewTexture(st);
        } catch (IOException ioe) {
            throw new RuntimeException("setPreviewTexture failed", ioe);
        }
    }

    /**
     * Releases the SurfaceTexture.
     */
    private void releaseSurfaceTexture() {
        if (surfaceTextureManager != null) {
            surfaceTextureManager.release();
            surfaceTextureManager = null;
        }
    }

    /**
     * Configures encoder and muxer state, and prepares the input Surface.  Initializes
     * videoEncoder, muxerWrapper1, codecInputSurface, videoBufferInfo, videoTrackInfo, and mMuxerStarted.
     */
    private void prepareEncoder(int width, int height, int bitRate) {
        this.eosSentToAudioEncoder = false;
        this.eosSentToVideoEncoder = false;
        this.fullStopReceived = false;
        this.videoBufferInfo = new MediaCodec.BufferInfo();
        this.videoTrackInfo = new TrackInfo();

        this.videoFormat = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, width, height);

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        this.videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        this.videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        this.videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, VIDEO_FRAME_RATE);
        this.videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, VIDEO_IFRAME_INTERVAL);
        PLog.logDebug(TAG, "prepareEncoder() -> Format: " + videoFormat);

        // Create a MediaCodec encoder, and configure it with our format.  Get a Surface
        // we can use for input and wrap it with a class that handles the EGL work.
        //
        // If you want to have two EGL contexts -- one for display, one for recording --
        // you will likely want to defer instantiation of CodeccodecInputSurface until after the
        // "display" EGL context is created, then modify the eglCreateContext call to
        // take eglGetCurrentContext() as the share_context argument.
        this.videoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE);
        this.videoEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        this.codecInputSurface = new CodecInputSurface(videoEncoder.createInputSurface());
        this.videoEncoder.start();

        this.audioBufferInfo = new MediaCodec.BufferInfo();
        this.audioTrackInfo = new TrackInfo();

        this.audioFormat = new MediaFormat();
        this.audioFormat.setString(MediaFormat.KEY_MIME, AUDIO_MIME_TYPE);
        this.audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        this.audioFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, AUDIO_SAMPLE_RATE);
        this.audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        this.audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 128000);
        this.audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384);

        this.audioEncoder = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE);
        this.audioEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        this.audioEncoder.start();

        // Output filename.  Ideally this would use Context.getFilesDir() rather than a
        // hard-coded output directory.
        String outputPath = PAndroidGlobals.TEMP_DIRECTORY + "chunktest." + width + "x" + height + String.valueOf(leadingChunk) + ".mp4";
        PLog.logDebug(TAG, "Output file is " + outputPath);


        // Create a MediaMuxer.  We can't add the video track and start() the muxer here,
        // because our MediaFormat doesn't have the Magic Goodies.  These can only be
        // obtained from the encoder after it has started processing data.
        //
        // We're not actually interested in multiplexing audio.  We just want to convert
        // the raw H.264 elementary stream we get from MediaCodec into a .mp4 file.
        //resetMediaMuxer(outputPath);
        this.muxerWrapper1 = new MediaMuxerWrapper(VIDEO_OUTPUT_FORMAT, leadingChunk);
        this.muxerWrapper2 = new MediaMuxerWrapper(VIDEO_OUTPUT_FORMAT, leadingChunk + 1); // prepared for next chunk


        this.videoTrackInfo.index = -1;
        this.videoTrackInfo.muxerWrapper = muxerWrapper1;
        this.audioTrackInfo.index = -1;
        this.audioTrackInfo.muxerWrapper = muxerWrapper1;
    }

    private void stopAndReleaseVideoEncoder() {
        this.eosSentToVideoEncoder = false;
        this.frameCount = 0;
        if (this.videoEncoder != null) {
            this.videoEncoder.stop();
            this.videoEncoder.release();
            this.videoEncoder = null;
        }
    }


    private void stopAndReleaseAudioEncoder() {
        this.lastEncodedAudioTimeStamp = 0;
        this.eosSentToAudioEncoder = false;

        if (this.audioEncoder != null) {
            this.audioEncoder.stop();
            this.audioEncoder.release();
            this.audioEncoder = null;
        }
    }

    private void stopAndReleaseEncoders() {
        this.stopAndReleaseVideoEncoder();
        this.stopAndReleaseAudioEncoder();
    }

    /**
     * This can be called within drainEncoder, when the end of stream is reached
     */
    private void chunkVideoEncoder() {
        this.stopAndReleaseVideoEncoder();
        // Start Encoder
        this.videoBufferInfo = new MediaCodec.BufferInfo();
        //videoTrackInfo = new TrackInfo();
        this.advanceVideoMediaMuxer();
        this.videoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE);
        this.videoEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        this.codecInputSurface.updateSurface(videoEncoder.createInputSurface());
        this.videoEncoder.start();
        this.codecInputSurface.makeEncodeContextCurrent();
    }

    private void advanceVideoMediaMuxer() {

        MediaMuxerWrapper videoMuxer = (this.videoTrackInfo.muxerWrapper == this.muxerWrapper1) ? this.muxerWrapper1 : this.muxerWrapper2;
        MediaMuxerWrapper audioMuxer = (this.audioTrackInfo.muxerWrapper == this.muxerWrapper1) ? this.muxerWrapper1 : this.muxerWrapper2;

        PLog.logDebug("advanceVideoMediaMuxer() -> AdvanceVideo", "video on " + ((videoTrackInfo.muxerWrapper == muxerWrapper1) ? "muxer1" : "muxer2"));

        // If both encoders are on same muxer, switch to other muxer.
        if(videoMuxer == audioMuxer) {
            this.leadingChunk++;
            if(videoMuxer == this.muxerWrapper1) {
                PLog.logDebug("advanceVideo", "encoders on same muxer. swapping.");
                this.videoTrackInfo.muxerWrapper = this.muxerWrapper2;
                // testing: can we start next muxer immediately given MediaCodec.getOutputFormat() values?

            } else if(videoMuxer == this.muxerWrapper2) {
                PLog.logDebug("advanceVideo", "encoders on same muxer. swapping.");
                this.videoTrackInfo.muxerWrapper = this.muxerWrapper1;
                // testing: can we start next muxer immediately given MediaCodec.getOutputFormat() values?
            }

            if(this.videoOutputFormat != null && this.audioOutputFormat != null) {
                this.videoTrackInfo.muxerWrapper.addTrack(this.videoOutputFormat);
                this.videoTrackInfo.muxerWrapper.addTrack(this.audioOutputFormat);
            }
            else{
                Log.e(TAG, "videoOutputFormat or audioOutputFormat is null!");
            }

        } else {
            // if encoders are separate, finalize this muxer, and switch to others
            PLog.logDebug("advanceVideo", "encoders on diff muxers. restarting");
            this.videoTrackInfo.muxerWrapper.restart(VIDEO_OUTPUT_FORMAT, this.leadingChunk + 1); // prepare muxer for next chunk, but don't alter leadingChunk
            this.videoTrackInfo.muxerWrapper = this.audioTrackInfo.muxerWrapper;
        }
    }

    /**
     * This can be called within drainEncoder, when the end of stream is reached
     */
    private void chunkAudioEncoder() {
        // Start the encoder
        this.stopAndReleaseAudioEncoder();
        this.audioBufferInfo = new MediaCodec.BufferInfo();
        this.advanceAudioMediaMuxer();
        this.audioEncoder = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE);
        this.audioEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        this.audioEncoder.start();
    }

    private void advanceAudioMediaMuxer() {

        MediaMuxerWrapper videoMuxer = (this.videoTrackInfo.muxerWrapper == this.muxerWrapper1) ? this.muxerWrapper1 : this.muxerWrapper2;
        MediaMuxerWrapper audioMuxer = (this.audioTrackInfo.muxerWrapper == this.muxerWrapper1) ? this.muxerWrapper1 : this.muxerWrapper2;
        PLog.logDebug("advanceAudioMediaMuxer() -> advanceAudio", "audio on " + ((this.audioTrackInfo.muxerWrapper == this.muxerWrapper1) ? "muxer1" : "muxer2"));

        if (videoMuxer == audioMuxer) {
            // If both encoders are on same muxer, switch to other muxer
            PLog.logDebug("advanceAudio", "encoders on same muxer. swapping.");
            this.leadingChunk++;
            if(videoMuxer == this.muxerWrapper1) {
                this.audioTrackInfo.muxerWrapper = this.muxerWrapper2;
            }else if(videoMuxer == this.muxerWrapper2) {
                this.audioTrackInfo.muxerWrapper = this.muxerWrapper1;
            }
            if(videoOutputFormat != null && audioOutputFormat != null) {
                this.audioTrackInfo.muxerWrapper.addTrack(this.videoOutputFormat);
                this.audioTrackInfo.muxerWrapper.addTrack(this.audioOutputFormat);
            }else{
                PLog.logError(TAG, "advanceAudioMediaMuxer() -> videoOutputFormat or audioOutputFormat is null!");
            }
        } else {
            // if encoders are separate, finalize this muxer, and switch to others
            PLog.logDebug(TAG, "advanceAudioMediaMuxer() -> Encoders on diff muxers. Restarting");
            audioTrackInfo.muxerWrapper.restart(VIDEO_OUTPUT_FORMAT, leadingChunk + 1); // prepare muxer for next chunk, but don't alter leadingChunk
            audioTrackInfo.muxerWrapper = videoTrackInfo.muxerWrapper;
        }
    }

    /**
     * Releases encoder resources.
     */
    private void releaseEncodersAndMuxer() {
        if (VERBOSE) PLog.logDebug(TAG, "releasing encoder objects");
        stopAndReleaseEncoders();
        if (muxerWrapper1 != null) {
            synchronized (muxerWrapper1.sync) {
                muxerWrapper1.stop();
                muxerWrapper1 = null;
            }
        }
        if (muxerWrapper2 != null) {
            synchronized (muxerWrapper2.sync) {
                muxerWrapper2.stop();
                muxerWrapper2 = null;
            }
        }
    }

    /**
     * Extracts all pending data from the encoder and forwards it to the muxer.
     * <p/>
     * If endOfStream is not set, this returns when there is no more data to drain.  If it
     * is set, we send EOS to the encoder, and then iterate until we see EOS on the output.
     * Calling this with endOfStream set should be done once, right before stopping the muxer.
     * <p/>
     * We're just using the muxer to get a .mp4 file (instead of a raw H.264 stream).  We're
     * not recording audio.
     */
    private void drainEncoder(MediaCodec encoder, MediaCodec.BufferInfo bufferInfo, TrackInfo trackInfo, boolean endOfStream) {
        final int TIMEOUT_USEC = 100;

        //TODO: Get Muxer from trackInfo
        MediaMuxerWrapper muxerWrapper = trackInfo.muxerWrapper;

        if (VERBOSE) PLog.logDebug(TAG, "drain" + ((encoder == videoEncoder) ? "Video" : "Audio") + "Encoder(" + endOfStream + ")");
        if (endOfStream && encoder == videoEncoder) {
            if (VERBOSE) PLog.logDebug(TAG, "sending EOS to " + ((encoder == videoEncoder) ? "video" : "audio") + " encoder");
            encoder.signalEndOfInputStream();
            eosSentToVideoEncoder = true;
        }
        //testing
        ByteBuffer[] encoderOutputBuffers = encoder.getOutputBuffers();

        while (true) {
            int encoderStatus = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    if (VERBOSE) PLog.logDebug(TAG, "no output available. aborting drain");
                    break;      // out of while
                } else {
                    if (VERBOSE) PLog.logDebug(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = encoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once

                if (muxerWrapper.isStarted()) {
                    PLog.logError(TAG, "drainEncoder() -> format changed after muxer start! Can or should we ignore it?");
                    //throw new RuntimeException("Format changed after muxer start!");
                }else{
                    MediaFormat newFormat = encoder.getOutputFormat();
                    if(encoder == videoEncoder)
                        videoOutputFormat = newFormat;
                    else if(encoder == audioEncoder)
                        audioOutputFormat = newFormat;

                    // now that we have the Magic Goodies, start the muxer
                    trackInfo.index = muxerWrapper.addTrack(newFormat);
                    if(!muxerWrapper.allTracksAdded())
                        break;  // Allow both encoders to send output format changed before attempting to write samples
                }

            } else if (encoderStatus < 0) {
                PLog.logWarning(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
                // let's ignore it
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_VIDEO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    if (VERBOSE) PLog.logDebug(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    bufferInfo.size = 0;
                }


                if (bufferInfo.size != 0) {
                    if (!trackInfo.muxerWrapper.isStarted()) {
                        Log.e(TAG, "Muxer not started. dropping " + ((encoder == videoEncoder) ? " video" : " audio") + " frames");
                        //throw new RuntimeException("muxer hasn't started");
                    } else{
                        // adjust the ByteBuffer values to match BufferInfo (not needed?)
                        encodedData.position(bufferInfo.offset);
                        encodedData.limit(bufferInfo.offset + bufferInfo.size);
                        if(encoder == audioEncoder) {
                            if(bufferInfo.presentationTimeUs < lastEncodedAudioTimeStamp)
                                bufferInfo.presentationTimeUs = lastEncodedAudioTimeStamp += 23219; // Magical AAC encoded frame time
                            lastEncodedAudioTimeStamp = bufferInfo.presentationTimeUs;
                        }
                        if(bufferInfo.presentationTimeUs < 0) {
                            bufferInfo.presentationTimeUs = 0;
                        }
                        muxerWrapper.writeSampleDataToMuxer(trackInfo.index, encodedData, bufferInfo);

                        if (VERBOSE)
                            PLog.logDebug(TAG, "sent " + bufferInfo.size + ((encoder == videoEncoder) ? " video" : " audio") + " bytes to muxer with pts " + bufferInfo.presentationTimeUs);

                    }
                }

                encoder.releaseOutputBuffer(encoderStatus, false);

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        PLog.logWarning(TAG, "reached end of stream unexpectedly");
                    } else {
                        muxerWrapper.finishTrack();
                        if (VERBOSE) PLog.logDebug(TAG, "end of " + ((encoder == videoEncoder) ? " video" : " audio") + " stream reached. ");
                        if(!fullStopReceived) {
                            if(encoder == videoEncoder) {
                                PLog.logDebug(TAG, "Chunking video encoder");
                                if (TRACE) Trace.beginSection("chunkVideoEncoder");
                                chunkVideoEncoder();
                                if (TRACE) Trace.endSection();
                            }else if(encoder == audioEncoder) {
                                PLog.logDebug(TAG, "Chunking audio encoder");
                                if (TRACE) Trace.beginSection("chunkAudioEncoder");
                                chunkAudioEncoder();
                                if (TRACE) Trace.endSection();
                            }else
                                Log.e(TAG, "Unknown encoder passed to drainEncoder!");
                        }else{

                            if(encoder == videoEncoder) {
                                PLog.logDebug(TAG, "Stopping and releasing video encoder");
                                stopAndReleaseVideoEncoder();
                            } else if(encoder == audioEncoder) {
                                PLog.logDebug(TAG, "Stopping and releasing audio encoder");
                                this.stopAndReleaseAudioEncoder();
                            }
                            //stopAndReleaseEncoders();
                        }
                    }
                    break;
                }
            }
        }
    }

}
