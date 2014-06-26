package tv.present.android.controllers;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import tv.present.android.R;
import tv.present.android.mediacore.PCameraHandler;
import tv.present.android.mediacore.PCameraRenderer;
import tv.present.android.mediacore.PChunkingRecorder;
import tv.present.android.models.PView;
import tv.present.android.util.PAndroidGlobals;
import tv.present.android.util.PLog;
import tv.present.android.views.PRecordingSessionView;

/**
 * Present Recording Session Controller Object
 *
 * This controller manages the creation of content on Present.
 *
 * June 20, 2014
 * @author Kyle Weisel (kyle@present.tv)
 *
 */
public class PRecordingSessionController extends PController implements SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG ="tv.present.android.controllers.PRecordingSessionController";

    private Camera camera;
    private PCameraHandler cameraHandler;
    private PChunkingRecorder chunkingRecorder;
    private PRecordingSessionView recordingSessionView;

    /**
     * Creates this view.
     * @param savedInstanceState is a Bundle of data that represents how this view once existed.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creation);

        PLog.logDebug(TAG, "onCreate() -> The savedInstanceState Bundle is " + (savedInstanceState == null ? "null" : "not null"));

        // If we aren't resuming an instance, get a recording session view and add it.
        if (savedInstanceState == null) {
            PView recordingSessionView = this.getRecordingSessionView();

            FragmentManager fragmentManager = this.getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.container, recordingSessionView);
            fragmentTransaction.commit();
            fragmentManager.executePendingTransactions();
        }

        PLog.logDebug(TAG, "onCreate() -> The recording session view is " + (this.recordingSessionView == null ? " null" : "not null"));

    }

    @Override
    protected void onStart() {
        super.onStart();
        PLog.logDebug(TAG, "onStart() -> Beginning of method");
        this.camera = Camera.open();
        this.cameraHandler = new PCameraHandler(this);
        this.chunkingRecorder = new PChunkingRecorder(this.getApplicationContext());
        this.recordingSessionView.setEGLContextVersion(PAndroidGlobals.EGL_CONTEXT_CLIENT_VERSION);
        this.recordingSessionView.setCameraHandler(this.cameraHandler);
        PLog.logDebug(TAG, "onStart() -> End of method");
    }

    /**
     * Inflate the menu.
     * @param menu is a Menu.
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    /**
     * Handle clicks on the action bar.  Home/Up clicks are handled automatically so long as a
     * parent is specified in the manifest.
     * @param item is the MenuItem that was clicked.
     * @return true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_about) {

            Toast.makeText(this, "You clicked the menu!", Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Connects the SurfaceTexture to the Camera preview output, and starts the preview.
     */
    public void handleSetSurfaceTexture(final SurfaceTexture surfaceTexture) {
        surfaceTexture.setOnFrameAvailableListener(this);
        this.recordingSessionView.setCameraPreviewSurfaceTexture(surfaceTexture);
        this.camera.startPreview();
    }

    public void onFrameAvailable(final SurfaceTexture surfaceTexture) {
        PLog.logDebug(TAG, "Frame is available for the surface texture.");
        this.recordingSessionView.requestGLViewRender();
    }

    /**
     * Gets the recording session view that this controller controls.  This will register the view
     * to the private recordingSessionView member.
     * @return a PRecordingSessionView.
     */
    public PRecordingSessionView getRecordingSessionView() {

        if (this.recordingSessionView == null) {
            PLog.logDebug(TAG, "getRecordingSessionView() -> A recording session view did not exist.  Creating a new one.");
            this.recordingSessionView = PRecordingSessionView.newInstance(this, this.cameraHandler, new PCameraRenderer(this.getApplicationContext()));
        }

        PLog.logDebug(TAG, "getRecordingSessionView() -> A recording session view " + (this.recordingSessionView == null ? "does not exist" : "exists"));
        return this.recordingSessionView;
    }

}
