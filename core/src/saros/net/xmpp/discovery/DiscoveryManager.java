package saros.net.xmpp.discovery;

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
import saros.annotations.Component;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.net.xmpp.roster.IRosterListener;
import saros.net.xmpp.roster.RosterTracker;
import saros.util.NamedThreadFactory;
import saros.util.StackTrace;
import saros.util.ThreadUtils;

/**
 * This class is responsible for performing ServiceDiscovery for features such as Jingle and Saros.
 * It uses XEP-0030 http://xmpp.org/extensions/xep-0030.html to ask a recipient for the particular
 * used feature.
 *
 * <p>DiscoveryManager caches for each JID a DiscoverInfo entry, so it can be asked which features
 * the different XMPP clients of a particular JID supports.
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
   * The cache contains the results of calls to querySupport indexed by the string value of a given
   * JID. If the discovery failed, a null value is stored.
   */
  private final Map<String, DiscoverInfoWrapper> cache =
      Collections.synchronizedMap(new HashMap<String, DiscoverInfoWrapper>());

  private final XMPPConnectionService connectionService;

  private final RosterTracker rosterTracker;

  private final CopyOnWriteArrayList<DiscoveryManagerListener> discoveryManagerListeners =
      new CopyOnWriteArrayList<DiscoveryManagerListener>();

  /** Thread pool to execute service discovery requests asynchronously. */
  private final ExecutorService threadPoolExecutor =
      new ThreadPoolExecutor(
          0,
          2,
          30,
          TimeUnit.SECONDS,
          new LinkedBlockingQueue<Runnable>(),
          new NamedThreadFactory("ServiceDiscoveryWorker", false));

  /**
   * This RosterListener closure is added to the RosterTracker to get notifications when the roster
   * changes.
   */
  private final IRosterListener rosterListener =
      new IRosterListener() {

        /**
         * Stores the most recent presence for each user, so we can keep track of away/available
         * changes which should not update the RosterView.
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
              LOG.debug(
                  "clearing cache entry of contact "
                      + rjid
                      + ": "
                      + infoWrapper.item.getChildElementXML());
            } else {
              LOG.debug(
                  "clearing cache entry of contact "
                      + rjid
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
            for (Presence presence : rosterTracker.getPresences(new JID(pjid))) {
              clearCache(presence);
            }
          }
        }

        @Override
        public void entriesAdded(Collection<String> addresses) {
          clearCache(addresses);
        }

        @Override
        public void entriesDeleted(Collection<String> addresses) {
          clearCache(addresses);
        }

        @Override
        public void entriesUpdated(Collection<String> addresses) {
          /*
           * TODO This is called to frequently by Smack and invalidates our
           * beautiful cache!
           */
          clearCache(addresses);
        }

        @Override
        public void presenceChanged(Presence presence) {

          if (hasOnlineStateChanged(presence)) clearCache(presence);
        }

        private boolean hasOnlineStateChanged(Presence presence) {
          Presence last = lastPresenceMap.get(presence.getFrom());
          lastPresenceMap.put(presence.getFrom(), presence);

          if (last == null) return false;

          return last.isAvailable() ^ presence.isAvailable();
        }

        @Override
        public void rosterChanged(Roster roster) {
          cache.clear();
        }
      };

  public DiscoveryManager(XMPPConnectionService connectionService, RosterTracker rosterTracker) {
    this.connectionService = connectionService;
    this.rosterTracker = rosterTracker;
    this.rosterTracker.addRosterListener(rosterListener);
  }

  @Override
  public void dispose() {
    rosterTracker.removeRosterListener(rosterListener);
    threadPoolExecutor.shutdownNow();
  }

  /**
   * Adds a {@link DiscoveryManagerListener}
   *
   * @param discoveryManagerListener
   */
  public void addDiscoveryManagerListener(DiscoveryManagerListener discoveryManagerListener) {
    discoveryManagerListeners.addIfAbsent(discoveryManagerListener);
  }

  /**
   * Removes a {@link DiscoveryManagerListener}
   *
   * @param discoveryManagerListener
   */
  public void removeDiscoveryManagerListener(DiscoveryManagerListener discoveryManagerListener) {
    discoveryManagerListeners.remove(discoveryManagerListener);
  }

  /**
   * Checks if the given {@linkplain JID} supports the requested feature. The JID may be non
   * resource qualified in which case all presences belonging to that JID are checked.
   *
   * <p>This method does <b>not</b> perform any I/O operation and will return immediately.
   *
   * <p><b>Please note the return value: <code>if(isFeatureSupported(foo, bar)) ... </code> is
   * likely to produce a {@link NullPointerException NPE}.</b>
   *
   * @param jid {@link JID} to query support for
   * @param namespace the namespace of the feature
   * @return <code>true</code> if the given feature is supported, <code>false</code> if it is not
   *     supported or <b><code>null</code> </b> if no information is available
   * @see #queryFeatureSupport(JID, String, boolean)
   */
  public Boolean isFeatureSupported(final JID jid, final String namespace) {
    checkJID(jid);

    Boolean supported = null;

    final List<JID> jidsToQuery = new ArrayList<JID>();

    if (jid.isBareJID()) jidsToQuery.addAll(rosterTracker.getAvailablePresences(jid));
    else jidsToQuery.add(jid);

    for (JID rqJID : jidsToQuery) {

      DiscoverInfoWrapper info = cache.get(rqJID.toString());
      if (info == null) continue;

      DiscoverInfo disco = info.item;

      if (disco == null) continue;

      supported = disco.containsFeature(namespace);

      if (supported) break;
    }

    return supported;
  }

  /**
   * Returns the RQ-JID of given plain JID supporting the feature of the given name-space if
   * available, otherwise null.
   *
   * <p>If not in the cache then a blocking cache update is performed.
   *
   * @param jid The JID of the user to find a supporting presence for. The JID can be resource
   *     qualified in which case the resource is stripped, before performing the look-up.
   * @blocking This method blocks until the ServiceDiscovery returns.
   * @reentrant This method can be called concurrently.
   * @caching If results are available in the cache, they are used instead of querying the server.
   */
  public JID getSupportingPresence(final JID jid, final String namespace) {
    checkJID(jid);

    for (Presence presence : rosterTracker.getPresences(jid.getBareJID())) {
      if (!presence.isAvailable()) continue;

      String rjid = presence.getFrom();
      if (rjid == null) {
        LOG.error("presence.getFrom() is null");
        continue;
      }

      JID jidToCheck = new JID(rjid);
      Boolean supported = queryFeatureSupport(jidToCheck, namespace);

      if (supported != null && supported) return jidToCheck;
    }

    return null;
  }

  /**
   * Query the given {@linkplain JID} for the requested feature. The JID may be non resource
   * qualified in which case all presences belonging to that JID are queried.
   *
   * <p>All registered {@linkplain #addDiscoveryManagerListener(DiscoveryManagerListener) listeners}
   * will be notified about the result. The request may be performed asynchronously in which case
   * the caller must ensure that a listener has already been added to retrieve the results.
   *
   * <p><b>Please note the return value: <code>if(queryFeatureSupport(foo, bar, async)) ... </code>
   * is likely to produce a {@link NullPointerException NPE}.</b>
   *
   * @param jid {@link JID} to query support for
   * @param namespace the namespace of the feature
   * @param async if <code>true</code> the request will be performed asynchronously
   * @return <code>true</code> if the given JID supports the feature or <code>false</code> if the
   *     given JID does not support the feature or <code>null</code> if the query fails or is
   *     performed asynchronously
   */
  public Boolean queryFeatureSupport(final JID jid, final String namespace, boolean async) {
    checkJID(jid);

    if (!async) return queryFeatureSupport(jid, namespace);

    threadPoolExecutor.execute(
        ThreadUtils.wrapSafe(
            LOG,
            new Runnable() {
              @Override
              public void run() {
                queryFeatureSupport(jid, namespace);
              }
            }));

    return null;
  }

  /**
   * Perform a service discovery and check if the given feature is among the features supported by
   * the given recipient. All registered listeners will be notified about the result.
   *
   * @param jid A RQ-JID (user@host/resource) of the user to query support for or a non RQ-JID to
   *     query all presences for this JID.
   * @blocking This method blocks until the ServiceDiscovery returns.
   * @reentrant This method can be called concurrently.
   * @caching If results are available in the cache, they are used instead of querying the server.
   */
  private Boolean queryFeatureSupport(JID jid, String namespace) {

    Boolean supported = null;

    checkJID(jid);

    DiscoverInfoWrapper wrapper;

    final List<JID> jidsToQuery = new ArrayList<JID>();

    if (jid.isBareJID()) jidsToQuery.addAll(rosterTracker.getAvailablePresences(jid));
    else jidsToQuery.add(jid);

    for (JID rqJID : jidsToQuery) {

      // add dummy
      synchronized (cache) {
        wrapper = cache.get(rqJID.toString());
        if (wrapper == null) {
          wrapper = new DiscoverInfoWrapper();
          cache.put(rqJID.toString(), wrapper);
        }
      }

      DiscoverInfo disco = null;

      // wait if there is already a discovery for the JID in progress
      synchronized (wrapper) {
        if (wrapper.isAvailable()) disco = wrapper.item;
        else {
          disco = wrapper.item = performServiceDiscovery(rqJID);
          if (disco != null) LOG.debug("Inserted DiscoveryInfo into Cache for: " + rqJID);
        }
      }

      // Null means that the discovery failed
      if (disco == null) {
        // and so we do not know if the feature is supported
        // notifyFeatureSupportUpdated(jid, namespace, false);
        continue;
      }

      notifyFeatureSupportUpdated(rqJID, namespace, disco.containsFeature(namespace));

      /*
       * loop through all presence regardless if we already know that the
       * feature is supported to notify the listener for every current
       * presence
       */
      if (supported != null) supported |= disco.containsFeature(namespace);
      else supported = disco.containsFeature(namespace);
    }

    return supported;
  }

  /**
   * Perform a ServiceDiscovery [1] and check if the given resource is among the features supported
   * by the given recipient.
   *
   * <p>[1] XEP-0030 http://xmpp.org/extensions/xep-0030.html
   *
   * @param jid The JID must have a resource identifier (user@host/resource), otherwise you get a
   *     blame StackTrace in your logs.
   * @return DiscoverInfo from recipient or null if an XMPPException was thrown.
   * @blocking This method blocks until the ServiceDiscovery returns.
   * @reentrant This method can be called concurrently.
   * @nonCaching This method does not use a cache, but queries the server directly.
   */
  private DiscoverInfo performServiceDiscovery(final JID jid) {

    if (jid.isBareJID()) {
      LOG.warn(
          "cannot perform service discovery on a non resource qualified jid: " + jid.toString(),
          new StackTrace());
      return null;
    }

    final Connection connection = connectionService.getConnection();

    if (connection == null) {
      LOG.warn("cannot not perform a service discovery because not connected to a XMPP server");
      return null;
    }

    ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(connection);

    try {
      return sdm.discoverInfo(jid.toString());
    } catch (XMPPException e) {

      LOG.warn(
          "Service Discovery failed on recipient "
              + jid.toString()
              + " server: "
              + connection.getHost(),
          e);

      /*
       * FIXME handle timeouts and error conditions differently ! see
       * http://xmpp.org/extensions/xep-0030.html#errors
       */
      return null;
    }
  }

  /** Notify all {@link DiscoveryManagerListener}s about an updated feature support. */
  private void notifyFeatureSupportUpdated(JID jid, String feature, boolean isSupported) {
    for (DiscoveryManagerListener discoveryManagerListener : discoveryManagerListeners) {
      discoveryManagerListener.featureSupportUpdated(jid, feature, isSupported);
    }
  }

  private void checkJID(JID jid) {
    if (jid == null) throw new NullPointerException("jid is null");

    if (!jid.isValid()) throw new IllegalArgumentException("jid is not valid: " + jid);
  }
}
