package tv.present.android.workers;

import android.os.AsyncTask;

import tv.present.android.interfaces.CreateAccountWorkerCallback;
import tv.present.models.User;

/**
 * Created by kbw28 on 5/29/14.
 */
public class CreateAccountWorker extends AsyncTask<String, Void, User> {

    private CreateAccountWorkerCallback createAccountWorkerCallback;

    public CreateAccountWorker(CreateAccountWorkerCallback callback) {
        this.createAccountWorkerCallback = callback;
    }

    // Inovked on UI before thread is executed
    @Override
    public void onPreExecute() {
        // Do nothing
    }

    @Override
    public User doInBackground(String ... params) {

        String username = params[0];
        String password = params[1];
        String emailAddress = params[2];
        String fullName = params[3];
        String phoneNumber = params[4];

        User result = User.add(username, password, emailAddress);

        return result;

    }

    // Will execute on UI thread:
    //  Check whether the boolean was true or false
    //  Advance the UI to the main screen on a successful login
    @Override
    public void onPostExecute(User user) {
        this.createAccountWorkerCallback.callbackCreateAccount(user);
    }

}
