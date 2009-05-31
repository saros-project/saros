package de.fu_berlin.inf.dpp.net.internal;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.Jingle;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.util.StackTrace;

/**
 * This class is responsible for performing ServiceDiscovery for features such
 * as Jingle and Saros. It uses XEP-0030
 * http://xmpp.org/extensions/xep-0030.html to ask a recipient for the
 * particular used feature.
 */
@Component(module = "net")
public class DiscoveryManager implements Disposable {
    private static final Logger log = Logger.getLogger(DiscoveryManager.class
        .getName());

    protected Map<String, DiscoverInfo> cache = new ConcurrentHashMap<String, DiscoverInfo>();

    @Inject
    protected Saros saros;

    @Inject
    protected RosterTracker rosterTracker;

    /**
     * This RosterListener closure is added to the RosterTracker to get
     * notifications if the roster is changed.
     * 
     * TODO invalidate cache selectively based on the JIDs provided
     */
    protected RosterListener rosterListener = new RosterListener() {

        public void entriesAdded(final Collection<String> addresses) {
            log.trace("entriesAdded");
            cache.clear();
        }

        public void entriesDeleted(final Collection<String> addresses) {
            log.trace("entriesDeleted");
            cache.clear();
        }

        public void entriesUpdated(final Collection<String> addresses) {
            log.trace("entriesUpdated");
            cache.clear();
        }

        public void presenceChanged(final Presence presence) {
            log.trace("presenceChanged");
            cache.clear();
        }

    };

    public DiscoveryManager(RosterTracker rosterTracker) {
        rosterTracker.addRosterListener(rosterListener);
    }

    /**
     * This must be called before finalization otherwise you will get NPE on
     * RosterTracker.
     */
    public void dispose() {
        rosterTracker.removeRosterListener(rosterListener);
    }

    /**
     * This method returns true if {@link Jingle#NAMESPACE} is supported by the
     * Jabber client connected under the given JID.
     * 
     * This method is syntactic sugar for
     * 
     * <code>getSupportingPresence(recipient, Jingle.NAMESPACE) != null;</code>
     * 
     * @blocking This method blocks until the ServiceDiscovery returns or until
     *           cache lock is available.
     * @reentrant This method can be called concurrently.
     * @caching If results are available in the cache, they are used instead of
     *          querying the server.
     */
    public boolean isJingleSupported(final JID recipient) {
        return getSupportingPresence(recipient, Jingle.NAMESPACE) != null;
    }

    /**
     * This method returns true if {@link Saros#NAMESPACE} is available on the
     * given JID.
     * 
     * This method is syntactic sugar for
     * 
     * <code>getSupportingPresence(recipient, Saros.NAMESPACE) != null;</code>
     * 
     * @blocking This method blocks until the ServiceDiscovery returns or until
     *           cache lock is available.
     * @reentrant This method can be called concurrently.
     * @caching If results are available in the cache, they are used instead of
     *          querying the server.
     */
    public boolean isSarosSupported(final JID recipient) {
        return getSupportingPresence(recipient, Saros.NAMESPACE) != null;
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
     * @caching If results are available in the cache, they are used instead of
     *          querying the server.
     */
    protected boolean isFeatureSupported(final JID recipient,
        final String feature) {
        if (recipient.getResource().equals(""))
            log.warn("Resource missing: ", new StackTrace());

        DiscoverInfo info = cache.get(recipient.toString());
        if (info == null) {
            // TODO block if a query for the recipient is already in progress
            info = querySupport(recipient);
            if (info == null)
                return false;
            log.debug("cached: " + recipient);
            cache.put(recipient.toString(), info);
        }
        return info.containsFeature(feature);
    }

    /**
     * Returns the JID of given recipient supporting the feature of the given
     * namespace if available, otherwise null.
     * 
     * @param recipient
     *            The JID of the user to find a supporting presence for. The JID
     *            can be resource qualified in which case the resource is
     *            stripped, before performing the look-up.
     * 
     * @blocking This method blocks until the ServiceDiscovery returns.
     * @reentrant This method can be called concurrently.
     * @caching If results are available in the cache, they are used instead of
     *          querying the server.
     */
    public JID getSupportingPresence(JID recipient, String namespace) {
        if (recipient == null)
            throw new IllegalArgumentException("JID cannot be null");

        // If the caller gave us a RQ-JID, strip the resource
        recipient = recipient.getBareJID();

        for (Presence presence : rosterTracker.getPresences(recipient)) {
            if (!presence.isAvailable())
                continue;

            JID jid = new JID(presence.getFrom());
            if (isFeatureSupported(jid, namespace)) {
                log.debug(jid + " provides " + namespace);
                return jid;
            }
        }

        return null;
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
     * @nonCaching This method does not use a cache, but queries the server
     *             directly.
     */
    protected DiscoverInfo querySupport(final JID recipient) {

        if (recipient.getResource().equals(""))
            log.warn("Service discovery is likely to "
                + "fail because resource is missing: " + recipient.toString(),
                new StackTrace());

        if (!saros.isConnected())
            throw new IllegalStateException("Not Connected");

        log.debug("querySupport: " + recipient.toString());
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