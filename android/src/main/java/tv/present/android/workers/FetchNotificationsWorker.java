package tv.present.android.workers;

import android.os.AsyncTask;

import tv.present.android.interfaces.FetchNotificationsWorkerCallback;
import tv.present.android.models.ApplicationCore;
import tv.present.android.util.PLog;
import tv.present.models.PResultSet;
import tv.present.models.UserActivity;
import tv.present.models.UserContext;

/**
 * Created by kbw28 on 5/29/14.
 */
public class FetchNotificationsWorker extends AsyncTask<Integer, Void, PResultSet<UserActivity>> {

    private static final String TAG = "tv.present.android.workers.FetchNotificationsWorker";
    private FetchNotificationsWorkerCallback fetchNotificationsWorkerCallback;

    public FetchNotificationsWorker(FetchNotificationsWorkerCallback callback) {
        this.fetchNotificationsWorkerCallback = callback;
    }

    // Inovked on UI before thread is executed
    @Override
    public void onPreExecute() {
        PLog.logDebug(TAG, "Spinning up the notifications worker");
    }

    @Override
    public PResultSet<UserActivity> doInBackground(Integer ... params) {

        final int cursor = params[0];
        final int limit = params[1];

        ApplicationCore appCore = ApplicationCore.getInstance();
        UserContext userContext = appCore.getUserContext();

        PResultSet<UserActivity> results = null;
        if (userContext != null) {
            results = UserActivity.getActivities(userContext, limit, cursor);
        }

        return results;
    }

    // Will execute on UI thread:
    //  Check whether the boolean was true or false
    //  Advance the UI to the main screen on a successful login
    @Override
    public void onPostExecute(PResultSet<UserActivity> resultSet) {
        PLog.logDebug(TAG, "The resultSet onPostExecute has size: " + resultSet.getResults().size());
        this.fetchNotificationsWorkerCallback.callbackFetchNotifications(resultSet);
    }

}
