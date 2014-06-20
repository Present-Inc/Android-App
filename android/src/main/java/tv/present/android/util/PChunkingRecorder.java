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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import tv.present.android.threads.AudioRecordRunnable;

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

    public static final int NANOSECS_PER_SEC = 1000000000;
    public static final int NANOSECS_PER_MICROSEC = 1000;

    public static final int DISPLAY_ORIENTATION_0 = 0;
    public static final int DISPLAY_ORIENTATION_90 = 90;
    public static final int DISPLAY_ORIENTATION_180 = 180;
    public static final int DISPLAY_ORIENTATION_270 = 270;

    private static final String TAG = "tv.present.android.util.PChunkingRecorder";

    private static final boolean VERBOSE = false;
    
    // Multimedia Settings
    private static final long MULTIMEDIA_CHUNK_DURATION_SEC = 10;

    // Audio Settings
    private static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";
    private static final int AUDIO_SAMPLE_RATE = 44100;
    private static final int AUDIO_SAMPLES_PER_FRAME = 1024;
    private static final int AUDIO_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int AUDIO_FRAME_TIME = 23219;
    
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
    private boolean fullStopReceived = false;
    private boolean firstFrameReady;
    private boolean eosReceived;

    // Debug values
    private int totalFrameCount = 0;
    private long startTime;
    private long endTime;

    /**
     * Constructs a PChunkingRecorder.
     * @param context is a Context to create the recorder with.
     */
    public PChunkingRecorder(Context context) {
        this.firstFrameReady = false;
        this.eosReceived = false;
    }

    public AudioRecord getAudioRecord() {
        return this.audioRecord;
    }

    public boolean isFirstFrameReady() {
        return this.firstFrameReady;
    }

    public boolean isAudioEosRequested() {
        return this.audioEosRequested;
    }

    public boolean isFullStopReceived() {
        return this.fullStopReceived;
    }

    public TrackInfo getAudioTrackInfo() {
        return this.audioTrackInfo;
    }

    public MediaCodec getAudioEncoder() {
        return this.audioEncoder;
    }

    public MediaCodec.BufferInfo getAudioBufferInfo() {
        return this.audioBufferInfo;
    }

    /**
     * Sets the GLSurfaceView to use as a display surface.
     * @param displaySurface is a GLSurfaceView.
     */
    public void setDisplaySurface(GLSurfaceView displaySurface) {
        this.displaySurface = displaySurface;
    }

    /**
     * Sets the EGLContext to use as the display context for the codec input surface.
     * @param context is an EGLContext.
     */
    public void setDisplayEGLContext(EGLContext context) {
        this.codecInputSurface.mEGLDisplayContext = context;
    }

    public void setAudioEOSRequested(final boolean eosRequested) {
        this.audioEosRequested = eosRequested;
    }

    /**
     * Starts the recording process.
     */
    public void startRecording() {

        PLog.logDebug(TAG, "startRecording() -> " + VIDEO_MIME_TYPE + " output " + VIDEO_WIDTH + "x" + VIDEO_HEIGHT + " @" + VIDEO_ENCODER_BIT_RATE + ".");

        try {
            this.prepareCamera(VIDEO_WIDTH, VIDEO_HEIGHT, Camera.CameraInfo.CAMERA_FACING_BACK);
            this.prepareEncoder(VIDEO_WIDTH, VIDEO_HEIGHT, VIDEO_ENCODER_BIT_RATE);
            this.codecInputSurface.makeEncodeContextCurrent();
            this.prepareSurfaceTexture();
            this.setupAudioRecord();
            this.startAudioRecord();
            this.startWhen = System.nanoTime();
            this.camera.startPreview();
            final SurfaceTexture surfaceTexture = this.surfaceTextureManager.getSurfaceTexture();
            this.eosReceived = false;

            // Feed any pending encoder output to the muxer and chunk the media if necessary.
            while (!(this.fullStopReceived && this.eosSentToVideoEncoder)) {

                final int FRAMES_PER_CHUNK = (int) MULTIMEDIA_CHUNK_DURATION_SEC * VIDEO_FRAME_RATE;
                this.eosReceived = ((this.frameCount % FRAMES_PER_CHUNK) == 0 && this.frameCount != 0);

                if (this.eosReceived) {
                    PLog.logDebug(TAG, "startRecording() -> Chunkpoint on frame: " + this.frameCount);
                }

                this.audioEosRequested = this.eosReceived;

                // Drain the video from the encoder
                synchronized (videoTrackInfo.muxerWrapper.sync) {
                    this.drainEncoder(this.videoEncoder, this.videoBufferInfo, this.videoTrackInfo, (this.eosReceived || this.fullStopReceived));
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
                PLog.logDebug(TAG, "startRecording() -> Sending frame to encoder.");
                this.codecInputSurface.swapBuffers();

                if (! this.firstFrameReady) {
                    this.startTime = System.nanoTime();
                    this.firstFrameReady = true;
                }

            }

            PLog.logDebug(TAG, "startRecording() -> Exiting video encode loop.");

        }
        // TODO: Don't catch a general exception!
        catch (Exception e) {
            PLog.logError(TAG, "startRecording() -> Just caught an encoding loop exception!");
            e.printStackTrace();
        }

    }

    /**
     * Stops the recording process.
     */
    public void signalStopRecording() {
        PLog.logDebug(TAG, "signalStopRecording() -> Method called.");
        this.fullStopReceived = true;
        final double recordingDurationSec = (System.nanoTime() - startTime) / (double) NANOSECS_PER_SEC;
        PLog.logDebug(TAG, "startRecording() -> Recorded " + recordingDurationSec + " seconds. Expected " + (VIDEO_FRAME_RATE * recordingDurationSec) + " frames. Got " + totalFrameCount + " for " + (totalFrameCount / recordingDurationSec) + " fps.");
    }

    /**
     * Called internally to finalize HQ and last chunk
     */
    private void stopRecording() {
        final boolean fullStopPerformed = true;
        //mMediaRecorderWrapper.signalStopRecording();
        this.releaseCamera();
        this.releaseEncodersAndMuxer();
        this.releaseSurfaceTexture();
        if (this.codecInputSurface != null) {
            this.codecInputSurface.release();
            this.codecInputSurface = null;
        }
    }

    /**
     * Sets up the audio recorder.
     */
    private void setupAudioRecord() {
        
        final int minBufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AUDIO_CHANNEL_CONFIG, AUDIO_FORMAT);
        int bufferSize = AUDIO_SAMPLES_PER_FRAME * 10;

        // If the buffer allocated is less than the minimum necessary size, reset the size to the
        // proper value.
        if (bufferSize < minBufferSize) {
            bufferSize = ((minBufferSize / AUDIO_SAMPLES_PER_FRAME) + 1) * AUDIO_SAMPLES_PER_FRAME * 2;
        }

        // Create a new AudioRecord object.
        this.audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,          // source
                AUDIO_SAMPLE_RATE,                      // sample rate (Hz)
                AUDIO_CHANNEL_CONFIG,                   // channels
                AUDIO_FORMAT,                           // audio format
                bufferSize);                            // buffer size (bytes)
    }

    /**
     * Starts the audio recorder.
     */
    private void startAudioRecord() {

        // Check to make sure the AudioRecord object is not null
        if (this.audioRecord != null) {

            final AudioRecordRunnable audioRecordRunnable = new AudioRecordRunnable(this);
            final Thread audioRecordThread = new Thread(audioRecordRunnable);

            audioRecordThread.start();

        }

    }

    /**
     * This method will send the current audio frame data to the audio encoder.
     * @param endOfStream is a boolean indicating the end of the stream.
     */
    public void sendAudioToEncoder(final boolean endOfStream) {

        try {

            ByteBuffer[] inputBuffers = audioEncoder.getInputBuffers();
            final int inputBufferIndex = audioEncoder.dequeueInputBuffer(-1);

            if (inputBufferIndex >= 0) {

                // Get the input data buffer from the inputBuffers array and then reset the position
                // on that buffer.
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();

                // Determine the presentation time in nanoseconds.
                long presentationTimeNs = System.nanoTime();
                final int inputLength =  audioRecord.read(inputBuffer, AUDIO_SAMPLES_PER_FRAME );
                presentationTimeNs -= (inputLength / AUDIO_SAMPLE_RATE ) / NANOSECS_PER_SEC;

                // Check for an audio record error for an invalid operation.
                if (inputLength == AudioRecord.ERROR_INVALID_OPERATION) {
                    PLog.logError(TAG, "sendAudioToEncoder() -> Audio read error!");
                }

                final long presentationTimeUs = (presentationTimeNs - startWhen) / NANOSECS_PER_MICROSEC;
                PLog.logDebug(TAG, "streamAudioToEncoder() -> Queueing " + inputLength + " audio bytes with pts " + presentationTimeUs + ".");

                if (endOfStream) {
                    PLog.logDebug(TAG, "streamAudioToEncoder() -> EOS received in sendAudioToEncoder.");
                    this.audioEncoder.queueInputBuffer(inputBufferIndex, 0, inputLength, presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    this.eosSentToAudioEncoder = true;
                }
                else {
                    this.audioEncoder.queueInputBuffer(inputBufferIndex, 0, inputLength, presentationTimeUs, 0);

                }

            }

        }
        catch (Throwable t) {
            PLog.logError(TAG, "sendAudioToEncoder() -> OfferAudioEncoder exception!");
            t.printStackTrace();
        }
    }

    /**
     * This method will attempt to find a preview size that matches the desired width and height
     * of the encoded video.  If it is unable to find a decent match, it will just use the default
     * preview size.
     * @param params are a set of Camera.Parameters.
     * @param width is the desired width as an integer.
     * @param height is the desired height as an integer.
     */
    private void choosePreviewSize(final Camera.Parameters params, final int width, final int height) {
        
        // We should make sure that the requested MPEG size is less than the preferred
        // size, and has the same aspect ratio.
        Camera.Size preferredPreviewSizeForVideo = params.getPreferredPreviewSizeForVideo();
        if (preferredPreviewSizeForVideo != null) {
            PLog.logDebug(TAG, "choosePreviewSize() -> Camera preferred preview size for video is " + preferredPreviewSizeForVideo.width + "x" + preferredPreviewSizeForVideo.height);
        }

        // Loop through all of the supported preview sizes that are available, and if one of them
        // matches with the requested sizes, set that size as the preview size.
        for (Camera.Size size : params.getSupportedPreviewSizes()) {
            if (size.width == width && size.height == height) {
                params.setPreviewSize(width, height);
                return;
            }
        }

        PLog.logWarning(TAG, "choosePreviewSize() -> Unable to set preview size to " + width + "x" + height);
        if (preferredPreviewSizeForVideo != null) {
            params.setPreviewSize(preferredPreviewSizeForVideo.width, preferredPreviewSizeForVideo.height);
        }
        
    }

    /**
     * Configures Camera for video capture.  Sets camera.
     * <p/>
     * Opens a Camera and sets parameters.  Does not start preview.
     */

    /**
     * Prepares the camera to capture video by opening and setting proper parameters.  This method
     * does not currently start a camera preview.
     * @param encWidth is the desired encoding width as an integer.
     * @param encHeight is the desired encoding height as an integer.
     * @param cameraType is an integer type of the camera.
     */
    private void prepareCamera(final int encWidth, final int encHeight, final int cameraType) {
        
        // Checks to make sure that the camera is a front or back facing camera.  If it's not one of
        // these we don't know what kind of camera it is so an exception should be thrown.
        if (cameraType != Camera.CameraInfo.CAMERA_FACING_FRONT && cameraType != Camera.CameraInfo.CAMERA_FACING_BACK) {
            throw new RuntimeException("An attempt was made to prepare the camera with an invalid camera type (ie: it is not a front or a rear facing camera)");
        }

        Camera.CameraInfo info = new Camera.CameraInfo();

        // Try to find a camera that faces in the direction that we requested.
        final int numCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, info);
            if (info.facing == cameraType) {
                this.camera = Camera.open(i);
                break;
            }
        }

        // If no camera was set, and we are looking for a camera that is facing front, we can
        // conclude that there is no camera on this phone that is facing front.  Therefore, we will
        // open the default camera.
        if (this.camera == null && cameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            PLog.logDebug(TAG, "prepareCamera() -> No front-facing camera found; opening default");
            this.camera = Camera.open();    // opens first back-facing camera
        }

        // If all else fails, throw a runtime exception because we are unable to open a camera.
        if (this.camera == null) {
            throw new RuntimeException("Unable to open camera!");
        }

        // Set the FPS range for the preview.
        // TODO: What preview FPS range are we really setting here?
        Camera.Parameters params = this.camera.getParameters();
        List<int[]> fpsRanges = params.getSupportedPreviewFpsRange();
        int[] maxFpsRange = fpsRanges.get(fpsRanges.size() - 1);
        params.setPreviewFpsRange(maxFpsRange[0], maxFpsRange[1]);

        this.choosePreviewSize(params, encWidth, encHeight);
        // leave the frame rate set to default
        this.camera.setParameters(params);
        this.camera.setDisplayOrientation(DISPLAY_ORIENTATION_90);

        Camera.Size size = params.getPreviewSize();
        PLog.logDebug(TAG, "prepareCamera() -> Camera preview size is " + size.width + "x" + size.height);

    }

    /**
     * Stops camera preview, and releases the camera to the system.
     */
    private void releaseCamera() {
        PLog.logDebug(TAG, "releaseCamera() -> Starting method");
        if (this.camera != null) {
            this.camera.stopPreview();
            this.camera.release();
            this.camera = null;
        }
    }

    /**
     * Configures a SurfaceTexture that will be used for the camera preview.  The SurfaceTexture is
     * obtained through the SurfaceTextureManager by using the getSurfaceTexture() method.  The
     * SurfaceTexture that is returned from this method is then set as the camera preview texture.
     */
    private void prepareSurfaceTexture() {
        this.surfaceTextureManager = new SurfaceTextureManager();
        SurfaceTexture surfaceTexture = this.surfaceTextureManager.getSurfaceTexture();
        try {
            this.camera.setPreviewTexture(surfaceTexture);
        }
        catch (IOException e) {
            throw new RuntimeException("prepareSurfaceTexture() -> SetPreviewTexture failed", e);
        }
    }

    /**
     * Releases the SurfaceTexture and the SurfaceTextureManager.
     */
    private void releaseSurfaceTexture() {
        if (this.surfaceTextureManager != null) {
            this.surfaceTextureManager.release();
            this.surfaceTextureManager = null;
        }
    }

    /**
     * Configures encoder and muxer state, and prepares the input Surface.  Initializes
     * videoEncoder, muxerWrapper1, codecInputSurface, videoBufferInfo, videoTrackInfo, and mMuxerStarted.
     */

    /**
     * Prepares the encoder and muxer state and prepares the Surface that is used as an input.  This
     * method will initialize the videoEncoder, muxerWrapper1, codecInputSurface, videoBufferInfo,
     * videoTrackInfo, and muxerStarted members.
     * @param width is the desired width as an integer.
     * @param height is the desired height as an integer.
     * @param bitRate is a desired bit rate as an integer.
     */
    private void prepareEncoder(final int width, final int height, final int bitRate) {

        this.eosSentToAudioEncoder = false;
        this.eosSentToVideoEncoder = false;
        this.fullStopReceived = false;
        this.videoBufferInfo = new MediaCodec.BufferInfo();
        this.videoTrackInfo = new TrackInfo();
        this.videoFormat = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, width, height);
        // Make sure that all of these properties are properly set.  Failure to do so will cause the
        // MediaCodec to throw an unhelpful exception.
        this.videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        this.videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        this.videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, VIDEO_FRAME_RATE);
        this.videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, VIDEO_IFRAME_INTERVAL);
        PLog.logDebug(TAG, "prepareEncoder() -> Format: " + videoFormat);
        
        /*
         * Create a MediaCodec encoder and configure it per our format.  We need to get a Surface
         * and then wrap the Surface with the class that does the EGL work.
         * 
         * In order to create two EGL contexts (ie: one for preview, and one for recording):
         *  -> Defer instantiation of the CodecInputSurface until after the "display" EGL context
         *     is created.
         *  -> Modify the eglCreateContext call so that it takes the return value of 
         *     eglGetCurrentContext() as the share_context argument.
         */
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

        // Here we will specify the output filename pattern.
        String outputPathPattern = PAndroidGlobals.TEMP_DIRECTORY + "cache" + String.valueOf(leadingChunk) + ".mp4";
        PLog.logDebug(TAG, "prepareEncoder() -> Output file is: " + outputPathPattern);

        /*
         * Create two MediaMuxers.  We don't (and can't) add the video track or start the muxer here
         * because the MediaFormat has not yet been discovered.  This will bo obtained from the
         * MediaCodec after it has started spitting out data.
         */
        this.muxerWrapper1 = new MediaMuxerWrapper(VIDEO_OUTPUT_FORMAT, leadingChunk);
        this.muxerWrapper2 = new MediaMuxerWrapper(VIDEO_OUTPUT_FORMAT, leadingChunk + 1); // prepared for next chunk

        // Add the MuxerWrappers to the TrackInfo data
        this.videoTrackInfo.index = -1;
        this.videoTrackInfo.muxerWrapper = muxerWrapper1;
        this.audioTrackInfo.index = -1;
        this.audioTrackInfo.muxerWrapper = muxerWrapper1;

    }

    /**
     * Stops and releases the video encoder.
     */
    private void stopAndReleaseVideoEncoder() {
        this.eosSentToVideoEncoder = false;
        this.frameCount = 0;
        if (this.videoEncoder != null) {
            this.videoEncoder.stop();
            this.videoEncoder.release();
            this.videoEncoder = null;
        }
    }

    /**
     * Stops and releases the audio encoder.
     */
    private void stopAndReleaseAudioEncoder() {
        this.eosSentToAudioEncoder = false;
        this.lastEncodedAudioTimeStamp = 0;
        if (this.audioEncoder != null) {
            this.audioEncoder.stop();
            this.audioEncoder.release();
            this.audioEncoder = null;
        }
    }

    /**
     * Stops and releases bot the audio and video encoders.
     */
    private void stopAndReleaseEncoders() {
        this.stopAndReleaseVideoEncoder();
        this.stopAndReleaseAudioEncoder();
    }

    /**
     * This method is called within the drainEncoder() method when an end of stream is reached.
     * Many times, this is a result of a new chunk of video being started.
     */
    private void chunkVideoEncoder() {

        this.stopAndReleaseVideoEncoder();
        this.videoBufferInfo = new MediaCodec.BufferInfo();
        // TODO: Determine if resetting the video track info is necessary.
        // this.videoTrackInfo = new TrackInfo();
        this.advanceVideoMediaMuxer();
        this.videoEncoder = MediaCodec.createEncoderByType(VIDEO_MIME_TYPE);
        this.videoEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        this.codecInputSurface.updateSurface(videoEncoder.createInputSurface());
        this.videoEncoder.start();
        this.codecInputSurface.makeEncodeContextCurrent();

    }

    /**
     * This method is called to advance to the next video MediaMuxer.
     */
    private void advanceVideoMediaMuxer() {

        MediaMuxerWrapper videoMuxer = (this.videoTrackInfo.muxerWrapper ==this.muxerWrapper1) ? this.muxerWrapper1 : this.muxerWrapper2;
        MediaMuxerWrapper audioMuxer = (this.audioTrackInfo.muxerWrapper == this.muxerWrapper1) ? this.muxerWrapper1 : this.muxerWrapper2;

        PLog.logDebug(TAG, "advanceVideoMediaMuxer() -> Video on " + ((videoTrackInfo.muxerWrapper == muxerWrapper1) ? "muxer1" : "muxer2"));

        // If both encoders are on the same muxer, switch to the other muxer.
        if (videoMuxer == audioMuxer) {

            this.leadingChunk++;

            if (videoMuxer == this.muxerWrapper1) {
                PLog.logDebug(TAG, "advanceVideoMediaMuxer() -> Encoders are on the same muxer. Swapping...");
                this.videoTrackInfo.muxerWrapper = this.muxerWrapper2;
                // TODO: Is it possible to start next muxer immediately given MediaCodec.getOutputFormat() values?
            }

            else if (videoMuxer == this.muxerWrapper2) {
                PLog.logDebug(TAG, "advanceVideoMediaMuxer() -> Encoders on same muxer. swapping.");
                this.videoTrackInfo.muxerWrapper = this.muxerWrapper1;
                // testing: can we start next muxer immediately given MediaCodec.getOutputFormat() values?
            }

            // If the output formats for both audio and video are not null, we will set the audio
            // output and video output as tracks on the video track info object.
            if (this.videoOutputFormat != null && this.audioOutputFormat != null) {
                this.videoTrackInfo.muxerWrapper.addTrack(this.videoOutputFormat);
                this.videoTrackInfo.muxerWrapper.addTrack(this.audioOutputFormat);
            }
            else {
                PLog.logError(TAG, "advanceVideoMediaMuxer() -> videoOutputFormat or audioOutputFormat is null!");
            }

        }

        // If both encoders are on seperate muxers, finalize the current muxer and switch to the other one.
        else {
            PLog.logDebug(TAG, "advanceVideoMediaMuxer() -> Encoders are on different muxers. Restarting...");
            // Prepare the encoder for the next chunk, but don't alter the leading chunk.
            this.videoTrackInfo.muxerWrapper.restart(VIDEO_OUTPUT_FORMAT, this.leadingChunk + 1);
            this.videoTrackInfo.muxerWrapper = this.audioTrackInfo.muxerWrapper;
        }
    }

    /**
     * This method is called within the drainEncoder() method when an end of stream is reached.
     * Many times, this is a result of a new chunk of audio being started.
     */
    private void chunkAudioEncoder() {

        this.stopAndReleaseAudioEncoder();
        this.audioBufferInfo = new MediaCodec.BufferInfo();
        this.advanceAudioMediaMuxer();
        this.audioEncoder = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE);
        this.audioEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        this.audioEncoder.start();

    }

    /**
     * This method is called to advance to the next video MediaMuxer.
     */
    private void advanceAudioMediaMuxer() {

        MediaMuxerWrapper videoMuxer = (this.videoTrackInfo.muxerWrapper == this.muxerWrapper1) ? this.muxerWrapper1 : this.muxerWrapper2;
        MediaMuxerWrapper audioMuxer = (this.audioTrackInfo.muxerWrapper == this.muxerWrapper1) ? this.muxerWrapper1 : this.muxerWrapper2;

        PLog.logDebug(TAG, "advanceVideoMediaMuxer() -> Audio on " + ((this.audioTrackInfo.muxerWrapper == this.muxerWrapper1) ? "muxer1" : "muxer2"));

        // If both encoders are on the same muxer, switch to the other muxer.
        if (videoMuxer == audioMuxer) {

            PLog.logDebug(TAG, "advanceAudioMediaMuxer() -> Encoders on same muxer. Swapping.");

            this.leadingChunk++;

            if (videoMuxer == this.muxerWrapper1) {
                this.audioTrackInfo.muxerWrapper = this.muxerWrapper2;
            }
            else if (videoMuxer == this.muxerWrapper2) {
                this.audioTrackInfo.muxerWrapper = this.muxerWrapper1;
            }

            if (videoOutputFormat != null && audioOutputFormat != null) {
                this.audioTrackInfo.muxerWrapper.addTrack(this.videoOutputFormat);
                this.audioTrackInfo.muxerWrapper.addTrack(this.audioOutputFormat);
            }
            else {
                PLog.logError(TAG, "advanceAudioMediaMuxer() -> videoOutputFormat or audioOutputFormat is null!");
            }

        }
        else {
            // If both encoders are on seperate muxers, finalize the current muxer and switch to the other one.
            PLog.logDebug(TAG, "advanceAudioMediaMuxer() -> Encoders on diff muxers. Restarting...");
            // Prepare the encoder for the next chunk, but don't alter the leading chunk.
            this.audioTrackInfo.muxerWrapper.restart(VIDEO_OUTPUT_FORMAT, this.leadingChunk + 1);
            this.audioTrackInfo.muxerWrapper = this.videoTrackInfo.muxerWrapper;
        }
    }

    /**
     * Releases encoder and muxer resources.
     */
    private void releaseEncodersAndMuxer() {
        PLog.logDebug(TAG, "releaseEncodersAndMuxer() -> Releasing encoder objects");
        stopAndReleaseEncoders();
        if (this.muxerWrapper1 != null) {
            synchronized (this.muxerWrapper1.sync) {
                this.muxerWrapper1.stop();
                this.muxerWrapper1 = null;
            }
        }
        if (this.muxerWrapper2 != null) {
            synchronized (this.muxerWrapper2.sync) {
                this.muxerWrapper2.stop();
                this.muxerWrapper2 = null;
            }
        }
    }

    /**
     * Extracts all pending data from the encoder and forwards it to the muxer.  If endOfStream is
     * not set, this returns when there is no more data to drain.  If it is set, an EOS will be sent
     * to the encoder, and we will then iterate until we see EOS come through on the output. Calling
     * this with endOfStream set should be done once, right before stopping the muxer.
     * @param encoder is the MediaCodec "encoder" to drain data from.
     * @param bufferInfo are the MediaCodec BufferInfos.
     * @param trackInfo is TrackInfo data regarding the stream we are about to mux.
     * @param endOfStream is a boolean indicating the end of the stream.
     */
    public void drainEncoder(MediaCodec encoder, MediaCodec.BufferInfo bufferInfo, TrackInfo trackInfo, boolean endOfStream) {

        final int TIMEOUT_USEC = 100;
        final MediaMuxerWrapper muxerWrapper = trackInfo.muxerWrapper;

        PLog.logDebug(TAG, "drainEncoder() -> " + ((encoder == videoEncoder) ? "Video" : "Audio") + "Encoder(" + endOfStream + ").");

        // If the end of stream boolean is true, and we are dealing with the video encoder, signal
        // EOS to the video encoder and set the local signal boolean to true.
        if (endOfStream && encoder == videoEncoder) {
            PLog.logDebug(TAG, "drainEncoder() -> Sending EOS to " + ((encoder == videoEncoder) ? "video" : "audio") + " encoder.");
            encoder.signalEndOfInputStream();
            this.eosSentToVideoEncoder = true;
        }

        ByteBuffer[] encoderOutputBuffers = encoder.getOutputBuffers();

        while (true) {

            final int encoderStatus = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);

            // Check if the encoder status is "try again later"
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {

                // When status is TAL & we're not at the end of stream, there is no output
                // available from the encoder yet.
                if (!endOfStream) {
                    PLog.logDebug(TAG, "drainEncoder() -> No output available to drain. Aborting drain.");
                    break;
                }
                // When EOS is signalled, spin while waiting to see it in the output.
                else {
                    PLog.logDebug(TAG, "drainEncoder() -> No output available, spinning to await EOS.");
                }

            }

            // Check if the encoder output buffers have changed since we last checked, be sure to
            // empty them into the encoderOutputBuffers ByteBuffer[].
            else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                encoderOutputBuffers = encoder.getOutputBuffers();
            }

            // The encoder output format change should happen before getting the buffers, and should
            // never happen more than once.  Check if the output format change is happening, and
            // if so, check that it only happens once.
            else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

                // Check to make sure the format change didn't occur after the muxer was already
                // started, and if it was, throw an error.
                if (muxerWrapper.isStarted()) {
                    PLog.logError(TAG, "drainEncoder() -> format changed after muxer start! Can or should we ignore it?");
                    throw new RuntimeException("Encoder output format changed after the muxer had already started!");
                }
                
                // If the format change happens before the muxer is started, this is a good thing,
                // and we can capture that format from the encoder and use it to start the muxer
                // (because the muxer needs to know the format of the encoder before it can start).
                else {
                    
                    final MediaFormat newFormat = encoder.getOutputFormat();
                    if (encoder == this.videoEncoder) {
                        this.videoOutputFormat = newFormat;
                    }
                    else if (encoder == this.audioEncoder) {
                        this.audioOutputFormat = newFormat;
                    }
    
                    trackInfo.index = muxerWrapper.addTrack(newFormat);

                    // Check if the muxer has all of its tracks added, or break out of the loop if 
                    // not.  We want to allow both encoders to send output format changed before 
                    // attempting to write samples to it.
                    if (!muxerWrapper.allTracksAdded()) {
                        break;
                    }
                    
                }

            }
            
            // Things should be pretty fucked up if we ever make it here...
            // TODO:  Should something be thrown here, or should we ignore it?
            else if (encoderStatus < 0) {
                PLog.logWarning(TAG, "drainEncoder() -> Got an unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
            } 
            

            else {

                // The encoder status points us to an index within the encoderOutputBuffers array
                // that is the most recent video data that can be drained.  Get it.
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];

                // Check to see that the encoded data we just got isn't null.
                if (encodedData == null) {
                    throw new RuntimeException("The data that was retrieved from the encoder output buffer #: " + encoderStatus + " was null!");
                }

                // Codec config data was pulled from the codec and fed to the muxer which produced
                // INFO_VIDEO_OUTPUT_FORMAT_CHANGED status.  Ignore this!
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    PLog.logDebug(TAG, "drainEncoder() -> Ignoring BUFFER_FLAG_CODEC_CONFIG");
                    bufferInfo.size = 0;
                }
                
                // Check to make sure the size of valid data within the buffer is not equal to zero.
                if (bufferInfo.size != 0) {
                    
                    // If there is valid data, and the muxer is not started, throw an error because
                    // we will be dropping audio and video frames.
                    if (!trackInfo.muxerWrapper.isStarted()) {
                        PLog.logError(TAG, "drainEncoder() -> Muxer is not started when we have valid data coming from the encoder.  Dropping " + ((encoder == videoEncoder) ? " video" : " audio") + " frames");
                        throw new RuntimeException("The muxer is not started when we have valid data coming from the encoder.");
                    }

                    // If the muxer is started, adjust the ByteBuffer values to match BufferInfo.
                    // There is the potential that this is not actually needed.
                    else {

                        encodedData.position(bufferInfo.offset);
                        encodedData.limit(bufferInfo.offset + bufferInfo.size);

                        // If we are dealing with the audio encoder, check and possibly correct the
                        // last encoded audio time stamp.
                        if (encoder.equals(this.audioEncoder)) {
                            if (bufferInfo.presentationTimeUs < this.lastEncodedAudioTimeStamp) {
                                bufferInfo.presentationTimeUs = this.lastEncodedAudioTimeStamp += AUDIO_FRAME_TIME;
                            }
                            this.lastEncodedAudioTimeStamp = bufferInfo.presentationTimeUs;
                        }

                        // The presentation time can never actually be less than zero, so if it is,
                        // make sure to set it back to 0.
                        if (bufferInfo.presentationTimeUs < 0) {
                            bufferInfo.presentationTimeUs = 0;
                        }

                        // Write the sample of encoded data that we just grabbed to the muxer.
                        muxerWrapper.writeSampleDataToMuxer(trackInfo.index, encodedData, bufferInfo);

                        PLog.logDebug(TAG, "drainEncoder() -> Just sent " + bufferInfo.size + ((encoder == videoEncoder) ? " video" : " audio") + " bytes to muxer with pts " + bufferInfo.presentationTimeUs + ".");

                    }
                }

                // We're now done with the output buffers, so be sure to release them so that they
                // can be used again.
                encoder.releaseOutputBuffer(encoderStatus, false);

                // Check for an EOS.
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {

                    // If we didn't signal for an end of stream, but got one anyway (from the parent
                    // if statement above), log this situation.
                    if (!endOfStream) {
                        PLog.logWarning(TAG, "drainEncoder() -> Reached end of stream unexpectedly.  EOS was never called, but it was received from the encoder anyway.");
                    }

                    // If EOS was signalled for, finish everything up and stop.
                    else {

                        muxerWrapper.finishTrack();

                        PLog.logDebug(TAG, "drainEncoder() -< End of " + ((encoder == videoEncoder) ? " video" : " audio") + " stream reached. ");

                        // A full stop is a stop that was triggered by the user by stopping the
                        // recording action.  If a full stop was not received, this means that the
                        // muxer is "soft stopping" at a chunk point.  The next encoder will pick up
                        // where this last encoder left off.
                        if (! this.fullStopReceived) {

                            // Check to see if this is the video encoder.  If it is, call the
                            // chunkVideoEncoder() function to shut this encoder down.
                            if (encoder == this.videoEncoder) {
                                PLog.logDebug(TAG, "drainEncoder() -> Chunking the video encoder.");
                                this.chunkVideoEncoder();
                            }

                            // If it is the audio encoder, call the chunkAudioEncoder() function to
                            // shut this encoder down.
                            else if (encoder == this.audioEncoder) {
                                PLog.logDebug(TAG, "drainEncoder() -> Chunking the audio encoder.");
                                this.chunkAudioEncoder();
                            }
                            else {
                                PLog.logError(TAG, "drainEncoder() -> Unknown encoder passed to the drainEncoder() method!");
                            }

                        }

                        // This code block will execute if we did in fact receive a full stop.
                        else {

                            if (encoder == this.videoEncoder) {
                                PLog.logDebug(TAG, "Stopping and releasing the video encoder.");
                                this.stopAndReleaseVideoEncoder();
                            } else if (encoder == this.audioEncoder) {
                                PLog.logDebug(TAG, "Stopping and releasing the audio encoder");
                                this.stopAndReleaseAudioEncoder();
                            }

                            // TODO: Why does this call not work properly?
                            // Not working when this is called, but I think it should be. -- KW
                            //
                            // this.stopAndReleaseEncoders();
                        }
                    }
                    break;
                }
            }
        }
    }

}
