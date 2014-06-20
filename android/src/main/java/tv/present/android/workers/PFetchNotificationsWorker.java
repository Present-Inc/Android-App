package tv.present.android.workers;

import android.os.AsyncTask;

import tv.present.android.interfaces.FetchNotificationsWorkerCallback;
import tv.present.android.models.PApplicationCore;
import tv.present.android.util.PLog;
import tv.present.api.PAPIInteraction;
import tv.present.models.PUserActivity;
import tv.present.models.PUserContext;
import tv.present.util.PResultSet;

/**
 * Created by kbw28 on 5/29/14.
 */
public class PFetchNotificationsWorker extends AsyncTask<Integer, Void, PResultSet<PUserActivity>> {

    private static final String TAG = "tv.present.android.workers.FetchNotificationsWorker";
    private FetchNotificationsWorkerCallback fetchNotificationsWorkerCallback;

    public PFetchNotificationsWorker(FetchNotificationsWorkerCallback callback) {
        this.fetchNotificationsWorkerCallback = callback;
    }

    // Inovked on UI before thread is executed
    @Override
    public void onPreExecute() {
        PLog.logDebug(TAG, "Spinning up the notifications worker");
    }

    @Override
    public PResultSet<PUserActivity> doInBackground(Integer ... params) {

        final int cursor = params[0];
        final int limit = params[1];

        PApplicationCore appCore = PApplicationCore.getInstance();
        PUserContext userContext = appCore.getUserContext();

        PAPIInteraction apiInteraction = new PAPIInteraction();
        PResultSet<PUserActivity> resultSet = apiInteraction.getUserActivities(userContext, 10, 0);

        if (resultSet == null ) {
            PLog.logError(TAG, "LEXMFJ - ResultSet is null!");
        }

        return resultSet;
    }

    // Will execute on UI thread:
    //  Check whether the boolean was true or false
    //  Advance the UI to the main screen on a successful login
    @Override
    public void onPostExecute(PResultSet<PUserActivity> resultSet) {
        if (resultSet == null) {
            PLog.logDebug(TAG, "The result set is null!");
        }
        else {
            PLog.logDebug(TAG, "The resultSet onPostExecute has size: " + resultSet.getResults().size());
        }
        this.fetchNotificationsWorkerCallback.callbackFetchNotifications(resultSet);
    }

}
