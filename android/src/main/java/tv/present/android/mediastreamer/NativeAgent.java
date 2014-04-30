package tv.present.android.mediastreamer;

import java.io.FileDescriptor;

import tv.present.android.util.PLog;

/**
 * Created by kbw28 on 4/30/14.
 */
public class NativeAgent {

    public static final String TAG = "tv.present.android.mediastreamer.NativeAgent";

    public NativeAgent() {
        /* empty constructor */
    }

    private static native int nativeCheckMedia(int wid, int hei, String fileName);

    public static boolean NativeCheckMedia(int wid, int hei, String filename) {
        PLog.logDebug(NativeAgent.TAG, "Call native function: checkmedia");
        if (nativeCheckMedia(wid, hei, filename) > 0)
            return true;
        else
            return false;
    }

    static private native int nativeStartStreamingMedia(FileDescriptor in, FileDescriptor out);
    static public void NativeStartStreamingMedia(FileDescriptor in, FileDescriptor out) {
        nativeStartStreamingMedia(in, out);
    }
    static private native void nativeStopStreamingMedia();
    static public void NativeStopStreamingMedia() {
        nativeStopStreamingMedia();
    }

    public static void LoadLibraries() {
        //Local library .so files before this activity created.
        System.loadLibrary("teaonly");
        System.loadLibrary("ipcamera");
    }


}
