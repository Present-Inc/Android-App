package tv.present.android.util;

/**
 * Specifies the different types of log levels available for the Present application.
 *
 * Log levels are defined as follows:
 *  None    ->  No logging is performed
 *  Error   ->  Critical events that prevent further execution of the program
 *  Warning ->  Events that hinder the further execution of the program, but allow it to continue
 *  Notice  ->  Informative events that come as a result of the regular execution of the program
 *  Verbose ->  Anything and everything.  Good for debugging but watch out, the log file gets large!
 *  WTF     ->  Google says it stands for "What a Terrible Failure".  Rightttttttt...
 */
public enum PLogLevel {
    Error,
    None,
    Notice,
    Verbose,
    Warning,
    WTF
}
