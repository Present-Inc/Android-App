package tv.present.android.interfaces;

import tv.present.models.PVideo;
import tv.present.util.PResultSet;

/**
 * Created by kbw28 on 6/5/14.
 */
public interface FetchHomeFeedWorkerCallback {
    public void callbackFetchHomeFeed(PResultSet<PVideo> resultSet);
}
