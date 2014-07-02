package tv.present.android.views;

import android.content.Context;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import tv.present.android.mediacore.PCameraRenderer;
import tv.present.android.util.PAndroidGlobals;
import tv.present.android.util.PLog;

/**
 * Created by kbw28 on 6/26/14.
 */
public class PGLSurfaceView extends GLSurfaceView {

    private static final String TAG = "tv.present.android.views.PGLSurfaceView";
    private PCameraRenderer renderer;
    private Camera camera;

    public PGLSurfaceView(Context context) {
        super(context);
        this.initialize();
    }

    public PGLSurfaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.initialize();
    }

    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        PLog.logDebug(TAG, "onSurfaceCreated() -> Start method");
        super.surfaceCreated(surfaceHolder);
    }

    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        PLog.logDebug(TAG, "onSurfaceDestroyed() -> Start method");
        super.surfaceDestroyed(surfaceHolder);
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {
        PLog.logDebug(TAG, "onSurfaceChanged() -> Start method");
        super.surfaceChanged(surfaceHolder, format, w, h);
    }

    private void initialize() {
        PLog.logDebug(TAG, "initialize() -> Start method");
        //this.camera = Camera.open();
        this.renderer = new PCameraRenderer(this.getContext());
        //this.renderer.setCamera(this.camera);
        this.setEGLContextClientVersion(PAndroidGlobals.EGL_CONTEXT_CLIENT_VERSION);
        this.setRenderer(this.renderer);
        this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }

}
