package tv.present.android.controllers;

import android.app.Fragment;

import java.util.ArrayList;

import tv.present.android.interfaces.Controller;
import tv.present.android.interfaces.FetchNotificationsWorkerCallback;
import tv.present.android.views.NotificationsFragment;
import tv.present.android.workers.FetchNotificationsWorker;
import tv.present.models.ResultSetActivities;
import tv.present.models.UserActivity;

public class NotificationsController extends Controller implements FetchNotificationsWorkerCallback {

    private static final String TAG = "tv.present.android.controllers.LoginController";
    private final NotificationsFragment fragment;

    public NotificationsController(NotificationsFragment fragment) {
        this.fragment = fragment;
    }

    public Fragment getFragment() {
        return this.fragment;
    }

    public void callbackFetchNotifications(ResultSetActivities resultSet) {
        // Loop through the notifications and update the view here

        if (resultSet.getResults() instanceof ArrayList<?>) {

        }
        ArrayList<UserActivity> results = (ArrayList<UserActivity>)resultSet.getResults();

        final int size = results.size();
        for (int i = 0; i < size; i++) {
            UserActivity userActivity = results.get(i);
            String message = userActivity.getSubject();
            String userProfileImageURL = userActivity.getSourceUser().getProfile().getProfilePictureURL();
            this.fragment.addNotification(userProfileImageURL, message);
        }
    }

    public void executeFetchNotifications(final Integer cursor, final Integer limit) {

        Integer[] params = new Integer[2];
        params[0] = cursor;
        params[1] = limit;

        FetchNotificationsWorker fetchNotificationsWorker = new FetchNotificationsWorker(this);
        fetchNotificationsWorker.execute(params);

    }

}
