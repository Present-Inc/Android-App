package tv.present.android.controllers;

import android.content.Intent;
import android.widget.Toast;

import tv.present.android.activities.MainActivity;
import tv.present.android.interfaces.Controller;
import tv.present.android.interfaces.CreateAccountWorkerCallback;
import tv.present.android.interfaces.LoginWorkerCallback;
import tv.present.android.interfaces.UpdateUserDetailsWorkerCallback;
import tv.present.android.util.PLog;
import tv.present.android.views.CreateAccountFragment;
import tv.present.android.workers.CreateAccountWorker;
import tv.present.android.workers.LoginWorker;
import tv.present.android.workers.UpdateUserDetailsWorker;
import tv.present.models.User;
import tv.present.models.UserContext;

/**
 *  The CreateAccountController manages the FragmentCreateAccount view.
 *  @author Kyle Weisel (kyle@present.tv)
 */
public class CreateAccountController extends Controller implements CreateAccountWorkerCallback, LoginWorkerCallback, UpdateUserDetailsWorkerCallback {

    private static final String TAG = "tv.present.android.controllers.CreateAccountController";
    private final CreateAccountFragment createAccountFragment;
    private UserContext userContext;
    private String username;
    private String password;
    private String emailAddress;
    private String fullName;
    private String phoneNumber;

    /**
     * Creates the CreateAccount controller and registers the fragment that this controller will
     * be controlling.
     * @param createAccountFragment
     */
    public CreateAccountController(CreateAccountFragment createAccountFragment) {
        this.createAccountFragment = createAccountFragment;
    }

    /**
     * Executes the creating of an account by the worker thread.
     * @param username is the Sting username.
     * @param password is the String password.
     * @param emailAddress is the String email address.
     * @param fullName is the user's full name as a String.
     * @param phoneNumber is the user's phone number String.
     */
    public void executeCreateAccount(String username, String password, String emailAddress, String fullName, String phoneNumber) {

        this.username = username;
        this.password = password;
        this.emailAddress = emailAddress;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;

        String[] temp = new String[5];
        temp[0] = username;
        temp[1] = password;
        temp[2] = emailAddress;
        temp[3] = fullName;
        temp[4] = phoneNumber;

        CreateAccountWorker createAccountWorker = new CreateAccountWorker(this);
        createAccountWorker.execute(temp);

    }

    /**
     * This function is a callback of the create account worker which will return a UserContext that
     * is either null or not null.
     * @param user is a User object associated with the create account request, which may be null if
     *              the account creation failed.
     */
    public void callbackCreateAccount(User user) {

        if (user != null) {
            // Account was created, now we need to login

            LoginWorker loginWorker = new LoginWorker(this);
            loginWorker.execute(this.username, this.password);

            Intent intent = new Intent(this.createAccountFragment.getActivity(), MainActivity.class);
            this.createAccountFragment.startActivity(intent);
        }
        else {
            // Account not created
            Toast.makeText(this.createAccountFragment.getView().getContext(), "Unable to create that account.  Try again!", Toast.LENGTH_LONG);

        }
    }

    /**
     * This method is a callback from the UpdateUserDetailsWorker.
     * @param successfulUpdate is a boolean value that indicates whether the user details update was
     *                         successful or not.
     */
    public void callbackUpdateUserDetails(boolean successfulUpdate) {
        // Regardless of whether the update was successful, move on.  We will just log the error.
        if (!successfulUpdate) {
            PLog.logError(TAG, "From the callback: user details were unable to be successfully updated.");
        }

        Intent intent = new Intent(this.createAccountFragment.getActivity(), MainActivity.class);
        this.createAccountFragment.startActivity(intent);
    }

    /**
     * This method is a callback from the LoginWorker.
     * @param successfulLogin is a boolean value that indicates whether the user loing was
     *                        successful or not.
     */
    public void callbackLogin(Boolean successfulLogin) {

        if (successfulLogin) {
            // Logged in, now update their account

            UpdateUserDetailsWorker updateUserDetailsWorker = new UpdateUserDetailsWorker(this);

            String[] params = new String[7];
            params[0] = this.fullName;
            params[1] = null;
            params[2] = null;
            params[3] = null;
            params[4] = null;
            params[5] = null;
            params[6] = this.phoneNumber;

            updateUserDetailsWorker.execute(params);
        }
        else {
            Toast.makeText(this.createAccountFragment.getView().getContext(), "Unable to login.  Try again!", Toast.LENGTH_LONG);
        }

    }

}