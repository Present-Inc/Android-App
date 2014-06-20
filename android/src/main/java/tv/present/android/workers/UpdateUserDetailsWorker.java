package tv.present.android.workers;

import android.os.AsyncTask;

import tv.present.android.interfaces.UpdateUserDetailsWorkerCallback;
import tv.present.android.models.PApplicationCore;
import tv.present.api.PAPIInteraction;
import tv.present.models.PUserContext;
import tv.present.util.PUtilities;

/**
 * A working thread that performs updates to user details on a background thread.
 */
public class UpdateUserDetailsWorker extends AsyncTask<String, Void, Boolean> {

    private UpdateUserDetailsWorkerCallback updateUserDetailsWorkerCallback;

    public UpdateUserDetailsWorker(UpdateUserDetailsWorkerCallback callback) {
        this.updateUserDetailsWorkerCallback = callback;
    }

    // Inovked on UI before thread is executed
    @Override
    public void onPreExecute() {
        // Do nothing
    }

    @Override
    public Boolean doInBackground(String ... params) {

        final String fullName = params[0];
        final String description = params[1];
        final String gender = params[2];
        final String location = params[3];
        final String websiteURL = params[4];
        final String emailAddress = params[5];
        final String phoneNumber = params[6];

        PApplicationCore appCore = PApplicationCore.getInstance();
        PUserContext userContext = appCore.getUserContext();
        PAPIInteraction apiInteraction = new PAPIInteraction();

        boolean retVal = apiInteraction.updateUserDetails(userContext, fullName, description, PUtilities.stringToGender(gender), location, websiteURL, emailAddress, phoneNumber);
        // Note: is APIClientUtilities the right place for stringToGender()?

        return retVal;
    }

    // Will execute on UI thread:
    //  Check whether the boolean was true or false
    //  Advance the UI to the main screen on a successful login
    @Override
    public void onPostExecute(Boolean successfulUpdate) {
        this.updateUserDetailsWorkerCallback.callbackUpdateUserDetails(successfulUpdate);
    }

}
