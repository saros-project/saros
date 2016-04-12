package de.fu_berlin.inf.dpp.connection;

import org.jivesoftware.smack.proxy.ProxyInfo;

import de.fu_berlin.inf.dpp.communication.connection.IProxyResolver;

/**
 * A {@link IProxyResolver} that does nothing.
 */
public class NullProxyResolver implements IProxyResolver {

    @Override
    public ProxyInfo resolve(String host) {
        return null;
    }
}
