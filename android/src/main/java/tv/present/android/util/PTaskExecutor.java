package tv.present.android.util;

import tv.present.android.models.PApplicationCore;
import tv.present.api.PAPIInteraction;
import tv.present.models.PUserContext;
import tv.present.util.PUtilities;

/**
 * Created by kbw28 on 6/10/14.
 */
public class PTaskExecutor {

    private static final String TAG = "tv.present.android.util.PTaskExecutor";

    public Boolean doLogin(String username, String password) {

        PApplicationCore appCore = PApplicationCore.getInstance();
        PUserContext userContext;

        // Check if for some reason we have a user context
        if (appCore.getUserContext() != null) {
            userContext = appCore.getUserContext();
        }
        // Otherwise, try to get one (ie: do the login)
        else {
            // Create a new API interaction, and request a user context
            PAPIInteraction apiInteraction = new PAPIInteraction();
            PLog.logDebug(TAG, "doLogin() -> Trying to get user context with username " + username + " and password " + password);
            userContext = apiInteraction.getUserContext(username, password);
            PLog.logDebug(TAG, "doLogin() -> After the request the user context is " + (userContext == null ? "null" : "not null") + ".");
        }

        // If the userContext came back not null, set it in the app core.
        if (userContext != null)  {
            appCore.setUserContext(userContext);
            return true;
        }
        else {
            return false;
        }

    }

    public Boolean doUpdateUserDetails(String fullName, String description, String gender, String location, String websiteURL, String emailAddress, String phoneNumber) {

        PApplicationCore appCore = PApplicationCore.getInstance();
        PUserContext userContext = appCore.getUserContext();
        PAPIInteraction apiInteraction = new PAPIInteraction();

        if (userContext == null) {
            PLog.logWarning(TAG, "User context was null in a doUpdateUserDetails() call!");
            return false;
        }

        boolean retVal = apiInteraction.updateUserDetails(userContext, fullName, description, PUtilities.stringToGender(gender), location, websiteURL, emailAddress, phoneNumber);
        // Note: is APIClientUtilities the right place for stringToGender()?

        return retVal;
    }
}
