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
import de.fu_berlin.inf.dpp.invitation.InvitationProcess;
import de.fu_berlin.inf.dpp.net.internal.extensions.SarosPacketExtension;
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
     * Sends the specified packet to the server.
     * 
     * @param forceSarosCompatibility
     *            if set to <code>true</code> the
     *            {@linkplain Packet#setPacketID(String) packet ID} will be
     *            overwritten with the current Saros Extension version:
     *            {@value SarosPacketExtension#VERSION}
     * 
     * @param packet
     *            the packet to send
     * @throws IOException
     *             if an I/O error occurs or no connection is established to a
     *             XMPP server
     */
    public void sendPacket(Packet packet, boolean forceSarosCompatibility)
        throws IOException;

    /**
     * <p>
     * Sends the given {@link PacketExtension} to the given {@link JID}. The
     * recipient has to be in the session or the extension will not be sent.
     * </p>
     * 
     * <p>
     * If the extension's raw data (bytes) is longer than
     * {@value #MAX_XMPP_MESSAGE_SIZE} or if there is a peer-to-peer bytestream
     * to the recipient the extension will be sent using the bytestream. Else it
     * will be sent by chat.
     * </p>
     * 
     * <p>
     * <s>Note: Does NOT ensure that peers receive messages in order because
     * there may be two completely different communication ways. See
     * {@link de.fu_berlin.inf.dpp.project.internal.ActivitySequencer} for
     * details.</s> There is currently only one communication way enabled !
     * </p>
     * 
     * @param recipient
     * @param extension
     * @throws IOException
     *             if sending by bytestreams fails and the extension raw data is
     *             longer than {@value #MAX_XMPP_MESSAGE_SIZE}
     */
    public void sendToSessionUser(JID recipient, PacketExtension extension)
        throws IOException;

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
    public void sendMessageToUser(JID jid, PacketExtension extension);

    /**
     * Sends a leave message to the participants of given Saros session. See
     * {@link InvitationProcess} for more information when this is supposed be
     * sent.
     * 
     * @param sarosSession
     *            the Saros session that this join message refers to.
     */
    public void sendLeaveMessage(ISarosSession sarosSession);
}
