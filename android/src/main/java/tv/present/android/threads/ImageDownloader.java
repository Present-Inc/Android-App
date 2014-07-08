package tv.present.android.threads;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import tv.present.android.util.PLog;

/**
 * Created by Ryan on 7/7/14.
 * AsyncTask to download images
 * Pass the URL for the .PNG image and it downloads the image
 * and saves the image to a location on the SD Card with the time as the file name + .png extension
 */

public class ImageDownloader extends AsyncTask<String, Void, Bitmap > {

    private final static String TAG = "tv.present.android.threads.ImageDownloader";

    @Override
    protected void onPostExecute(Bitmap result)
    {
        File sdCardDirectory = Environment.getExternalStorageDirectory();
        File image = new File(sdCardDirectory, String.valueOf(System.currentTimeMillis()) + ".png");
        boolean success = false;

        // Encode the file as a PNG image.
        FileOutputStream outStream;
        try
        {
            outStream = new FileOutputStream(image);
            result.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
            success = true;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (success)
            PLog.logDebug(TAG, "Image successfully saved");
        else
            PLog.logDebug(TAG, "Error while saving image");
    }

    @Override
    protected void onProgressUpdate(Void... params)
    {}

    @Override
    protected Bitmap doInBackground(String... params) {
        final DefaultHttpClient client = new DefaultHttpClient();
        final HttpGet getRequest = new HttpGet(params[0]);

        try
        {
            HttpResponse response = client.execute(getRequest);

            //Check for error
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK)
            {
                PLog.logDebug(TAG, "Error " + statusCode +
                        " while retrieving bitmap for " + params[0]);
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null)
            {
                InputStream inputStream = null;
                try
                {
                    inputStream = entity.getContent();

                    return BitmapFactory.decodeStream(inputStream);
                }
                finally
                {
                    if (inputStream != null)
                        inputStream.close();
                    entity.consumeContent();
                }
            }
        }
        catch (Exception e)
        {
            getRequest.abort();
            PLog.logDebug(TAG, "Error while retrieving bitmap from " + params[0] + e.toString());
        }

        return null;
    }
}
