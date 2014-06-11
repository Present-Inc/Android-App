package tv.present.android.util;

/**
 * Present Observer Update Type Enumeration
 *
 * This enumeration defines the types of updates that can be broadcast by subjects to their
 * observers.
 *
 * June 11, 2014
 *
 * @author  Kyle Weisel (kyle@present.tv)
 */
public enum PActionIdentifier {
    CREATE_ACCOUNT,
    FETCH_NOTIFICATIONS,
    LOGIN_ACCOUNT,
    LOGIN_ACCOUNT_AFTER_CREATION
}
