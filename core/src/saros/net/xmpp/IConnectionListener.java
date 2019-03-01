/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
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
package saros.net.xmpp;

import org.jivesoftware.smack.Connection;
import saros.net.ConnectionState;

/**
 * A listener for changes to the current connection state. Use {@link
 * XMPPConnectionService#addListener(IConnectionListener)} to attach it.
 *
 * @author rdjemili
 */
public interface IConnectionListener {

  /**
   * Is fired when the state of the connection changes.
   *
   * @param connection The affected XMPP-connection that changed its state
   * @param state the new state of the connection. If the state is <code>ERROR</code>, you can use
   *     {@link XMPPConnectionService#getConnectionError()} to get the error message.
   */
  public void connectionStateChanged(Connection connection, ConnectionState state);
}
