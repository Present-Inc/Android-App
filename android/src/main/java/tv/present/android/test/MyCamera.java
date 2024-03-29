package tv.present.android.test;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;

import java.io.IOException;

/**
 * Created by kbw28 on 6/26/14.
 */
public class MyCamera {
    private final static String LOG_TAG = "MyCamera";

    private Camera mCamera;
    private Parameters mCameraParams;
    private Boolean running = false;

    void start(SurfaceTexture surface)
    {
        Log.v(LOG_TAG, "Starting Camera");

        mCamera = Camera.open(0);
        mCameraParams = mCamera.getParameters();
        Log.v(LOG_TAG, mCameraParams.getPreviewSize().width + " x " + mCameraParams.getPreviewSize().height);

        try {
            mCamera.setPreviewTexture(surface);
            mCamera.startPreview();
            running = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void stop()
    {
        if (running) {
            Log.v(LOG_TAG, "Stopping Camera");
            mCamera.stopPreview();
            mCamera.release();
            running = false;
        }
    }
}
