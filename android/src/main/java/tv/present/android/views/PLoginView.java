package tv.present.android.views;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import tv.present.android.R;
import tv.present.android.controllers.EntryController;
import tv.present.android.controllers.PController;
import tv.present.android.models.PView;
import tv.present.android.util.PLog;

public final class PLoginView extends PView implements View.OnFocusChangeListener, View.OnClickListener {

    private static final String TAG = "tv.present.android.views.LoginView";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PLoginView newInstance(PController controller) {
        Bundle arguments = new Bundle();
        arguments.putSerializable("controller", controller);
        PLog.logDebug(TAG, "newInstance -> the controller put into fragment arguments was " + (controller == null ? "in fact" : "not") + " null");
        PLog.logDebug(TAG, "newInstance -> the controller got out of the fragment arguments was " + (arguments.get("controller") == null ? "in fact" : "not") + " null");
        PLoginView loginView = new PLoginView();
        loginView.setController(controller);
        loginView.setArguments(arguments);
        return loginView;
    }

    /**
     * Constructs a LoginView.  Unfortunately this method has to remain public due to Android
     * specification, but it should never be used to instantiate this view.  Instead, the
     * newInstance() method should be called.  Funny things will happen if you don't do it this way.
     */
    public PLoginView() {
        /* Empty constructor */
    }

    /**
     * Inflates and prepares this fragment view.  Within this method, the root layout element is
     * given focus so that the username field doesn't steal it.  OnFocusChange listeners are also
     * registered to both the username and password fields to toggle the keyboard when a field gains
     * or loses focus.
     * @param inflater is the LayoutInflater to use to inflate this layout.
     * @param container is a ViewGroup container that will display this inflated layout.
     * @param savedInstanceState is a Bundle of data that represents a previous instance state to
     *                           restore this view to.
     * @return a View that is the LoginFragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        PLog.logDebug(TAG, "Creating and configuring fragment view.");

        final View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        // This app is not compatible with API levels that require the SupportActionBar, therefore,
        // we will not use it.
        @SuppressLint("AppCompatMethod")
        ActionBar actionBar = this.getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        this.registerTouchListeners(rootView);

        final RelativeLayout rootLayout = (RelativeLayout) rootView.findViewById(R.id.loginRootLayout);
        rootLayout.requestFocus();

        final EditText passwordField = (EditText) rootView.findViewById(R.id.passwordField);
        final EditText usernameField = (EditText) rootView.findViewById(R.id.usernameField);
        passwordField.setOnFocusChangeListener(this);
        usernameField.setOnFocusChangeListener(this);
        usernameField.clearFocus();

        final Button loginButton = (Button) rootView.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);

        final TextView createAccountText = (TextView) rootView.findViewById(R.id.createAccountText);
        createAccountText.setOnClickListener(this);

        final TextView forgotPasswordText = (TextView) rootView.findViewById(R.id.forgotPasswordText);
        forgotPasswordText.setOnClickListener(this);

        return rootView;

    }

    /**
     * This method is called when focus changes on either of the two input textboxes.
     * @param view is the view that called this method (ie: the EditText box).
     * @param hasFocus is a boolean value which specifies whether the view has focus or no longer does (ie: lost it).
     */
    public void onFocusChange(final View view, final boolean hasFocus) {

        PLog.logDebug(TAG, "Focus has changed to " + hasFocus + " on the " + view.toString() + " view.");

        final InputMethodManager inputMethodManager = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        // Shows the keyboard when a login field gets focus.
        if(hasFocus) {
            final EditText field = (EditText) view;
            inputMethodManager.showSoftInput(field, InputMethodManager.SHOW_FORCED);
        }
        /* Hides it on lost focus.
        else {
            final EditText field = (EditText) view;
            view.setFocusable(false);
            view.setFocusableInTouchMode(false);
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } */

    }

    /**
     * Handles touch events.  If the calling View is not a textbox, we hide the keyboard.
     * @param view is the View that was touched.
     * @param event is the MotionEvent that occured.
     * @return false
     */
    public boolean onTouch(View view, MotionEvent event) {

        if (view instanceof EditText) {
            final InputMethodManager inputMethodManager = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            return false;
        }
        else {
            this.hideKeyboard(this.controller);
            return false;
        }

    }

    /**
     * Handles long clicks.
     * @param view is a View that was long clicked.
     * @return a boolean value for which I am unsure of the meaning.
     */
    public boolean onLongClick(View view) {
        return false;
    }

    /**
     * Handles clicks on buttons.
     * @param view is the View (Button) that was clicked.
     */
    public void onClick(View view) {

        if (view instanceof Button) {

            final EditText username = (EditText) this.getActivity().findViewById(R.id.usernameField);
            final EditText password = (EditText) this.getActivity().findViewById(R.id.passwordField);
            PLog.logDebug(TAG, "Performing login with username " + username.getText().toString() + " and password: " + password.getText().toString());
            ((EntryController)this.controller).executeLogin(username.getText().toString(), password.getText().toString());

        }
        else if (view instanceof TextView) {

            switch(view.getId()) {

                // Create a new account activity
                case R.id.createAccountText :
                    ((EntryController)this.controller).startCreateAccountView();
                    break;

                // Start the forgot password activity
                case R.id.forgotPasswordText :

                    break;
            }

            final String text = "We got you covered!";
            Toast.makeText(this.getActivity().getBaseContext(), text, Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Sets the text displayed on the login button.
     * @param text is the text to be displayed on the button as a String.
     */
    public void setLoginButtonText(final String text) {

        View currentView = this.getView();
        final Button loginButton;
        if (currentView != null) {
            loginButton = (Button) this.getView().findViewById(R.id.loginButton);
            loginButton.setText(text);
        }

    }

    /**
     * Sets the state of the login button.
     * @param shouldBeEnabled is a boolean that is true if the button should be enabled, or false
     *                        otherwise
     */
    public void setLoginButtonEnabled(final boolean shouldBeEnabled) {

        View currentView = this.getView();
        final Button loginButton;
        if (currentView != null) {
            loginButton = (Button) this.getView().findViewById(R.id.loginButton);
            loginButton.setEnabled(shouldBeEnabled);
        }

    }

}
