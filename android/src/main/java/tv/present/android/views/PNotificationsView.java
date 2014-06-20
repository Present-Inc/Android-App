package tv.present.android.views;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import tv.present.android.R;
import tv.present.android.controllers.CoreController;
import tv.present.android.controllers.PController;
import tv.present.android.models.PView;
import tv.present.android.util.PAndroidUtils;
import tv.present.android.util.PKeys;
import tv.present.android.util.PLog;
import tv.present.android.workers.DownloadImageWorker;

/**
 * Present Notifications View
 *
 * The {@link PNotificationsView} is the
 * {@link tv.present.android.models.PView} that displays the user's current list of notifications.
 *
 * June 10, 2014
 *
 * @author  Kyle Weisel (kyle@present.tv)
 */
public class PNotificationsView extends PView implements View.OnFocusChangeListener, View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "tv.present.android.views.NotificationsView";

    private ListView listView;

    public PNotificationsView() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PNotificationsView newInstance(PController controller) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(PKeys.KEY_CONTROLLER, controller);
        PNotificationsView notificationsView = new PNotificationsView();
        notificationsView.setController(controller);
        notificationsView.setArguments(arguments);
        return notificationsView;
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
        this.setController((CoreController) this.getArguments().getSerializable(PKeys.KEY_CONTROLLER));

        View rootView = inflater.inflate(R.layout.view_notifications, container, false);
        this.listView = (ListView) rootView.findViewById(R.id.notificationsList);

        // This will be preceeded by a show cached notifications call
        //this.controller.updateNotifications();
        PLog.logDebug(TAG, "onCreateView() -> Just before calling executeFetchNotifications on the controller, the controller is " + (this.controller == null ? "null" : "not null") + ".");
        ((CoreController) this.controller).executeFetchNotifications(0, 20);

        return rootView;

    }

    public void addViewToTable(View view) {
        //this.tableLayout.addView(view);
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

    public void addNotification(String profileImageURL, String message) {

        LayoutInflater inflater = (LayoutInflater) this.getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        TableRow tableRow = (TableRow) inflater.inflate(R.layout.tablerow_notification, null, false);

        tableRow.setOnClickListener(this);
        tableRow.setOnLongClickListener(this);
        ImageView tableRowHR = (ImageView) inflater.inflate(R.layout.imageview_hr, null, false);

        TextView tableRowTextView = (TextView) tableRow.findViewById(R.id.notificationTextView);
        tableRowTextView.setText(message);

        PCircularImageView circularImageView = (PCircularImageView) tableRow.findViewById(R.id.notificationSrcProfile);
        AsyncTask downloadImageworker = new DownloadImageWorker(circularImageView);
        //this.tableLayout.addView(tableRow);
        //this.tableLayout.addView(tableRowHR);

    }

}