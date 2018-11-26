package de.fu_berlin.inf.dpp.ui.eventhandler;

import de.fu_berlin.inf.dpp.SarosConstants;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.preferences.EclipsePreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smackx.ServiceDiscoveryManager;

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
            if (Boolean.getBoolean("de.fu_berlin.inf.dpp.server.SUPPORTED")) {
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
