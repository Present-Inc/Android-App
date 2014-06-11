package tv.present.android.interfaces;

import tv.present.util.PResultSet;
import tv.present.models.PUserActivity;

/**
 *  This interface defines the callback method used by the FetchNotificationsWorker.
 *  @author Kyle Weisel (kyle@present.tv)
 */
public interface FetchNotificationsWorkerCallback {
    public void callbackFetchNotifications(PResultSet<PUserActivity> resultSet);
}
