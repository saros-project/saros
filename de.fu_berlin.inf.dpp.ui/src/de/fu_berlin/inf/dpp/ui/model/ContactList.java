package de.fu_berlin.inf.dpp.ui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the contact list of an account.
 * It consists of an {@link de.fu_berlin.inf.dpp.ui.model.Account}
 * and a list of contact entries.
 *
 * Effectively immutable.
 */
public class ContactList {

    /**
     * Used to avoid null checks in the renderer.
     * TODO For the account a special object has to be created
     */
    public final static ContactList EMPTY_CONTACT_LIST = new ContactList(new Account("Not", "Connected"),
        Collections.<Contact>emptyList());

    private final Account account;
    private final List<Contact> contactList;

    /**
     * @param account the account the contact list belongs to
     * @param contactList the list of contacts
     */
    public ContactList(Account account, List<Contact> contactList) {
        this.account = account;
        this.contactList = new ArrayList<Contact>(contactList);
    }

    /**
     * @return the display name of the active account
     */
    String getDisplayTitle() {
        return account.getUsername() + "@" + account.getDomain();
    }
}
