package tv.present.android.workers;

import android.os.AsyncTask;

import tv.present.android.interfaces.CreateAccountWorkerCallback;
import tv.present.api.PAPIInteraction;
import tv.present.factories.PObjectFactory;
import tv.present.models.PUser;

/**
 * A working thread that performs account creation on a background thread.  Successful account
 * creation will automatically log a user in, so this thread will return a UserContext on success.
 */
public final class PCreateAccountWorker extends AsyncTask<String, Void, PUser> {

    private CreateAccountWorkerCallback createAccountWorkerCallback;

    public PCreateAccountWorker(CreateAccountWorkerCallback callback) {
        this.createAccountWorkerCallback = callback;
    }

    // Inovked on UI before thread is executed
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
    public PUser doInBackground(String ... params) {

        // Gather parameters
        final String username = params[0];
        final String password = params[1];
        final String emailAddress = params[2];

        // Try to create the user
        PObjectFactory objectFactory = new PObjectFactory();

        PAPIInteraction apiInteraction = new PAPIInteraction();
        return apiInteraction.addUser(username, password, emailAddress);

    }

    // Will execute on UI thread:
    //  Check whether the boolean was true or false
    //  Advance the UI to the main screen on a successful login
    @Override
    public void onPostExecute(PUser user) {
        this.createAccountWorkerCallback.callbackCreateAccount(user);
    }


}
