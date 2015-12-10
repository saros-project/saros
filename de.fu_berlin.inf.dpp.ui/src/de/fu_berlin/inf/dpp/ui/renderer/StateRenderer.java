package de.fu_berlin.inf.dpp.ui.renderer;

import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;

import de.fu_berlin.inf.ag_se.browser.extensions.IJQueryBrowser;
import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.ui.JavaScriptAPI;
import de.fu_berlin.inf.dpp.ui.model.Account;
import de.fu_berlin.inf.dpp.ui.model.Contact;
import de.fu_berlin.inf.dpp.ui.model.State;

/**
 * This class is responsible for transferring information about the state of the
 * application to the browser so they can be displayed. These information are
 * encapsulated in {@link de.fu_berlin.inf.dpp.ui.model.State}.
 * 
 * For convenience, this class also manages the
 * {@link de.fu_berlin.inf.dpp.ui.model.State} via listeners for the
 * {@link de.fu_berlin.inf.dpp.net.ConnectionState} and the
 * {@link org.jivesoftware.smack.Roster}, from which the list of
 * {@link de.fu_berlin.inf.dpp.ui.model.Contact}s is created.
 */
public class StateRenderer extends Renderer {

    private static final Logger LOG = Logger.getLogger(StateRenderer.class);

    private final XMPPConnectionService connectionService;

    private Roster roster;

    private State state = State.INIT_STATE;

    /**
     * Created by PicoContainer
     * 
     * @param connectionService
     * @see HTMLUIContextFactory
     */
    public StateRenderer(XMPPConnectionService connectionService) {
        this.connectionService = connectionService;
        this.connectionService.addListener(connectionListener);
    }

    @Override
    public synchronized void render(IJQueryBrowser browser) {
        JavaScriptAPI.updateState(browser, this.state);
    }

    private final IConnectionListener connectionListener = new IConnectionListener() {
        @Override
        public void connectionStateChanged(Connection connection,
            ConnectionState connectionState) {

            switch (connectionState) {
            case CONNECTED:
                synchronized (StateRenderer.this) {
                    state = new State(new Account(connection.getUser()),
                        connection.getRoster(), connectionState);
                }
                break;
            case CONNECTING:
                synchronized (StateRenderer.this) {
                    roster = connection.getRoster();
                    roster.addRosterListener(rosterListener);
                    state.setConnectionState(connectionState);
                }
                break;
            case DISCONNECTING:
                synchronized (StateRenderer.this) {
                    roster.removeRosterListener(rosterListener);
                    state.setConnectionState(connectionState);
                }
                break;
            case NOT_CONNECTED:
                state.setAccount(null);
                state.setContactList(Collections.<Contact> emptyList());
                state.setConnectionState(connectionState);
                break;
            case ERROR:
                state.setAccount(null);
                state.setContactList(Collections.<Contact> emptyList());
                state.setConnectionState(ConnectionState.NOT_CONNECTED);
                // TODO better error handling
                LOG.error("StateListener: error");
                break;
            default:
                LOG.error("Undefined connection state");
            }

            render();
        }
    };

    private final RosterListener rosterListener = new RosterListener() {
        @Override
        public void entriesAdded(Collection<String> addresses) {
            state.setContactList(roster);
            render();
        }

        @Override
        public void entriesUpdated(Collection<String> addresses) {
            state.setContactList(roster);
            render();
        }

        @Override
        public void entriesDeleted(Collection<String> addresses) {
            state.setContactList(roster);
            render();
        }

        @Override
        public void presenceChanged(Presence presence) {
            state.setContactList(roster);
            render();
        }
    };
}
