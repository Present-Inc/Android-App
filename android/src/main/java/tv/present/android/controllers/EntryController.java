package tv.present.android.controllers;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import tv.present.android.R;
import tv.present.android.exceptions.InvalidCallbackResultIdentifierException;
import tv.present.android.interfaces.ThreadCallback;
import tv.present.android.models.PCallbackResult;
import tv.present.android.models.PView;
import tv.present.android.threads.PLoginThread;
import tv.present.android.util.PCallbackIdentifiers;
import tv.present.android.util.PLog;
import tv.present.android.views.CreateAccountView;
import tv.present.android.views.LoginView;

/**
 * Present Entry Controller Object
 *
 * This controller is the entry point for the application.  If there is no user currently logged in
 * it will launch the login view.  It also handles a few other pre-login tasks and views.
 *
 * June 10, 2014
 * @author Kyle Weisel (kyle@present.tv)
 *
 */
public final class EntryController extends PController implements ThreadCallback {

    private static final String TAG ="tv.present.android.controllers.EntryController";
    private LoginView loginView;
    private CreateAccountView createAccountView;

    /**
     * Creates this view.
     * @param savedInstanceState is a Bundle of data that represents how this view once existed.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (savedInstanceState == null) {
            PView loginView = this.getLoginView();
            getFragmentManager().beginTransaction().add(R.id.container, loginView).commit();
        }
    }

    /**
     * Inflate the menu.
     * @param menu is a Menu.
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    /**
     * Handle clicks on the action bar.  Home/Up clicks are handled automatically so long as a
     * parent is specified in the manifest.
     * @param item is the MenuItem that was clicked.
     * @return true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_about) {

            Toast.makeText(this, "You clicked the menu!", Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void executeLogin(String username, String password) {
        PLoginThread loginThread = new PLoginThread(PCallbackIdentifiers.LOGIN, this);
        String[] temp = new String[2];
        temp[0] = username;
        temp[1] = password;
        loginThread.execute(temp);
    }

    public void startCreateAccountView() {
        final FragmentManager fragmentManager = this.getFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Create and configure the create account view
        PView loginView = this.getCreateAccountView();
        fragmentTransaction.replace(R.id.container, loginView);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void executeCreateAccount(String username, String password, String emailAddress, String fullName, String phoneNumber) {
        // TODO: Implement
    }

    public void executeStartForgotPassword() {
        // TODO: Implement
    }

    /**
     * This method handles a callback from a thread that was told to use this object as as it's
     * callback.  Most likely, this thread was also launched from this object.
     * @param callbackData is a PCallbackResult<Boolean>.
     */
    public void threadCallback(PCallbackResult<?> callbackData) {

        // Get the callback identifier and the data from the PCallbackResult<Boolean> parameter
        final int callbackIdentifier = callbackData.getIdentifier();

        try {

            // Switch on the identifier to determine what action we need to take.  This really only
            // needs to happen if there are multiple callbacks with the same return type being
            // handled by this controller.
            switch(callbackIdentifier) {

                /* ########## LOGIN ACTION ########## */
                case PCallbackIdentifiers.LOGIN:

                    final Boolean successfulLogin = (Boolean) callbackData.getResultData();
                    // Check to see whether login was successful
                    if (successfulLogin) {
                        // Start the main activity if the result is true (ie: the login was
                        // successful, and the context was stored in the application core).
                        Intent intent = new Intent(this, CoreController.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        this.startActivity(intent);
                    }
                    else {
                        // Otherwise, display an error message in the current activity.
                        Toast.makeText(this.getBaseContext(), "There was a problem with those credentials.  Please try again!", Toast.LENGTH_LONG).show();
                    }
                    break;

                /* ########## CREATE ACCOUNT ACTION ########## */
                case PCallbackIdentifiers.CREATE_ACCOUNT:

                    final Boolean successfulCreation = (Boolean) callbackData.getResultData();

                    if (successfulCreation) {
                        // User was created, now update details
                        // TODO: Implement
                    }
                    else {
                        Toast.makeText(this.getBaseContext(), "Unable to login.  Try again!", Toast.LENGTH_LONG).show();
                    }
                    break;

                /* ########## DEFAULT ACTION ########## */
                default:
                    throw new InvalidCallbackResultIdentifierException("An incorrect callback identifier was passed back to " + TAG + ".  The identifier was " + callbackIdentifier);

            }

        } catch (InvalidCallbackResultIdentifierException e) {
            PLog.logError(TAG, "Caught InvalidCallbackResultIdentifierException with message: " + e.getMessage());
        }

    }

    /**
     * Gets the create account view that this controller controls.
     * @return a CreateAccountView.
     */
    public CreateAccountView getCreateAccountView() {
        if (this.createAccountView == null) {
            PLog.logDebug(TAG, "getCreateAccountView() -> A create account view did not exist.  Creating a new one.");
            this.createAccountView = CreateAccountView.newInstance(this);
        }
        return this.createAccountView;
    }

    /**
     * Gets the login view that this controller controls.
     * @return a LoginView.
     */
    public LoginView getLoginView() {
        if (this.loginView == null) {
            PLog.logDebug(TAG, "getLoginView() -> A login view did not exist.  Creating a new one.");
            this.loginView = LoginView.newInstance(this);
        }
        return this.loginView;
    }

}