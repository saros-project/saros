package de.fu_berlin.inf.dpp.ui.browser_functions;

import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountLocator;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.net.xmpp.subscription.SubscriptionHandler;
import de.fu_berlin.inf.dpp.ui.model.Account;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

/**
 * Bundles all backend calls for the contact list.
 */
public class ContactListCoreService {

    private final ConnectionHandler connectionHandler;

    private final XMPPConnectionService connectionService;

    private final SubscriptionHandler subscriptionHandler;

    private final XMPPAccountLocator accountLocator;

    private final XMPPAccountStore accountStore;

    public ContactListCoreService(ConnectionHandler connectionHandler,
        XMPPConnectionService connectionService,
        SubscriptionHandler subscriptionHandler,
        XMPPAccountLocator accountLocator, XMPPAccountStore accountStore) {
        this.connectionHandler = connectionHandler;
        this.connectionService = connectionService;
        this.subscriptionHandler = subscriptionHandler;
        this.accountLocator = accountLocator;
        this.accountStore = accountStore;
    }


    /**
     * Connects the given XMPP account to the server.
     *
     * @param account representing an XMPP account
     */
    public void connect(Account account) {
        XMPPAccount xmppAccount = accountLocator.findAccount(account.getBareJid());
        accountStore.setAccountActive(xmppAccount);
        connectionHandler.connect(false);
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
