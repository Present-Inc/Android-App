package tv.present.android.workers;

import android.os.AsyncTask;

import tv.present.android.interfaces.FetchHomeFeedWorkerCallback;
import tv.present.android.models.PApplicationCore;
import tv.present.android.util.PLog;
import tv.present.api.PAPIInteraction;
import tv.present.models.PUserContext;
import tv.present.models.PVideo;
import tv.present.util.PResultSet;

/**
 * Created by kbw28 on 6/5/14.
 */
public class FetchHomeFeedWorker extends AsyncTask<Integer, Void, PResultSet<PVideo>> {

    private static final String TAG = "tv.present.android.workers.HomeFeedWorker";
    private FetchHomeFeedWorkerCallback fetchHomeFeedWorkerCallback;

    public FetchHomeFeedWorker(FetchHomeFeedWorkerCallback callback) {
        this.fetchHomeFeedWorkerCallback = callback;
    }

    // Inovked on UI before thread is executed
    @Override
    public void onPreExecute() {
        PLog.logDebug(TAG, "Spinning up the notifications worker");
    }

    @Override
    public PResultSet<PVideo> doInBackground(Integer ... params) {

        final int cursor = params[0];
        final int limit = params[1];

        PApplicationCore appCore = PApplicationCore.getInstance();
        PUserContext userContext = appCore.getUserContext();

        PAPIInteraction apiInteraction = new PAPIInteraction();
        PResultSet<PVideo> resultSet = apiInteraction.getHomeVideos(userContext, 10, 0);

        if (resultSet == null ) {
            PLog.logError(TAG, "LEXMFJ - ResultSet is null!");
        }

        return resultSet;
    }

    // Will execute on UI thread:
    //  Check whether the boolean was true or false
    //  Advance the UI to the main screen on a successful login
    @Override
    public void onPostExecute(PResultSet<PVideo> resultSet) {
        if (resultSet == null) {
            PLog.logDebug(TAG, "The result set is null!");
        }
        else {
            PLog.logDebug(TAG, "The resultSet onPostExecute has size: " + resultSet.getResults().size());
        }
        this.fetchHomeFeedWorkerCallback.callbackFetchHomeFeed(resultSet);
    }

}

