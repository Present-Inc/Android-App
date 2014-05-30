package tv.present.android.interfaces;

import tv.present.models.PResultSet;
import tv.present.models.UserActivity;

/**
 *  This interface defines the callback method used by the FetchNotificationsWorker.
 *  @author Kyle Weisel (kyle@present.tv)
 */
public interface FetchNotificationsWorkerCallback {
    public void callbackFetchNotifications(PResultSet<UserActivity> resultSet);
}
