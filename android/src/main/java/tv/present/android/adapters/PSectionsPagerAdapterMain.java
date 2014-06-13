package tv.present.android.adapters;

import android.app.Fragment;
import android.app.FragmentManager;

import tv.present.android.R;
import tv.present.android.controllers.CoreController;
import tv.present.android.controllers.PController;
import tv.present.android.views.HomeFeedView;
import tv.present.android.views.PPlaceholderView;

/**
 * Present MainSections Pager Adapter
 *
 * This {@link tv.present.android.adapters.PSectionsPagerAdapter} is the adapter that controls the
 * creation of views for the main tabbed interface.
 *
 * June 10, 2014
 *
 * @author  Kyle Weisel (kyle@present.tv)
 */
public final class PSectionsPagerAdapterMain extends PSectionsPagerAdapter {

    public PSectionsPagerAdapterMain(FragmentManager fragmentManager, PController controller) {
        super(fragmentManager, controller);
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
                returnValue = ((CoreController) this.controller).getNotificationsView();
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
