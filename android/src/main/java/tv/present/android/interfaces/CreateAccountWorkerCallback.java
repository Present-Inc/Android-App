package tv.present.android.interfaces;

import tv.present.models.PUser;

/**
 *  This interface defines the callback method used by the CreateAccountWorker.
 *  @author Kyle Weisel (kyle@present.tv)
 */
public interface CreateAccountWorkerCallback {
    public void callbackCreateAccount(PUser user);
}
