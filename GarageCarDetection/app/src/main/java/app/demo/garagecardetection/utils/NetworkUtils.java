
package app.demo.garagecardetection.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

public class NetworkUtils {


    /**
     * Indicates whether the device is online.
     *
     * @return
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if ((cm.getActiveNetworkInfo() != null) && (cm.getActiveNetworkInfo().isAvailable())
                && (cm.getActiveNetworkInfo().isConnected())) {
            return true;
        }
        return false;
    }

    public static boolean isWifiEnabled(Context context) {
        WifiManager mng = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return mng.isWifiEnabled();
    }
}