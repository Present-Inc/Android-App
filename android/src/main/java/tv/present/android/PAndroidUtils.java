package tv.present.android;

/**
 * Created by kbw28 on 4/25/14.
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

}
