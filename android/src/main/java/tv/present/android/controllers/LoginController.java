package tv.present.android.controllers;

import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import tv.present.android.activities.MainActivity;
import tv.present.android.interfaces.Controller;
import tv.present.android.interfaces.LoginWorkerCallback;
import tv.present.android.views.LoginFragment;
import tv.present.android.workers.LoginWorker;

/**
 * Created by kbw28 on 5/29/14.
 */
public class LoginController extends Controller implements LoginWorkerCallback {

    private static final String TAG = "tv.present.android.controllers.LoginController";
    private final LoginFragment loginFragment;

    public LoginController(LoginFragment loginFragment) {
        this.loginFragment = loginFragment;
    }

    public LoginFragment getLoginFragment() {
        return this.loginFragment;
    }

    /**
     * Performs a login on a background thread.  The thread will call back to the doLoginCallback()
     * method on completion.
     * @param username is the username String to try.
     * @param password is the password String to try.
     */
    public void executeLogin(String username, String password) {

        AsyncTask loginWorker = new LoginWorker(this);
        String[] temp = new String[2];
        temp[0] = username;
        temp[1] = password;
        loginWorker.execute(temp);

    }

    /**
     * This function is a callback of the login worker.  After the worker completes
     * @param successfulLogin
     */
    public void callbackLogin(Boolean successfulLogin) {

        if (successfulLogin) {
            // Move to the main activity
            Intent intent = new Intent(this.loginFragment.getActivity(), MainActivity.class);
            this.loginFragment.startActivity(intent);
        }
        else {
            // Display an error message in the current activity
            Toast.makeText(this.loginFragment.getActivity().getBaseContext(), "Blashhsdf", Toast.LENGTH_SHORT).show();
        }

    }


}
