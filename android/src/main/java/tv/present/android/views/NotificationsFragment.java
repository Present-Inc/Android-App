package tv.present.android.views;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import tv.present.android.R;
import tv.present.android.presenters.NotificationsPresenter;
import tv.present.android.util.PAndroidUtils;
import tv.present.android.util.PLog;

public class NotificationsFragment extends Fragment implements View.OnFocusChangeListener, View.OnClickListener, View.OnTouchListener, View.OnLongClickListener  {

    private static final String TAG = "tv.present.android.views.NotificationsListFragment";
    private final int REQUEST_CODE_PROFILE_IMAGE_CAPTURE = 8;

    private View rootView;
    private TableLayout tableLayout;
    private ImageButton choosePhotoButton;
    private NotificationsPresenter presenter;

    public NotificationsFragment() {
        this.presenter = new NotificationsPresenter(this);
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static NotificationsFragment newInstance() {
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

        this.rootView = inflater.inflate(R.layout.fragment_notifications, container, false);
        this.tableLayout = (TableLayout) this.rootView.findViewById(R.id.notificationsTableLayout);

        // This will be preceeded by a show cached notifications call
        this.presenter.updateNotifications();

        return rootView;

    }

    public void addViewToTable(View view) {
        this.tableLayout.addView(view);
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
        else if (view instanceof TableRow) {
            PLog.logDebug(TAG, "TableRow was touched");
            Toast.makeText(this.getActivity(), "Test!", Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            PAndroidUtils.hideKeyboardInActivity(this.getActivity());
            return false;
        }

    }

    public boolean onLongClick(View view) {
        if (view instanceof TableRow) {
            TableRow row = (TableRow) view;
            PLog.logDebug(TAG, "TableRow was long clicked");
            Toast.makeText(this.getActivity(), "Long click, yo (from Controller)!", Toast.LENGTH_LONG).show();
            row.setBackgroundColor(this.getResources().getColor(R.color.color_light_select));
            return true;
        }

        return true;
    }

    /**
     * Handles clicks on buttons.
     * @param view is the View (Button) that was clicked.
     */
    public void onClick(View view) {

        PLog.logDebug(TAG, "Registered cick on view " + view.toString());
        if (view instanceof TableRow) {
            PLog.logDebug(TAG, "TableRow was clicked");
            Toast.makeText(this.getActivity(), "Blah blah blah", Toast.LENGTH_LONG).show();
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
    }

    public ImageButton getChoosePhotoButton() {
        return this.choosePhotoButton;
    }

}
