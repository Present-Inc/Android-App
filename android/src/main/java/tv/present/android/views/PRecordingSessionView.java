package tv.present.android.views;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
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
import tv.present.android.controllers.PEntryController;
import tv.present.android.mediacore.PCameraHandler;
import tv.present.android.mediacore.PCameraRenderer;
import tv.present.android.models.PView;
import tv.present.android.util.PAndroidGlobals;
import tv.present.android.util.PLog;

/**
 * Created by kbw28 on 6/20/14.
 */
public class PRecordingSessionView extends PView {

    private static final String TAG = "tv.present.android.views.PCreationalView";

    private Camera camera;
    private GLSurfaceView glSurfaceView;
    private PCameraRenderer cameraRenderer;
    private PCameraHandler cameraHandler;
    private boolean isRecording;

    /**
     * Returns a new instance of this view.
     */
    public static PRecordingSessionView newInstance(PController controller, PCameraHandler cameraHandler, PCameraRenderer cameraRenderer) {

        Bundle arguments = new Bundle();
        arguments.putSerializable("controller", controller);
        PLog.logDebug(TAG, "newInstance -> the controller put into fragment arguments was " + (controller == null ? "in fact" : "not") + " null");
        PLog.logDebug(TAG, "newInstance -> the controller got out of the fragment arguments was " + (arguments.get("controller") == null ? "in fact" : "not") + " null");
        PRecordingSessionView creationalView = new PRecordingSessionView();
        creationalView.setController(controller);
        creationalView.setCameraHandler(cameraHandler);
        creationalView.setCameraRenderer(cameraRenderer);
        creationalView.setArguments(arguments);

        return creationalView;

    }

    /**
     * Constructs a PRecordingSessionView.
     */
    public PRecordingSessionView() {

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

        GLSurfaceView test = (GLSurfaceView) rootView.findViewById(R.id.recordingPreviewSV);

        PLog.logDebug(TAG, "Performing surfaceview test.  The surface view was " + (test == null ? "null" : "not null"));

        this.glSurfaceView = (GLSurfaceView) rootView.findViewById(R.id.recordingPreviewSV);
        return rootView;
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

            // Get all of the data from the view
            String emailAddress = ((EditText) rootView.findViewById(R.id.createEmailAddressField)).getText().toString();
            String username = ((EditText) rootView.findViewById(R.id.createUsernameField)).getText().toString();
            String password = ((EditText) rootView.findViewById(R.id.createPasswordField)).getText().toString();
            String fullName = ((EditText) rootView.findViewById(R.id.createFullNameField)).getText().toString();
            String phoneNumber = ((EditText) rootView.findViewById(R.id.createPhoneNumberField)).getText().toString();

            ((PEntryController) this.controller).executeCreateAccount(username, password, emailAddress, fullName, phoneNumber);
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
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void requestGLViewRender() {
        PLog.logDebug(TAG, "Requesting render on the GLView.");
        this.glSurfaceView.requestRender();
    }

    public GLSurfaceView getGlSurfaceView() {
        return this.glSurfaceView;
    }

    public void setEGLContextVersion(final int version) {
        PLog.logWarning(TAG, "The GLSurfaceView is " + (this.glSurfaceView == null ? "null" : "not null"));
        this.glSurfaceView.setEGLContextClientVersion(version);
    }

    public void setEGLRenderer(GLSurfaceView.Renderer renderer) {
        this.glSurfaceView.setRenderer(renderer);
    }

    public void setEGLRenderMode(final int renderMode) {
        this.glSurfaceView.setRenderMode(renderMode);
    }

    public void setCameraHandler(final PCameraHandler cameraHandler) {
        this.cameraHandler = cameraHandler;
    }

    private void configureSurfaceView() {
        this.setEGLContextVersion(PAndroidGlobals.EGL_CONTEXT_CLIENT_VERSION);
        this.glSurfaceView.setRenderer(this.cameraRenderer);
    }

    public void setCameraRenderer(PCameraRenderer renderer) {
        this.cameraRenderer = renderer;
    }

    public void associateSV() {
        this.glSurfaceView = (GLSurfaceView) this.getView().findViewById(R.id.recordingPreviewSV);
    }



}
