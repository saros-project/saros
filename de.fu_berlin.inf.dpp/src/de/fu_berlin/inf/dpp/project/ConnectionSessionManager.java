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

    private ConnectionState currentState = ConnectionState.NOT_CONNECTED;
    /**
     * Flag indicating that we're allowed to call disposeConnection().<br>
     * We only want to call disposeConnection() in state NOT_CONNECTED. If we
     * get there from DISCONNECTING, everything is fine, but if we get there
     * from ERROR, we need to know if ever were in the CONNECTED state before
     * the ERROR occurred. That's why we need this flag.
     */
    private boolean disposable = false;

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
                ConnectionState previousState = currentState;
                currentState = newState;

                switch (newState) {
                case CONNECTING:
                    break;

                case CONNECTED:
                    for (ConnectionSessionListener listener : listeners) {
                        listener.prepareConnection(connection);
                    }
                    for (ConnectionSessionListener listener : listeners) {
                        listener.startConnection();
                    }
                    disposable = false;
                    break;

                case DISCONNECTING:
                    for (ConnectionSessionListener listener : Util
                        .reverse(listeners)) {
                        listener.stopConnection();
                    }
                    disposable = true;
                    break;

                case NOT_CONNECTED:
                    if (disposable) {
                        for (ConnectionSessionListener listener : Util
                            .reverse(listeners)) {
                            listener.disposeConnection();
                        }
                        disposable = false;
                    }
                    break;

                case ERROR:
                    // We can only get here from CONNECTED and CONNECTING. If we
                    // get here from CONNECTING, we shouldn't do anything.
                    if (previousState == ConnectionState.CONNECTED) {
                        for (ConnectionSessionListener listener : Util
                            .reverse(listeners)) {
                            listener.stopConnection();
                        }
                        disposable = true;
                    }
                    break;
                }
            }
        });
    }
}
