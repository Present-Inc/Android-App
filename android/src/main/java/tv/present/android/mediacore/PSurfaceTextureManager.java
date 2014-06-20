package tv.present.android.mediacore;

import android.graphics.SurfaceTexture;

import tv.present.android.util.PLog;

/**
 * Present Surface Texture Manager
 *
 * This class manages a SurfaceTexture.  It creates the SurfaceTexture and SurfaceTextureRenderer
 * objects and provides functions that wait for frames and then render them to the current EGL
 * surface.  This SurfaceTexture can be passed to the setPreviewTexture() method on the Camera
 * in order to receive camera output.
 *
 * June 19, 2014
 * @author Kyle Weisel (kyle@present.tv)
 *
 */
public final class PSurfaceTextureManager implements SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = "tv.present.android.recorder.SurfaceTextureManager";
    private final Object frameSyncObject = new Object();
    private SurfaceTexture surfaceTexture;
    private PSurfaceTextureRenderer textureRenderer;
    private boolean frameAvailable;

    /**
     * Creates an instance of a SurfaceTextureManager.  In addition, this constructor will also
     * create an instance of a SurfaceTexture and SurfaceTextureRenderer.
     */
    public PSurfaceTextureManager() {

        this.textureRenderer = new PSurfaceTextureRenderer();
        this.textureRenderer.surfaceCreated();
        PLog.logDebug(TAG, "SurfaceTextureManager() -> " + String.format("textureID=%d", textureRenderer.getTextureId()));
        this.surfaceTexture = new SurfaceTexture(this.textureRenderer.getTextureId());
        this.surfaceTexture.setOnFrameAvailableListener(this);

    }

    /**
     * Latches the next buffer into the texture.  This must be called from the thread that created
     * the OutputSurface object.
     */
    public final void awaitNewImage() {

        final int TIMEOUT_MS = 4500;

        synchronized (this.frameSyncObject) {

            while (! this.frameAvailable) {

                try {
                    // Wait for onFrameAvailable() to signal us.  Use a timeout to avoid stalling if
                    // the signal never arrives.
                    PLog.logDebug(TAG, "awaitNewImage() -> Waiting for frame in thread.");
                    this.frameSyncObject.wait(TIMEOUT_MS);
                    if (! this.frameAvailable) {
                        // TODO: If there is a "spurious wakeup", continue the while loop.
                        throw new RuntimeException("Camera frame wait timed out!");
                    }
                } catch (InterruptedException e) {
                    // Throw it further up!
                    throw new RuntimeException(e);
                }

            }

            this.frameAvailable = false;
        }

        // Latch the data.
        this.textureRenderer.checkGlError("Before updateTexImage");
        this.surfaceTexture.updateTexImage();

    }

    /**
     * Changes the fragment shader value.
     * @param fragmentShader a new String value for the fragment shader.
     */
    public final void changeFragmentShader(final String fragmentShader) {
        textureRenderer.changeFragmentShader(fragmentShader);
    }

    /**
     * Draws the data from SurfaceTexture onto the current EGL surface.
     */
    public final void drawImage() {
        this.textureRenderer.drawFrame(this.surfaceTexture);
    }

    /**
     * Gets the surface texture.
     * @return the SurfaceTexture object.
     */
    public final SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    /**
     * Releases the SurfaceTexture and the SurfaceTextureRenderer.
     */
    public final void release() {

        // TODO: Determine why this throws a bunch of warnings.
        // This line causes a bunch of warnings to be thrown that look like this:
        //  --> W BufferQueue: [unnamed-3997-2] cancelBuffer: BufferQueue has been abandoned!
        //
        // surfaceTexture.release();

        this.textureRenderer = null;
        this.surfaceTexture = null;
    }

    /**
     * Implementation of the OnFrameAvailableListener interface.
     * @param surfaceTexture is a surfaceTexture that has the frame available.
     */
    @Override
    public final void onFrameAvailable(final SurfaceTexture surfaceTexture) {

        PLog.logDebug(TAG, "onNewFrameAvailable() -> New frame is now available.");

        synchronized (this.frameSyncObject) {

            if (this.frameAvailable) {
                throw new RuntimeException("onFrameAvailable() -> frameAvailable already set, frame could be dropped!");
            }
            this.frameAvailable = true;
            this.frameSyncObject.notifyAll();

        }

    }

}
