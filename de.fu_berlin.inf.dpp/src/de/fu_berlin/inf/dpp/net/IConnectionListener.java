/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.net;

import org.jivesoftware.smack.XMPPConnection;

import de.fu_berlin.inf.dpp.Saros;

/**
 * A listener for changes to the current connection state. Use
 * {@link Saros#addListener(IConnectionListener)} to attach it.
 * 
 * @author rdjemili
 */
public interface IConnectionListener {

    /**
     * Is fired when the state of the connection changes.
     * 
     * @param connection
     *            The affected XMPP-connection that changed its state
     * @param newState
     *            the new state of the connection. If the new state is
     *            <code>ERROR</code>, you can use
     *            {@link Saros#getConnectionError()} to get the error message.
     */
    public void connectionStateChanged(XMPPConnection connection,
        ConnectionState newState);
}
