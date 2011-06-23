package de.fu_berlin.inf.dpp.net.util;

import java.lang.reflect.Method;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5Proxy;

/**
 * Static networking class, dealing with local IP retrieval
 */
public class NetworkingUtils {

    protected static Logger log = Logger.getLogger("de.fu_berlin.inf.dpp.net");

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

        List<InetAddress> ips = new LinkedList<InetAddress>();

        // Holds last ipv4 index in ips list (used to sort IPv4 before IPv6 IPs)
        int ipv4Index = 0;

        // Prepare method calls by reflection
        Class<NetworkInterface> networkInterfaceClass = NetworkInterface.class;
        Method mIsUp = null;
        Method mIsLoopback = null;
        try {
            mIsUp = networkInterfaceClass.getMethod("isUp", (Class[]) null);
        } catch (Exception e) {
            // ignore Java 1.6 feature
        }
        try {
            mIsLoopback = networkInterfaceClass.getMethod("isLoopback",
                (Class[]) null);
        } catch (Exception e) {
            // ignore Java 1.6 feature
        }

        // Get all network interfaces
        Enumeration<NetworkInterface> eInterfaces = NetworkInterface
            .getNetworkInterfaces();

        // Enumerate interfaces and enumerate all Internet addresses of each
        if (eInterfaces != null) {
            while (eInterfaces.hasMoreElements()) {
                NetworkInterface ni = eInterfaces.nextElement();

                // skip loopback devices and not running interfaces
                try {
                    if (mIsLoopback != null)
                        if ((Boolean) mIsLoopback.invoke(ni, (Object[]) null))
                            continue;
                    if (mIsUp != null)
                        if ((Boolean) mIsUp.invoke(ni, (Object[]) null) == false)
                            continue;
                } catch (Exception e) {
                    // ignore Java 1.6 feature
                }

                Enumeration<InetAddress> iaddrs = ni.getInetAddresses();
                while (iaddrs.hasMoreElements()) {
                    InetAddress iaddr = iaddrs.nextElement();

                    // in case ni.isLoopback failed to invoke
                    if (iaddr.isLoopbackAddress())
                        continue;

                    if (iaddr instanceof Inet6Address) {
                        if (includeIPv6Addresses)
                            ips.add(iaddr);
                    } else
                        ips.add(ipv4Index++, iaddr);
                }
            }
        }

        return ips;
    }

    public static boolean isSocks5ProxyRunning() {
        boolean localSocks5ProxyEnabled = SmackConfiguration
            .isLocalSocks5ProxyEnabled();

        SmackConfiguration.setLocalSocks5ProxyEnabled(false);
        boolean isRunning = Socks5Proxy.getSocks5Proxy().isRunning();
        SmackConfiguration.setLocalSocks5ProxyEnabled(localSocks5ProxyEnabled);

        return isRunning;
    }

}
