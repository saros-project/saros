package saros.net.xmpp.contact;

import java.util.HashMap;
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
import saros.SarosConstants;
import saros.net.xmpp.JID;
import saros.util.NamedThreadFactory;

/**
 * A Simple Service to get and cache Feature Support of XMPP resources.
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

  private Map<String, Boolean> resourcesSarosSupport = new HashMap<>();
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
          resourcesSarosSupport = new HashMap<>();
        });
  }

  /**
   * Query Feature Support for {@link SarosConstants#XMPP_FEATURE_NAMESPACE}.
   *
   * @param fullJid of contact
   * @param resultCallback to call on positive result
   */
  void querySarosSupport(JID fullJid, Consumer<Boolean> resultCallback) {
    discoveryExecutor.execute(
        () -> {
          String jid = fullJid.getRAW();
          boolean supportsFeature;
          if (resourcesSarosSupport.containsKey(jid))
            supportsFeature = resourcesSarosSupport.get(jid);
          else {
            supportsFeature = runDiscovery(jid, SarosConstants.XMPP_FEATURE_NAMESPACE);
            resourcesSarosSupport.put(jid, supportsFeature);
          }

          if (supportsFeature) resultCallback.accept(supportsFeature);
        });
  }

  private boolean runDiscovery(String fullJid, String feature) {
    if (discoveryManager == null) return false;
    try {
      DiscoverInfo discoverInfo = discoveryManager.discoverInfo(fullJid);
      return discoverInfo.containsFeature(feature);
    } catch (XMPPException e) {
      log.debug("Feature discovery for " + fullJid + " failed", e);
      return false;
    }
  }

  /**
   * Removes known resources.
   *
   * @param fullJids List with fullJids from resources
   */
  void removeResources(List<JID> fullJids) {
    discoveryExecutor.execute(
        () -> fullJids.forEach(jid -> resourcesSarosSupport.remove(jid.getRAW())));
  }

  /** Shuts down the executor thread. Needs to be called after usage! */
  void stop() {
    discoveryExecutor.shutdownNow();
  }
}
