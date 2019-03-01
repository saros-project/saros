package saros.ui.eventhandler;

import org.eclipse.jface.preference.IPreferenceStore;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import saros.SarosConstants;
import saros.net.ConnectionState;
import saros.net.xmpp.IConnectionListener;
import saros.net.xmpp.XMPPConnectionService;
import saros.preferences.EclipsePreferenceConstants;

public class ServerPreferenceHandler {

  private IPreferenceStore preferenceStore;

  private IConnectionListener connectionListener =
      new IConnectionListener() {

        @Override
        public void connectionStateChanged(Connection connection, ConnectionState newState) {

          // Adding the feature while state is CONNECTING would be much
          // better, yet it's not possible since the ServiceDiscoveryManager
          // is not available at that point
          if (ConnectionState.CONNECTED.equals(newState)) {
            if (Boolean.getBoolean("saros.server.SUPPORTED")) {
              if (preferenceStore.getBoolean(EclipsePreferenceConstants.SERVER_ACTIVATED)) {
                addServerFeature(connection);
              } else {
                removeServerFeature(connection);
              }
            }
          }
        }
      };

  public ServerPreferenceHandler(
      XMPPConnectionService connectionService, IPreferenceStore preferenceStore) {
    this.preferenceStore = preferenceStore;

    connectionService.addListener(connectionListener);
  }

  private void addServerFeature(Connection connection) {
    if (connection == null) return;

    ServiceDiscoveryManager discoveryManager = ServiceDiscoveryManager.getInstanceFor(connection);

    if (discoveryManager == null) return;

    discoveryManager.addFeature(SarosConstants.NAMESPACE_SERVER);
  }

  private void removeServerFeature(Connection connection) {
    if (connection == null) return;

    ServiceDiscoveryManager discoveryManager = ServiceDiscoveryManager.getInstanceFor(connection);

    if (discoveryManager == null) return;

    discoveryManager.removeFeature(SarosConstants.NAMESPACE_SERVER);
  }
}
