package tv.present.android.views;

import android.app.Activity;
import android.app.Fragment;
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
import tv.present.android.util.PLog;

public class LoginFragment extends Fragment implements View.OnFocusChangeListener, View.OnClickListener, View.OnTouchListener {

    private static final String TAG = "tv.present.android.views.LoginFragment";

    public LoginFragment() {
        /* empty constructor */
    }

    /**
     * Registers default touch listeners on all of the view objects that are contained within a
     * view.
     * @param view is a View to register listeners to.  If it is a ViewGroup, this function will be
     *             called recursively.
     */
    private void registerTouchListeners(View view) {
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(this);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                this.registerTouchListeners(innerView);
            }
        }
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

        View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        this.registerTouchListeners(rootView);

        RelativeLayout rootLayout = (RelativeLayout) rootView.findViewById(R.id.loginRootLayout);
        rootLayout.requestFocus();

        EditText passwordField = (EditText) rootView.findViewById(R.id.passwordField);
        EditText usernameField = (EditText) rootView.findViewById(R.id.usernameField);
        passwordField.setOnFocusChangeListener(this);
        usernameField.setOnFocusChangeListener(this);
        usernameField.clearFocus();

        Button loginButton = (Button) rootView.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);

        TextView forgotPasswordText = (TextView) rootView.findViewById(R.id.forgotPasswordText);
        forgotPasswordText.setOnClickListener(this);

        this.getActivity().getActionBar().hide();

        return rootView;

    }

    /**
     * This method is called when focus changes on either of the two input textboxes.
     * @param view is the view that called this method (ie: the EditText box).
     * @param hasFocus is a boolean value which specifies whether the view has focus or no longer does (ie: lost it).
     */
    public void onFocusChange(final View view, final boolean hasFocus) {

        PLog.logDebug(TAG, "Focus has changed to " + hasFocus + " on the " + view.toString() + " view.");

        InputMethodManager inputMethodManager = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        // Shows the keyboard when a login field gets focus.
        if(hasFocus) {
            EditText field = (EditText) view;
            inputMethodManager.showSoftInput(field, InputMethodManager.SHOW_FORCED);
        }
        // Hides it on lost focus.
        else {
            EditText field = (EditText) view;
            view.setFocusable(false);
            view.setFocusableInTouchMode(false);
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

    public void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void removeFocus(View view) {

    }

    public boolean onTouch(View view, MotionEvent event) {

        if (view instanceof EditText) {
            InputMethodManager inputMethodManager = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            return false;
        }
        else {
            this.hideKeyboard(this.getActivity());
            return false;
        }

    }

    public void onClick(View view) {

        if (view instanceof Button) {
            EditText username = (EditText) this.getActivity().findViewById(R.id.usernameField);
            EditText password = (EditText) this.getActivity().findViewById(R.id.passwordField);

            String text = "Your username was " + username.getText() + " and password was " + password.getText();

            Toast.makeText(this.getActivity().getBaseContext(), text, Toast.LENGTH_LONG).show();
        }
        else if (view instanceof TextView) {
            String text = "Oh shit, we got you covered!";
            Toast.makeText(this.getActivity().getBaseContext(), text, Toast.LENGTH_LONG).show();
        }


    }


}
