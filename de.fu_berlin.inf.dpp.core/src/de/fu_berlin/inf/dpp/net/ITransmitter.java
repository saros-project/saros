/*
 * DPP - Serious Distributed Pair Programming (c) Freie Universität Berlin -
 * Fachbereich Mathematik und Informatik - 2006 (c) Riad Djemili - 2006
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 1, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 */

package de.fu_berlin.inf.dpp.net;

import java.io.IOException;

import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.annotations.Component;

/**
 * A humble interface that is responsible for network functionality. The idea
 * behind this interface is to only encapsulates the least possible amount of
 * functionality - the one that can't be easily tested.
 * 
 * @author rdjemili
 */

@Component(module = "net")
public interface ITransmitter {

    /**
     * @JTourBusStop 3, Architecture Overview, Network Layer - Transmitter:
     * 
     *               The Network Layer is responsible for communicating with
     *               other participants by sending and receiving messages. This
     *               Interface is the main entrance point for sending messages.
     *               (...)
     * 
     */
    /**
     * Sends the specified packet to the server.
     * 
     * @param packet
     *            the packet to send
     * @throws IOException
     *             if an I/O error occurs or no connection is established to a
     *             XMPP server
     */
    public void sendPacket(Packet packet) throws IOException;

    /**
     * Sends the given {@link PacketExtension} to the given {@link JID}.
     * 
     * @param connectionID
     * @param recipient
     * @param extension
     * @throws IOException
     */
    public void sendToSessionUser(String connectionID, JID recipient,
        PacketExtension extension) throws IOException;

    /**
     * Sends the given {@link PacketExtension} to the given {@link JID} over the
     * currently established XMPP connection. There is <b>no</b> guarantee that
     * this message (extension) will arrive at the recipients side !
     * 
     * 
     * @param jid
     *            the recipient of the extension
     * @param extension
     *            the to send
     */
    // FIXME rename to sendExtension
    public void sendMessageToUser(JID jid, PacketExtension extension);
}
