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
 * 
 * DiscoveryManager caches for each JID a DiscoverInfo entry, so it can be asked
 * which features the different XMPP clients of a particular JID supports.
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
     */
    protected RosterListener rosterListener = new RosterListener() {

        protected void clearCache(Presence presence) {
            String rjid = presence.getFrom();
            if (rjid == null) {
                log.error("presence.getFrom() is null");
                return;
            }
            cache.remove(rjid);
        }

        protected void clearCache(Collection<String> addresses) {
            for (String pjid : addresses) {
                /*
                 * TODO We should remove all presences kept for the given
                 * addresses
                 */
                for (Presence presence : rosterTracker.getPresences(new JID(
                    pjid))) {
                    clearCache(presence);
                }
            }
        }

        public void entriesAdded(Collection<String> addresses) {
            log.trace("entriesAdded");
            clearCache(addresses);
        }

        public void entriesDeleted(Collection<String> addresses) {
            log.trace("entriesDeleted");
            clearCache(addresses);
        }

        public void entriesUpdated(Collection<String> addresses) {
            log.trace("entriesUpdated");
            clearCache(addresses);
        }

        public void presenceChanged(Presence presence) {
            log.trace("presenceChanged");
            clearCache(presence);
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
     * Jabber client connected under the given plain JID (a RQ-JID is stripped
     * of its resource)
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
     * given plain JID (a RQ-JID is stripped of its resource).
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
     * Returns true if there is an available presence which supports the given
     * feature. Returns false if all available presences do not support the
     * given feature. Returns null if there is no available presences supporting
     * the feature, but not all presences have been queried for support yet.
     * 
     * This method will not trigger any updates or block but rather just use the
     * cache and return quickly.
     * 
     * @reentrant
     * @nonBlocking
     */
    public Boolean isSupportedNonBlock(JID jid, String namespace) {

        jid = jid.getBareJID();

        boolean allCached = true;

        for (JID rqJID : rosterTracker.getAvailablePresences(jid)) {

            DiscoverInfo disco = cache.get(rqJID.toString());
            if (disco == null) {
                allCached = false;
                continue;
            }

            if (disco.containsFeature(Saros.NAMESPACE))
                return true;
        }

        if (allCached) {
            return false;
        } else {
            return null;
        }
    }

    /**
     * Returns the RQ-JID of given plain JID supporting the feature of the given
     * name-space if available, otherwise null.
     * 
     * If not in the cache then a blocking cache update is performed.
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

            String rjid = presence.getFrom();
            if (rjid == null) {
                log.error("presence.getFrom() is null");
                continue;
            }

            JID jidToCheck = new JID(rjid);
            if (isFeatureSupported(jidToCheck, namespace)) {
                return jidToCheck;
            }
        }

        return null;
    }

    /**
     * Perform a ServiceDiscovery and check if the given feature is among the
     * features supported by the given recipient.
     * 
     * @param recipient
     *            A RQ-JID (user@host/resource) of the user to query support
     *            for.
     * 
     * @blocking This method blocks until the ServiceDiscovery returns.
     * @reentrant This method can be called concurrently.
     * @caching If results are available in the cache, they are used instead of
     *          querying the server.
     */
    protected boolean isFeatureSupported(JID recipient, String feature) {

        if (recipient.getResource().equals(""))
            log.warn("Resource missing: ", new StackTrace());

        DiscoverInfo info = cache.get(recipient.toString());
        if (info == null) {
            // TODO block if a query for the recipient is already in progress
            info = querySupport(recipient);

            if (info == null)
                return false;

            log.debug("Inserting DiscoveryInfo into Cache for: " + recipient);
            cache.put(recipient.toString(), info);
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
     * @return DiscoverInfo from recipient or null if an XMPPException was
     *         thrown.
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

        ServiceDiscoveryManager sdm = ServiceDiscoveryManager
            .getInstanceFor(saros.getConnection());

        try {
            return sdm.discoverInfo(recipient.toString());
        } catch (XMPPException e) {

            log.warn("Service Discovery failed on recipient "
                + recipient.toString() + " server:"
                + saros.getConnection().getHost() + ":", e);
            return null;
        }
    }
}