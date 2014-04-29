package tv.present.android.mediastreamer;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import tv.present.android.util.PLog;

/**
 * The
 */
public class StreamLoop {

    private static String TAG = "tv.present.android.mediastreamer.StreamLoop";
    private PLog logger = PLog.getInstance();
    private static int BUFFER_SIZE_BYTES = 1024;
    private LocalSocket socketReceiver, socketSender;
    private LocalServerSocket localServerSocket;
    private String localAddress;

    public StreamLoop (String address) {
        PLog.logDebug(this.TAG, "Using local address: " + address);
        this.localAddress = address;
        try {
            this.localServerSocket = new LocalServerSocket(this.localAddress);
        } catch (IOException e) {
            PLog.logWarning(this.TAG, "Caught IOException in constructor.  Details: " + e.toString());
            e.printStackTrace();
        }
    }

    public InputStream getInputStream() throws IOException {
        return this.socketReceiver.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return this.socketSender.getOutputStream();
    }

    public FileDescriptor getSocketReceiverFileDescriptor() {
        return this.socketReceiver.getFileDescriptor();
    }

    public FileDescriptor getSocketSenderFileDescriptor() {
        return this.socketSender.getFileDescriptor();
    }

    public boolean initLoop() {
        this.socketReceiver = new LocalSocket();
        try {
            this.socketReceiver.connect(new LocalSocketAddress(this.localAddress));
            this.socketReceiver.setReceiveBufferSize(StreamLoop.BUFFER_SIZE_BYTES);
            this.socketReceiver.setSendBufferSize(StreamLoop.BUFFER_SIZE_BYTES);
            this.socketSender = this.localServerSocket.accept();
            this.socketSender.setReceiveBufferSize(StreamLoop.BUFFER_SIZE_BYTES);
            this.socketSender.setSendBufferSize(StreamLoop.BUFFER_SIZE_BYTES);
        } catch (IOException e) {
            PLog.logWarning(this.TAG, "Caught IOException in initLoop().  Details: " + e.toString());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean releaseLoop() {
        try {
            if (this.socketReceiver != null) {
                this.socketReceiver.close();
            }
            if (this.socketSender != null) {
                this.socketSender.close();
            }
        } catch (IOException e) {
            PLog.logWarning(this.TAG, "Caught IOException in releaseLoop().  Details: " + e.toString());
            e.printStackTrace();
            return false;
        }

        this.socketSender = null;
        this.socketReceiver = null;

        return true;
    }

}
