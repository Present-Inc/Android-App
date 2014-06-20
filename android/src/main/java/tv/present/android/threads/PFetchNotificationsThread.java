package tv.present.android.threads;

import android.os.AsyncTask;

import tv.present.android.interfaces.ThreadCallback;
import tv.present.android.models.PApplicationCore;
import tv.present.android.models.PCallbackResult;
import tv.present.android.util.PLog;
import tv.present.api.PAPIInteraction;
import tv.present.models.PUserActivity;
import tv.present.models.PUserContext;
import tv.present.util.PResultSet;

/**
 * Present Fetch Notifications Thread
 *
 * This thread is responsible for fetching notifications for the current user context so long as one
 * exists.  It will call back to a specified ThreadCallback with a PResultSet<PUserActivity>
 * response.
 *
 * June 10, 2014
 * @author Kyle Weisel (kyle@present.tv)
 */
public class PFetchNotificationsThread extends AsyncTask<Integer, Void, PResultSet<PUserActivity>> {

    private static final String TAG = "tv.present.android.threads.FetchNotificationsThread";
    private final int identifier;
    private final ThreadCallback threadCallback;

    public PFetchNotificationsThread(final int identifier, final ThreadCallback callback) {
        this.identifier = identifier;
        this.threadCallback = callback;
    }

    // Inovked on UI before thread is executed
    @Override
    public void onPreExecute() {
        PLog.logDebug(TAG, "Spinning up the notifications thread");
    }

    @Override
    public PResultSet<PUserActivity> doInBackground(final Integer ... params) {

        final int cursor = params[0];
        final int limit = params[1];

        PApplicationCore appCore = PApplicationCore.getInstance();
        PUserContext userContext = appCore.getUserContext();

        PAPIInteraction apiInteraction = new PAPIInteraction();
        PResultSet<PUserActivity> resultSet = apiInteraction.getUserActivities(userContext, cursor, limit);

        if (resultSet == null ) {
            PLog.logWarning(TAG, "The result set from the API interaction came back null!");
        }

        return resultSet;
    }

    // Will execute on UI thread:
    //  Check whether the boolean was true or false
    //  Advance the UI to the main screen on a successful login
    @Override
    public void onPostExecute(final PResultSet<PUserActivity> resultSet) {
        if (resultSet == null) {
            PLog.logWarning(TAG, "The result set came back null!");
        }
        else {
            PLog.logDebug(TAG, "The resultSet onPostExecute has size: " + resultSet.getResults().size());
        }
        this.threadCallback.threadCallback(new PCallbackResult<PResultSet<PUserActivity>>(this.identifier, resultSet));
    }

}
