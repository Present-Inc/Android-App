package tv.present.android.models;

import tv.present.models.UserContext;

/**
 *  Maintains the state of the application.
 */
public class ApplicationModel {

    private static volatile ApplicationModel instance;
    private UserContext userContext;

    /**
     * Gets the one (and only) instance of the model.
     * Note - the is an issue with the synchronized call at this time (05/28/2014)
     * @return  the model object.
     */
    public static ApplicationModel getInstance() {

        if(ApplicationModel.instance == null) {
            //synchronized(PAndroidLogger.getInstance()) {
            if(ApplicationModel.instance == null) {
                ApplicationModel.instance = new ApplicationModel();
            }
            //}
        }

        return ApplicationModel.instance;
    }

    /**
     * Invalidates this model (which should only be living while the application is running).
     * @return true if the model has been invalidated, false otherwise.
     */
    public boolean invalidate() {

        this.userContext = null;
        this.instance = null;

        if (this.userContext == null && this.instance == null) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Instantiates the ApplicationModel.
     */
    private ApplicationModel() {
        /* Empty private constructor.  This model is a singleton and needs to be acquired using the static getInstance() method. */
    }

    public UserContext getUserContext() {
        return this.userContext;
    }

    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
    }
}
