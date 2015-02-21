package de.fu_berlin.inf.dpp.ui.manager;

import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.ui.browser_functions.ContactListBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.browser_functions.ContactListRenderer;
import de.fu_berlin.inf.dpp.ui.model.Account;
import de.fu_berlin.inf.dpp.ui.model.ContactList;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;

import java.util.Collection;

/**
 * This class encapsulates listeners for chances in the connection state
 * and contact list.
 * It delegates the display and management those information
 * to the {@link ContactListRenderer}.
 */
public class ContactListManager {

    private static final Logger LOG = Logger
        .getLogger(ContactListManager.class);

    private final XMPPConnectionService connectionService;

    private final ContactListRenderer contactListRenderer;

    private Roster roster;

    public ContactListManager(XMPPConnectionService connectionService,
        ContactListRenderer contactListRenderer) {
        this.connectionService = connectionService;
        this.contactListRenderer = contactListRenderer;
        this.connectionService.addListener(connectionListener);
    }

    //TODO this is just a very minimal implementation
    private final IConnectionListener connectionListener = new IConnectionListener() {
        @Override
        public void connectionStateChanged(Connection connection,
            ConnectionState state) {
            switch (state) {
            case CONNECTED:
                synchronized (ContactListManager.this) {
                    ContactList contactList = new ContactList(
                        new Account(connection.getUser()),
                        connection.getRoster());
                    contactListRenderer.render(state, contactList);
                }
                break;
            case CONNECTING:
                synchronized (ContactListManager.this) {
                    roster = connection.getRoster();
                    roster.addRosterListener(rosterListener);
                    contactListRenderer.renderConnectionState(state);
                }
                break;
            case DISCONNECTING:
                synchronized (ContactListManager.this) {
                    roster.removeRosterListener(rosterListener);
                    contactListRenderer.renderConnectionState(state);
                }
                break;
            case ERROR:
                //TODO better error handling
                LOG.error("StateListener: error");
                break;
            case NOT_CONNECTED:
                contactListRenderer
                    .render(state, ContactList.EMPTY_CONTACT_LIST);
                break;
            }
        }
    };

    private final RosterListener rosterListener = new RosterListener() {
        @Override
        public void entriesAdded(Collection<String> addresses) {
            contactListRenderer.renderContactList(roster);
        }

        @Override
        public void entriesUpdated(Collection<String> addresses) {
            contactListRenderer.renderContactList(roster);
        }

        @Override
        public void entriesDeleted(Collection<String> addresses) {
            contactListRenderer.renderContactList(roster);
        }

        @Override
        public void presenceChanged(Presence presence) {
            contactListRenderer.renderContactList(roster);
        }
    };
}
