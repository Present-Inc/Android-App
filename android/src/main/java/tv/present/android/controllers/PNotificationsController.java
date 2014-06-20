package tv.present.android.controllers;

import android.app.Fragment;

import java.util.ArrayList;

import tv.present.android.interfaces.FetchNotificationsWorkerCallback;
import tv.present.android.util.PLog;
import tv.present.android.views.PNotificationsView;
import tv.present.android.workers.PFetchNotificationsWorker;
import tv.present.models.PUserActivity;
import tv.present.util.PResultSet;

public class PNotificationsController extends PController implements FetchNotificationsWorkerCallback {

    private static final String TAG = "tv.present.android.controllers.NotificationsController";
    private final PNotificationsView fragment;

    public PNotificationsController(PNotificationsView fragment) {
        this.fragment = fragment;
    }

    public Fragment getFragment() {
        return this.fragment;
    }

    public void callbackFetchNotifications(PResultSet<PUserActivity> resultSet) {
        // Loop through the notifications and update the view here

        ArrayList<PUserActivity> results = resultSet.getResults();

        PLog.logNotice(TAG, "Results array has a size of " + results.size());


        final int size = results.size();
        for (int i = 0; i < size; i++) {
            PLog.logNotice(TAG, "Looping through the results " + i);
            PUserActivity userActivity = results.get(i);
            if (userActivity == null) {
                PLog.logError(TAG, "The user activity " + i + " was null!");

                PUserActivity result2 = results.get(i+1);
                if (result2 == null) {
                    PLog.logError(TAG, "The user activity " + i+1 + " was also null!");
                }
                else {
                    PLog.logNotice(TAG, "The user activity " + (i+1) + " was not null, however.  In fact " + result2.getSourceUser().getUsername());
                }
            }
            else {
                PLog.logNotice(TAG, "The user activity " + i + " was not null!");
            }
            String message = userActivity.getSubject();
            String userProfileImageURL = userActivity.getSourceUser().getProfile().getProfilePictureURL();
            this.fragment.addNotification(userProfileImageURL, message);
        }
    }

    public void executeFetchNotifications(final Integer cursor, final Integer limit) {

        Integer[] params = new Integer[2];
        params[0] = cursor;
        params[1] = limit;

        PLog.logDebug(TAG, "Starting the notifications worker");
        PFetchNotificationsWorker fetchNotificationsWorker = new PFetchNotificationsWorker(this);
        fetchNotificationsWorker.execute(params);

    }

}
