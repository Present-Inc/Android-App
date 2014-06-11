package tv.present.android.threads;

import android.os.AsyncTask;

import tv.present.android.interfaces.ThreadCallback;
import tv.present.android.models.PCallbackResult;
import tv.present.android.models.PCallbackResultWrapper;
import tv.present.android.util.PTaskExecutor;
import tv.present.android.util.PLog;
import tv.present.api.PAPIInteraction;
import tv.present.models.PUser;

/**
 * Present Create Account Thread
 *
 * This thread is responsible for creating an account, logging that account in, and then updating
 * the user details for the account.  It will call back to a specified ThreadCallback with a Boolean
 * response.
 *
 * June 10, 2014
 * @author Kyle Weisel (kyle@present.tv)
 *
 */
public final class CreateAccountThread extends AsyncTask<String, Void, Boolean> {

    private static final String TAG = "tv.present.android.threads.CreateAccountThread";
    private final int identifier;
    private final ThreadCallback threadCallback;

    /**
     * Constructs a CreateAccountThread.
     * @param identifier is an integer identifier to uniquely identify this request.
     * @param callback is a class an implementor of the ThreadCallback interface that will recieve
     *                 the results of this request.
     */
    public CreateAccountThread(final int identifier, final ThreadCallback callback) {
        this.identifier = identifier;
        this.threadCallback = callback;
    }

    /**
     * This method is invoked on the UI thread just before this thread is created and executed.
     */
    @Override
    public void onPreExecute() {
        // Do nothing
    }

    /**
     * Performs a user account creation in a background thread.  On successful execution, a
     * UserContext will automatically be created and returned to the caller.
     * @param params is a String[] array with the following structure:
     *               params[0] => username
     *               params[1] => password
     *               params[2] => email address
     * @return a non-null UserContext if the account was created successfully, otherwise null.
     */
    @Override
    public Boolean doInBackground(String ... params) {

        // Gather parameters
        final String username = params[0];
        final String password = params[1];
        final String emailAddress = params[2];
        final String fullName = params[3];
        final String phoneNumber = params[4];

        // Try to create the user
        PAPIInteraction apiInteraction = new PAPIInteraction();

        PUser user = apiInteraction.addUser(username, password, emailAddress);

        // Check if the user was created successfully, and if so we will log them in and edit their
        // account details.
        if (user != null) {
            PTaskExecutor taskExecutor = new PTaskExecutor();

            // Now try to log the user in
            if (taskExecutor.doLogin(username, password)) {
                PLog.logDebug(TAG, "Created and logged in as user " + username);                // Now we must change the user details
                return taskExecutor.doUpdateUserDetails(fullName, null, null, null, null, null, phoneNumber);

            }
            // Invalid login
            else {
                PLog.logError(TAG, "The task executor was unable to do a login after the user was created!");
                return false;
            }
        }

        return null;

    }

    /**
     * Executes on the UI thread after the completion of this thread.
     * @param successfulUpdate is a Boolean value that indicates the status of the update.
     */
    @Override
    public void onPostExecute(Boolean successfulUpdate) {
        this.threadCallback.threadCallback(new PCallbackResultWrapper(new PCallbackResult<Boolean>(this.identifier, successfulUpdate)));
    }

}