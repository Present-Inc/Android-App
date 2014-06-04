package tv.present.android.controllers;

import android.app.Fragment;

import java.util.ArrayList;

import tv.present.android.interfaces.Controller;
import tv.present.android.interfaces.FetchNotificationsWorkerCallback;
import tv.present.android.util.PLog;
import tv.present.android.views.NotificationsFragment;
import tv.present.android.workers.FetchNotificationsWorker;
import tv.present.models.PResultSet;
import tv.present.models.UserActivity;

public class NotificationsController extends Controller implements FetchNotificationsWorkerCallback {

    private static final String TAG = "tv.present.android.controllers.NotificationsController";
    private final NotificationsFragment fragment;

    public NotificationsController(NotificationsFragment fragment) {
        this.fragment = fragment;
    }

    public Fragment getFragment() {
        return this.fragment;
    }

    public void callbackFetchNotifications(PResultSet<UserActivity> resultSet) {
        // Loop through the notifications and update the view here

        ArrayList<UserActivity> results = resultSet.getResults();

        PLog.logNotice(TAG, "Results array has a size of " + results.size());


        final int size = results.size();
        for (int i = 0; i < size; i++) {
            PLog.logNotice(TAG, "Looping through the results " + i);
            UserActivity userActivity = results.get(i);
            if (userActivity == null) {
                PLog.logError(TAG, "The user activity " + i + " was null!");

                UserActivity result2 = results.get(i+1);
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
        FetchNotificationsWorker fetchNotificationsWorker = new FetchNotificationsWorker(this);
        fetchNotificationsWorker.execute(params);

    }

}
