package tv.present.android.mediacore;

/**
 * Created by kbw28 on 5/13/14.
 */

import android.opengl.EGLContext;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

import tv.present.android.util.PLog;

/**
 * Handles encoder state change requests.  The handler is created on the encoder thread.
 */
public class EncoderHandler extends Handler {

    private static final String TAG ="tv.present.android.mediacore.EncoderHandler";
    private WeakReference<TextureMovieEncoder> mWeakEncoder;

    public EncoderHandler(TextureMovieEncoder encoder) {
        mWeakEncoder = new WeakReference<TextureMovieEncoder>(encoder);
    }

    @Override  // runs on encoder thread
    public void handleMessage(Message inputMessage) {
        int what = inputMessage.what;
        Object obj = inputMessage.obj;

        TextureMovieEncoder encoder = mWeakEncoder.get();
        if (encoder == null) {
            PLog.logWarning(this.TAG, "EncoderHandler.handleMessage: encoder is null");
            return;
        }

        switch (what) {
            case TextureMovieEncoder.MSG_START_RECORDING:
                encoder.handleStartRecording((EncoderConfig) obj);
                break;
            case TextureMovieEncoder.MSG_STOP_RECORDING:
                encoder.handleStopRecording();
                break;
            case TextureMovieEncoder.MSG_FRAME_AVAILABLE:
                long timestamp = (((long) inputMessage.arg1) << 32) |
                        (((long) inputMessage.arg2) & 0xffffffffL);
                encoder.handleFrameAvailable((float[]) obj, timestamp);
                break;
            case TextureMovieEncoder.MSG_SET_TEXTURE_ID:
                encoder.handleSetTexture(inputMessage.arg1);
                break;
            case TextureMovieEncoder.MSG_UPDATE_SHARED_CONTEXT:
                encoder.handleUpdateSharedContext((EGLContext) inputMessage.obj);
                break;
            case TextureMovieEncoder.MSG_QUIT:
                Looper.myLooper().quit();
                break;
            default:
                throw new RuntimeException("Unhandled msg what=" + what);
        }
    }
}
