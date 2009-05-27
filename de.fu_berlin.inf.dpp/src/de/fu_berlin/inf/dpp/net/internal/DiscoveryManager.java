/**
 * 
 */
package de.fu_berlin.inf.dpp.net.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.Jingle;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.util.StackTrace;

/**
 * This class is responsible for performing ServiceDiscovery for features such
 * as Jingle and Saros.
 * 
 * TODO Invalidate Cache if presences or connection change
 */
@Component(module = "net")
public class DiscoveryManager {
    private static final Logger log = Logger.getLogger(DiscoveryManager.class
        .getName());

    protected Map<JID, DiscoverInfo> cache = new ConcurrentHashMap<JID, DiscoverInfo>();

    @Inject
    protected Saros saros;

    /**
     * This method returns true if {@link Jingle#NAMESPACE} is supported by the
     * Jabber client connected under the given JID (including Resource)
     * 
     * @param recipient
     *            The JID must have a resource identifier (user@host/resource),
     *            otherwise you get a blame StackTrace in your logs.
     * 
     * @blocking This method blocks until the ServiceDiscovery returns or until
     *           cache lock is available.
     * @reentrant This method can be called concurrently.
     */
    public boolean isJingleSupported(JID recipient) {
        return isFeatureSupported(recipient, Jingle.NAMESPACE);
    }

    /**
     * This method returns true if {@link Saros#NAMESPACE} is available on the
     * given JID.
     * 
     * @param recipient
     *            The JID must have a resource identifier (user@host/resource),
     *            otherwise you get a blame StackTrace in your logs.
     * 
     * @blocking This method blocks until the ServiceDiscovery returns or until
     *           cache lock is available.
     * @reentrant This method can be called concurrently.
     */
    public boolean isSarosSupported(JID recipient) {
        return isFeatureSupported(recipient, Saros.NAMESPACE);
    }

    /**
     * Perform a ServiceDiscovery and check if the given feature is among the
     * features supported by the given recipient.
     * 
     * @param recipient
     *            The JID must have a resource identifier (user@host/resource),
     *            otherwise you get a blame StackTrace in your logs.
     * 
     * @blocking This method blocks until the ServiceDiscovery returns.
     * @reentrant This method can be called concurrently.
     */
    public boolean isFeatureSupported(JID recipient, String feature) {

        DiscoverInfo info = cache.get(recipient);
        if (info == null) {
            // TODO block if a query for the recipient is already in progress
            info = querySupport(recipient);
            if (info == null)
                return false;
            cache.put(recipient, info);
        }
        return info.containsFeature(feature);
    }

    /**
     * Perform a ServiceDiscovery [1] and check if the given resource is among
     * the features supported by the given recipient.
     * 
     * [1] XEP-0030 http://xmpp.org/extensions/xep-0030.html
     * 
     * @param recipient
     *            The JID must have a resource identifier (user@host/resource),
     *            otherwise you get a blame StackTrace in your logs.
     * @return DiscoverInfo from recipient or null if XMPPException was thrown.
     * 
     * @blocking This method blocks until the ServiceDiscovery returns.
     * @reentrant This method can be called concurrently.
     */
    protected DiscoverInfo querySupport(JID recipient) {
        
        if (recipient.getResource().equals(""))
            log.warn("Service discovery is likely to "
                + "fail because resource is missing: " + recipient.toString(),
                new StackTrace());

        if (!saros.isConnected())
            throw new IllegalStateException("Not Connected");

        ServiceDiscoveryManager sdm = ServiceDiscoveryManager
            .getInstanceFor(saros.getConnection());

        try {
            return sdm.discoverInfo(recipient.toString());
        } catch (XMPPException e) {
            log.warn("Service Discovery failed on recipient "
                + recipient.toString() + ":", e);
            return null;
        }
    }
}