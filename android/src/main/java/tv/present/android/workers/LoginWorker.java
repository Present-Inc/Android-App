package tv.present.android.workers;

import android.os.AsyncTask;

import tv.present.android.models.ApplicationCore;
import tv.present.models.UserContext;

/**
 * Does the work required to perform a user login in the background.
 */
public class LoginWorker extends AsyncTask<String, Void, Boolean>{

    // Inovked on UI before thread is executed
    @Override
    public void onPreExecute() {

    }

    @Override
    public Boolean doInBackground(String ... params) {

        ApplicationCore appCore = ApplicationCore.getInstance();
        UserContext userContext;

        // Check if for some reason we have a user context
        if (appCore.getUserContext() != null) {
            userContext = appCore.getUserContext();
        }
        // Otherwise, try to get one (ie: do the login)
        else {
            String username = params[0];
            String password = params[1];
            userContext = UserContext.create(username, password);   // Could be null
        }

        // If the userContext came back not null, set it in the app core.
        if (userContext != null)  {
            appCore.setUserContext(userContext);
            return true;
        }

        return false;

    }

    // Will execute on UI thread
    @Override
    public void onPostExecute(Boolean bool) {

    }
}
