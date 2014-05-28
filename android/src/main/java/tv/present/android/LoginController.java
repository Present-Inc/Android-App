package tv.present.android;

import android.os.AsyncTask;

import tv.present.models.UserContext;

/**
 * Created by kbw28 on 4/23/14.
 */
public class LoginController extends AsyncTask<String, Void, UserContext> {

    public AsyncTaskResponse delegate = null;

    @Override
    public UserContext doInBackground(String ... params) {

        String username = params[0];
        String password = params[1];

        UserContext userContext = UserContext.create(username, password);

        return userContext;

    }

    @Override
    public void onPostExecute(UserContext userContext) {
        this.delegate.processAsyncResponse(userContext);
    }



}
