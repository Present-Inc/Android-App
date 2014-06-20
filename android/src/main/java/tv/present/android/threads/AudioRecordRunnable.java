package tv.present.android.threads;

import tv.present.android.mediacore.PChunkingRecorder;
import tv.present.android.util.PLog;

/**
 * Present Audio Record Runnable
 *
 * This class is the runnable thread for the audio recorder.
 *
 * June 19, 2014
 * @author Kyle Weisel (kyle@present.tv)
 *
 */
public final class AudioRecordRunnable implements Runnable {

    private static final String TAG = "tv.present.android.threads.AudioRecordRunnable";
    private final PChunkingRecorder chunkingRecorder;

    /**
     * Constructs an AudioRecordThread.
     * @param chunkingRecorder is a PChunkingRecorder (which is probably a reference to the object
     *                         that created this thread)
     */
    public AudioRecordRunnable(final PChunkingRecorder chunkingRecorder) {
        this.chunkingRecorder = chunkingRecorder;
    }

    /**
     * This method is called when the thread is started.
     */
    @Override
    public final void run() {

        this.chunkingRecorder.getAudioRecord().startRecording();
        boolean audioEosRequestedCopy, fullStopReceivedCopy;

        while(true) {

            if (! this.chunkingRecorder.isFirstFrameReady()) {
                continue;
            }

            // Creating a copy of this boolean so that it does not change within the loop.
            audioEosRequestedCopy = this.chunkingRecorder.isAudioEosRequested();
            fullStopReceivedCopy = this.chunkingRecorder.isFullStopReceived();

            if (audioEosRequestedCopy || fullStopReceivedCopy) {
                // TODO: Should we post an EOSRecieved message to the handler?
                PLog.logDebug(TAG, "run() -> Audio loop caught audioEosRequested / fullStopReceived " + audioEosRequestedCopy + " " + fullStopReceivedCopy);
                this.chunkingRecorder.sendAudioToEncoder(true);
            }

            if (fullStopReceivedCopy) {
                PLog.logDebug(TAG, "run() -> Stopping AudioRecord");
                this.chunkingRecorder.getAudioRecord().stop();
            }

            synchronized (this.chunkingRecorder.getAudioTrackInfo().muxerWrapper.sync) {
                this.chunkingRecorder.drainEncoder(this.chunkingRecorder.getAudioEncoder(), this.chunkingRecorder.getAudioBufferInfo(), this.chunkingRecorder.getAudioTrackInfo(), audioEosRequestedCopy || fullStopReceivedCopy);
            }

            if (audioEosRequestedCopy) {
                this.chunkingRecorder.setAudioEOSRequested(false);
            }

            if (! fullStopReceivedCopy) {
                this.chunkingRecorder.sendAudioToEncoder(false);
            }
            else {
                break;
            }

        }

    }

}
