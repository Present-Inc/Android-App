package tv.present.android.controllers;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import tv.present.android.R;
import tv.present.android.adapters.PSectionsPagerAdapter;
import tv.present.android.exceptions.InvalidCallbackResultIdentifierException;
import tv.present.android.interfaces.ThreadCallback;
import tv.present.android.models.PCallbackResult;
import tv.present.android.models.PCallbackResultWrapper;
import tv.present.android.threads.FetchNotificationsThread;
import tv.present.android.util.PCallbackIdentifiers;
import tv.present.android.util.PLog;
import tv.present.models.PUser;
import tv.present.util.PResultSet;

/**
 * Created by kbw28 on 6/10/14.
 */
public class CoreController extends PController implements ActionBar.TabListener, ThreadCallback {

    private static final String TAG = "tv.present.android.controllers.CoreController";

    // Provides fragments for each of the sections
    PSectionsPagerAdapter mSectionsPagerAdapter;

    // View pager to host the section contents
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // This app is not compatible with API levels that require the SupportActionBar, therefore,
        // we will not use it.
        @SuppressLint("AppCompatMethod")
        final ActionBar actionBar = getActionBar();

        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            actionBar.setIcon(R.drawable.trans);
        }

        // Create the adapter that will return a fragment for each of the
        // primary sections of the activity.
        mSectionsPagerAdapter = new PSectionsPagerAdapter(getFragmentManager(), this);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            //actionBar.addTab(actionBar.newTab().setIcon(R.drawable.icon_home).setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));

            switch(i) {
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

    private ActionBar.Tab tabCreator(ActionBar actionBar, int resource) {
        ActionBar.Tab newTab = actionBar.newTab();
        if(newTab != null) {
            newTab.setIcon(resource);
            newTab.setTabListener(this);
        }
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());

        // This app is not compatible with API levels that require the SupportActionBar, therefore,
        // we will not use it.
        @SuppressLint("AppCompatMethod")
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(this.mSectionsPagerAdapter.getPageTitle(tab.getPosition()));
        }

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public void executeFetchNotifications(final int cursor, final int limit) {
        FetchNotificationsThread notificationsThread = new FetchNotificationsThread(PCallbackIdentifiers.FETCH_NOTIFICATIONS, this);
        int[] temp = new int[2];
        temp[0] = cursor;
        temp[1] = limit;
        notificationsThread.execute(cursor, limit);
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


                    final PResultSet<?> callbackResult = (PResultSet<PUser>) callbackData.getResultData();

                    final PResultSet rs = callbackResult;
                    ArrayList<PUser> alpu = rs.getResults();


                    // Check to see whether login was successful
                    if (callbackResult) {
                        // Start the main activity if the result is true (ie: the login was
                        // successful, and the context was stored in the application core).
                        Intent intent = new Intent(this, CoreController.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        this.startActivity(intent);
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




}
