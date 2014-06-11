package tv.present.android.interfaces;

import tv.present.android.util.PActionIdentifier;

/**
 * Present View Observer Interface
 *
 * This interface should be implemented by views so that they can respond to updates sent out by
 * controllers.
 *
 * June 11, 2014
 *
 * @author  Kyle Weisel (kyle@present.tv)
 */
public interface PViewObserver {
    public void updateView(PActionIdentifier updateType);
}
