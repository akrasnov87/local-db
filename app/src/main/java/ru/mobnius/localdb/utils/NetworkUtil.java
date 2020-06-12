package ru.mobnius.localdb.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class NetworkUtil {
    /**
     * Get IP address from first non-localhost interface
     * @return  address or empty string
     */
    public static String getIPv4Address() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                List<InetAddress> addresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress address : addresses) {
                    if (!address.isLoopbackAddress()) {
                        String s = address.getHostAddress();

                        boolean isIPv4 = s.indexOf(':') < 0;

                        if (isIPv4)
                            return s;
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }
}
