package tv.present.android.media;

import android.graphics.SurfaceTexture;
import tv.present.android.util.PLog;

/**
 * This class manages a SurfaceTexture.  It is responsible for creating SurfaceTexture and
 * SurfaceTextureRenderer objects, as well as providing the functions that wait for frames and
 * render them to the current EGL surface.
 *
 * The SurfaceTexture can be passed to Camera.setPreviewTexture() to receive camera output.
 */
public final class SurfaceTextureManager implements SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = "tv.present.android.media.SurfaceTextureManager";
    private SurfaceTexture mSurfaceTexture;
    private SurfaceTextureRenderer mSurfaceTextureRenderer;
    private Object mFrameSyncObject = new Object();     // guards mFrameAvailable
    private boolean mFrameAvailable;

    /**
     * Creates instances of TextureRender and SurfaceTexture.
     */
    public SurfaceTextureManager() {
        this.mSurfaceTextureRenderer = new SurfaceTextureRenderer();
        this.mSurfaceTextureRenderer.surfaceCreated();
        PLog.logDebug(this.TAG, "Texture ID: " + mSurfaceTextureRenderer.getTextureId());
        this.mSurfaceTexture = new SurfaceTexture(mSurfaceTextureRenderer.getTextureId());
        this.mSurfaceTexture.setOnFrameAvailableListener(this);
    }

    public void release() {
        // this causes a bunch of warnings that appear harmless but might confuse someone:
        //  W BufferQueue: [unnamed-3997-2] cancelBuffer: BufferQueue has been abandoned!
        //mSurfaceTexture.release();

        this.mSurfaceTextureRenderer = null;
        this.mSurfaceTexture = null;
    }

    /**
     * Returns the SurfaceTexture.
     */
    public SurfaceTexture getSurfaceTexture() {
        return this.mSurfaceTexture;
    }

    /**
     * Replaces the fragment shader.
     */
    public void changeFragmentShader(final String fragmentShader) {
        this.mSurfaceTextureRenderer.changeFragmentShader(fragmentShader);
    }

    /**
     * Latches the next buffer into the texture.  Must be called from the thread that created
     * the OutputSurface object.
     */
    public void awaitNewImage() {

        final int TIMEOUT_MS = 2500;

        synchronized (this.mFrameSyncObject) {

            while (!this.mFrameAvailable) {
                try {
                    // Wait for onFrameAvailable() to signal us.  Use a timeout to avoid
                    // stalling the test if it doesn't arrive.
                    this.mFrameSyncObject.wait(TIMEOUT_MS);
                    if (!this.mFrameAvailable) {
                        // TODO: if "spurious wakeup", continue while loop
                        throw new RuntimeException("Camera frame wait timed out");
                    }
                } catch (InterruptedException ie) {
                    // shouldn't happen
                    throw new RuntimeException(ie);
                }
            }
            this.mFrameAvailable = false;
        }

        // Latch the data.
        mSurfaceTextureRenderer.checkGlError("before updateTexImage");
        mSurfaceTexture.updateTexImage();
    }

    /**
     * Draws the data from SurfaceTexture onto the current EGL surface.
     */
    public void drawImage() {
        this.mSurfaceTextureRenderer.drawFrame(this.mSurfaceTexture);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        PLog.logDebug(this.TAG, "New frame available");
        synchronized (this.mFrameSyncObject) {
            if (this.mFrameAvailable) {
                throw new RuntimeException("mFrameAvailable already set, frame could be dropped");
            }
            mFrameAvailable = true;
            mFrameSyncObject.notifyAll();
        }
    }
}
