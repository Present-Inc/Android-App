package tv.present.android.models;

import android.app.Activity;
import android.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import tv.present.android.controllers.PController;

/**
 * Present Abstract View Object
 *
 * This is an abstract view object that all Present views will inherit from.  All views will have
 * to implement an OnClickListener, OnLongClickListener, and OnTouchListener.
 *
 * June 20, 2014
 *
 * @author  Kyle Weisel (kyle@present.tv)
 */
public abstract class PView extends Fragment implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {

    protected PController controller;

    /**
     * Constructs a PView object.
     */
    public PView() {
        /* Empty constructor */
    }

    /**
     * Gets the controller for this view (fragment).
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

    /**
     * Sets the controller for this view (fragment).
     * @param controller is the PController to set as the controller for this view.
     */
    public void setController(PController controller) {
        this.controller = controller;
    }

}
