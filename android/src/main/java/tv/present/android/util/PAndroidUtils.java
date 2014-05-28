package tv.present.android.util;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Utilities class used with common utility functions.
 * @author Kyle Weisel (kyle@present.tv)
 */
public class PAndroidUtils {

    /**
     * Gets the version of the Android SDK version installed.
     * @return the integer version of the Android SDK, or -1 if unable to determine.
     */
    public int getAndroidSDKVersion() {
        int version = 0;
        try {
            version = Integer.valueOf(android.os.Build.VERSION.SDK);
        } catch (NumberFormatException e) {
            version = -1;
        }
        return version;
    }

    /**
     * Hides the soft keyboard.
     * @param activity is the calling Activity.
     */
    public static void hideKeyboardInActivity(Activity activity) {
        final View currentView = activity.getCurrentFocus();
        if (currentView != null) {
            final InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(currentView.getWindowToken(), 0);
        }
    }

}
