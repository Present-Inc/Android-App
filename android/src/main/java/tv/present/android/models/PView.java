package tv.present.android.models;

import android.app.Activity;
import android.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import tv.present.android.controllers.PController;


public abstract class PView extends Fragment implements View.OnTouchListener {

    protected PController controller;

    /**
     * Gets the controller for this fragment.
     * @return
     */
    public PController getController() {
        return this.controller;
    }


    /**
     * Hides the soft keyboard.
     * @param controller is the calling Activity.
     */
    public void hideKeyboard(PController controller) {
        final View currentView = controller.getCurrentFocus();
        if (currentView != null) {
            final InputMethodManager inputMethodManager = (InputMethodManager) controller.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(currentView.getWindowToken(), 0);
        }
    }

    /**
     * Registers default touch listeners on all of the view objects that are contained within a
     * view.
     * @param view is a View to register listeners to.  If it is a ViewGroup, this function will be
     *             called recursively.
     */
    protected void registerTouchListeners(View view) {
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(this);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                final View innerView = ((ViewGroup) view).getChildAt(i);
                this.registerTouchListeners(innerView);
            }
        }
    }



}
