package tv.present.android.adapters;

import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import tv.present.android.controllers.PController;

/**
 * Present Sections Pager Adapter
 *
 * This {@link android.support.v13.app.FragmentPagerAdapter} is an abstract class for
 * PSectionsPagerAdapters.
 *
 * June 10, 2014
 *
 * @author  Kyle Weisel (kyle@present.tv)
 */
public abstract class PSectionsPagerAdapter extends FragmentPagerAdapter {

    protected final PController controller;

    public PSectionsPagerAdapter(FragmentManager fragmentManager, PController controller) {
        super(fragmentManager);
        this.controller = controller;
    }

}