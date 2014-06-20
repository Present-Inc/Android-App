package tv.present.android.workers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import tv.present.android.R;
import tv.present.android.util.PLog;

/**
 * Created by kbw28 on 5/29/14.
 */
public class PDownloadImageWorker extends AsyncTask<String, Void, Bitmap> {

    private static final String TAG = "tv.present.android.workers.DownloadImageWorker";
    private ImageView target;

    public PDownloadImageWorker(ImageView targetView) {
        this.target = targetView;

    }

    // Inovked on UI before thread is executed
    @Override
    public void onPreExecute() {
        PLog.logDebug(TAG, "Spinning up the notifications worker");
    }

    @Override
    public Bitmap doInBackground(String ... params) {

        String profileImageURL = params[0];
        try {
            return BitmapFactory.decodeStream((InputStream) new URL(profileImageURL).getContent());
        } catch (MalformedURLException e) {
            PLog.logWarning(TAG, "Caught a MalformedURLException while attempting to download an image.");
            return null;
        } catch(IOException e) {
            PLog.logWarning(TAG, "Caught an IOException while attempting to download an image.");
            return null;
        }

    }

    // Will execute on UI thread:
    //  Check whether the boolean was true or false
    //  Advance the UI to the main screen on a successful login
    @Override
    public void onPostExecute(Bitmap bitmap) {
        Bitmap bm = BitmapFactory.decodeResource(this.target.getContext().getResources(), R.drawable.unknown_person);
        this.target.setImageBitmap(bm);
    }

}
