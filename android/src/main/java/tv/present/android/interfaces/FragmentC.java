package tv.present.android.interfaces;

import android.app.Fragment;

/**
 *  The abstract Controller class defines Fragment that is controlled by a Controller.
 *  @author Kyle Weisel (kyle@present.tv)
 */
public abstract class FragmentC extends Fragment {

    protected Controller controller;

    /**
     * Gets the controller for this fragment.
     * @return
     */
    public Controller getController() {
        return this.controller;
    }

}
