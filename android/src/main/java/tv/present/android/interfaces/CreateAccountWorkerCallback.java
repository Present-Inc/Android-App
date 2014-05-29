package tv.present.android.interfaces;

import tv.present.models.User;

/**
 * This interface defines the callback method used by the create account worker.
 */
public interface CreateAccountWorkerCallback {
    public void callbackCreateAccount(User user);
}
