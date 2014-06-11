package tv.present.android.workers;

import android.os.AsyncTask;

import tv.present.android.interfaces.LoginWorkerCallback;
import tv.present.android.models.ApplicationCore;
import tv.present.api.PAPIInteraction;
import tv.present.models.PUserContext;

/**
 * Does the work required to perform a user login in the background.
 */
    public class LoginWorker extends AsyncTask<String, Void, Boolean> {

    private LoginWorkerCallback loginWorkerCallback;

    public LoginWorker(LoginWorkerCallback callback) {
        this.loginWorkerCallback = callback;
    }

    // Inovked on UI before thread is executed
    @Override
    public void onPreExecute() {
        // Do nothing
    }

    // Will execute on UI thread:
    //  Check whether the boolean was true or false
    //  Advance the UI to the main screen on a successful login
    @Override
    public void onPostExecute(Boolean successfulLogin) {
        this.loginWorkerCallback.callbackLogin(successfulLogin);
    }

}
