/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.ui.manager;

import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.net.xmpp.subscription.SubscriptionHandler;
import de.fu_berlin.inf.dpp.ui.browser_functions.ContactListRenderer;
import de.fu_berlin.inf.dpp.ui.browser_functions.ContactListBrowserFunctions;
import de.fu_berlin.inf.dpp.ui.model.Account;
import de.fu_berlin.inf.dpp.ui.model.Contact;
import de.fu_berlin.inf.dpp.ui.model.ContactList;
import de.fu_berlin.inf.dpp.util.ComponentLookup;
import org.apache.log4j.Logger;
import org.eclipse.swt.browser.Browser;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

/**
 * This class manages the connection to the backend for the contact list and
 * calls the appropriate methods in the {@link de.fu_berlin.inf.dpp.ui.browser_functions.ContactListRenderer}
 * class to get the contact list rendered in the browser.
 */
public class ContactListManager {

    private static final Logger LOG = Logger
        .getLogger(ContactListBrowserFunctions.class);

    private final ConnectionHandler connectionHandler;

    private final XMPPConnectionService connectionService;

    private ContactListRenderer contactListRenderer;

    private ContactList contactList;

    private SubscriptionHandler subscriptionHandler;

    private ContactListManager() {
        connectionService = ComponentLookup.getConnectionService();
        connectionHandler = ComponentLookup.getConnectionHandler();
        subscriptionHandler = ComponentLookup.getSubscriptionHandler();

        //TODO this is just a very minimal implementation
        connectionService.addListener(new IConnectionListener() {
            @Override
            public void connectionStateChanged(Connection connection,
                ConnectionState state) {
                switch (state) {
                case CONNECTED:
                    LOG.debug("StateListener: connected!!");
                    contactList = addContactsToContactList(
                        connection.getRoster());
                    contactListRenderer.renderContactList(contactList);
                    break;
                case CONNECTING:
                    LOG.debug("StateListener: connecting ");
                    break;
                case DISCONNECTING:
                    LOG.debug("StateListener: disconnecting");
                    break;
                case ERROR:
                    //TODO better error handling
                    LOG.error("StateListener: error");
                    break;
                case NOT_CONNECTED:
                    LOG.debug("StateListener: not connected");
                    contactList.clear();
                    contactListRenderer.renderContactList(contactList);
                    break;
                }
            }
        });
    }

    /**
     * Create a ContactListManager for a given browser instance.
     * To complete the creation, the communication classes, e.g. browser functions
     * are also created here.
     *
     * @param browser the current SWT browser instance
     * @return the newly created ContactListManager
     */
    public static ContactListManager createManager(Browser browser) {
        ContactListRenderer contactListRenderer = new ContactListRenderer(
            browser);
        ContactListManager contactListManager = new ContactListManager();
        contactListManager.setContactListRenderer(contactListRenderer);
        ContactListBrowserFunctions contactListBrowserFunctions = new ContactListBrowserFunctions(
            browser);
        contactListBrowserFunctions.setContactListManager(contactListManager);
        contactListBrowserFunctions.createJavascriptFunctions();
        return contactListManager;
    }

    /**
     * Adds the roster entries to the contact list managed by this class.
     * Old entries are left untouched.
     *
     * @param roster the roster
     * @return the contact list containing the roster entries
     */
    private ContactList addContactsToContactList(Roster roster) {
        for (RosterEntry contactEntry : roster.getEntries()) {
            Presence presence = roster.getPresence(contactEntry.getUser());
            //TODO consider all type, e.g. subscription pending
            boolean isOnline = presence.getType() == Presence.Type.available;
            contactList.add(new Contact(contactEntry.getUser(), isOnline));
        }
        return contactList;
    }

    /**
     * Connects the given XMPP account to the server.
     *
     * @param account representing an XMPP account
     */
    public void connect(Account account) {
        contactList = new ContactList(account);
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
    public void deleteContact(JID jid) {
        RosterEntry entry = connectionService.getConnection().getRoster()
            .getEntry(jid.getRAW());
        try {
            XMPPUtils
                .removeFromRoster(connectionService.getConnection(), entry);
        } catch (XMPPException e) {
            throw new RuntimeException(e);
        }
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

    public void setContactListRenderer(ContactListRenderer renderer) {
        contactListRenderer = renderer;
    }
}
