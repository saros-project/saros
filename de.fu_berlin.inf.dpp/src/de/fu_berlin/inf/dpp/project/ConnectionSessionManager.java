package de.fu_berlin.inf.dpp.project;

import org.jivesoftware.smack.XMPPConnection;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Implements the lifecycle management of all ConnectionSessionListeners
 */
public class ConnectionSessionManager {

    @Inject
    ConnectionSessionListener[] listeners;

    public ConnectionSessionManager(Saros saros) {
        saros.addListener(new IConnectionListener() {

            public void connectionStateChanged(XMPPConnection connection,
                ConnectionState newState) {

                switch (newState) {
                case CONNECTED:

                    for (ConnectionSessionListener listener : listeners) {
                        listener.prepare(connection);
                    }

                    for (ConnectionSessionListener listener : listeners) {
                        listener.start();
                    }

                    break;
                case CONNECTING:
                case DISCONNECTING:

                    // Cannot do anything until the Connection is up/down
                    break;

                case ERROR:

                    for (ConnectionSessionListener listener : Util
                        .reverse(listeners)) {
                        listener.stop();
                    }
                    break;

                case NOT_CONNECTED:

                    for (ConnectionSessionListener listener : Util
                        .reverse(listeners)) {
                        listener.stop();
                    }

                    for (ConnectionSessionListener listener : Util
                        .reverse(listeners)) {
                        /*
                         * TODO SS This ConnectionSessionManager violates the
                         * contract of the ConnectionSessionListener (dispose
                         * was called twice!)
                         */
                        listener.dispose();
                    }
                    break;
                }
            }
        });
    }

}
