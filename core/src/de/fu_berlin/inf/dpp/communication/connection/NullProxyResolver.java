package de.fu_berlin.inf.dpp.communication.connection;

import org.jivesoftware.smack.proxy.ProxyInfo;

/** A {@link IProxyResolver} that does nothing. */
public class NullProxyResolver implements IProxyResolver {

  @Override
  public ProxyInfo resolve(String host) {
    return null;
  }
}
