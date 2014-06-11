package tv.present.android.threads;

import android.os.AsyncTask;

import tv.present.android.interfaces.ThreadCallback;
import tv.present.android.models.PCallbackResult;
import tv.present.android.util.PTaskExecutor;

/**
 * Performs a login operation in a Thread.  This thread will automatically register the user context
 * in the application core, and return a boolean value to it's callback method.
 */
public final class LoginThread extends AsyncTask<String, Void, Boolean> {

    private final int identifier;
    private final ThreadCallback threadCallback;

    public LoginThread(final int identifier, final ThreadCallback callback) {
        this.identifier = identifier;
        this.threadCallback = callback;
    }

    // Invoked on UI before thread is executed
    @Override
    public void onPreExecute() {
        // Do nothing
    }

    @Override
    public Boolean doInBackground(String ... params) {
        final String username = params[0];
        final String password = params[1];
        PTaskExecutor taskExecutor = new PTaskExecutor();
        return taskExecutor.doLogin(username, password);
    }

    // Will execute on UI thread:
    // Check whether the boolean was true or false
    //s Advance the UI to the main screen on a successful login
    @Override
    public void onPostExecute(Boolean successfulLogin) {
        this.threadCallback.threadCallback(new PCallbackResult<Boolean>(this.identifier, successfulLogin));
    }

}
