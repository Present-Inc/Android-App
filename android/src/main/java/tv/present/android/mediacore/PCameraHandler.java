package tv.present.android.mediacore;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.Serializable;
import java.lang.ref.WeakReference;

import tv.present.android.controllers.PRecordingSessionController;
import tv.present.android.util.PLog;

/**
 * Handles camera operation requests from other threads.  Necessary because the Camera
 * must only be accessed from one thread.  This object is created on the UI thread, and all handlers
 * run there.  Messages are sent from other threads, using sendMessage().
 */
public class PCameraHandler extends Handler implements Serializable {

    public static final int MSG_SET_SURFACE_TEXTURE = 0;
    
    private static final String TAG = "tv.present.android.mediacore.PCameraHandler";

    private WeakReference<PRecordingSessionController> weakActivityReference;

    /**
     * Constructs a PCameraHandler object.
     * @param controller is a PController object.
     */
    public PCameraHandler(final PRecordingSessionController controller) {
        this.weakActivityReference = new WeakReference<PRecordingSessionController>(controller);
    }

    /**
     * Drops the reference to the activity.  Useful as a paranoid measure to ensure that attempts to
     * access a stale Activity through a handler are caught.
     */
    public void invalidateHandler() {
        this.weakActivityReference.clear();
    }

    @Override  // runs on UI thread
    public void handleMessage(final Message inputMessage) {

        int messageNum = inputMessage.what;
        Log.d(TAG, "handleMessage() -> PCameraHandler [" + this + "]: what = " + messageNum);

        PRecordingSessionController controller = this.weakActivityReference.get();

        if (controller == null) {
            PLog.logWarning(TAG, "CameraHandler.handleMessage: Controller is null!");
            return;
        }

        switch (messageNum) {
            case MSG_SET_SURFACE_TEXTURE:
                controller.handleSetSurfaceTexture((SurfaceTexture) inputMessage.obj);
                break;
            default:
                throw new RuntimeException("unknown msg " + messageNum);
        }
        
    }
}