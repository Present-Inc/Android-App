package tv.present.android.controllers;

import tv.present.android.interfaces.Controller;
import tv.present.android.interfaces.CreateAccountWorkerCallback;
import tv.present.android.views.CreateAccountFragment;
import tv.present.android.workers.CreateAccountWorker;
import tv.present.models.User;

/**
 * Created by kbw28 on 5/29/14.
 */
public class CreateAccountController extends Controller implements CreateAccountWorkerCallback {

    private static final String TAG = "tv.present.android.controllers.CreateAccountController";
    private final CreateAccountFragment createAccountFragment;

    public CreateAccountController(CreateAccountFragment createAccountFragment) {
        this.createAccountFragment = createAccountFragment;
    }

    public void executeCreateAccount(String username, String password, String emailAddress, String fullName, String phoneNumber) {
        String[] temp = new String[5];

        temp[0] = username;
        temp[1] = password;
        temp[2] = emailAddress;
        temp[3] = fullName;
        temp[4] = phoneNumber;

        CreateAccountWorker createAccountWorker = new CreateAccountWorker(this);
        createAccountWorker.execute(temp);

    }

    public void callbackCreateAccount(User user) {

        if (user == null) {
            // Account was not created
        }
        else {
            // Account creation successful
        }
    }


}
