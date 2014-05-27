package tv.present.android.views;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import tv.present.android.R;
import tv.present.android.util.PLog;

public class NotificationsFragment extends Fragment implements View.OnFocusChangeListener, View.OnClickListener, View.OnTouchListener {

    private static final String TAG = "tv.present.android.views.NotificationsListFragment";
    private final int REQUEST_CODE_PROFILE_IMAGE_CAPTURE = 8;

    ImageButton choosePhotoButton;

    public NotificationsFragment() {
        /* empty constructor */
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static NotificationsFragment newInstance() {
        //PlaceholderFragment fragment = new PlaceholderFragment();
        //Bundle args = new Bundle();
        //args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        //fragment.setArguments(args);
        return new NotificationsFragment();
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

        View rootView = inflater.inflate(R.layout.fragment_notifications, container, false);
        TableLayout tableLayout = (TableLayout) rootView.findViewById(R.id.notificationsTableLayout);

        for (int i = 0; i < 10; i++) {

            TableRow tableRow = (TableRow) inflater.inflate(R.layout.tablerow_notification, null, false);
            ImageView tableRowHR = (ImageView) inflater.inflate(R.layout.imageview_hr, null, false);

            TextView tableRowTextView = (TextView) tableRow.findViewById(R.id.notificationTextView);
            tableRowTextView.setText("Dan started a Present:  Real G's");

            tableLayout.addView(tableRow);
            tableLayout.addView(tableRowHR);


        }








        /*ActionBar actionBar = this.getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }*/

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

        PLog.logDebug(TAG, "Registered cick on view " + view.toString());
        if (view instanceof TableRow) {
            Toast.makeText(this.getActivity(), "Blah blah blah", Toast.LENGTH_LONG);
        }



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

    /**
     * Registers default touch listeners on all of the view objects that are contained within a
     * view.
     * @param view is a View to register listeners to.  If it is a ViewGroup, this function will be
     *             called recursively.
     */
    private void registerTouchListeners(View view) {
        view.setOnTouchListener(this);
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                final View innerView = ((ViewGroup) view).getChildAt(i);
                this.registerTouchListeners(innerView);
            }
        }
    }

}