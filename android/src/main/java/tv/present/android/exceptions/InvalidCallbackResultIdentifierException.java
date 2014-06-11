package tv.present.android.exceptions;

/**
 * Present Invalid Callback Result Identifier Exception
 *
 * The InvalidCallbackResultIdentifierException is generally thrown when the callback identifier
 * from a response does not match one of the expected identifiers (ie: the object that implements
 * the ThreadCallback interface does not check for the right callback identifier).
 *
 * June 10, 2014
 *
 * @author  Kyle Weisel (kyle@present.tv)
 */
public class InvalidCallbackResultIdentifierException extends Exception {

    private static final long serialVersionUID = -8547547830218965444L;

    public InvalidCallbackResultIdentifierException(String message) {
        super(message);
    }
}
