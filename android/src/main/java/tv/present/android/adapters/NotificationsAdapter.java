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
public final class NotificationsAdapter extends PAdapter {

    private static final String TAG = "tv.present.android.adapters.NotificationsAdapter";
    private final LayoutInflater layoutInflater;
    private PResultSet<PUserActivity> userActivitiesResultSet;
    private PView parentView;

    /**
     * Constructs a NotificationsAdapter.
     * @param context is a Context.
     * @param userActivitiesResultSet is the source PResultSet with type PUserActivity.
     * @param parentView is the parent that the views provided by the notifications adapter will
     *                   be loaded into.
     */
    public NotificationsAdapter(Context context, PResultSet<PUserActivity> userActivitiesResultSet, PView parentView) {
        this.layoutInflater = LayoutInflater.from(context);
        this.userActivitiesResultSet = userActivitiesResultSet;
        this.parentView = parentView;
    }

    /**
     * Sets the response data.
     * @param userActivitiesResultSet is a PResultSet of type PUserActivity.
     */
    public void setData(PResultSet<PUserActivity> userActivitiesResultSet) {
        if (this.userActivitiesResultSet != null) {
            this.userActivitiesResultSet = null;
        }
        this.userActivitiesResultSet = userActivitiesResultSet;
        notifyDataSetChanged();
    }

    /**
     * Gets a view for each individual result.
     * @param i is the result number as an integer.
     * @param view is an existing PView to add to.
     * @param parent is the parent View that the product view will be inserted into.
     * @return a View with data bound.
     */
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

    /**
     * Gets the number of results.
     * @return the number of results as an integer.
     */
    @Override
    public int getCount() {
        return this.userActivitiesResultSet.getResults().size();
    }

    /**
     * Gets an object.
     * @param i is the index of the object to get within the response.
     * @return the Object at index i.
     */
    @Override
    public Object getItem(final int i) {
        return this.userActivitiesResultSet.getResults().get(i);
    }

    /**
     * Gets the id of an item (which is the same as the parameter, so don't call this method).
     * @param i is an integer.
     * @return the integer i.
     */
    @Override
    public long getItemId(final int i) {
        return i;
    }

}
