package saros.net.xmpp.contact;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import saros.net.ResourceFeature;
import saros.net.xmpp.JID;
import saros.util.NamedThreadFactory;

/**
 * A Simple Service to get and cache {@link ResourceFeature} Support of XMPP resources.
 *
 * <p>All methods are non-blocking and run in a separate thread, which should be stopped after usage
 * via {@link #stop()}.
 */
class DiscoveryService {
  private static final Logger log = Logger.getLogger(DiscoveryService.class);

  /** Single-Threaded Executor to handle all Feature Discoveries and map modification. */
  private final ExecutorService discoveryExecutor =
      new ThreadPoolExecutor(
          0,
          1,
          30,
          TimeUnit.SECONDS,
          new LinkedBlockingQueue<Runnable>(),
          new NamedThreadFactory("XMPPContactService-DiscoveryThread", false));

  private Map<String, EnumSet<ResourceFeature>> resourcesFeatureSupport = new HashMap<>();
  private ServiceDiscoveryManager discoveryManager;

  /**
   * Needs to be called on connection changes!
   *
   * @param connection current connection
   */
  void connectionChanged(Connection connection) {
    discoveryExecutor.execute(
        () -> {
          if (connection == null) {
            discoveryManager = null;
            return;
          }
          discoveryManager = ServiceDiscoveryManager.getInstanceFor(connection);
          resourcesFeatureSupport = new HashMap<>();
        });
  }

  /**
   * Query Feature Support.
   *
   * @param fullJid of contact
   * @param resultCallback to call on result
   */
  void queryFeatureSupport(JID fullJid, Consumer<EnumSet<ResourceFeature>> resultCallback) {
    discoveryExecutor.execute(
        () -> {
          String jid = fullJid.getRAW();
          EnumSet<ResourceFeature> features;
          if (resourcesFeatureSupport.containsKey(jid)) {
            features = resourcesFeatureSupport.get(jid);
          } else {
            features = runDiscovery(jid);
            resourcesFeatureSupport.put(jid, features);
          }

          resultCallback.accept(features);
        });
  }

  private EnumSet<ResourceFeature> runDiscovery(String fullJid) {
    EnumSet<ResourceFeature> features = EnumSet.noneOf(ResourceFeature.class);

    if (discoveryManager == null) return features;
    try {
      DiscoverInfo discoverInfo = discoveryManager.discoverInfo(fullJid);

      for (Iterator<DiscoverInfo.Feature> it = discoverInfo.getFeatures(); it.hasNext(); ) {
        ResourceFeature.getFeature(it.next().getVar()).ifPresent(features::add);
      }
    } catch (XMPPException e) {
      log.debug("Feature discovery for " + fullJid + " failed", e);
    }

    return features;
  }

  /**
   * Removes known resources.
   *
   * @param fullJids List with fullJids from resources
   */
  void removeResources(List<JID> fullJids) {
    discoveryExecutor.execute(
        () -> fullJids.forEach(jid -> resourcesFeatureSupport.remove(jid.getRAW())));
  }

  /** Shuts down the executor thread. Needs to be called after usage! */
  void stop() {
    discoveryExecutor.shutdownNow();
  }
}
