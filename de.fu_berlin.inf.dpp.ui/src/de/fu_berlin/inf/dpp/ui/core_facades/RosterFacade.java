package de.fu_berlin.inf.dpp.ui.core_facades;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.HTMLUIContextFactory;
import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;

/**
 * Bundles all backend calls to alter the currently active account's contact
 * list, or roster.
 */
public class RosterFacade {

    private static final String CONNECTION_STATE_FAILURE = "Invalide state, connection might be lost.";

    private final XMPPConnectionService connectionService;

    /**
     * Created by PicoContainer
     * 
     * @param connectionService
     * @see HTMLUIContextFactory
     */
    public RosterFacade(XMPPConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    /**
     * Deletes a contact from the contact list
     * 
     * @param jid
     *            the JID of the contact to be deleted
     */
    public void deleteContact(JID jid) throws XMPPException {
        try {
            XMPPUtils.removeFromRoster(connectionService.getConnection(),
                getEntry(jid));
        } catch (IllegalStateException e) {
            throw new XMPPException(CONNECTION_STATE_FAILURE, e);
        }
    }

    /**
     * Renames a contact (given by JID)
     * 
     * @param jid
     *            the JID of the contact to be renamed
     * @param name
     *            the new name of the contact
     * @throws XMPPException
     */
    public void renameContact(JID jid, String name) throws XMPPException {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }

        try {
            getEntry(jid).setName(name);
        } catch (IllegalStateException e) {
            throw new XMPPException(CONNECTION_STATE_FAILURE, e);
        }
    }

    /**
     * Adds a contact to the contact list
     * 
     * @param jid
     *            the JID of the contact to be added
     * @param nickname
     *            the nickname of the contact
     */
    public void addContact(JID jid, String nickname) throws XMPPException {
        try {
            getRoster().createEntry(jid.getBase(), nickname, null);
        } catch (IllegalStateException e) {
            throw new XMPPException(CONNECTION_STATE_FAILURE, e);
        }
    }

    /**
     * @param jid
     *            to get the associated roster entry from
     * @return the roster entry for the given jid
     * @throws XMPPException
     *             if the connection isn't established,<br>
     *             if no entry couldn't been found
     */
    private RosterEntry getEntry(JID jid) throws XMPPException {
        RosterEntry entry = getRoster().getEntry(jid.getBase());
        if (entry == null) {
            throw new XMPPException("Couldn't find an entry for "
                + jid.getBareJID());
        }
        return entry;
    }

    /**
     * Note that all modifying methods of the returned roster instance might
     * throw {@link IllegalStateException} if the connection is lost in between
     * operations.
     * 
     * @return the roster for the currently active connection.
     * @throws XMPPException
     *             if the connection isn't established,<br>
     * 
     */
    private Roster getRoster() throws XMPPException {
        Roster roster = connectionService.getRoster();
        if (roster == null) {
            throw new XMPPException(CONNECTION_STATE_FAILURE);
        }

        return roster;
    }
}
