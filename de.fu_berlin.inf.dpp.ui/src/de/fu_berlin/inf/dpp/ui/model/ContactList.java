package de.fu_berlin.inf.dpp.ui.model;

import java.util.ArrayList;
import java.util.Collections;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;

import java.util.List;

/**
 * Represents the contact list of an account.
 * It consists of an {@link de.fu_berlin.inf.dpp.ui.model.Account}
 * and a list of contact entries.
 * <p/>
 * Effectively immutable.
 */
public class ContactList {

    /**
     * Used to avoid null checks in the renderer.
     * TODO For the account a special object has to be created
     */
    public final static ContactList EMPTY_CONTACT_LIST = new ContactList(
        new Account("Not", "Connected"), Collections.<Contact>emptyList());

    private final Account account;
    private final List<Contact> contactList;

    /**
     * @param account     the account the contact list belongs to
     * @param contactList the list of contacts
     */
    public ContactList(Account account, List<Contact> contactList) {
        this.account = account;
        this.contactList = new ArrayList<Contact>(contactList);
    }

    /**
     * @param account the account the contact list belongs to
     * @param roster  the roster used to fill the contact list
     */
    public ContactList(Account account, Roster roster) {
        this.account = account;
        contactList = createListOfContacts(roster);
    }

    /**
     * @return the display name of the active account
     */
    String getDisplayTitle() {
        return account.getBareJid();
    }

    /**
     * Re-create the contact list according to the roster. The associated account
     * remains the same.
     * It returns a new instance, the old one remains unchanged.
     *
     * @param roster the roster used to fill the contact list
     * @return a new contact list object
     */
    public ContactList rebuild(Roster roster) {
        return new ContactList(account, createListOfContacts(roster));
    }

    /**
     * Adds the roster entries as contact object to a new list.
     *
     * @param roster the roster
     * @return the list containing the roster entries as contacts
     */
    private List<Contact> createListOfContacts(Roster roster) {
        List<Contact> res = new ArrayList<Contact>(roster.getEntries().size());
        for (RosterEntry entry : roster.getEntries()) {
            Presence presence = roster.getPresence(entry.getUser());
            res.add(Contact.createContact(entry, presence));
        }
        return res;
    }

}
