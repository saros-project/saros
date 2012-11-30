package de.fu_berlin.inf.dpp.net.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5Proxy;

/**
 * Static networking class, dealing with local IP retrieval
 */
public class NetworkingUtils {

    /**
     * Retrieves all IP addresses from all non-loopback-, running network
     * devices of the local host. <br>
     * IPv4 addresses are sorted before IPv6 addresses (to let connecting to
     * IPv4 IPs before attempting their IPv6 equivalents when iterating the
     * List).
     * 
     * @param includeIPv6Addresses
     *            flag if IPv6 addresses are added to the result
     * @return List<{@link String}> of all retrieved IP addresses
     * @throws UnknownHostException
     * @throws SocketException
     */
    public static List<InetAddress> getAllNonLoopbackLocalIPAdresses(
        boolean includeIPv6Addresses) throws UnknownHostException,
        SocketException {

        LinkedList<InetAddress> ips = new LinkedList<InetAddress>();

        for (Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
            .getNetworkInterfaces(); networkInterfaces.hasMoreElements();) {

            NetworkInterface networkInterface = networkInterfaces.nextElement();

            if (networkInterface.isLoopback() || !networkInterface.isUp())
                continue;

            Enumeration<InetAddress> inetAddresses = networkInterface
                .getInetAddresses();

            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();

                if (inetAddress.isLoopbackAddress())
                    continue;

                if (inetAddress instanceof Inet6Address) {
                    if (includeIPv6Addresses)
                        ips.addLast(inetAddress);
                } else
                    ips.addFirst(inetAddress);
            }
        }

        return ips;
    }

    /**
     * Returns the Socks5Proxy object without changing its running state.
     */
    public static Socks5Proxy getSocks5ProxySafe() {
        boolean isLocalS5Penabled = SmackConfiguration
            .isLocalSocks5ProxyEnabled();

        SmackConfiguration.setLocalSocks5ProxyEnabled(false);

        Socks5Proxy proxy = Socks5Proxy.getSocks5Proxy();

        SmackConfiguration.setLocalSocks5ProxyEnabled(isLocalS5Penabled);

        return proxy;
    }

    /**
     * Adds a specified IP (String) to the list of addresses of the Socks5Proxy.
     * (the target attempts the stream host addresses one by one in the order of
     * the list)
     * 
     * @param ip
     *            String of the address of the Socks5Proxy (stream host)
     * @param inFront
     *            boolean flag, if the address is to be inserted in front of the
     *            list. If <code>false</code>, address is added at the end of
     *            the list.
     */
    public static void addProxyAddress(String ip, boolean inFront) {
        Socks5Proxy proxy = getSocks5ProxySafe();

        if (!inFront) {
            proxy.addLocalAddress(ip);
            return;
        }
        ArrayList<String> list = new ArrayList<String>(
            proxy.getLocalAddresses());
        list.remove(ip);
        list.add(0, ip);
        proxy.replaceLocalAddresses(list);
    }

}
