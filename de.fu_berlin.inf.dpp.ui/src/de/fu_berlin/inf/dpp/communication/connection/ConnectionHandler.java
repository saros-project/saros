package de.fu_berlin.inf.dpp.communication.connection;/*
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

import de.fu_berlin.inf.dpp.net.xmpp.ConnectionConfigurationFactory;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import org.jivesoftware.smack.XMPPException;
import org.picocontainer.annotations.Inject;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountLocator;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;

/**
 * TODO the whole connection handler has to be moved to the core
 * this is just a stub which is needed by the HTML-UI
 */
public class ConnectionHandler {

    @Inject
    private XMPPAccountLocator accountLocator;

    @Inject
    private XMPPConnectionService connectionService;

    @Inject
    private XMPPAccountStore accountStore;

    /**
     * Connects the XMPP account with the given name
     *
     * @param username the username of the XMPP account
     */
    public void connectUser(String username) {
        final XMPPAccount account = accountLocator.findAccount(username);
        try {
            connectionService.connect(ConnectionConfigurationFactory
                    .createConnectionConfiguration(account.getDomain(),
                        account.getServer(), account.getPort(),
                        account.useTLS(), account.useSASL()),
                account.getUsername(), account.getPassword());
        } catch (XMPPException e) {
            //TODO error notification / handling
            throw new RuntimeException(e);
        }
        accountStore.setAccountActive(account);
    }
}
