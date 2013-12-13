package de.fu_berlin.inf.dpp.ui.eventhandler;

import org.eclipse.jface.preference.IPreferenceStore;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smackx.ServiceDiscoveryManager;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;

public class ServerPreferenceHandler {

    private IPreferenceStore preferenceStore;

    private IConnectionListener connectionListener = new IConnectionListener() {

        @Override
        public void connectionStateChanged(Connection connection,
            ConnectionState newState) {

            // Adding the feature while state is CONNECTING would be much
            // better, yet it's not possible since the ServiceDiscoveryManager
            // is not available at that point
            if (ConnectionState.CONNECTED.equals(newState)) {
                if (Boolean.getBoolean("de.fu_berlin.inf.dpp.server.SUPPORTED")) {
                    if (preferenceStore
                        .getBoolean(PreferenceConstants.SERVER_ACTIVATED)) {
                        addServerFeature(connection);
                    } else {
                        removeServerFeature(connection);
                    }
                }
            }
        }
    };

    public ServerPreferenceHandler(SarosNet sarosNet,
        IPreferenceStore preferenceStore) {
        this.preferenceStore = preferenceStore;

        sarosNet.addListener(connectionListener);
    }

    private void addServerFeature(Connection connection) {
        if (connection == null)
            return;

        ServiceDiscoveryManager discoveryManager = ServiceDiscoveryManager
            .getInstanceFor(connection);

        if (discoveryManager == null)
            return;

        discoveryManager.addFeature(Saros.NAMESPACE_SERVER);
    }

    private void removeServerFeature(Connection connection) {
        if (connection == null)
            return;

        ServiceDiscoveryManager discoveryManager = ServiceDiscoveryManager
            .getInstanceFor(connection);

        if (discoveryManager == null)
            return;

        discoveryManager.removeFeature(Saros.NAMESPACE_SERVER);
    }
}
