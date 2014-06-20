package tv.present.android.loaders;

import android.content.Context;
import android.os.Bundle;

import tv.present.android.models.PApplicationCore;
import tv.present.android.util.PKeys;
import tv.present.android.util.PLog;
import tv.present.api.PAPIInteraction;
import tv.present.models.PUserActivity;
import tv.present.models.PUserContext;
import tv.present.util.PResultSet;

public final class PNotificationsLoader<T> extends PLoader<T> {

    private static final String TAG = "tv.present.android.loaders.NotificationsLoader";
    private Bundle arguments;

    public PNotificationsLoader(Context context, Bundle args) {
        super(context);
        this.arguments = args;
    }

    @Override
    public T loadInBackground() {

        final int cursor = arguments.getInt(PKeys.KEY_CURSOR);
        final int limit = arguments.getInt(PKeys.KEY_LIMIT);

        PApplicationCore appCore = PApplicationCore.getInstance();
        PUserContext userContext = appCore.getUserContext();

        PAPIInteraction apiInteraction = new PAPIInteraction();
        PResultSet<PUserActivity> resultSet = apiInteraction.getUserActivities(userContext, cursor, limit);

        if (resultSet == null ) {
            PLog.logWarning(TAG, "The result set from the API interaction came back null!");
        }

        return (T) resultSet;
    }

}
