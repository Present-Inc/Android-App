package tv.present.android;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends Activity {

    private static final String TAG = "tv.present.Present.CameraActivity";
    private static final int VIDEO_CHUNK_LENGTH_MS = 10000;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private CameraPreview mPreview;
    private Camera mCamera;
    private CameraAutoFocusCallback mAutoFocusCallback;
    private boolean mAlreadyFocused;
    private AccelerometerListener mAccelerometerListener;
    private MediaRecorder mMediaRecorder, mMediaRecorder2;
    private SensorManager sensorManager;
    private String videoFileName;
    private boolean isRecording = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_camera);
        mAccelerometerListener = new AccelerometerListener();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.isRecording = false;
        this.sensorManager.unregisterListener(mAccelerometerListener);
        this.releaseMediaRecorder();
        this.releaseCamera();
        this.removePreviewSurface();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.preparePreviewSurface();
    }

    private void preparePreviewSurface() {
        if (this.checkCameraHardware(CameraActivity.this)) {
            this.mCamera = getCameraInstance();
            if (this.mCamera != null) {
                this.mPreview = new CameraPreview(CameraActivity.this, this.mCamera);
                ViewGroup vg = (ViewGroup) findViewById(R.id.camera_preview_lay);
                vg.addView(this.mPreview);
            } else {
                // Do what now?
            }
        }
    }

    private void removePreviewSurface() {
        ViewGroup vg = (ViewGroup) findViewById(R.id.camera_preview_lay);
        vg.removeView(this.mPreview);
    }

    /**
     * Checks if this device has a camera.
     * @param context
     * @return
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * Safe way to get an instance of the camera.
     * @return an instance of the Camera if one is able to be used, null otherwise.
     */
    public static Camera getCameraInstance() {
        Camera camera = null;
        camera = Camera.open();
        Camera.Parameters cameraParameters = camera.getParameters();
        cameraParameters.setPreviewFpsRange(25, 30);     // Find the right duple
        camera.setParameters(cameraParameters);
        try {
            camera = Camera.open();
        } catch (Exception e) { // Figure out what exception this really is.  Exception is too general...
            // Camera is not available (in use or does not exist)
        }
        return camera;
    }

    /** A basic Camera preview class */
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            this.mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            this.mHolder = getHolder();
            this.mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            this.mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

            this.setKeepScreenOn(true);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            this.mCamera.setDisplayOrientation(90);

            try {
                this.mCamera.setPreviewDisplay(holder);
                this.mCamera.startPreview();
                mAutoFocusCallback = new CameraAutoFocusCallback();
                setCameraFocus(mAutoFocusCallback);
                sensorManager.registerListener(mAccelerometerListener, sensorManager.getDefaultSensor(SensorManager.SENSOR_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
            } catch (IOException e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your
            // activity.
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events
            // here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (this.mHolder.getSurface() == null) {
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                this.mCamera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }

            // start preview with new settings
            try {
                this.mCamera.setPreviewDisplay(this.mHolder);
                this.mCamera.startPreview();

            } catch (Exception e) {
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }
    }

    private boolean prepareVideoRecorder() {

        this.mMediaRecorder = new MediaRecorder();
        this.mCamera.stopPreview();
        this.mCamera.unlock();

        this.mMediaRecorder.setCamera(mCamera);

        this.mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        this.mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        this.mMediaRecorder.setOrientationHint(90);

        this.mMediaRecorder.setMaxDuration(this.VIDEO_CHUNK_LENGTH_MS);

        if (getAndroidSDKVersion() >= 8) {
            this.mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        } else {
            this.mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            this.mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            this.mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        }
        this.videoFileName = getOutputMediaFile(MEDIA_TYPE_VIDEO).toString();
        this.mMediaRecorder.setOutputFile(videoFileName);
        this.mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        try {
            this.mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            this.releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            this.releaseMediaRecorder();
            return false;
        }
        return true;
    }

    /** Create a File for saving the video */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Present");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }

    private static File getOutputMediaFile2(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Present");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID2_" + timeStamp + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }

    private void releaseMediaRecorder() {
        if (this.mMediaRecorder != null) {
            this.mMediaRecorder.reset(); // clear recorder configuration
            this.mMediaRecorder.release(); // release the recorder object
            this.mMediaRecorder = null;
            this.mCamera.lock(); // lock camera for later use
        }
    }

    private void releaseCamera() {
        if (this.mCamera != null) {
            this.mCamera.release();
            this.mCamera = null;
        }
    }

    /**
     * This method is fired when someone presses the capture button.
     * @param v is the view of the Button pressed.
     */
    public void capture(View v) {

        // If we're already recording, stop it.
        if (this.isRecording) {
            this.mMediaRecorder.stop();
            this.releaseMediaRecorder();
            this.mCamera.lock();
            setTitle("Not Recording");
            isRecording = false;
        }
        // If we aren't recording, start doing it.
        else {
            this.sensorManager.unregisterListener(mAccelerometerListener);
            if (this.prepareVideoRecorder()) {
                this.mMediaRecorder.start();
                this.setTitle("Recording");
                this.isRecording = true;
            } else {
                this.releaseMediaRecorder();
            }
        }
    }

    private int getAndroidSDKVersion() {
        int version = 0;
        try {
            version = Integer.valueOf(android.os.Build.VERSION.SDK);
        } catch (NumberFormatException e) {
        }
        return version;
    }

    /**
     * Sets the camera focus mode.
     * @param autoFocus an AutoFocusCallback function to pass to the Camera.
     * @return true if the focus method is valid and was set to autoFocus, false otherwise.
     */
    private boolean setCameraFocus(AutoFocusCallback autoFocus) {
        if (this.mCamera.getParameters().getFocusMode().equals(Parameters.FOCUS_MODE_AUTO) || mCamera.getParameters().getFocusMode().equals(Parameters.FOCUS_MODE_MACRO)) {
            this.mCamera.autoFocus(autoFocus);
            return true;
        }
        return false;
    }


    private class CameraAutoFocusCallback implements AutoFocusCallback {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            mAlreadyFocused = true;
        }
    }

    private class AccelerometerListener implements SensorEventListener {
        private float mLastX = 0;
        private float mLastY = 0;
        private float mLastZ = 0;

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float deltaX = Math.abs(mLastX - x);
            float deltaY = Math.abs(mLastY - y);
            float deltaZ = Math.abs(mLastZ - z);

            if (deltaX > 1 && mAlreadyFocused) {
                mAlreadyFocused = false;
                setCameraFocus(mAutoFocusCallback);
            }
            if (deltaY > 1 && mAlreadyFocused) {
                mAlreadyFocused = false;
                setCameraFocus(mAutoFocusCallback);
            }
            if (deltaZ > 1 && mAlreadyFocused) {
                mAlreadyFocused = false;
                setCameraFocus(mAutoFocusCallback);
            }

            mLastX = x;
            mLastY = y;
            mLastZ = z;
        }

    }
}

