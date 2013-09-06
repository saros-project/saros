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
package de.fu_berlin.inf.dpp.net.internal;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.internal.extensions.SarosLeaveExtension;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * The one ITransmitter implementation which uses Smack Chat objects.
 * 
 * Hides the complexity of dealing with changing XMPPConnection objects and
 * provides convenience functions for sending messages.
 */
@Component(module = "net")
public class XMPPTransmitter implements ITransmitter, IConnectionListener {

    private static final Logger log = Logger.getLogger(XMPPTransmitter.class);

    /** size in bytes that a packet extension must exceed to be compressed */
    private static final int PACKET_EXTENSION_COMPRESS_THRESHOLD = Integer
        .getInteger(
            "de.fu_berlin.inf.dpp.net.transmitter.PACKET_EXTENSION_COMPRESS_THRESHOLD",
            32);

    private static final boolean ALLOW_CHAT_TRANSFER_FALLBACK = Boolean
        .getBoolean("de.fu_berlin.inf.dpp.net.transmitter.ALLOW_CHAT_TRANSFER_FALLBACK");

    private final SessionIDObservable sessionID;

    private final DataTransferManager dataManager;

    private Connection connection;

    public XMPPTransmitter(SessionIDObservable sessionID,
        DataTransferManager dataManager, SarosNet sarosNet) {
        sarosNet.addListener(this);
        this.dataManager = dataManager;
        this.sessionID = sessionID;
    }

    /* Methods to remove from the IFACE START */

    @Override
    public void sendLeaveMessage(ISarosSession sarosSession) {

        PacketExtension extension = SarosLeaveExtension.PROVIDER
            .create(new SarosLeaveExtension(sessionID.getValue()));

        /*
         * FIXME the new Session-6 feature assumes that the host is the last
         * user who must receive the leave message a.k.a as all other users have
         * removed us from their sessions ... again using P2P here is WTF
         */

        /*
         * HACK notify the host last, using an average amount of 2 seconds
         * before sending the leave message which should be enough under normal
         * circumstances to have the other packets reach their destination about
         * the globe.
         */

        List<User> remoteUsers = sarosSession.getRemoteUsers();

        User host = sarosSession.getHost();

        boolean hostPresent = remoteUsers.contains(host);

        remoteUsers.remove(host);

        for (User user : remoteUsers)
            sendExtension(user.getJID(), extension);

        if (!sarosSession.isHost() && hostPresent) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            sendExtension(host.getJID(), extension);
        }
    }

    /* Methods to remove from the IFACE END */

    @Override
    public void sendExtensionByStream(JID recipient, PacketExtension extension)
        throws IOException {

        /*
         * The TransferDescription can be created out of the session, the name
         * and namespace of the packet extension and standard values and thus
         * transparent to users of this method.
         */
        TransferDescription transferDescription = TransferDescription
            .createCustomTransferDescription().setRecipient(recipient)
            // .setSender(set by DataTransferManager)
            .setType(extension.getElementName())
            .setNamespace(extension.getNamespace());

        byte[] data = extension.toXML().getBytes("UTF-8");

        if (!dataManager.getTransferMode(recipient).isP2P()
            && data.length < MAX_XMPP_MESSAGE_SIZE
            && ALLOW_CHAT_TRANSFER_FALLBACK) {

            sendExtension(recipient, extension);
            return;
        }

        if (data.length > PACKET_EXTENSION_COMPRESS_THRESHOLD)
            transferDescription.setCompressContent(true);

        IOException ioe;

        try {
            // recipient is included in the transfer description
            dataManager.sendData(transferDescription, data);
            return;
        } catch (IOException e) {
            ioe = e;
            log.error(
                "could not send packet extension through a direct connection ("
                    + Utils.formatByte(data.length) + ")", e);
        }

        if (data.length < MAX_XMPP_MESSAGE_SIZE && ALLOW_CHAT_TRANSFER_FALLBACK) {
            log.warn("sending packet extension through chat");
            sendExtension(recipient, extension);
            return;
        }

        throw ioe;
    }

    @Override
    public void sendExtension(JID jid, PacketExtension extension) {
        Message message = new Message();
        message.addExtension(extension);
        message.setTo(jid.toString());

        assert jid.toString().equals(message.getTo());

        try {
            sendPacket(message);
        } catch (IOException e) {
            log.error("could not send message to " + Utils.prefix(jid), e);
        }
    }

    @Override
    public synchronized void sendPacket(Packet packet) throws IOException {

        if (isConnectionInvalid())
            throw new IOException("not connected to a XMPP server");

        try {
            connection.sendPacket(packet);
        } catch (Exception e) {
            throw new IOException("could not send packet " + packet + " : "
                + e.getMessage(), e);
        }
    }

    /**
     * Determines if the connection can be used. Helper method for error
     * handling.
     * 
     * @return false if the connection can be used, true otherwise.
     */
    private synchronized boolean isConnectionInvalid() {
        return connection == null || !connection.isConnected();
    }

    @Override
    public synchronized void connectionStateChanged(Connection connection,
        ConnectionState state) {

        switch (state) {
        case CONNECTING:
            this.connection = connection;
            break;
        case ERROR:
        case NOT_CONNECTED:
            this.connection = null;
            break;
        default:
            break; // NOP
        }
    }
}
