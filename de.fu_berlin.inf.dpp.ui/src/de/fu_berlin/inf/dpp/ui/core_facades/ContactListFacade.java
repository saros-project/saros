package de.fu_berlin.inf.dpp.ui.core_facades;

import de.fu_berlin.inf.dpp.ui.browser_functions.MainPageBrowserFunctions;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.ui.model.Account;

/**
 * Bundles all backend calls for connecting and for managing the contact list.
 *
 * TODO: maybe this class should be split into connection facade and contactlisfacade
 * The downside is that {@link MainPageBrowserFunctions} would have to deal
 * with one additional class dependency
 */
public class ContactListFacade {

    private final ConnectionHandler connectionHandler;

    private final XMPPConnectionService connectionService;

    private final XMPPAccountStore accountStore;

    public ContactListFacade(ConnectionHandler connectionHandler,
        XMPPConnectionService connectionService, XMPPAccountStore accountStore) {
        this.connectionHandler = connectionHandler;
        this.connectionService = connectionService;
        this.accountStore = accountStore;
    }

    /**
     * Connects the given XMPP account to the server.
     * 
     * @param account
     *            representing an XMPP account
     */
    public void connect(Account account) {
        XMPPAccount xmppAccount = accountStore
            .findAccount(account.getBareJid());
        accountStore.setAccountActive(xmppAccount);
        connectionHandler.connect(xmppAccount, false);
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
        Connection connection = connectionService.getConnection();

        if(connection == null){
            return;
        }

        Roster roster = connection.getRoster();

        if(roster == null) {
            return;
        }

        RosterEntry entry = roster.getEntry(jid.getBase());

        if(entry == null) {
            return;
        }

        XMPPUtils.removeFromRoster(connectionService.getConnection(), entry);
    }

    /**
     * Renames a contact (given by JID)
     *
     * @param jid the JID of the contact to be renamed
     * @param name the new name of the contact
     * @throws XMPPException
     */
    public void renameContact(JID jid, String name) throws XMPPException {
        Connection connection = connectionService.getConnection();

        if(connection == null){
            return;
        }

        Roster roster = connection.getRoster();

        if(roster == null) {
            return;
        }

        RosterEntry entry = roster.getEntry(jid.getBase());

        if(entry == null) {
            return;
        }

        entry.setName(name);
    }

    /**
     * Adds a contact to the contact list
     *
     * @param jid the JID of the contact to be added
     * @param nickname the nickname of the contact
     */
    public void addContact(JID jid, String nickname) throws XMPPException {
        Connection connection = connectionService.getConnection();

        if(connection == null){
            return;
        }

        Roster roster = connection.getRoster();

        if(roster == null) {
            return;
        }

        roster.createEntry(jid.getBase(), nickname, null);
    }
}
