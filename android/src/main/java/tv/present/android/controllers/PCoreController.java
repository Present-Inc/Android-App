package tv.present.android.controllers;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import tv.present.android.R;
import tv.present.android.adapters.PNotificationsAdapter;
import tv.present.android.adapters.PSectionsPagerAdapter;
import tv.present.android.adapters.PSectionsPagerAdapterMain;
import tv.present.android.exceptions.InvalidCallbackResultIdentifierException;
import tv.present.android.interfaces.ThreadCallback;
import tv.present.android.models.PCallbackResult;
import tv.present.android.util.PCallbackIdentifiers;
import tv.present.android.util.PKeys;
import tv.present.android.util.PLog;
import tv.present.android.views.PHomeFeedView;
import tv.present.android.views.PNotificationsView;
import tv.present.models.PUserActivity;
import tv.present.util.PResultSet;

/**
 * Present Core Controller Object
 *
 * This controller is the core controller of the application.  It is responsible for handling the
 * "main" views, such as watching and exploring Presents.
 *
 * June 10, 2014
 *
 * @author  Kyle Weisel (kyle@present.tv)
 */
public class PCoreController extends PController implements ActionBar.TabListener, ThreadCallback, LoaderManager.LoaderCallbacks<PResultSet> {

    private static final String TAG = "tv.present.android.controllers.CoreController";
    private PSectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private PNotificationsView notificationsView;
    private PHomeFeedView homeFeedView;

    private PNotificationsAdapter notificationsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* ==== Suppressing application compatibility warning ====
         * Reason:  This application only supports API levels that have a proper implementation of
         * the action bar, and therefore there is no need to use the support action bar.
         */
        @SuppressLint("AppCompatMethod")
        final ActionBar actionBar = getActionBar();

        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            actionBar.setIcon(R.drawable.trans);
        }

        // Create the adapter that will return a fragment for each of the
        // primary sections of the activity.
        mSectionsPagerAdapter = new PSectionsPagerAdapterMain(getFragmentManager(), this);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

                if (actionBar != null) {
                    actionBar.setSelectedNavigationItem(position);
                }

            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            //actionBar.addTab(actionBar.newTab().setIcon(R.drawable.icon_home).setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));

            if (actionBar != null) {
                switch (i) {
                    case 0:
                        actionBar.addTab(this.tabCreator(actionBar, R.drawable.icon_home));
                        break;
                    case 1:
                        actionBar.addTab(this.tabCreator(actionBar, R.drawable.icon_notifications));
                        break;
                    case 2:
                        actionBar.addTab(this.tabCreator(actionBar, R.drawable.icon_discover));
                        break;
                    case 3:
                        actionBar.addTab(this.tabCreator(actionBar, R.drawable.icon_profile));
                        break;
                    default:
                        PLog.logWarning(TAG, "Reached default case when building tabs, within switch on counter i.");
                        break;
                }
            }

        }
    }

    private ActionBar.Tab tabCreator(ActionBar actionBar, int resource) {
        ActionBar.Tab newTab = actionBar.newTab();
        newTab.setIcon(resource);
        newTab.setTabListener(this);
        return newTab;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = this.getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());


        /* ==== Suppressing application compatibility warning ====
         * Reason:  This application is fundamentally not compatible with API levels that would
         * require the support action bar by choice.  Therefore, we don't care that we aren't using
         * the support action bar.
         */
        @SuppressLint("AppCompatMethod")
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(this.mSectionsPagerAdapter.getPageTitle(tab.getPosition()));
        }

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        /* empty */
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        /* empty */
    }

    public void executeFetchNotifications(final int cursor, final int limit) {

        final PNotificationsAdapter notificationsAdapter = new PNotificationsAdapter(this, null, this.notificationsView);
        final Bundle params = new Bundle();
        params.putInt(PKeys.KEY_CURSOR, cursor);
        params.putInt(PKeys.KEY_LIMIT, limit);
        this.getLoaderManager().initLoader(PCallbackIdentifiers.FETCH_NOTIFICATIONS, params, this);

    }

    /**
     * This method handles a callback from a thread that was told to use this object as as it's
     * callback.  Most likely, this thread was also launched from this object.
     * @param callbackData is a PCallbackResult<T>.
     */
    public void threadCallback(PCallbackResult callbackData) {

        // Get the callback identifier and the data from the PCallbackResult<Boolean> parameter
        final int callbackIdentifier = callbackData.getIdentifier();

        try {
            // Switch on the identifier to determine what action we need to take.  This really only
            // needs to happen if there are multiple callbacks with the same return type being
            // handled by this controller.
            switch(callbackIdentifier) {

                case PCallbackIdentifiers.FETCH_NOTIFICATIONS:

                    /* ==== Suppressing unchecked cast warning ====
                     * Reason:  Since a single interface defines the callback method that all
                     * threads must use for their callbacks, we must define the type generically in
                     * the interface.  Therefore the result data contained within the
                     * PCallbackResult will be unknown at compile-time and therefore can't be
                     * checked.  We assume that results coming from the API interaction methods are
                     * correct, and that by switching on the callback identifier here we can
                     * guarantee that we will know what type we are dealing with.
                     */
                    @SuppressWarnings("unchecked")
                    final PResultSet<PUserActivity> callbackResultSet = (PResultSet<PUserActivity>) callbackData.getResultData();
                    ArrayList<PUserActivity> arrayResult = callbackResultSet.getResults();


                    // Check to see whether login was successful
                    if (arrayResult != null) {

                        final int size = arrayResult.size();
                        for (int i = 0; i < size; i++) {
                            PLog.logNotice(TAG, "Looping through the results " + i);
                            PUserActivity userActivity = arrayResult.get(i);
                            if (userActivity == null) {
                                PLog.logError(TAG, "The user activity " + i + " was null!");

                                PUserActivity result2 = arrayResult.get(i+1);
                                if (result2 == null) {
                                    PLog.logError(TAG, "The user activity " + i+1 + " was also null!");
                                }
                                else {
                                    PLog.logNotice(TAG, "The user activity " + (i+1) + " was not null, however.  In fact " + result2.getSourceUser().getUsername());
                                }
                            }
                            else {
                                PLog.logNotice(TAG, "The user activity " + i + " was not null!");
                            }

                            String message = null, userProfileImageURL = null;

                            if (userActivity != null) {
                                message = userActivity.getSubject();
                                userProfileImageURL = userActivity.getSourceUser().getProfile().getProfilePictureURL();
                            }

                            this.notificationsView.addNotification(userProfileImageURL, message);
                        }

                    }
                    else {
                        // Otherwise, display an error message in the current activity.
                        Toast.makeText(this.getBaseContext(), "There was a problem with those credentials.  Please try again!", Toast.LENGTH_LONG).show();
                    }
                    break;

                default:
                    throw new InvalidCallbackResultIdentifierException("An incorrect callback identifier was passed back to " + TAG + ".  The identifier was " + callbackIdentifier);
            }
        } catch (InvalidCallbackResultIdentifierException e) {
            PLog.logError(TAG, "Caught InvalidCallbackResultIdentifierException with message: " + e.getMessage());
        }

    }

    /**
     * Gets the home view that this controller controls.
     * @return a HomeFeedView.
     */
    public PHomeFeedView getHomeFeedView() {
        if (this.homeFeedView == null) {
            PLog.logDebug(TAG, "getNotificationsView() -> A notifications view did not exist.  Creating a new one.");
            this.homeFeedView = PHomeFeedView.newInstance(this);
        }
        return this.homeFeedView;
    }

    /**
     * Gets the notifications view that this controller controls.
     * @return a NotificationsView.
     */
    public PNotificationsView getNotificationsView() {
        if (this.notificationsView == null) {
            PLog.logDebug(TAG, "getNotificationsView() -> A notifications view did not exist.  Creating a new one.");
            this.notificationsView = PNotificationsView.newInstance(this);
        }
        return this.notificationsView;
    }

    @Override
    public Loader onCreateLoader(final int id, final Bundle args) {

        return null;
    }

    @Override
    public void onLoaderReset(Loader loader) {
        // reset the loader
    }

    @Override
    public void onLoadFinished(Loader<PResultSet> loader, PResultSet cursor) {

    }

}
