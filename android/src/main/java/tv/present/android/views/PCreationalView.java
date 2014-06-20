package tv.present.android.views;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import tv.present.android.R;
import tv.present.android.controllers.PController;
import tv.present.android.controllers.PEntryController;
import tv.present.android.models.PView;
import tv.present.android.util.PLog;

/**
 * Created by kbw28 on 6/20/14.
 */
public class PCreationalView extends PView {

    private static final String TAG = "tv.present.android.views.PCreationalView";

    /**
     * Returns a new instance of this view.
     */
    public static PCreationalView newInstance(PController controller) {

        Bundle arguments = new Bundle();
        arguments.putSerializable("controller", controller);
        PLog.logDebug(TAG, "newInstance -> the controller put into fragment arguments was " + (controller == null ? "in fact" : "not") + " null");
        PLog.logDebug(TAG, "newInstance -> the controller got out of the fragment arguments was " + (arguments.get("controller") == null ? "in fact" : "not") + " null");
        PCreationalView creationalView = new PCreationalView();
        creationalView.setController(controller);
        creationalView.setArguments(arguments);

        return creationalView;

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
     * Handles clicks on buttons.
     * @param view is the View (Button) that was clicked.
     */
    public void onClick(View view) {

        View rootView = this.getView();

        if (view instanceof Button) {

            // Get all of the data from the view
            String emailAddress = ((EditText) rootView.findViewById(R.id.createEmailAddressField)).getText().toString();
            String username = ((EditText) rootView.findViewById(R.id.createUsernameField)).getText().toString();
            String password = ((EditText) rootView.findViewById(R.id.createPasswordField)).getText().toString();
            String fullName = ((EditText) rootView.findViewById(R.id.createFullNameField)).getText().toString();
            String phoneNumber = ((EditText) rootView.findViewById(R.id.createPhoneNumberField)).getText().toString();

            ((PEntryController) this.controller).executeCreateAccount(username, password, emailAddress, fullName, phoneNumber);
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

}
