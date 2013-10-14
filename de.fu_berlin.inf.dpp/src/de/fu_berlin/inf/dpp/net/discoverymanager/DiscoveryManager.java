package de.fu_berlin.inf.dpp.net.discoverymanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.picocontainer.Disposable;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.IRosterListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
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

    private static final Logger LOG = Logger.getLogger(DiscoveryManager.class);

    private static class DiscoverInfoWrapper {
        public DiscoverInfo item;

        public boolean isAvailable() {
            return item != null;
        }
    }

    /**
     * The cache contains the results of calls to querySupport indexed by the
     * string value of a given JID. If the discovery failed, a null value is
     * stored.
     */
    private final Map<String, DiscoverInfoWrapper> cache = Collections
        .synchronizedMap(new HashMap<String, DiscoverInfoWrapper>());

    private final SarosNet network;

    private final RosterTracker rosterTracker;

    private final CopyOnWriteArrayList<DiscoveryManagerListener> discoveryManagerListeners = new CopyOnWriteArrayList<DiscoveryManagerListener>();

    /** Thread pool to execute service discovery requests asynchronously. */
    private final ExecutorService threadPoolExecutor = new ThreadPoolExecutor(
        0, 2, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
        new NamedThreadFactory("ServiceDiscoveryWorker", false));

    /**
     * This RosterListener closure is added to the RosterTracker to get
     * notifications when the roster changes.
     */
    private final IRosterListener rosterListener = new IRosterListener() {

        /**
         * Stores the most recent presence for each user, so we can keep track
         * of away/available changes which should not update the RosterView.
         */
        private final Map<String, Presence> lastPresenceMap = new HashMap<String, Presence>();

        private void clearCache(Presence presence) {
            String rjid = presence.getFrom();

            if (rjid == null) {
                LOG.error("presence.getFrom() is null");
                return;
            }

            DiscoverInfoWrapper infoWrapper = cache.remove(rjid);

            if (infoWrapper != null) {
                if (infoWrapper.isAvailable()) {
                    LOG.debug("clearing cache entry of contact " + rjid + ": "
                        + infoWrapper.item.getChildElementXML());
                } else {
                    LOG.debug("clearing cache entry of contact " + rjid
                        + " but cache entry is empty (a discovery is "
                        + "still running or the last one failed)");
                }
            }
        }

        private void clearCache(Collection<String> addresses) {
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

        @Override
        public void entriesAdded(Collection<String> addresses) {
            LOG.trace("entriesAdded");
            clearCache(addresses);
        }

        @Override
        public void entriesDeleted(Collection<String> addresses) {
            LOG.trace("entriesDeleted");
            clearCache(addresses);
        }

        @Override
        public void entriesUpdated(Collection<String> addresses) {
            LOG.trace("entriesUpdated");
            /*
             * TODO This is called to frequently by Smack and invalidates our
             * beautiful cache!
             */
            clearCache(addresses);
        }

        @Override
        public void presenceChanged(Presence current) {
            LOG.trace("presenceChanged: " + current.toString());

            if (hasOnlineStateChanged(current))
                clearCache(current);

            lastPresenceMap.put(current.getFrom(), current);
        }

        private boolean hasOnlineStateChanged(Presence presence) {
            Presence last = lastPresenceMap.get(presence.getFrom());
            if (last == null)
                return false;

            if (((last.isAvailable() || last.isAway()) && (presence
                .isAvailable() || presence.isAway()))) {
                return false;
            }

            return true;
        }

        @Override
        public void rosterChanged(Roster roster) {
            cache.clear();
        }
    };

    public DiscoveryManager(SarosNet network, RosterTracker rosterTracker) {
        this.network = network;
        this.rosterTracker = rosterTracker;
        this.rosterTracker.addRosterListener(rosterListener);
    }

    /**
     * This must be called before finalization otherwise you will get NPE on
     * RosterTracker.
     */
    @Override
    public void dispose() {
        rosterTracker.removeRosterListener(rosterListener);
        threadPoolExecutor.shutdownNow();
    }

    /**
     * Checks if the given {@linkplain JID} supports the requested feature. If
     * the JID is not resource qualified a best attempt is made to check all
     * resources that are available for the given JID. This method does
     * <b>not</b> perform any I/O operation and will return immediately.
     * 
     * @param recipient
     *            {@link JID} of the contact to query support for
     * @param namespace
     *            the namespace of the feature
     * @return <code>true</code> if the given feature is supported,
     *         <code>false</code> if it is not supported or <b><code>null</code>
     *         </b> if no information is available
     */
    public Boolean isFeatureSupported(final JID recipient,
        final String namespace) {

        Boolean supported = null;

        final List<JID> jidsToQuery = new ArrayList<JID>();

        if (recipient.isBareJID())
            jidsToQuery.addAll(rosterTracker.getAvailablePresences(recipient));
        else
            jidsToQuery.add(recipient);

        for (JID rqJID : jidsToQuery) {

            DiscoverInfoWrapper info = cache.get(rqJID.toString());
            if (info == null)
                continue;

            DiscoverInfo disco = info.item;

            if (disco == null)
                continue;

            supported = disco.containsFeature(namespace);

            if (supported)
                break;
        }

        return supported;
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
                LOG.error("presence.getFrom() is null");
                continue;
            }

            JID jidToCheck = new JID(rjid);
            if (isFeatureSupportedInternal(jidToCheck, namespace)) {
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
    public void cacheSarosSupport(final JID contact) {
        Boolean supported = isFeatureSupported(contact, Saros.NAMESPACE);

        if (supported != null && supported)
            return;

        threadPoolExecutor.execute(Utils.wrapSafe(LOG, new Runnable() {
            @Override
            public void run() {
                getSupportingPresence(contact, Saros.NAMESPACE);
            }
        }));
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
    private boolean isFeatureSupportedInternal(JID recipient, String feature) {

        if (recipient.getResource().equals(""))
            LOG.warn("Resource missing: ", new StackTrace());

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
                    LOG.debug("Inserted DiscoveryInfo into Cache for: "
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
    private DiscoverInfo querySupport(final JID recipient) {

        if (recipient.getResource().equals(""))
            LOG.warn("Service discovery is likely to "
                + "fail because resource is missing: " + recipient.toString(),
                new StackTrace());

        final Connection connection = network.getConnection();

        if (connection == null)
            throw new IllegalStateException("Not Connected");

        ServiceDiscoveryManager sdm = ServiceDiscoveryManager
            .getInstanceFor(connection);

        try {
            return sdm.discoverInfo(recipient.toString());
        } catch (XMPPException e) {

            LOG.warn(
                "Service Discovery failed on recipient " + recipient.toString()
                    + " server: " + connection.getHost(), e);
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
        discoveryManagerListeners.addIfAbsent(discoveryManagerListener);
    }

    /**
     * Removes a {@link DiscoveryManagerListener}
     * 
     * @param discoveryManagerListener
     */
    public void removeDiscoveryManagerListener(
        DiscoveryManagerListener discoveryManagerListener) {
        discoveryManagerListeners.remove(discoveryManagerListener);
    }

    /**
     * Notify all {@link DiscoveryManagerListener}s about an updated feature
     * support.
     */
    private void notifyFeatureSupportUpdated(JID jid, String feature,
        boolean isSupported) {
        for (DiscoveryManagerListener discoveryManagerListener : discoveryManagerListeners) {
            discoveryManagerListener.featureSupportUpdated(jid, feature,
                isSupported);
        }
    }
}
