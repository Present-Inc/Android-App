package tv.present.android.interfaces;

import android.app.Fragment;

/**
 *
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
