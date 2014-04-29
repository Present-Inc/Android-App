package tv.present.android.mediastreamer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

import tv.present.android.util.PLog;

/**
 * Implements a simple streaming server using NanoHTTPD as a base.
 */
public class StreamingServer extends NanoHTTPD {

    private static String TAG = "tv.present.android.mediastreamer.StreamServer";
    private OnRequestListen mOnRequestListen = null;
    private File homeDir;
    private Response streamResponse = null;

    public static interface OnRequestListen {
        public abstract InputStream onRequest();
        public abstract void requestDone();
    }

    public StreamingServer(int port, String root) throws IOException {
        super(port, new File(root).getAbsoluteFile() );
        homeDir = new File(root);
    }

    public void setOnRequestListen(OnRequestListen onRequestListen) {
        mOnRequestListen = onRequestListen;
    }

    public Response serve( String uri, String method, Properties header, Properties parms, Properties files ) {
        PLog.logDebug(this.TAG, "Serve method: " + method + " - URI: " + uri + ".");

        if (uri.equalsIgnoreCase("/live.mp4") ) {

            Response response  = new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "Error 404 - file not found.");

            if (this.mOnRequestListen == null) {
                return response;
            } 
            else {
                
                InputStream inputStream  = this.mOnRequestListen.onRequest();
                
                if (inputStream == null) {
                    return response;
                }

                if (this.streamResponse == null ) {
                    Random random = new Random();
                    String etag = Integer.toHexString(random.nextInt());
                    response = new Response( HTTP_OK, "video/mp4", inputStream);
                    response.addHeader( "Connection", "Keep-alive");
                    response.addHeader( "ETag", etag);
                    response.isStreaming = true;
                    this.streamResponse = response;
                }

                return response;

            }

        }
        else {
            return serveFile( uri, header, homeDir, true );
        }
    }

    public void serveDone(Response response) {
        if (response.mimeType.equalsIgnoreCase("video/mp4") && response == this.streamResponse) {
            if (this.mOnRequestListen != null) {
                this.mOnRequestListen.requestDone();
                this.streamResponse = null;
            }
        }
    }
}
