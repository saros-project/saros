package de.fu_berlin.inf.dpp.ui.browser_functions;

import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandlerCore;
import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.net.xmpp.subscription.SubscriptionHandler;
import de.fu_berlin.inf.dpp.ui.model.Account;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.picocontainer.annotations.Inject;

/**
 * Bundles all backend calls for the contact list.
 */
public class ContactListCoreService {

    private final ConnectionHandlerCore connectionHandler;

    private final XMPPConnectionService connectionService;

    private final SubscriptionHandler subscriptionHandler;

    public ContactListCoreService(ConnectionHandlerCore connectionHandler,
        XMPPConnectionService connectionService,
        SubscriptionHandler subscriptionHandler) {
        this.connectionHandler = connectionHandler;
        this.connectionService = connectionService;
        this.subscriptionHandler = subscriptionHandler;
    }


    /**
     * Connects the given XMPP account to the server.
     *
     * @param account representing an XMPP account
     */
    public void connect(Account account) {
        connectionHandler.connectUser(account.getBareJid());
    }

    /**
     * Disconnects the currently connected account.
     */
    public void disconnect() {
        connectionService.disconnect();
    }

    /**
     * Deletes a contact from the contact list
     *
     * @param jid the JID of the contact to be deleted
     */
    public void deleteContact(JID jid) throws XMPPException {
        RosterEntry entry = connectionService.getConnection().getRoster()
            .getEntry(jid.getRAW());
            XMPPUtils
                .removeFromRoster(connectionService.getConnection(), entry);
        subscriptionHandler.removeSubscription(jid);
    }

    /**
     * Adds a contact to the contact list
     *
     * @param jid the JID of the contact to be added
     */
    public void addContact(JID jid) {
        subscriptionHandler.addSubscription(jid, true);
    }
}
