package tv.present.android.interfaces;

import android.graphics.Bitmap;

/**
 *  This interface defines the callback method used by the DownloadImageWorker.
 *  @author Kyle Weisel (kyle@present.tv)
 */
public interface DownloadImageWorkerCallback {
    public void callbackImageDownload(Bitmap bitmap);
}
