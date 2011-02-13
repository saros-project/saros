package de.fu_berlin.inf.dpp.net.internal.discoveryManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.Jingle;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.IRosterListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.net.internal.discoveryManager.events.DiscoveryManagerListener;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Utils;

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

    /**
     * Exception used to signify that no entry has been found in the cache
     */
    public static class CacheMissException extends Exception {
        private static final long serialVersionUID = -4340008321236181064L;
    }

    private static final Logger log = Logger.getLogger(DiscoveryManager.class);

    /**
     * The cache contains the results of calls to querySupport indexed by the
     * string value of a given JID. If the discovery failed, a null value is
     * stored.
     */
    protected Map<String, DiscoverInfoWrapper> cache = Collections
        .synchronizedMap(new HashMap<String, DiscoverInfoWrapper>());

    @Inject
    protected Saros saros;

    @Inject
    protected RosterTracker rosterTracker;

    protected List<DiscoveryManagerListener> discoveryManagerListeners = new ArrayList<DiscoveryManagerListener>();

    /*
     * Queues incoming calls that check Saros support by going to the discovery
     * server. Max number of concurrent threads = 3.
     */
    ExecutorService supportExecutor = Executors.newFixedThreadPool(3);

    /**
     * This RosterListener closure is added to the RosterTracker to get
     * notifications when the roster changes.
     */
    protected IRosterListener rosterListener = new IRosterListener() {

        /**
         * Stores the most recent presence for each user, so we can keep track
         * of away/available changes which should not update the RosterView.
         */
        protected Map<String, Presence> lastPresenceMap = new HashMap<String, Presence>();

        protected void clearCache(Presence presence) {
            String rjid = presence.getFrom();
            if (rjid == null) {
                log.error("presence.getFrom() is null");
                return;
            }

            DiscoverInfoWrapper infoWrapper = cache.remove(rjid);
            if (infoWrapper != null) {
                if (infoWrapper.isAvailable()) {
                    log.debug("Clearing cache entry of buddy " + rjid + ": "
                        + infoWrapper.item.getChildElementXML());
                } else {
                    log.debug("Clearing cache entry of buddy " + rjid
                        + " but cache entry is empty (a discovery is "
                        + "still running or the last one failed)");
                }
            }
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
            // TODO This is called to frequently by smack and invalidates our
            // beautiful cache!
            clearCache(addresses);
        }

        public void presenceChanged(Presence current) {
            log.trace("presenceChanged: " + current.toString());
            if (hasOnlineStateChanged(current))
                clearCache(current);
            lastPresenceMap.put(current.getFrom(), current);
        }

        protected boolean hasOnlineStateChanged(Presence presence) {
            Presence last = lastPresenceMap.get(presence.getFrom());
            if (last == null)
                return false;

            if (((last.isAvailable() || last.isAway()) && (presence
                .isAvailable() || presence.isAway()))) {
                return false;
            }

            return true;
        }

        public void rosterChanged(Roster roster) {
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
     * XMPP client connected under the given plain JID (a RQ-JID is stripped of
     * its resource)
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
     * given feature.
     * 
     * This method will not trigger any updates or block but rather just use the
     * cache and return quickly.
     * 
     * @throws CacheMissException
     *             if there is no available presences supporting the feature,
     *             but not all presences have been queried for support yet.
     * 
     * @reentrant
     * @nonBlocking
     */
    public boolean isSupportedNonBlock(JID jid, String namespace)
        throws CacheMissException {

        jid = jid.getBareJID();

        boolean allCached = true;

        for (JID rqJID : rosterTracker.getAvailablePresences(jid)) {

            if (!cache.containsKey(rqJID.toString())) {
                allCached = false;
                continue;
            }

            DiscoverInfoWrapper info = cache.get(rqJID.toString());
            if (info == null)
                continue;

            DiscoverInfo disco = info.item;
            if (disco != null && disco.containsFeature(namespace))
                return true;
        }

        if (allCached) {
            return false;
        }

        throw new CacheMissException();
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
     * Begin a thread that populates Saros-support information for all people in
     * the given list.
     * 
     * @nonblocking This method start a new ASync thread.
     */
    public void cacheSarosSupport(final JID buddy) {
        try {
            isSupportedNonBlock(buddy, Saros.NAMESPACE);
        } catch (CacheMissException e) {
            supportExecutor.execute(new Runnable() {
                public void run() {
                    Utils.runSafeAsync(log, new Runnable() {
                        public void run() {
                            isSarosSupported(buddy);
                        }
                    });
                }
            });
        }
    }

    /**
     * DiscoverInfo wrapper.
     */
    protected static class DiscoverInfoWrapper {
        public DiscoverInfo item;

        public boolean isAvailable() {
            return item != null;
        }
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

        DiscoverInfoWrapper info;

        // add dummy
        synchronized (cache) {
            info = cache.get(recipient.toString());
            if (info == null) {
                info = new DiscoverInfoWrapper();
                cache.put(recipient.toString(), info);
            }
        }

        DiscoverInfo disco = null;

        // FIXME: If a cache clear appears at this point it is possible to have
        // more than one discovery running for the same JID.

        // wait if there is one discovery in progress
        synchronized (info) {
            if (info.isAvailable())
                disco = info.item;
            else {
                disco = info.item = querySupport(recipient);
                if (disco != null)
                    log.debug("Inserted DiscoveryInfo into Cache for: "
                        + recipient);
            }
        }

        // Null means that the discovery failed
        if (disco == null) {
            notifyFeatureSupportUpdated(recipient, feature, false);
            return false;
        }

        notifyFeatureSupportUpdated(recipient, feature,
            disco.containsFeature(feature));
        return disco.containsFeature(feature);
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

            log.warn(
                "Service Discovery failed on recipient " + recipient.toString()
                    + " server:" + saros.getConnection().getHost() + ":", e);
            return null;
        }
    }

    /**
     * Adds a {@link DiscoveryManagerListener}
     * 
     * @param discoveryManagerListener
     */
    public void addDiscoveryManagerListener(
        DiscoveryManagerListener discoveryManagerListener) {
        this.discoveryManagerListeners.add(discoveryManagerListener);
    }

    /**
     * Removes a {@link DiscoveryManagerListener}
     * 
     * @param discoveryManagerListener
     */
    public void removeDiscoveryManagerListener(
        DiscoveryManagerListener discoveryManagerListener) {
        this.discoveryManagerListeners.remove(discoveryManagerListener);
    }

    /**
     * Notify all {@link DiscoveryManagerListener}s about an updated feature
     * support.
     */
    public void notifyFeatureSupportUpdated(JID jid, String feature,
        boolean isSupported) {
        for (DiscoveryManagerListener discoveryManagerListener : this.discoveryManagerListeners) {
            discoveryManagerListener.featureSupportUpdated(jid, feature,
                isSupported);
        }
    }
}
