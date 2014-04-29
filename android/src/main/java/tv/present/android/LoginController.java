package tv.present.android;

import android.os.AsyncTask;

import tv.present.api.PUserContext;

/**
 * Created by kbw28 on 4/23/14.
 */
public class LoginController extends AsyncTask<String, Void, PUserContext> {

    public AsyncTaskResponse delegate = null;

    @Override
    public PUserContext doInBackground(String ... params) {

        String username = params[0];
        String password = params[1];

        PUserContext userContext = PUserContext.create(username, password);

        return userContext;

    }

    @Override
    public void onPostExecute(PUserContext userContext) {
        this.delegate.processAsyncResponse(userContext);
    }



}
