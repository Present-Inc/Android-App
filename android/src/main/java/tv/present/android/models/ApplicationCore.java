package tv.present.android.models;

import java.io.Serializable;

import tv.present.models.UserContext;

/**
 *  Maintains the state of the application.  Most importantly, this class stores the logged in user
 *  context for reference later on.
 *
 *  @author  Kyle Weisel (kyle@present.tv)
 *
 */
public final class ApplicationCore implements Serializable {

    private static volatile ApplicationCore instance;
    private UserContext userContext;

    /**
     * Gets the one (and only) instance of the application core.
     * @return  the ApplicationCore object.
     */
    public static ApplicationCore getInstance() {

        if(ApplicationCore.instance == null) {
            synchronized(ApplicationCore.class) {
                if (ApplicationCore.instance == null) {
                    ApplicationCore.instance = new ApplicationCore();
                }
            }
        }

        return ApplicationCore.instance;
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
     * Instantiates the ApplicationModel.  This constructor is private, and therefore this object
     * needs to be acquired through the public static getInstance() method.
     */
    private ApplicationCore() {
        this.userContext = null;
    }

    public UserContext getUserContext() {
        return this.userContext;
    }

    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
    }
}
