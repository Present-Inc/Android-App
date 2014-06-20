package tv.present.android.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import tv.present.android.R;
import tv.present.android.controllers.PController;
import tv.present.android.models.PView;
import tv.present.android.util.PLog;

/**
 * Created by kbw28 on 6/10/14.
 */
public class PHomeFeedView extends PView implements View.OnFocusChangeListener, View.OnClickListener {

    private static final String TAG = "tv.present.android.views.HomeFeedView";
    private final int REQUEST_CODE_PROFILE_IMAGE_CAPTURE = 8;

    ImageButton choosePhotoButton;

    public PHomeFeedView() {
        /* empty constructor */
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PHomeFeedView newInstance(PController controller) {
        Bundle arguments = new Bundle();
        arguments.putSerializable("controller", controller);
        PHomeFeedView homeFeedView = new PHomeFeedView();
        homeFeedView.setArguments(arguments);
        return homeFeedView;
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
        View rootView = inflater.inflate(R.layout.fragment_home_feed, container, false);
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
        /* Hides it on lost focus. - Don't do this
        else {
            EditText field = (EditText) view;
            view.setFocusable(false);
            view.setFocusableInTouchMode(false);
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }*/

    }

    /**
     * Hides the soft keyboard.
     * @param activity is the calling Activity.
     */
    public void hideKeyboard(Activity activity) {
        final View currentView = activity.getCurrentFocus();
        if (currentView != null) {
            final InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(currentView.getWindowToken(), 0);
        }
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
            this.hideKeyboard(this.getActivity());
            return false;
        }

    }

    /**
     * Handles clicks on buttons.
     * @param view is the View (Button) that was clicked.
     */
    public void onClick(View view) {

        // Remember: there is only one ImageButton on this view
        if (view instanceof ImageButton) {
            final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(this.getActivity().getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_CODE_PROFILE_IMAGE_CAPTURE);
            }
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
     * Handles the callback from another activity that was started for a result.
     * @param requestCode is the integer code of the requesting Activity.
     * @param resultCode is the integer result code from the Activity.
     * @param data is any returned Intent data from the finished Activity.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PROFILE_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            final Bundle extras = data.getExtras();
            final Bitmap imageBitmap = (Bitmap) extras.get("data");
            this.choosePhotoButton.setImageBitmap(imageBitmap);
        }
    }

}
