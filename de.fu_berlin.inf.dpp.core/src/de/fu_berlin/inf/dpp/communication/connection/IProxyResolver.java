package de.fu_berlin.inf.dpp.communication.connection;

import org.jivesoftware.smack.proxy.ProxyInfo;

/**
 * Interface for resolving proxy information. It is up to the implementation to return the best
 * proxy information, i.e prefer Socks5 over HTTP.
 */
public interface IProxyResolver {

  /**
   * Resolves the proxy information for the given host.
   *
   * @param host the host to resolve
   * @return the {@link ProxyInfo proxy information} needed to establish a proxy tunnel or <code>
   *     null</code> if no information could be resolved
   */
  public ProxyInfo resolve(String host);
}
