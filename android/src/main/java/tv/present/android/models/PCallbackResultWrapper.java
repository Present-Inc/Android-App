package tv.present.android.models;

/**
 * Present Callback Result Wrapper
 *
 * This object wraps a PCallbackResult.
 *
 * June 10, 2014
 * @author Kyle Weisel (kyle@present.tv)
 *
 */
public class PCallbackResultWrapper<T> {

    private PCallbackResult<T> callbackResult;

    public PCallbackResultWrapper(PCallbackResult callbackResult) {
        this.callbackResult = callbackResult;
    }

    public int getCallbackIdentifier() {
        return this.callbackResult.getIdentifier();
    }
    public PCallbackResult getCallbackResultObject() {
        return this.callbackResult;
    }

}
