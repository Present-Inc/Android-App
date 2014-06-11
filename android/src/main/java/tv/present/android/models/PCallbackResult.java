package tv.present.android.models;

/**
 * Present Callback Result
 * June 10, 2014
 * @author Kyle Weisel (kyle@present.tv)
 */
public final class PCallbackResult<T> {

    private int identifier;
    private T payload;

    /**
     * Constructs a PCallbackResult object.
     * @param object is a generic type of any object to set as the payload.
     */
    public PCallbackResult(final int identifier, final T object) {
        this.identifier = identifier;
        this.payload = object;
    }

    /**
     * Gets the payload.
     * @return the generic type payload object.
     */
    public T getResultData() {
        return this.payload;
    }

    /**
     * Gets the identifier of the result.
     * @return the identifier as an integer
     */
    public int getIdentifier() {
        return this.identifier;
    }

    /**
     * Sets the payload.
     * @param object is the object to set the payload to.
     */
    public void setResult(T object) {
        this.payload = object;
    }

}
