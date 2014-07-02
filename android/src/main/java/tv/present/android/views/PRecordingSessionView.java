package tv.present.android.views;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;

import tv.present.android.R;
import tv.present.android.controllers.PController;
import tv.present.android.mediacore.PCameraHandler;
import tv.present.android.mediacore.PCameraRenderer;
import tv.present.android.models.PView;
import tv.present.android.util.PKeys;
import tv.present.android.util.PLog;

public class PRecordingSessionView extends PView {

    private static final String TAG = "tv.present.android.views.PRecordingSessionView";

    private Camera camera;
    private PGLSurfaceView glSurfaceView;
    private PCameraRenderer cameraRenderer;
    private PCameraHandler cameraHandler;

    /**
     * Returns a new instance of this view.
     */
    public static PRecordingSessionView newInstance(PController controller, PCameraHandler cameraHandler, PCameraRenderer cameraRenderer) {

        PLog.logDebug(TAG, "newInstance() -> A new instance of this view is being requested");
        Bundle arguments = new Bundle();
        arguments.putSerializable(PKeys.KEY_CONTROLLER, controller);
        arguments.putSerializable(PKeys.KEY_CAMERA_HANDLER, cameraHandler);
        arguments.putSerializable(PKeys.KEY_CAMERA_RENDERER, cameraRenderer);
        PRecordingSessionView recordingSessionView = new PRecordingSessionView();
        recordingSessionView.setController(controller);
        recordingSessionView.setCameraHandler(cameraHandler);
        //recordingSessionView.setCameraRenderer(cameraRenderer);
        recordingSessionView.setArguments(arguments);

        return recordingSessionView;

    }

    /**
     * Constructs a PRecordingSessionView.
     */
    public PRecordingSessionView() {
        PLog.logDebug(TAG, "PRecordingSessionView() -> Constructor called");
        this.camera = Camera.open();
        /* Empty constructor */
    }

    /**
     * Inflates and prepares this fragment view.
     * @param inflater is the LayoutInflater to use to inflate this layout.
     * @param container is a ViewGroup container that will display this inflated layout.
     * @param savedInstanceState is a Bundle of data that represents a previous instance state to
     *                           restore this view to.
     * @return a View that is the LoginFragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        PLog.logDebug(TAG, "onCreateView() -> Creating and configuring fragment view.");
        View rootView = inflater.inflate(R.layout.fragment_create_present, container, false);

        //PLog.logDebug(TAG, "onCreateView() -> Inflating to find the GLSurfaceView " + (aflView.findViewById(R.id.recordingPreviewSV) == null ? "didnt work" : "worked!"));


        //this.glSurfaceView = (GLSurfaceView) aflView.findViewById(R.id.recordingPreviewSV);
        PLog.logDebug(TAG, "onCreateView() -> this.glSurfaceView = " + (this.glSurfaceView == null ? "null" : "not null"));
        PLog.logDebug(TAG, "onCreateView() -> On inflation, looking for GLSurfaceView and it " + (rootView.findViewById(R.id.recordingPreviewSV) == null ? "is null" : "is not null"));
        this.glSurfaceView = (PGLSurfaceView) rootView.findViewById(R.id.recordingPreviewSV);
        PLog.logDebug(TAG, "onCreateView() -> this.glSurfaceView = " + (this.glSurfaceView == null ? "null" : "not null"));

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle saved) {
        super.onActivityCreated(saved);
        this.glSurfaceView = (PGLSurfaceView) this.getView().findViewById(R.id.recordingPreviewSV);
        Log.i(TAG, "onActivityCreated");
    }

    /**
     * Handles touch events.  If the calling View is not a textbox, we hide the keyboard.
     * @param view is the View that was touched.
     * @param event is the MotionEvent that occured.
     * @return false
     */
    public boolean onTouch(View view, MotionEvent event) {

        if (view instanceof EditText) {
            final InputMethodManager inputMethodManager = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            return false;
        }
        else {
            this.hideKeyboard(this.controller);
            return false;
        }

    }

    /**
     * Handles clicks on buttons.
     * @param view is the View (Button) that was clicked.
     */
    public void onClick(View view) {

        View rootView = this.getView();

        if (view instanceof Button) {
            /* Record/stop button was clicked */
        }

    }

    /**
     * Handles long clicks.
     * @param view is a View that was long clicked.
     * @return a boolean value for which I am unsure of the meaning.
     */
    public boolean onLongClick(View view) {
        return false;
    }

    public void setCameraPreviewSurfaceTexture(SurfaceTexture surfaceTexture) {
        try {
            this.camera.setPreviewTexture(surfaceTexture);
            //this.camera.setPre
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPST() {
        //this.camera.setPreviewTexture();
    }

    public void requestGLViewRender() {
        PLog.logDebug(TAG, "Requesting render on the GLView.");
        this.glSurfaceView.requestRender();
    }

    public GLSurfaceView getGlSurfaceView() {
        return this.glSurfaceView;
    }

    /**
     * Sets the EGL context version.  We don't (and shouldn't) do this from here, as the new
     * PGLSurfaceView now takes care of it.
     * @param version is th integer version of the EGL context version to set.
     */
    public void setEGLContextVersion(final int version) {
        PLog.logWarning(TAG, "The GLSurfaceView is " + (this.glSurfaceView == null ? "null" : "not null"));
        PLog.logWarning(TAG, "By the way, the camera renderer is  " + (this.cameraRenderer == null ? "also null" : "not null"));
        //this.glSurfaceView.setEGLContextClientVersion(version);
    }

    /*public void setEGLRenderer(GLSurfaceView.Renderer renderer) {
        this.glSurfaceView.setRenderer(renderer);
    }*/

    public void setEGLRenderMode(final int renderMode) {
        this.glSurfaceView.setRenderMode(renderMode);
    }

    public void setCameraHandler(final PCameraHandler cameraHandler) {
        this.cameraHandler = cameraHandler;
    }

    /* private void configureSurfaceView() {
        this.setEGLContextVersion(PAndroidGlobals.EGL_CONTEXT_CLIENT_VERSION);
        this.glSurfaceView.setRenderer(this.cameraRenderer);
    }

    public void setCameraRenderer(PCameraRenderer renderer) {
        this.cameraRenderer = renderer;
    } */

}
