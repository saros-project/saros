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

package de.fu_berlin.inf.dpp.ui.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the contact list of an account.
 * It consists of an {@link de.fu_berlin.inf.dpp.ui.model.Account}
 * and a list of contact entries.
 */
public class ContactList {

    private Account account;
    private List<Contact> contactList = new ArrayList<Contact>();

    /**
     * @param account the account the contact list belongs to
     */
    public ContactList(Account account) {
        this.account = account;
    }

    /**
     * @return the display name of the active account
     */
    String getDisplayTitle() {
        return account.getUsername() + "@" + account.getDomain();
    }

    /**
     * Adds contact to the contact list
     * @param contact the contact to be added
     */
    public void add(Contact contact) {
        contactList.add(contact);
    }

    /**
     * Clears all entries of the contact list.
     * The account remains set.
     */
    public void clear() {
        contactList.clear();
    }
}
