package tv.present.android.exceptions;

/**
 * The APIRequestPrereqException is generally thrown when the APIConnector has missing, invalid, or uninitialized
 * connection parameters (ie: the connector is not in the proper state to make a connection).
 *
 * @author Kyle Weisel (kyle@present.tv)
 */
public class InvalidCallbackResultIdentifierException extends Exception {

    private static final long serialVersionUID = -8547547830218965444L;

    public InvalidCallbackResultIdentifierException(String message) {
        super(message);
    }
}
