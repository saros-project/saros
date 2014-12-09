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

package de.fu_berlin.inf.dpp.net.xmpp;

import org.jivesoftware.smack.ConnectionConfiguration;

/**
 * The content of this class was moved from the ConnectServerAction class of the
 * IntelliJ plug-in as it needed by the new UI module.
 * <p/>
 * TODO there is already one big TODO to move the ConnectionHandler into the core.
 * Either Arndt or Matthias will do this as part of their theses.
 * I just need this one methods now for my next commit.
 */
public class ConnectionConfigurationFactory {

    public static ConnectionConfiguration createConnectionConfiguration(
        String domain, String server, int port, boolean useTLS,
        boolean useSASL) {
        ConnectionConfiguration connectionConfiguration = null;

        if (server.length() == 0)
            connectionConfiguration = new ConnectionConfiguration(domain);
        else
            connectionConfiguration = new ConnectionConfiguration(server, port,
                domain);

        connectionConfiguration.setSASLAuthenticationEnabled(useSASL);

        if (!useTLS)
            connectionConfiguration
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);

        connectionConfiguration.setReconnectionAllowed(false);

        return connectionConfiguration;
    }
}
