package de.fu_berlin.inf.dpp.ui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;

import de.fu_berlin.inf.dpp.net.ConnectionState;

/**
 * Represents the state of the browser application. It consists of an
 * {@link de.fu_berlin.inf.dpp.ui.model.Account}, a list of
 * {@link de.fu_berlin.inf.dpp.ui.model.Contact}s and the
 * {@link de.fu_berlin.inf.dpp.net.ConnectionState}.
 * <p/>
 */
public class State {

    /**
     * Used to avoid null checks in the renderer.
     */
    public final static State INIT_STATE = new State(null,
        Collections.<Contact> emptyList(),
        de.fu_berlin.inf.dpp.net.ConnectionState.NOT_CONNECTED);

    private Account activeAccount;

    private List<Contact> contactList;

    private ConnectionState connectionState;

    /**
     * @param activeAccount
     *            the currently active account
     * @param contactList
     *            the list of contacts of the active account
     * @param connectionState
     *            the current connection state of the active account
     */
    public State(Account activeAccount, List<Contact> contactList,
        ConnectionState connectionState) {
        this.activeAccount = activeAccount;
        this.contactList = new ArrayList<Contact>(contactList);
        this.connectionState = connectionState;
    }

    /**
     * @param activeAccount
     *            the currently active account
     * @param roster
     *            the roster used to fill the contact list
     * @param connectionState
     *            the current connection state of the active account
     */
    public State(Account activeAccount, Roster roster,
        ConnectionState connectionState) {
        this.activeAccount = activeAccount;
        this.contactList = createListOfContacts(roster);
        this.connectionState = connectionState;
    }

    /**
     * Re-create the contact list according to the roster.
     * 
     * @param roster
     *            the roster used to fill the contact list
     */
    public void setContactList(Roster roster) {
        this.contactList = createListOfContacts(roster);
    }

    public void setContactList(List<Contact> contactList) {
        this.contactList = contactList;
    }

    /**
     * Set the currently active account. May be null if there is no account
     * active.
     * 
     * @param activeAccount
     *            the active account or null
     */
    public void setAccount(Account activeAccount) {
        this.activeAccount = activeAccount;
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    /**
     * Returns the active account or null if there is no account active.
     * 
     * @return the active account or null
     */
    public Account getActiveAccount() {
        return activeAccount;
    }

    public List<Contact> getContactList() {
        return contactList;
    }

    public ConnectionState getConnectionState() {
        return this.connectionState;
    }

    /**
     * Adds the roster entries as contact object to a new list.
     * 
     * @param roster
     *            the roster
     * @return the list containing the roster entries as contacts
     */
    private List<Contact> createListOfContacts(Roster roster) {
        List<Contact> res = new ArrayList<Contact>(roster.getEntries().size());
        /*
         * Buggish SMACK crap at its best ! The entries returned here can be
         * just plain references (see implementation) so we have to lookup them
         * correctly !
         */
        for (RosterEntry entryReference : roster.getEntries()) {
            final RosterEntry correctEntry = roster.getEntry(entryReference
                .getUser());

            if (correctEntry == null)
                continue;

            Presence presence = roster.getPresence(correctEntry.getUser());
            res.add(Contact.createContact(correctEntry, presence));
        }
        return res;
    }

}
