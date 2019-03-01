package saros.server.net;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import saros.SarosConstants;
import saros.annotations.Component;
import saros.net.ConnectionState;
import saros.net.xmpp.IConnectionListener;
import saros.net.xmpp.XMPPConnectionService;

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
