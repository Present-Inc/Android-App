package tv.present.android.presenters;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import tv.present.android.R;
import tv.present.android.views.NotificationsFragment;

/**
 * Manages interactions with the Notifications View
 */
public final class NotificationsPresenter {

    private static final String TAG = "tv.present.android.controllers.NotificationsPresenter";
    private final int REQUEST_CODE_PROFILE_IMAGE_CAPTURE = 8;
    private NotificationsFragment subject;

    public NotificationsPresenter(NotificationsFragment fragment) {
        this.subject = fragment;
    }

    /**
     * Updates the notifications view to display the most recent notifications.
     */
    public void updateNotifications() {

        LayoutInflater inflater = (LayoutInflater) this.subject.getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Create a new table row, set it's listeners, and add it to the current tableLayout

        for (int i = 0; i < 20; i++) {

            TableRow tableRow = (TableRow) inflater.inflate(R.layout.tablerow_notification, null, false);

            tableRow.setOnClickListener(this.subject);
            tableRow.setOnLongClickListener(this.subject);
            ImageView tableRowHR = (ImageView) inflater.inflate(R.layout.imageview_hr, null, false);

            TextView tableRowTextView = (TextView) tableRow.findViewById(R.id.notificationTextView);
            tableRowTextView.setText("Dan started a Present:  Title2");

            this.subject.addViewToTable(tableRow);
            this.subject.addViewToTable(tableRowHR);

        }

    }

}