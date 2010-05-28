package de.fu_berlin.inf.dpp.project;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPConnection;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Implements the life-cycle management of all {@link ConnectionSessionListener}
 * 
 */
@Component(module = "net")
public class ConnectionSessionManager {

    private static final Logger log = Logger
        .getLogger(ConnectionSessionManager.class.getName());

    @Inject
    ConnectionSessionListener[] listeners;

    ConnectionState currentState = ConnectionState.NOT_CONNECTED;

    public ConnectionSessionManager(Saros saros) {
        saros.addListener(new IConnectionListener() {

            public void connectionStateChanged(XMPPConnection connection,
                ConnectionState newState) {

                if (!currentState.isValidFollowState(newState)) {
                    log.error("State Transition Violation - Current: "
                        + currentState + " New: " + newState + " Allowed: "
                        + currentState.getAllowedFollowState(),
                        new StackTrace());
                }
                currentState = newState;

                switch (newState) {
                case CONNECTED:

                    for (ConnectionSessionListener listener : listeners) {
                        listener.prepareConnection(connection);
                    }

                    for (ConnectionSessionListener listener : listeners) {
                        listener.startConnection();
                    }

                    break;
                case CONNECTING:
                    break;
                case DISCONNECTING:
                    for (ConnectionSessionListener listener : Util
                        .reverse(listeners)) {
                        listener.stopConnection();
                    }

                    for (ConnectionSessionListener listener : Util
                        .reverse(listeners)) {
                        /*
                         * TODO SS This ConnectionSessionManager violates the
                         * contract of the ConnectionSessionListener (dispose
                         * was called twice!)
                         */
                        listener.disposeConnection();
                    }

                    // Cannot do anything until the Connection is up/down
                    break;

                case ERROR:

                    for (ConnectionSessionListener listener : Util
                        .reverse(listeners)) {
                        listener.stopConnection();
                    }
                    break;

                case NOT_CONNECTED:

                    break;
                }
            }
        });
    }

}
