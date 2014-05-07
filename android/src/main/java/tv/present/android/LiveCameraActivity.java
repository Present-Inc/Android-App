package tv.present.android;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.widget.FrameLayout;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import tv.present.android.media.CodecInputSurface;
import tv.present.android.media.SurfaceTextureManager;
import tv.present.android.util.PLog;


public class LiveCameraActivity extends Activity implements TextureView.SurfaceTextureListener {

    private static final String TAG = "tv.present.Present.LiveCameraActivity";

    // Constants
    private static final File OUTPUT_DIR = Environment.getExternalStorageDirectory();
    private static final String MIME_TYPE = "video/avc";    // H.264 Advanced Video Coding
    private static final int FRAME_RATE = 30;               // 30fps
    private static final int IFRAME_INTERVAL = 5;           // 5 seconds between I-frames
    private static final long DURATION_SEC = 8;             // 8 seconds of video
    private static final int MEDIA_WIDTH = 640;
    private static final int MEDIA_HEIGHT = 480;
    private static final int MEDIA_ENC_RATE = 6000000;
    private static final String SWAPPED_FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nvarying vec2 vTextureCoord;\nuniform samplerExternalOES sTexture;\nvoid main() {\n  gl_FragColor = texture2D(sTexture, vTextureCoord).gbra;\n}\n";

    // Encoder properties


    // Encoder and Muxer
    private Camera mCamera;
    private TextureView mTextureView;
    private MediaCodec mMediaCodec;
    private CodecInputSurface mCodecInputSurface;
    private MediaMuxer mMediaMuxer;
    private int mTrackIndex;
    private boolean mMediaMuxerRunning;
    private SurfaceTextureManager mSurfaceTextureManager;
    private MediaCodec.BufferInfo mBufferInfo;




    private void prepareCamera(final int encWidth, final int encHeight) {

        if (this.mCamera != null) {
            throw new RuntimeException("Camera has already been initialized!");
        }

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

        // Try to get the rear-facing video camera.
        final int numCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                this.mCamera = Camera.open(i);
                break;
            }
        }
        // If unable to find rear-facing camera, just use the default
        if (this.mCamera == null) {
            Log.d(TAG, "No rear-facing camera found!  Opening default camera.");
            this.mCamera = Camera.open();
        }
        if (this.mCamera == null) {
            throw new RuntimeException("Unable to open any camera!");
        }

        // Camera has been opened at this point...

        Camera.Parameters cameraParameters = this.mCamera.getParameters();

        this.choosePreviewSize(cameraParameters, encWidth, encHeight);

        // Leave the frame rate set to the default
        this.mCamera.setParameters(cameraParameters);

        Camera.Size cameraSize = cameraParameters.getPreviewSize();
        PLog.logDebug(this.TAG, "Camera preview size is " + cameraSize.width + "x" + cameraSize.height);

    }

    private static void choosePreviewSize(Camera.Parameters cameraParameters, int width, int height) {

        // We should make sure that the requested MPEG size is less than the preferred
        // size, and has the same aspect ratio.
        Camera.Size ppsfv = cameraParameters.getPreferredPreviewSizeForVideo();
        if (ppsfv != null) {
            PLog.logDebug(TAG, "Camera preferred preview size for video is: " + ppsfv.width + "x" + ppsfv.height);
        }

        for (Camera.Size size : cameraParameters.getSupportedPreviewSizes()) {
            if (size.width == width && size.height == height) {
                cameraParameters.setPreviewSize(width, height);
                return;
            }
        }

        PLog.logWarning(TAG, "Unable to set preview size to " + width + "x" + height);
        if (ppsfv != null) {
            cameraParameters.setPreviewSize(ppsfv.width, ppsfv.height);
        }

    }

    private void drainEncoder(boolean endOfStream) {

        final int TIMEOUT_USEC = 10000;
        PLog.logDebug(this.TAG, "drainEncoder(" + endOfStream + ")");

        if (endOfStream) {
            PLog.logDebug(this.TAG, "drainEncoder() -> Sending EOS to encoder");
            this.mMediaCodec.signalEndOfInputStream();
        }

        ByteBuffer[] encoderOutputBuffers = this.mMediaCodec.getOutputBuffers();

        while (true) {

            int encoderStatus = this.mMediaCodec.dequeueOutputBuffer(this.mBufferInfo, TIMEOUT_USEC);

            // No output is yet available
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) {
                    break;  // Break out of loop
                } else {
                    PLog.logDebug(this.TAG, "No output from encoder available. Busy waiting on EOS");
                }
            }
            // Unexpected behviour for the encoder
            else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                encoderOutputBuffers = this.mMediaCodec.getOutputBuffers();
            }
            // Should happen before receiving buffers, and should only happen once
            else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

                if (this.mMediaMuxerRunning) {
                    throw new RuntimeException("Encoder format changed more than once");
                }

                MediaFormat newFormat = this.mMediaCodec.getOutputFormat();
                PLog.logDebug(this.TAG, "Encoder output format changed: " + newFormat);

                // We not have the output format and can start the muxer
                this.mTrackIndex = this.mMediaMuxer.addTrack(newFormat);
                this.mMediaMuxer.start();
                this.mMediaMuxerRunning = true;

            }
            // Unexpected encoder status
            else if (encoderStatus < 0) {
                PLog.logWarning(this.TAG, "Unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
                // Ignore it!  Do something about this in production.
            }

            else {

                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];

                if (encodedData == null) {
                    throw new RuntimeException("The encoderOutputBuffer " + encoderStatus + " was null!");
                }

                if ((this.mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    PLog.logDebug(this.TAG, "Ignoring BUFFER_FLAG_CODEC_CONFIG");
                    this.mBufferInfo.size = 0;
                }

                if (this.mBufferInfo.size != 0) {

                    if (!mMediaMuxerRunning) {
                        throw new RuntimeException("Muxer hasn't started!");
                    }

                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(this.mBufferInfo.offset);
                    encodedData.limit(this.mBufferInfo.offset + this.mBufferInfo.size);

                    mMediaMuxer.writeSampleData(mTrackIndex, encodedData, this.mBufferInfo);

                    PLog.logDebug(this.TAG, "Sent " + this.mBufferInfo.size + " bytes to the muxer");
                }

                this.mMediaCodec.releaseOutputBuffer(encoderStatus, false);

                if ((this.mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        PLog.logWarning(this.TAG, "Reached end of stream unexpectedly");
                    } else {
                        PLog.logDebug(this.TAG, "End of stream reached");
                    }
                    break;
                }
            }
        }
    }

    public void startRecordingSequence(SurfaceTexture surfaceTexture) {
        // Record the start time and specify a desired end
        long startTime = System.nanoTime();
        long requestEndTime = startTime + DURATION_SEC * 1000000000L;
        //SurfaceTexture surfaceTexture = this.mSurfaceTextureManager.getSurfaceTexture();
        int frameCount = 0;

        // While "we can" keep...
        while (System.nanoTime() < requestEndTime) {

            // Feed any pending encoder output into the muxer
            this.drainEncoder(false);


                /* ########## REMOVE THIS EVENTUALLY ########### */
            // Switch up the colors every 15 frames.  Besides demonstrating the use of
            // fragment shaders for video editing, this provides a visual indication of
            // the frame rate: if the camera is capturing at 15fps, the colors will change
            // once per second.
            if ((frameCount % 15) == 0) {
                PLog.logNotice("MOFO", "Score!");
                String fragmentShader = null;
                if ((frameCount & 0x01) != 0) {
                    fragmentShader = SWAPPED_FRAGMENT_SHADER;
                }
                this.mSurfaceTextureManager.changeFragmentShader(fragmentShader);
            }
            frameCount++;
                /* ########## /REMOVE THIS EVENTUALLY ########### */

            // Acquire a new frame of input, and render it to the Surface.  If we had a
            // GLSurfaceView we could switch EGL contexts and call drawImage() a second
            // time to render it on screen.  The texture can be shared between contexts by
            // passing the GLSurfaceView's EGLContext as eglCreateContext()'s share_context
            // argument.
            this.mSurfaceTextureManager.awaitNewImage();
            this.mSurfaceTextureManager.drawImage();

            // Set the presentation time stamp from the SurfaceTexture's time stamp.  This
            // will be used by MediaMuxer to set the PTS in the video.

            PLog.logVerbose(this.TAG, "present: " + ((surfaceTexture.getTimestamp() - startTime) / 1000000.0) + "ms");

            this.mCodecInputSurface.setPresentationTime(surfaceTexture.getTimestamp());

            // Submit it to the encoder.  The eglSwapBuffers call will block if the input
            // is full, which would be bad if it stayed full until we dequeued an output
            // buffer (which we can't do, since we're stuck here).  So long as we fully drain
            // the encoder before supplying additional input, the system guarantees that we
            // can supply another frame without blocking.

            PLog.logDebug(this.TAG, "Sending frame to encoder");

            this.mCodecInputSurface.swapBuffers();
        }

        // Send end-of-stream to encoder, and drain remaining output
        drainEncoder(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTextureView = new TextureView(this);
        SurfaceTexture st = mTextureView.getSurfaceTexture();
        mTextureView.setSurfaceTextureListener(this);



        setContentView(mTextureView);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        //mCamera = Camera.open();

        PLog.logError("MOFO", "ABC123");

        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
            mTextureView.setLayoutParams(new FrameLayout.LayoutParams(
                    previewSize.width, previewSize.height, Gravity.CENTER));

            try {
                mCamera.setPreviewTexture(surface);
        } catch (IOException t) {
        }
        PLog.logError("MOFO", "What the hell?");
        this.prepareCamera(this.MEDIA_WIDTH, this.MEDIA_HEIGHT);
        PLog.logError("MOFO", "What the fuck?");
        this.prepareEncoder(this.MEDIA_WIDTH, this.MEDIA_HEIGHT, this.MEDIA_ENC_RATE);


        mCamera.startPreview();

        this.startRecordingSequence(surface);


    }

    private void prepareEncoder(int width, int height, int bitRate) {

        this.mBufferInfo = new MediaCodec.BufferInfo();

        MediaFormat format = MediaFormat.createVideoFormat(this.MIME_TYPE, width, height);

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
        PLog.logDebug(this.TAG, "Format: " + format);


        // Create a MediaCodec encoder, and configure it with our format.  Get a Surface
        // we can use for input and wrap it with a class that handles the EGL work.
        //
        // If you want to have two EGL contexts -- one for display, one for recording --
        // you will likely want to defer instantiation of CodecInputSurface until after the
        // "display" EGL context is created, then modify the eglCreateContext call to
        // take eglGetCurrentContext() as the share_context argument.
        this.mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        this.mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);


        this.mCodecInputSurface = new CodecInputSurface(this.mMediaCodec.createInputSurface());
        this.mMediaCodec.start();

        // Output filename.  Ideally this would use Context.getFilesDir() rather than a
        // hard-coded output directory.
        String outputPath = new File(OUTPUT_DIR, "tesurfaceTexture" + width + "x" + height + ".mp4").toString();
        PLog.logNotice(this.TAG, "Output file is " + outputPath);

        // Create a MediaMuxer.  We can't add the video track and start() the muxer here,
        // because our MediaFormat doesn't have the Magic Goodies.  These can only be
        // obtained from the encoder after it has started processing data.
        //
        // We're not actually interested in multiplexing audio.  We just want to convert
        // the raw H.264 elementary stream we get from MediaCodec into a .mp4 file.
        try {
            mMediaMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException ioe) {
            throw new RuntimeException("MediaMuxer creation failed", ioe);
        }

        mTrackIndex = -1;
        mMediaMuxerRunning = false;
    }



    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, the Camera does all the work for us
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.stopPreview();
        mCamera.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Update your view here!
    }
}
