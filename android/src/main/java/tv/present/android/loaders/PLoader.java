package tv.present.android.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;

/**
 * Present Abstract Data Loader
 *
 * This loader is an abstract asynchronous loader that can be used to provide data to a view
 * adapter.
 *
 * June 13, 2014
 * @author Kyle Weisel (kyle@present.tv)
 */
public abstract class PLoader<T> extends AsyncTaskLoader<T> {

    public PLoader(Context context) {
        super(context);
    }

}
