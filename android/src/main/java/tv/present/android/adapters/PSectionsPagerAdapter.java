package tv.present.android.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import tv.present.android.R;
import tv.present.android.controllers.PController;
import tv.present.android.views.HomeFeedView;
import tv.present.android.views.NotificationsView;
import tv.present.android.views.PPlaceholderView;

/**
 * Present Sections Pager Adapter
 *
 * This {@link android.support.v13.app.FragmentPagerAdapter} returns a fragment that corresponds
 * to a given tab.
 *
 * June 10, 2014
 *
 * @author  Kyle Weisel (kyle@present.tv)
 */
public class PSectionsPagerAdapter extends FragmentPagerAdapter {

    private final PController controller;

    public PSectionsPagerAdapter(FragmentManager fragmentManager, PController controller) {
        super(fragmentManager);
        this.controller = controller;
    }

    @Override
    public Fragment getItem(int position) {

        final Fragment returnValue;

        // Switch on the tab position that we need a fragment for.
        switch (position) {
            case 0:
                returnValue = HomeFeedView.newInstance(this.controller);
                break;
            case 1:
                returnValue = NotificationsView.newInstance(this.controller);
                break;
            default:
                returnValue = PPlaceholderView.newInstance(position + 1);
                break;
        }

        return returnValue;


    }

    @Override
    public int getCount() {
        // Show 4 total pages.
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return this.controller.getString(R.string.title_section_1);
            case 1:
                return this.controller.getString(R.string.title_section_2);
            case 2:
                return this.controller.getString(R.string.title_section_3);
            case 3:
                return this.controller.getString(R.string.title_section_4);
        }
        return null;
    }

}