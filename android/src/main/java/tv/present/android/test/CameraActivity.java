package tv.present.android.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import tv.present.android.R;

/**
 * Created by kbw28 on 6/26/14.
 */
public class CameraActivity extends Activity {
    private MyGLSurfaceView glSurfaceView;
    private MyCamera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.camera_layout, null);

        MyGLSurfaceView sv = (MyGLSurfaceView) v;

        sv.setCamera(new MyCamera());

        setContentView(sv);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mCamera.stop();
    }
}
