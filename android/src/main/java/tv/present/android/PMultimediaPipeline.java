package tv.present.android;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.GLES20;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import tv.present.android.media.CodecInputSurface;
import tv.present.android.util.PLog;

/**
 *
 */
public class PMultimediaPipeline {

    private static final String TAG = "tv.present.android.PMultimediaPipeline";
    private MediaCodec mediaCodecAudio, mediaCodecVideo;
    private MediaMuxer mediaMuxer;

    private CodecInputSurface mediaInputSurface;

    // Encoder (codec) parameters (Audio)
    private final String AUDIO_MIME_TYPE = "audio/mpeg";
    private final int AUDIO_SAMPLE_RATE = 32000;
    private final int AUDIO_NUM_CHANNELS = 2;

    // Encoder (codec) parameters (Video)
    private final String VIDEO_MIME_TYPE = "video/avc";
    private final int VIDEO_FRAME_RATE = 15;
    private final int VIDEO_IFRAME_INTERVAL = 10;
    private final int VIDEO_NUM_TOTAL_FRAMES = 150;

    private final File OUTPUT_DIR = Environment.getExternalStorageDirectory();

    // RGB color values for generated frames
    private static final int TEST_R0 = 0;
    private static final int TEST_G0 = 136;
    private static final int TEST_B0 = 0;
    private static final int TEST_R1 = 236;
    private static final int TEST_G1 = 50;
    private static final int TEST_B1 = 186;

    // Interior parameters
    private int mVideoHeight = -1,
                mVideoWidth = -1,
                mVideoBitRate = -1;
    private int trackIndex;
    private boolean muxerIsRunning;

    private MediaCodec.BufferInfo videoBufferInfo;

    public PMultimediaPipeline() {
        this.muxerIsRunning = false;
        this.videoBufferInfo = new MediaCodec.BufferInfo();
    }

    public PMultimediaPipeline(final int height, final int width) {
        this.muxerIsRunning = false;
        this.mVideoHeight = height;
        this.mVideoWidth = width;
        this.videoBufferInfo = new MediaCodec.BufferInfo();
    }

    public void prepareCodecs() {

        /* Audio Codec
        MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
        MediaFormat audioMediaFormat = MediaFormat.createAudioFormat(this.AUDIO_MIME_TYPE, this.bitRate, this.AUDIO_NUM_CHANNELS);*/

        // Video Codec

        MediaFormat videoMediaFormat = MediaFormat.createVideoFormat(this.VIDEO_MIME_TYPE, this.mVideoWidth, this.mVideoHeight);
        videoMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        videoMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, this.mVideoBitRate);
        videoMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, this.VIDEO_FRAME_RATE);
        videoMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, this.VIDEO_IFRAME_INTERVAL);

        this.mediaCodecVideo = MediaCodec.createEncoderByType(this.VIDEO_MIME_TYPE);
        this.mediaCodecVideo.configure(videoMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        this.mediaInputSurface = new CodecInputSurface(this.mediaCodecVideo.createInputSurface());
        this.mediaCodecVideo.start();


        String outputPath = this.OUTPUT_DIR + "/" + this.segmentFileName();

        // Configure, but don't start the muxer
        try {
            this.mediaMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create the media muxer!");
        }

        this.trackIndex = -1;
        this.muxerIsRunning = false;

    }

    /**
     * Releases the codec resources.  May be called after a full or failed initialization.
     */
    public void releaseCodecs() {
        if (this.mediaCodecVideo != null) {
            this.mediaCodecVideo.stop();
            this.mediaCodecVideo.release();
            this.mediaCodecVideo = null;
        }
        if (this.mediaInputSurface != null) {
            this.mediaInputSurface.release();
            this.mediaInputSurface = null;
        }
        if(this.mediaMuxer != null) {
            this.mediaMuxer.stop();
            this.mediaMuxer.release();
            this.mediaMuxer = null;
        }
    }

    private void drainEncoder(boolean endOfStream) {
        final int TIMEOUT_SEC = 10000;
        PLog.logDebug(this.TAG, "PMultimediaPipeline->drainEncoder(" + endOfStream + ") has been called.");
        if (endOfStream) {
            PLog.logDebug(this.TAG, "PMultimediaPipeline->drainEncoder() is sending EOS to the mediaCodecVideo encoder.");
            this.mediaCodecVideo.signalEndOfInputStream();
        }

        ByteBuffer[] videoEncoderOutputBuffers = this.mediaCodecVideo.getOutputBuffers();

        while(true) {

            final int encoderStatus = this.mediaCodecVideo.dequeueOutputBuffer(this.videoBufferInfo, TIMEOUT_SEC);

            // Sometimes, there isn't any output available
            if(encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if(endOfStream) {
                    PLog.logDebug(this.TAG, "No output available...busy waiting for EOS.");
                } else {
                    break;
                }
            }
            // Other times there is new data available
            else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                videoEncoderOutputBuffers = this.mediaCodecVideo.getOutputBuffers();
            }
            else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if(this.muxerIsRunning) {
                    throw new RuntimeException("Output format of mediaCodecVideo changed more than once.");
                }

                MediaFormat newFormat = this.mediaCodecVideo.getOutputFormat();
                PLog.logDebug(this.TAG, "Encoder output format changed to: " + newFormat.toString());

                this.trackIndex = this.mediaMuxer.addTrack(newFormat);
                this.mediaMuxer.start();
                this.muxerIsRunning = true;
            }
            else if (encoderStatus < 0) {
                PLog.logWarning(this.TAG, "There was an unexpected result from mediaCodecVideo.dequeueOutputBuffer() : " + encoderStatus);
            }
            else {
                ByteBuffer encodedData = videoEncoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("The mediaCodecVideo output buffer " + encoderStatus + " came back null.");
                }

                //
                if((this.videoBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    PLog.logDebug(this.TAG, "Ignoring BUFFER_FLAG_CODEC_CONFIG.");
                    this.videoBufferInfo.size = 0;
                }
                if(this.videoBufferInfo.size != 0) {
                    if(!this.muxerIsRunning) {
                        throw new RuntimeException("The MediaMuxer has yet to start, or wasn't started in the first place!");
                    }
                    encodedData.position(this.videoBufferInfo.offset);
                    encodedData.limit(this.videoBufferInfo.offset + this.videoBufferInfo.size);

                    this.mediaMuxer.writeSampleData(this.trackIndex, encodedData, this.videoBufferInfo);
                    PLog.logDebug(this.TAG, "Just sent " + this.videoBufferInfo.size + " bytes to the MediaMuxer.");
                }

                this.mediaCodecVideo.releaseOutputBuffer(encoderStatus, false);

                if ((this.videoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        PLog.logWarning(this.TAG, "Reached end of stream unexpectedly!");
                    } else {
                        PLog.logDebug(this.TAG, "Reached the end of stream (EOS).");
                    }
                    break;
                }
            }
        }

    }

    private void generateSurfaceFrame(int frameIndex) {

        frameIndex %= 8;

        int startX, startY;
        if (frameIndex < 4) {
            // (0,0) is bottom-left in GL
            startX = frameIndex * (this.mVideoWidth / 4);
            startY = this.mVideoHeight / 2;
        } else {
            startX = (7 - frameIndex) * (this.mVideoWidth / 4);
            startY = 0;
        }

        GLES20.glClearColor(TEST_R0 / 255.0f, TEST_G0 / 255.0f, TEST_B0 / 255.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(startX, startY, this.mVideoWidth/ 4, this.mVideoHeight / 2);
        GLES20.glClearColor(TEST_R1 / 255.0f, TEST_G1 / 255.0f, TEST_B1 / 255.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);

    }

    public void run() {
        //QVGA at 2MBPS
        this.mVideoHeight = 480;
        this.mVideoWidth = 480;
        this.mVideoBitRate = 2000000;

        try {
            this.prepareCodecs();
            this.mediaInputSurface.makeCurrent();

            for (int i = 0; i < this.VIDEO_NUM_TOTAL_FRAMES; i++) {
                this.drainEncoder(false);
                this.generateSurfaceFrame(i);
                this.mediaInputSurface.setPresentationTime(this.computePresentationTimeNsec(i));
                PLog.logDebug(this.TAG, "Sending frame " + i  + " to the encoder.");
                this.mediaInputSurface.swapBuffers();
            }

            PLog.logDebug(this.TAG, "Finally sending drainEncoder(true).");
            this.drainEncoder(true);

        } finally {
            this.releaseCodecs();
        }

    }

    /**
     * Generates the presentation time for frame N, in nanoseconds.
     */
    private long computePresentationTimeNsec(int frameIndex) {
        final long ONE_BILLION = 1000000000;
        return frameIndex * ONE_BILLION / this.VIDEO_FRAME_RATE;
    }

    private String segmentFileName() {
        return "filename.mp4";
    }





}
