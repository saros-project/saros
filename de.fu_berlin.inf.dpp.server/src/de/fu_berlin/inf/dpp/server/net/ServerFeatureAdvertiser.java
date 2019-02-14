package de.fu_berlin.inf.dpp.server.net;

import de.fu_berlin.inf.dpp.SarosConstants;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smackx.ServiceDiscoveryManager;

/** Instructs the network layer to advertise this Saros instance as a server. */
@Component(module = "server")
public class ServerFeatureAdvertiser {

  private static final Logger LOG = Logger.getLogger(ServerFeatureAdvertiser.class);

  private IConnectionListener connectionListener =
      new IConnectionListener() {
        /** Configures server feature advertising for newly established connections. */
        @Override
        public void connectionStateChanged(Connection connection, ConnectionState newState) {

          if (newState == ConnectionState.CONNECTING) {
            advertiseServerFeature(connection);
          }
        }
      };

  /**
   * Initializes the ServerFeatureAdvertiser.
   *
   * @param connectionService service to listen for new connections with
   */
  public ServerFeatureAdvertiser(XMPPConnectionService connectionService) {
    connectionService.addListener(connectionListener);
  }

  private static void advertiseServerFeature(Connection connection) {
    LOG.info("Starting to advertise ourselves as server");

    ServiceDiscoveryManager discoveryManager = ServiceDiscoveryManager.getInstanceFor(connection);

    discoveryManager.addFeature(SarosConstants.NAMESPACE_SERVER);
  }
}
