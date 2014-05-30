package tv.present.android.controllers;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import tv.present.android.activities.MainActivity;
import tv.present.android.interfaces.Controller;
import tv.present.android.interfaces.LoginWorkerCallback;
import tv.present.android.workers.LoginWorker;

/**
 * The LoginController manages the FragmentLogin view.
 * @author Kyle Weisel (kyle@present.tv)
 */
public class LoginController extends Controller implements LoginWorkerCallback {

    private static final String TAG = "tv.present.android.controllers.LoginController";
    private final Fragment fragment;

    public LoginController(Fragment fragment) {
        this.fragment = fragment;
    }

    public Fragment getFragment() {
        return this.fragment;
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
     * This function is a callback of the login worker.
     * @param successfulLogin is a boolean value which indicates whether the login was successful
     *                        or not.
     */
    public void callbackLogin(Boolean successfulLogin) {

        if (successfulLogin) {
            // Move to the main activity
            Intent intent = new Intent(this.fragment.getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            this.fragment.startActivity(intent);
        }
        else {
            // Display an error message in the current activity
            Toast.makeText(this.fragment.getActivity().getBaseContext(), "There was a problem with those credentials.  Please try again!", Toast.LENGTH_LONG).show();
        }

    }

}
