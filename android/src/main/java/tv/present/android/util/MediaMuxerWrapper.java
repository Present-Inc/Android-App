package tv.present.android.util;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Present Media Muxer Wrapper
 *
 * This class manages recording and chunking the resulting video data so that it can be uploaded
 * to the server.
 *
 * June 18, 2014
 * @author Kyle Weisel (kyle@present.tv)
 *
 */
public final class MediaMuxerWrapper {

    private static final String TAG = "tv.present.android.util.MediaMuxerWrapper";
    private static int TOTAL_NUM_TRACKS = 2;

    // TODO: Consider making the chunk integer a private member.
    private MediaMuxer muxer;
    private PChunkingRecorder chunkingRecorder;
    private boolean started;
    private int numTracksAdded = 0;
    private int numTracksFinished = 0;

    public final Object sync = new Object();

    /**
     * Constructs a new MediaMuxerWrapper.
     * @param chunkingRecorder is a reference to a PChunking recorder that is using this wrapper.
     * @param format is the integer valued format that the muxer should operate with.
     * @param chunk is the integer chunk number that this muxer will be processing.
     */
    public MediaMuxerWrapper(final PChunkingRecorder chunkingRecorder, final int format, final int chunk) {
        this.started = false;
        this.chunkingRecorder = chunkingRecorder;
        this.restart(format, chunk);
    }

    /**
     * Adds a media format track to the muxer and returns a track index to the caller.
     * @param format is a MediaFormat to be added as a track.
     * @return an integer that represents the track index this format has on the muxer.
     */
    public final int addTrack(final MediaFormat format) {
        this.numTracksAdded++;
        final int trackIndex = muxer.addTrack(format);
        if(this.numTracksAdded == TOTAL_NUM_TRACKS) {
            PLog.logDebug(TAG, "All tracks added, starting " + ((this == chunkingRecorder.getMuxerWrapper1()) ? "muxer1" : "muxer2") + "!");
            this.start();
        }
        return trackIndex;
    }

    /**
     * Finishes a track.
     */
    public final void finishTrack() {
        numTracksFinished++;
        if(numTracksFinished == TOTAL_NUM_TRACKS) {
            PLog.logNotice(TAG, "All tracks finished, stopping " + ((this == chunkingRecorder.getMuxerWrapper1()) ? "muxer1" : "muxer2") + "!");
            this.stop();
        }

    }

    /**
     * Determines whether all tracks have been added to this muxer.
     * @return true if all tracks have been added, false otherwise.
     */
    public final boolean isAllTracksAdded() {
        return (numTracksAdded == TOTAL_NUM_TRACKS);
    }

    /**
     * Determines whether all tracks have been finished by this muxer.
     * @return true if all tracks have been finished, false otherwise.
     */
    public final boolean isAllTracksFinished() {
        return (numTracksFinished == TOTAL_NUM_TRACKS);
    }

    /**
     * Gets status of the muxer.
     * @return true if the muxer is started, false otherwise.
     */
    public final boolean isStarted() {
        return this.started;
    }

    /**
     * Restarts the muxer that this object wraps.
     * @param format is the new integer format to start the muxer with.
     * @param chunk is the integer chunk number that this muxer will be processing.
     */
    public final void restart(final int format, final int chunk) {
        this.stop();
        try {
            this.muxer = new MediaMuxer(this.outputPathForChunk(chunk), format);
        } catch (IOException e) {
            throw new RuntimeException("MediaMuxer creation failed", e);
        }
    }

    /**
     * Stops the muxer.
     */
    public final void stop() {

        // Only do work if the muxer is not null.
        if(this.muxer != null) {

            if (! this.isAllTracksFinished()) {
                PLog.logError(TAG, "Stopping muxer before all tracks were finished!");
            }

            if (! this.started) {
                PLog.logError(TAG, "Stopping muxer before it was started.  Figure that one out...");
            }

            this.muxer.stop();
            this.muxer.release();
            this.muxer = null;
            this.started = false;
            this.numTracksAdded = 0;
            this.numTracksFinished = 0;

        }

    }

    /**
     * Provides an interface to write sample data to the muxer.  Other classes, (specifically the
     * PChunkingRecorder) will use this method to write data to this wrapper's muxer.
     * @param trackIndex is the track index integer.
     * @param byteBuffer is a ByteBuffer of data.
     * @param bufferInfo is a BufferInfo object that corresponds to the ByteBuffer data.
     */
    public final void writeSampleDataToMuxer(final int trackIndex, ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
        this.muxer.writeSampleData(trackIndex, byteBuffer, bufferInfo);
    }

    /**
     * Generates an output path for a specific chunk of video.
     * @param chunkNumber is the integer value of the chunk of video.
     * @return a String that is the valid output path for this video.
     */
    private String outputPathForChunk(final int chunkNumber) {
        return PAndroidGlobals.TEMP_DIRECTORY + "cache" + chunkNumber + ".mp4";
    }

    /**
     * Starts the muxer.
     */
    private final void start() {
        this.muxer.start();
        this.started = true;
    }

}
