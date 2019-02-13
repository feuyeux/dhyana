package org.feuyeux.dhyana.transport;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
/**
 * @author feuyeux@gmail.com
 * @date 2019/01/09
 */
public class NetworkUtils {
    public static String getNodeIp() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                if ("lo".equals(netInterface.getName())) {
                    continue;
                }
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = addresses.nextElement();
                    if (ip instanceof Inet4Address) {
                        String t = ip.getHostAddress();
                        if (!"127.0.0.1".equals(t)) {
                            return t;
                        }
                    }
                }
            }
            return null;
        } catch (SocketException e) {
            return null;
        }
    }
}
