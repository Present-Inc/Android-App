package tv.present.android.mediacore;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.view.Surface;

import tv.present.android.util.PLog;

/**
 * Present Codec Input Surface
 *
 * This class manages staete associated with a Surface that is used as a MediaCodec encoder input.
 * The constructor will take a Surface that is obtained from MediaCodec.crateInputSurface(), and
 * uses that Surface to create an EGL window surface.  Any calls to eglSwapBuffers() cause a frame
 * of data to be sent to the video encoder.
 *
 * IMPORTANT:  This object owns the Surface -- releasing this will release the Surface too.
 *
 * June 18, 2014
 * @author Kyle Weisel (kyle@present.tv)
 */
public class PCodecInputSurface {

    private static final String TAG = "tv.present.android.util.CodecInputSurface";
    private static final int EGL_RECORDABLE_ANDROID = 0x3142;

    public EGLContext eglDisplayContext = EGL14.EGL_NO_CONTEXT;

    private EGLDisplay eglDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLContext eglEncodeContext = EGL14.EGL_NO_CONTEXT;
    private EGLSurface eglSurface = EGL14.EGL_NO_SURFACE;
    private Surface surface;
    private EGLConfig[] configs;
    private int[] surfaceAttribs = { EGL14.EGL_NONE };

    /**
     * Constructs a PCodecInputSurface object from a raw Surface object.
     * @param surface is a Surface object to use when creating the CodecInputSurface.
     */
    public PCodecInputSurface(final Surface surface) {
        if (surface != null) {
            this.surface = surface;
            this.eglSetup();
        }
        else {
            throw new NullPointerException("There was no surface passed to the constructor of CodecInputSurface");
        }
    }

    /**
     * Prepares EGL 2.0.
     */
    private void eglSetup() {

        PLog.logDebug(TAG, "eglSetup() -> creating EGL14 Surface");

        this.eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);

        if (this.eglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglSetup() -> Unable to get EGL14 display!");
        }

        int[] version = new int[2];

        if (!EGL14.eglInitialize(this.eglDisplay, version, 0, version, 1)) {
            throw new RuntimeException("eglSetup() -> Unable to initialize EGL14");
        }

        // Configure EGL for recording and OpenGL ES 2.0.
        int[] attribList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE,
                EGL14.EGL_OPENGL_ES2_BIT, EGL_RECORDABLE_ANDROID, 1,
                EGL14.EGL_NONE
        };

        this.configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        
        EGL14.eglChooseConfig(this.eglDisplay, attribList, 0, configs, 0, configs.length, numConfigs, 0);
        this.checkEglError("eglSetup() -> eglCreateContext RGB888+recordable ES2");

        // Configure context for OpenGL ES 2.0.
        int[] attrib_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        
        if(eglDisplayContext == EGL14.EGL_NO_CONTEXT) {
            PLog.logError(TAG, "eglSetup() -> eglDisplayContext not set properly");
        }

        this.eglEncodeContext = EGL14.eglCreateContext(eglDisplay, configs[0], EGL14.eglGetCurrentContext(), attrib_list, 0);
        this.checkEglError("eglCreateContext");

        // Create a window surface and attach it to the Surface we received.
        this.createEGLSurfaceFrosurface();

        this.checkEglError("eglCreateWindowSurface");

    }

    /**
     * Makes the current member EGLDisplayContext current.
     */
    public void makeDisplayContextCurrent(){
        this.makeCurrent(eglDisplayContext);
    }

    /**
     * Makes the current eglEncodeContext current.
     */
    public void makeEncodeContextCurrent(){
        this.makeCurrent(this.eglEncodeContext);
    }

    /**
     * Makes the EGLDisplay and EGLSurface current.
     */
    private void makeCurrent(final EGLContext context) {
        EGL14.eglMakeCurrent(this.eglDisplay, this.eglSurface, this.eglSurface, context);
        this.checkEglError("CodecInputSurface::makeCurrent()");
    }

    /**
     * Discards all resources held by this class, most notably the EGL context.  Also releases the
     * Surface that was passed to the constructor.
     */
    public void release() {

        if (this.eglDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(this.eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(this.eglDisplay, this.eglSurface);
            EGL14.eglDestroyContext(this.eglDisplay, this.eglEncodeContext);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(this.eglDisplay);
        }

        this.surface.release();
        this.eglDisplay = EGL14.EGL_NO_DISPLAY;
        this.eglEncodeContext = EGL14.EGL_NO_CONTEXT;
        this.eglSurface = EGL14.EGL_NO_SURFACE;
        this.surface = null;

    }

    /**
     * Sends the presentation time stamp to EGL.  Time is expressed in nanoseconds.
     * @param nsecs is the presentation time in nanoseconds.
     */
    public void setPresentationTime(final long nsecs) {
        EGLExt.eglPresentationTimeANDROID(eglDisplay, eglSurface, nsecs);
        this.checkEglError("eglPresentationTimeANDROID");
    }

    /**
     * Calls eglSwapBuffers.  This is the method that will "publish" the current frame.
     */
    public boolean swapBuffers() {
        final boolean result = EGL14.eglSwapBuffers(eglDisplay, eglSurface);
        this.checkEglError("CodecInputSurface::swapBuffers()");
        return result;
    }

    /**
     * Destroys the old surface and replaces it with a new surface.
     * @param newSurface is the new Surface to replace the old one with.
     */
    public void updateSurface(final Surface newSurface){
        EGL14.eglDestroySurface(this.eglDisplay, this.eglSurface);
        this.surface = newSurface;
        // Create EGL surface from the new Surface
        this.createEGLSurfaceFrosurface();
        this.checkEglError("eglCreateWindowSurface");
    }

    /**
     * Checks if there is an EGL error and will throw a Runtime exception if one is found.
     * @param message is the message to pass to the runtime exception that will be thrown on error.
     */
    private void checkEglError(String message) {
        int error;
        if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
            throw new RuntimeException(message + ": EGL error: 0x" + Integer.toHexString(error));
        }
    }

    /**
     * Creates an EGL surface from a regular surface.  This method cannot be called before the
     * surface has been set (if you can even find a way to do that).
     */
    private void createEGLSurfaceFrosurface() {
        this.eglSurface = EGL14.eglCreateWindowSurface(this.eglDisplay, configs[0], this.surface, this.surfaceAttribs, 0);
    }

}