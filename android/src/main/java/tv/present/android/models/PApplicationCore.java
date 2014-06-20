package tv.present.android.models;

import java.io.Serializable;

import tv.present.models.PUserContext;

/**
 *  Maintains the state of the application.  Most importantly, this class stores the logged in user
 *  context for reference later on.
 *
 *  @author  Kyle Weisel (kyle@present.tv)
 *
 */
public final class PApplicationCore implements Serializable {

    private static volatile PApplicationCore instance;
    private PUserContext userContext;

    /**
     * Gets the one (and only) instance of the application core.
     * @return  the ApplicationCore object.
     */
    public static PApplicationCore getInstance() {

        if(PApplicationCore.instance == null) {
            synchronized(PApplicationCore.class) {
                if (PApplicationCore.instance == null) {
                    PApplicationCore.instance = new PApplicationCore();
                }
            }
        }

        return PApplicationCore.instance;
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
    private PApplicationCore() {
        this.userContext = null;
    }

    public PUserContext getUserContext() {
        return this.userContext;
    }

    public void setUserContext(PUserContext userContext) {
        this.userContext = userContext;
    }

}
