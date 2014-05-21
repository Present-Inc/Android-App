package tv.present.android;

import android.os.Environment;

/**
 * Defines global values and variables used throughout the application.
 * @author      Kyle Weisel (kyle@present.tv)
 * @version     1.0
 * @since       1.0
 */
public final class PAndroidGlobals {

    /**
     * Private constructor prevents instantiating this object.
     */
    private PAndroidGlobals() {
    }

    /**
     * Log levels:
     * NONE - no logs will be generated
     * ERROR - only critical faults will be logged
     * WARNING - critical errors and noncritical warnings will be logged
     * NOTICE - errors, warnings, and notices will be logged (good for debugging)
     * VERBOSE - everything possible will be logged (not fully implemented yet)
     * WTF - errors that should never happen
     */
    public static final PLogLevel LOG_LEVEL = PLogLevel.None;

    public static boolean CUSTOM_LOGGING = true;

    /**
     * Directory to store the log file.  Don't use leading or trailing slashes unless you are going
     * multiple directories deep.  For example, to use directory SDCard/Present, simply use
     * "Present" below.  To use directory {SDCard}/Present/Logs, you would put "Present/Logs" below.
     */
    public static final String LOG_DIRECTORY = "Present";

    /**
     * The name of the file to write log messages to, without an extension (that's below).  Please
     * note that this may become a prefix in the future if multiple log files would come about.
     */
    public static final String LOG_FILENAME = "debug";

    /**
     * The extension of the log file.  Linux based systems do not require an extension, and
     * therefore, this setting can be left blank if you desire.
     */
    public static final String LOG_FILE_EXTENSION = ".txt";

    /**
     * Key for Present API
     */
    public static final String API_CLIENT_KEY = "VzAtQ1Lc8hkoC0zABoTGdEwH5sffDWmHQ1wNxxxz";     // Production key
    // public static final String API_CLIENT_KEY = "9NGWHMPPU1GLXmRmhD9X7lkF5WmdNC0dRmnD5kla";  // Development key

    /**
     * Splash screen delay (number of seconds)
     */
    public static final int SPLASH_DELAY = 2;

    public static final String SD_ROOT_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath();

    public static final String TEMP_DIRECTORY = PAndroidGlobals.SD_ROOT_DIRECTORY + "/Present";



}


