package tv.present.android.mediastreamer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import tv.present.android.util.PLog;

/**
 * This class returns data regarding the state of the network on the phone.
 */
public class NetworkInfoAdapter {

    private static String TAG = "tv.present.android.mediastreamer.NetworkInfoAdapter";
    private static Map<String, String> infoMap = new HashMap<String, String>();
    private static Map<Integer, String> phoneType = new HashMap<Integer, String>();
    private static Map<Integer, String> networkType = new HashMap<Integer, String>();

    private static final String UNKNOWN_TYPE = "unknown";

    static {
        phoneType.put(0, "None");
        phoneType.put(1, "GSM");
        phoneType.put(2, "CDMA");
        networkType.put(0, "Unknown");
        networkType.put(1, "GPRS");
        networkType.put(2, "EDGE");
        networkType.put(3, "UMTS");
        networkType.put(4, "CDMA");
        networkType.put(5, "EVDO_0");
        networkType.put(6, "EVDO_A");
        networkType.put(7, "1xRTT");
        networkType.put(8, "HSDPA");
        networkType.put(9, "HSUPA");
        networkType.put(10, "HSPA");
        networkType.put(11, "IDEN");
        infoMap.put("Cell", "false");
        infoMap.put("Mobile", "false");
        infoMap.put("Wi-Fi", "false");
    }

    public static void update(Context context) {
        infoMap.put("Cell", "false");
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            infoMap.put("Cell", "true");
            if (telephonyManager.getCellLocation() != null) {
                infoMap.put("Cell location", telephonyManager.getCellLocation().toString());
            }
            infoMap.put("Cell type", getPhoneType(telephonyManager.getPhoneType()));
        }
        infoMap.put("Mobile", "false");
        infoMap.put("Wi-Fi", "false");
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = (NetworkInfo) connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            NetworkInterface networkInterface = getInternetInterface();
            infoMap.put("IP", getIPAddress(networkInterface));
            String type = (String) networkInfo.getTypeName();
            if (type.equalsIgnoreCase("mobile")) {
                infoMap.put("Mobile", "true");
                infoMap.put("Mobile type", getNetworkType(telephonyManager.getNetworkType()));
                infoMap.put("Signal", "Good!");
            } else if (wifiManager.isWifiEnabled()) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                infoMap.put("Wi-Fi", "true");
                infoMap.put("SSID", wifiInfo.getSSID());
                infoMap.put("Signal", "Good!");
            }
        }
    }

    public static String getInfo(String key) {
        return infoMap.containsKey(key) ? infoMap.get(key) : "";
    }

    private static String getPhoneType(Integer key) {
        if (phoneType.containsKey(key)) {
            return phoneType.get(key);
        }
        else {
            return NetworkInfoAdapter.UNKNOWN_TYPE;
        }
    }

    private static String getNetworkType(Integer key) {
        if (networkType.containsKey(key)) {
            return networkType.get(key);
        }
        else {
            return NetworkInfoAdapter.UNKNOWN_TYPE;
        }
    }

    private static String getIPAddress(NetworkInterface networkInterface) {
        String result = "";
        for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
            InetAddress inetAddress = enumIpAddr.nextElement();
            result = inetAddress.getHostAddress();
        }
        return result;
    }

    private static NetworkInterface getInternetInterface() {
        try {
            for (Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces(); enumeration.hasMoreElements(); ) {
                NetworkInterface networkInterface = enumeration.nextElement();
                if (!networkInterface.equals(NetworkInterface.getByName("lo"))) {
                    return networkInterface;
                }
            }
        } catch (SocketException e) {
            PLog.logWarning(NetworkInfoAdapter.TAG, "Caught SocketException in getInternetInterface().  Details: " + e.toString());
            return null;
        }
        return null;
    }

}