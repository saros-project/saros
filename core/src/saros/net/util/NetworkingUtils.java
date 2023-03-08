package saros.net.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5Proxy;

/** Static networking class, dealing with local IP retrieval */
public class NetworkingUtils {

  private static final Logger log = Logger.getLogger(NetworkingUtils.class);

  /**
   * Retrieves all IP addresses from all non-loopback-, running network devices of the local host.
   * <br>
   * IPv4 addresses are sorted before IPv6 addresses (to let connecting to IPv4 IPs before
   * attempting their IPv6 equivalents when iterating the List).
   *
   * @param includeIPv6Addresses flag if IPv6 addresses are added to the result
   * @return List<{@link String}> of all retrieved IP addresses
   */
  public static List<InetAddress> getAllNonLoopbackLocalIPAddresses(boolean includeIPv6Addresses) {

    LinkedList<InetAddress> addresses = new LinkedList<InetAddress>();

    Enumeration<NetworkInterface> networkInterfaces;

    try {
      networkInterfaces = NetworkInterface.getNetworkInterfaces();
    } catch (SocketException e) {
      log.error("failed to retrieve available network interfaces", e);
      return addresses;
    }

    while (networkInterfaces.hasMoreElements()) {

      NetworkInterface networkInterface = networkInterfaces.nextElement();

      try {
        if (networkInterface.isLoopback() || !networkInterface.isUp()) continue;
      } catch (SocketException e) {
        log.warn("skipping network interface: " + networkInterface, e);
        continue;
      }

      Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();

      while (inetAddresses.hasMoreElements()) {
        InetAddress inetAddress = inetAddresses.nextElement();

        if (inetAddress.isLoopbackAddress()) continue;

        if (inetAddress instanceof Inet6Address) {
          if (includeIPv6Addresses) addresses.addLast(inetAddress);
        } else addresses.addFirst(inetAddress);
      }
    }

    return addresses;
  }

  /** Returns the Socks5Proxy object without changing its running state. */
  public static Socks5Proxy getSocks5ProxySafe() {
    boolean isLocalS5Penabled = SmackConfiguration.isLocalSocks5ProxyEnabled();

    SmackConfiguration.setLocalSocks5ProxyEnabled(false);

    Socks5Proxy proxy = Socks5Proxy.getSocks5Proxy();

    SmackConfiguration.setLocalSocks5ProxyEnabled(isLocalS5Penabled);

    return proxy;
  }
}
