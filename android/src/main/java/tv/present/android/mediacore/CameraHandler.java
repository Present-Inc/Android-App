package tv.present.android.mediacore;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

import tv.present.android.CameraCaptureActivity;
import tv.present.android.util.PLog;

/**
 * Handles camera operation requests from other threads.  Necessary because the Camera
 * must only be accessed from one thread.
 * <p>
 * The object is created on the UI thread, and all handlers run there.  Messages are
 * sent from other threads, using sendMessage().
 */
public class CameraHandler extends Handler {

    private static final String TAG = "tv.present.android.mediacore.CameraHandler";
    public static final int MSG_SET_SURFACE_TEXTURE = 0;

    // Weak reference to the Activity; only access this from the UI thread.
    private WeakReference<CameraCaptureActivity> mWeakActivity;

    public CameraHandler(CameraCaptureActivity activity) {
        mWeakActivity = new WeakReference<CameraCaptureActivity>(activity);
    }

    /**
     * Drop the reference to the activity.  Useful as a paranoid measure to ensure that
     * attempts to access a stale Activity through a handler are caught.
     */
    public void invalidateHandler() {
        mWeakActivity.clear();
    }

    @Override  // runs on UI thread
    public void handleMessage(Message inputMessage) {
        int what = inputMessage.what;
        PLog.logDebug(this.TAG, "CameraHandler [" + this + "]: what=" + what);

        CameraCaptureActivity activity = mWeakActivity.get();
        if (activity == null) {
            Log.w(TAG, "CameraHandler.handleMessage: activity is null");
            return;
        }

        switch (what) {
            case MSG_SET_SURFACE_TEXTURE:
                activity.handleSetSurfaceTexture((SurfaceTexture) inputMessage.obj);
                break;
            default:
                throw new RuntimeException("unknown msg " + what);
        }
    }
}
