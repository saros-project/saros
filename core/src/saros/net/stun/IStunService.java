package saros.net.stun;

import java.net.InetSocketAddress;
import java.util.Collection;

public interface IStunService {

  public static final int DEFAULT_STUN_PORT = 3478;

  /**
   * Returns whether one of the available network interfaces is connected directly to the Internet.
   *
   * @return <code>true</code> if a network interfaces is connected directly to the Internet, <code>
   *     false</code> otherwise or if no discovery was performed or is still running.
   */
  public boolean isDirectConnectionAvailable();

  /**
   * Returns the currently discovered public IP addresses. The collection will be empty if no
   * discovery has performed yet, is still running or failed. The collection may also be incomplete
   * if the discovery process has not finished for all network interfaces at the time this method is
   * called.
   *
   * @return the currently discovered public IP addresses and the port associated with this IP.
   */
  public Collection<InetSocketAddress> getPublicIpAddresses();

  /**
   * Starts a WAN (public / external) IP discovery of this system using STUN protocol RFC 5389 via
   * UDP.
   *
   * @param stunAddress address of the STUN server
   * @param stunPort port of the STUN server, if 0 the default STUN port is used
   * @param timeout timeout in milliseconds before the discovery is aborted
   * @return the currently discovered public IP addresses and the port associated with this IP.
   * @blocking this method blocks until the discovery has finished or the timeout is exceeded
   */
  public Collection<InetSocketAddress> discover(String stunAddress, int stunPort, int timeout);
}
