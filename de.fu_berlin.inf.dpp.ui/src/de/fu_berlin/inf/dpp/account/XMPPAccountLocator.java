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

package de.fu_berlin.inf.dpp.account;

import de.fu_berlin.inf.dpp.net.xmpp.JID;
import org.picocontainer.annotations.Inject;

/**
 * The content of this class was moved from the ConnectServerAction class of the
 * IntelliJ plug-in as it needed by the new UI module.
 * It is in a new class as {@link de.fu_berlin.inf.dpp.account.XMPPAccountStore}
 * is already pretty big.
 * TODO this has probably to be moved into the core in a more structured way
 */
public class XMPPAccountLocator {

    @Inject
    private XMPPAccountStore accountStore;

    /**
     * Searches for a username in the account store.
     *
     * @param jidString the jid of the user as string
     * @return the matching XMPP account or null in case of no match
     */
    public XMPPAccount findAccount(String jidString) {
        JID jid = new JID(jidString);
        String username = jid.getName();
        String domain = jid.getDomain();

        for (XMPPAccount account : accountStore.getAllAccounts()) {
            if (domain.equalsIgnoreCase(account.getDomain()) && username
                .equalsIgnoreCase(account.getUsername())) {
                return account;
            }
        }

        return null;
    }
}
