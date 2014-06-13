package tv.present.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tv.present.android.R;
import tv.present.android.models.PView;
import tv.present.android.views.CircularImageView;
import tv.present.models.PUserActivity;
import tv.present.util.PResultSet;

/**
 * Present Notifications Adapter
 *
 * This adapter is used to bind data to notification detail views for the notifications section of
 * the Present application.
 *
 * June 13, 2014
 *
 * @author  Kyle Weisel (kyle@present.tv)
 */
public class NotificationsAdapter extends PAdapter {

    private static final String TAG = "tv.present.android.adapters.NotificationsAdapter";
    private final LayoutInflater layoutInflater;
    private PResultSet<PUserActivity> userActivitiesResultSet;
    private PView parentView;

    public NotificationsAdapter(Context context, PResultSet<PUserActivity> userActivitiesResultSet, PView parentView) {
        this.layoutInflater = LayoutInflater.from(context);
        this.userActivitiesResultSet = userActivitiesResultSet;
        this.parentView = parentView;
    }

    public void setData(PResultSet<PUserActivity> userActivitiesResultSet) {
        if (this.userActivitiesResultSet != null) {
            this.userActivitiesResultSet = null;
        }
        this.userActivitiesResultSet = userActivitiesResultSet;
        notifyDataSetChanged();
    }

    public View getView(int i, View view, ViewGroup parent) {

        PUserActivity userActivity = (PUserActivity) this.getItem(i);

        if (view == null) {
            view = this.layoutInflater.inflate(R.layout.detail_notification, null, false);
        }

        view.setOnClickListener(this.parentView);
        view.setOnLongClickListener(this.parentView);

        //ImageView tableRowHR = (ImageView) inflater.inflate(R.layout.imageview_hr, null, false);

        TextView tableRowTextView = (TextView) view.findViewById(R.id.notificationTextView);
        tableRowTextView.setText(userActivity.getSubject());

        CircularImageView circularImageView = (CircularImageView) view.findViewById(R.id.notificationProfileImage);
        //AsyncTask downloadImageworker = new DownloadImageWorker(circularImageView);

        return view;

    }

    @Override
    public int getCount() {
        return this.userActivitiesResultSet.getResults().size();
    }

    @Override
    public Object getItem(final int i) {
        return this.userActivitiesResultSet.getResults().get(i);
    }

    @Override
    public long getItemId(final int i) {
        return i;
    }

}
