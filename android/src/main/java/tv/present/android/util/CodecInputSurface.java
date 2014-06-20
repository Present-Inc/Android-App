package tv.present.android.util;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.view.Surface;

/**
 * Present Codec Input Surface
 *
 * This class manages state associated with a Surface that is used as a MediaCodec encoder input.
 * The constructor will take a Surface that is obtained from MediaCodec.createInputSurface(), and
 * uses that Surface to create an EGL window surface.  Any calls to eglSwapBuffers() cause a frame
 * of data to be sent to the video encoder.
 *
 * IMPORTANT:  This object owns the Surface -- releasing this will release the Surface too.
 *
 * June 18, 2014
 * @author Kyle Weisel (kyle@present.tv)
 */
public class CodecInputSurface {

    public EGLContext mEGLDisplayContext = EGL14.EGL_NO_CONTEXT;

    private static final String TAG = "tv.present.android.util.CodecInputSurface";
    private static final int EGL_RECORDABLE_ANDROID = 0x3142;

    private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLContext mEGLEncodeContext = EGL14.EGL_NO_CONTEXT;
    private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
    private Surface mSurface;
    private EGLConfig[] configs;
    private int[] surfaceAttribs = { EGL14.EGL_NONE };

    /**
     * Constructs a CodecInputSurface object from a raw Surface object.
     * @param surface is a Surface object to use when creating the CodecInputSurface.
     */
    public CodecInputSurface(Surface surface) {
        if (surface != null) {
            this.mSurface = surface;
            this.eglSetup();
        }
        else {
            throw new NullPointerException("There was no surface passed to the constructor of CodecInputSurface");
        }
    }

    /**
     * Destroys the old surface and replaces it with a new surface.
     * @param newSurface is the new Surface to replace the old one with.
     */
    public void updateSurface(Surface newSurface){
        EGL14.eglDestroySurface(this.mEGLDisplay, this.mEGLSurface);
        this.mSurface = newSurface;
        // Create EGL surface from the new Surface
        this.mEGLSurface = EGL14.eglCreateWindowSurface(this.mEGLDisplay, configs[0], this.mSurface, this.surfaceAttribs, 0);
        this.checkEglError("eglCreateWindowSurface");
    }

    /**
     * Prepares EGL 2.0.
     */
    private void eglSetup() {

        PLog.logDebug(TAG, "eglSetup() -> creating EGL14 Surface");

        this.mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);

        if (this.mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglSetup() -> Unable to get EGL14 display!");
        }

        int[] version = new int[2];

        if (!EGL14.eglInitialize(this.mEGLDisplay, version, 0, version, 1)) {
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
        
        EGL14.eglChooseConfig(this.mEGLDisplay, attribList, 0, configs, 0, configs.length, numConfigs, 0);
        this.checkEglError("eglSetup() -> eglCreateContext RGB888+recordable ES2");

        // Configure context for OpenGL ES 2.0.
        int[] attrib_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        
        if(mEGLDisplayContext == EGL14.EGL_NO_CONTEXT) {
            PLog.logError(TAG, "eglSetup() -> mEGLDisplayContext not set properly");
        }

        this.mEGLEncodeContext = EGL14.eglCreateContext(mEGLDisplay, configs[0], EGL14.eglGetCurrentContext(), attrib_list, 0);
        this.checkEglError("eglCreateContext");

        // Create a window surface and attach it to the Surface we received.
        this.mEGLSurface = EGL14.eglCreateWindowSurface(this.mEGLDisplay, configs[0], this.mSurface, this.surfaceAttribs, 0);
        this.checkEglError("eglCreateWindowSurface");

    }

    /**
     * Discards all resources held by this class, most notably the EGL context.  Also releases the
     * Surface that was passed to the constructor.
     */
    public void release() {

        if (this.mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(this.mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(this.mEGLDisplay, this.mEGLSurface);
            EGL14.eglDestroyContext(this.mEGLDisplay, this.mEGLEncodeContext);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(this.mEGLDisplay);
        }

        this.mSurface.release();
        this.mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        this.mEGLEncodeContext = EGL14.EGL_NO_CONTEXT;
        this.mEGLSurface = EGL14.EGL_NO_SURFACE;
        this.mSurface = null;

    }

    /**
     * Makes the current member EGLDisplayContext current.
     */
    public void makeDisplayContextCurrent(){
        this.makeCurrent(mEGLDisplayContext);
    }

    /**
     * Makes the current MEGLEncodeContext current.
     */
    public void makeEncodeContextCurrent(){
        this.makeCurrent(this.mEGLEncodeContext);
    }

    /**
     * Makes the EGLDisplay and EGLSurface current.
     */
    private void makeCurrent(final EGLContext context) {
        EGL14.eglMakeCurrent(this.mEGLDisplay, this.mEGLSurface, this.mEGLSurface, context);
        this.checkEglError("CodecInputSurface::makeCurrent()");
    }

    /**
     * Calls eglSwapBuffers.  This is the method that will "publish" the current frame.
     */
    public boolean swapBuffers() {
        final boolean result = EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
        this.checkEglError("CodecInputSurface::swapBuffers()");
        return result;
    }

    /**
     * Sends the presentation time stamp to EGL.  Time is expressed in nanoseconds.
     * @param nsecs is the presentation time in nanoseconds.
     */
    public void setPresentationTime(final long nsecs) {
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, nsecs);
        this.checkEglError("eglPresentationTimeANDROID");
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

}