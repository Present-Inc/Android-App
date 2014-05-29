package tv.present.android.interfaces;

import android.app.Fragment;

/**
 * Created by kbw28 on 5/29/14.
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
