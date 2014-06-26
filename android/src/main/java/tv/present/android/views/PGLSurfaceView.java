package tv.present.android.views;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import tv.present.android.mediacore.PCameraRenderer;
import tv.present.android.util.PAndroidGlobals;

/**
 * Created by kbw28 on 6/26/14.
 */
public class PGLSurfaceView extends GLSurfaceView {

    private PCameraRenderer renderer;

    public PGLSurfaceView(Context context) {
        super(context);
        this.initialize();
    }

    public PGLSurfaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.initialize();
    }

    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        super.surfaceCreated(surfaceHolder);
    }

    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        super.surfaceDestroyed(surfaceHolder);
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {
        super.surfaceChanged(surfaceHolder, format, w, h);
    }

    private void initialize() {
        this.renderer = new PCameraRenderer(this.getContext());
        this.setEGLContextClientVersion(PAndroidGlobals.EGL_CONTEXT_CLIENT_VERSION);
        this.setRenderer(this.renderer);
        this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

}
