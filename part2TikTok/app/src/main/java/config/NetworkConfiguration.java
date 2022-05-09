package config;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class NetworkConfiguration {
    public static final int seed = Math.abs((new Random().nextInt())%30000);

    public static final String INITIAL_BROKER_IP = "192.168.2.17";
    public static final int INITIAL_BROKER_PORT = 14321;

    public static String INITIAL_PUBLISHER_IP = "unknown";
    public static final int INITIAL_PUBLISHER_PORT = 20000 + seed;

    public static void loadIP() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (isIPv4) {
                            INITIAL_PUBLISHER_IP = sAddr;
                            return;
                        } else {
                            int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                            INITIAL_PUBLISHER_IP = delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                        }
                    }
                }
            }
        } catch (Exception ex ) {
            INITIAL_PUBLISHER_IP = "unknown";
        }
    }
}
