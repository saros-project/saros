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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserConnectionState;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensionUtils;
import de.fu_berlin.inf.dpp.net.internal.extensions.SarosLeaveExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.SarosPacketExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserListReceivedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserListRequestExtension;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
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

    /*
     * Stefan Rossbach: remove this retry "myth". Only fallback once, and if the
     * fallback to IBB fails just give up.
     */

    /**
     * Maximum retry attempts to send an activity. Retry attempts will switch to
     * prefer IBB on MAX_TRANSFER_RETRIES/2.
     */
    public static final int MAX_TRANSFER_RETRIES = 4;

    private final IReceiver receiver;

    private final SessionIDObservable sessionID;

    private final DataTransferManager dataManager;

    private Connection connection;

    @Inject
    private SarosSessionObservable sarosSessionObservable;

    @Inject
    private SarosLeaveExtension.Provider leaveExtensionProvider;

    @Inject
    private UserListReceivedExtension.Provider userListReceivedExtensionProvider;

    @Inject
    private UserListRequestExtension.Provider userListRequestExtProv;

    public XMPPTransmitter(SessionIDObservable sessionID,
        DataTransferManager dataManager, SarosNet sarosNet, IReceiver receiver) {
        sarosNet.addListener(this);
        this.dataManager = dataManager;
        this.sessionID = sessionID;
        this.receiver = receiver;
    }

    public void sendUserListConfirmation(JID to) {
        log.trace("Sending buddy list confirmation to " + Utils.prefix(to));
        sendMessageToUser(to,
            userListReceivedExtensionProvider
                .create(new UserListReceivedExtension(sessionID.getValue())),
            true);
    }

    // FIXME remove this method !
    private SarosPacketCollector installReceiver(PacketFilter filter) {
        return receiver.createCollector(filter);
    }

    // FIXME move to XMPPReceiver
    @Override
    public SarosPacketCollector getUserListConfirmationCollector() {

        PacketFilter filter = PacketExtensionUtils.getSessionIDFilter(
            userListReceivedExtensionProvider, sessionID);

        return installReceiver(filter);
    }

    // FIXME move to XMPPReceiver
    @Override
    public boolean receiveUserListConfirmation(SarosPacketCollector collector,
        List<User> fromUsers, IProgressMonitor monitor)
        throws LocalCancellationException {

        if (isConnectionInvalid())
            return false;

        ArrayList<JID> fromUserJIDs = new ArrayList<JID>();
        for (User user : fromUsers) {
            fromUserJIDs.add(user.getJID());
        }
        try {
            Packet result;
            JID jid;
            while (fromUserJIDs.size() > 0) {
                if (monitor.isCanceled())
                    throw new LocalCancellationException();

                // Wait up to [timeout] milliseconds for a result.
                result = collector.nextResult(100);
                if (result == null)
                    continue;

                jid = new JID(result.getFrom());
                if (!fromUserJIDs.remove(jid)) {
                    log.warn("Buddy list confirmation from unknown buddy: "
                        + Utils.prefix(jid));
                } else {
                    log.debug("Buddy list confirmation from: "
                        + Utils.prefix(jid));
                }
                /*
                 * TODO: what if a user goes offline during the invitation? The
                 * confirmation will never arrive!
                 */
            }
            return true;
        } finally {
            collector.cancel();
        }
    }

    @Override
    public void sendLeaveMessage(ISarosSession sarosSession) {

        PacketExtension extension = leaveExtensionProvider
            .create(new SarosLeaveExtension(sessionID.getValue()));

        for (User participant : sarosSession.getRemoteUsers())
            sendMessageToUser(participant.getJID(), extension, true);
    }

    @Override
    public void sendUserListRequest(JID user) {
        sendMessageToUser(user,
            userListRequestExtProv.create(new UserListRequestExtension(
                sessionID.getValue())));
    }

    /* Methods to remove from the IFACE END */

    @Override
    public void sendToSessionUser(JID recipient, PacketExtension extension)
        throws IOException {

        String currentSessionID = sessionID.getValue();
        ISarosSession session = sarosSessionObservable.getValue();

        if (session == null)
            throw new IOException("no session running");
        /*
         * The TransferDescription can be created out of the session, the name
         * and namespace of the packet extension and standard values and thus
         * transparent to users of this method.
         */
        TransferDescription transferDescription = TransferDescription
            .createCustomTransferDescription().setRecipient(recipient)
            .setSender(session.getLocalUser().getJID())
            .setType(extension.getElementName())
            .setNamespace(extension.getNamespace())
            .setExtensionVersion(SarosPacketExtension.VERSION)
            .setSessionID(currentSessionID);

        byte[] data = null;

        try {
            data = extension.toXML().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IOException(
                "corrupt JVM installation - UTF-8 charset is not supported", e);
        }

        int retry = 0;
        do {

            if (!dataManager.getTransferMode(recipient).isP2P()
                && data.length < MAX_XMPP_MESSAGE_SIZE
                && ALLOW_CHAT_TRANSFER_FALLBACK) {

                sendMessageToUser(recipient, extension, true);
                break;

            } else {
                try {

                    if (data.length > PACKET_EXTENSION_COMPRESS_THRESHOLD)
                        transferDescription.setCompressContent(true);

                    // recipient is included in the transfer description
                    dataManager.sendData(transferDescription, data);
                    break;

                } catch (IOException e) {
                    // else send by chat if applicable
                    if (data.length < MAX_XMPP_MESSAGE_SIZE
                        && ALLOW_CHAT_TRANSFER_FALLBACK) {
                        log.warn("could not send packet extension through a direct connection, falling back to chat transfer");
                        sendMessageToUser(recipient, extension, true);
                        break;
                    } else {

                        log.error("could not send packet extension through a direct connection ("
                            + Utils.formatByte(data.length)
                            + "): "
                            + e.getMessage());

                        if (retry == MAX_TRANSFER_RETRIES / 2) {
                            // set bytestream connections prefer IBB
                            log.info("enabling fallback mode for recipient: "
                                + recipient);
                            dataManager.setFallbackConnectionMode(recipient);
                        }

                        if (retry < MAX_TRANSFER_RETRIES) {
                            log.info("Transfer retry #" + retry + "...");
                            continue;
                        }
                        throw e;

                    }
                }
            }
        } while (++retry <= MAX_TRANSFER_RETRIES);
    }

    @Override
    public void sendMessageToUser(JID jid, PacketExtension extension) {
        sendMessageToUser(jid, extension, false);
    }

    /**
     * Sends a message to a buddy
     * 
     * @param jid
     *            buddy the message is send to
     * @param extension
     *            extension that is send
     * @param sessionMembersOnly
     *            if true extension is only send if the buddy is in the same
     *            session
     */
    private void sendMessageToUser(JID jid, PacketExtension extension,
        boolean sessionMembersOnly) {
        Message message = new Message();
        message.addExtension(extension);
        message.setTo(jid.toString());
        sendMessageToUser(jid, message, sessionMembersOnly);
    }

    /**
     * Sends the given {@link Message} to the given {@link JID}. The recipient
     * has to be in the session or the message will not be sent.
     * 
     * @param sessionMembersOnly
     * 
     */
    private void sendMessageToUser(JID jid, Message message,
        boolean sessionMembersOnly) {

        final ISarosSession session = sarosSessionObservable.getValue();

        if (sessionMembersOnly) {
            if (session == null) {
                log.warn("could not send message because session has ended");
                return;
            }

            final User participant = session.getUser(jid);

            if (participant == null) {
                log.warn("could not send message to participant "
                    + Utils.prefix(jid)
                    + ", because he/she is no longer part of the current session");
                return;
            }

            /*
             * FIXME: it is possible that a user goes to invisible state ! Once
             * again. Sending data over a state less protocol is not the best
             * design decision !!!
             * 
             * FIXME: the network layer should not handle the state of the
             * current Saros session !!!
             */
            if (participant.getConnectionState() == UserConnectionState.OFFLINE) {
                // FIXME: let the method handle the synchronization, not the
                // caller !
                SWTUtils.runSafeSWTAsync(log, new Runnable() {
                    @Override
                    public void run() {
                        log.info("removing participant " + participant
                            + " from the session because he/she is offline");
                        session.removeUser(participant);
                    }
                });
                return;
            }
        }

        assert jid.toString().equals(message.getTo());

        try {
            sendPacket(message, true);
        } catch (IOException e) {
            log.error("could not send message to " + Utils.prefix(jid), e);
        }
    }

    @Override
    public synchronized void sendPacket(Packet packet,
        boolean forceSarosCompatibility) throws IOException {

        if (isConnectionInvalid())
            throw new IOException("not connected to a XMPP server");

        try {
            if (forceSarosCompatibility)
                packet.setPacketID(SarosPacketExtension.VERSION);

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

    private synchronized void prepareConnection(final Connection connection) {

        this.connection = connection;

        // FIXME: move to XMMPReceiver
        this.connection.addPacketListener(new PacketListener() {

            @Override
            public void processPacket(final Packet packet) {
                receiver.processPacket(packet);
            }
        }, null);
    }

    private synchronized void disposeConnection() {
        if (connection == null) {
            log.error("disposeConnection() called twice.");
            return;
        }
        connection = null;
    }

    @Override
    public synchronized void connectionStateChanged(Connection connection,
        ConnectionState newState) {
        if (newState == ConnectionState.CONNECTED)
            prepareConnection(connection);
        else if (this.connection != null)
            disposeConnection();
    }
}
