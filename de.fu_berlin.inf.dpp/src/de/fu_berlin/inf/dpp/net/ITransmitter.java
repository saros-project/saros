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
import de.fu_berlin.inf.dpp.invitation.SessionNegotiation;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * A humble interface that is responsible for network functionality. The idea
 * behind this interface is to only encapsulates the least possible amount of
 * functionality - the one that can't be easily tested.
 * 
 * @author rdjemili
 */
@Component(module = "net")
public interface ITransmitter {

    public static final int MAX_XMPP_MESSAGE_SIZE = 16378;

    /**
     * Sends the specified packet.
     * 
     * @param packet
     *            the packet to send
     * @throws IOException
     *             if an I/O error occurs
     */
    public void sendPacket(Packet packet) throws IOException;

    /**
     * <p>
     * Sends the given {@link PacketExtension extension} to the given
     * {@link JID recipient}.
     * </p>
     * The underlying implementation must ensure that the extension was sent to
     * the recipient. </p>
     * 
     * @param recipient
     *            the recipient of the extension
     * @param extension
     *            the extension to send
     * @throws IOException
     *             if an I/O error occurs
     */
    public void sendExtensionByStream(JID recipient, PacketExtension extension)
        throws IOException;

    /**
     * <p>
     * Sends the given {@link PacketExtension extension} to the given
     * {@link JID recipient}.
     * </p>
     * <p>
     * It is up to the underlying implementation to ensure that the extension
     * was sent or not.
     * </p>
     * <p>
     * Clients should not use this method when they have to ensure that the
     * extension must reach the recipient.
     * </p>
     * 
     * @param recipient
     *            the recipient of the extension
     * @param extension
     *            the extension to send
     */
    public void sendExtension(JID recipient, PacketExtension extension);

    /**
     * Sends a leave message to the participants of given Saros session. See
     * {@link SessionNegotiation} for more information when this is supposed be
     * sent.
     * 
     * @param sarosSession
     *            the Saros session that this join message refers to.
     */
    public void sendLeaveMessage(ISarosSession sarosSession);
}
