package tv.present.android.util;
import android.os.Environment;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Wraps the Android Logger so that we can direct logs to multiple sources, and for flexibility in
 * future revisions of this application.  This is a singleton class and can only ever be
 * instantiated once through the getInstance() method.
 *
 *  @author     Kyle Weisel (kyle@present.tv)
 *
 */
public final class PLog {

    private final String TAG = "tv.present.android.util.PAndroidLogger.java";
    private static volatile PLog instance = null;
    private final PLogLevel logLevel;
    private final String logFile;
    private final String logDirectory;

    /**
     * Initializes the PAndroidLogger
     * @param logLevel  the log level that the logger will operate at as a LogLevelAndroid
     * @param logFile   the filename (with extension) to write to as a String
     */
    private PLog(PLogLevel logLevel, String logDirectory, String logFile) {
        this.logLevel = logLevel;
        this.logDirectory = logDirectory;
        this.logFile = logFile;
    }

    /**
     * Writes a message to the custom log file on the SD card.
     * @param   message     the message to write to the log file as a string
     * @return  void
     *
     */
    private void writeToLog(String message) {

        String SDRootDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        String logDirectory = SDRootDirectory + "/" + this.logDirectory;
        File logFile = new File(logDirectory, this.logFile);

        Date today = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String incidentDate = dateFormat.format(today);

        if (!logFile.exists()) {
            try {
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            }
            catch (IOException e) {
                Log.e(this.TAG, "Error:  Unable to create custom log file at location " + this.logDirectory + "/" + this.logFile + ".");
                e.printStackTrace();
            }
        }

        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
            writer.append(incidentDate + "\t" + message);
            writer.newLine();
            writer.close();
        }
        catch (IOException e) {
            Log.e(this.TAG, "Error:  Unable to write to custom log file at location " + this.logDirectory + "/" + this.logFile + ".");
            e.printStackTrace();
        }

    }

    private void sendLogAttachment(String emailAddress) {

        // Send an email to the address specified.

    }

    /**
     * Gets the one (and only) instance of the logger.
     * Note - the is an issue with the synchronized call at this time (03/06/2014)
     * @return  the logger object
     */
    public static PLog getInstance() {

        if(PLog.instance == null) {
            //synchronized(PAndroidLogger.getInstance()) {
            if(PLog.instance == null) {
                String logFileName = PAndroidGlobals.LOG_FILENAME + PAndroidGlobals.LOG_FILE_EXTENSION;
                PLog.instance = new PLog(PAndroidGlobals.LOG_LEVEL, PAndroidGlobals.LOG_DIRECTORY, logFileName);
            }
            //}
        }

        return PLog.instance;
    }

    public static int logError(String tag, String message) {
        PLog logger = PLog.getInstance();
        logger.writeToLog("Error:\tSource: " + tag + "\tMessage:" + message + "\t");
        return Log.e(tag, message);
    }

    public static int logError(String tag, String message, Throwable throwable) {
        PLog logger = PLog.getInstance();
        logger.writeToLog("Error:\tSource: " + tag + "\tMessage:" + message + "\t");
        return Log.e(tag, message, throwable);
    }

    public static int logWarning(String tag, String message) {
        PLog logger = PLog.getInstance();
        logger.writeToLog("Warning:\tSource: " + tag + "\tMessage:" + message + "\t");
        return Log.w(tag, message);
    }

    public static int logWarning(String tag, String message, Throwable throwable) {
        PLog logger = PLog.getInstance();
        logger.writeToLog("Warning:\tSource: " + tag + "\tMessage:" + message + "\t");
        return Log.w(tag, message, throwable);
    }

    public static int logNotice(String tag, String message) {
        PLog logger = PLog.getInstance();
        logger.writeToLog("Notice:\tSource: " + tag + "\tMessage:" + message + "\t");
        return Log.i(tag, message);
    }

    public static int logNotice(String tag, String message, Throwable throwable) {
        PLog logger = PLog.getInstance();
        logger.writeToLog("Notice:\tSource: " + tag + "\tMessage:" + message + "\t");
        return Log.i(tag, message, throwable);
    }

    public static int logVerbose(String tag, String message) {
        PLog logger = PLog.getInstance();
        logger.writeToLog("Verbose:\tSource: " + tag + "\tMessage:" + message + "\t");
        return Log.v(tag, message);
    }

    public static int logVerbose(String tag, String message, Throwable throwable) {
        PLog logger = PLog.getInstance();
        logger.writeToLog("Verbose:\tSource: " + tag + "\tMessage:" + message + "\t");
        return Log.v(tag, message, throwable);
    }

    public static int logDebug(String tag, String message) {
        PLog logger = PLog.getInstance();
        logger.writeToLog("Debug:\tSource: " + tag + "\tMessage:" + message + "\t");
        return Log.d(tag, message);
    }

    public int logDebug(String tag, String message, Throwable throwable) {
        PLog logger = PLog.getInstance();
        logger.writeToLog("Debug:\tSource: " + tag + "\tMessage:" + message + "\t");
        return Log.d(tag, message, throwable);
    }

}