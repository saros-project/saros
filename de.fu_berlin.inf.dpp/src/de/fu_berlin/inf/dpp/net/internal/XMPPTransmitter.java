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
package de.fu_berlin.inf.dpp.net.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.InvitationProcess;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject.IncomingTransferObjectExtensionProvider;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.TimedActivityDataObject;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.internal.DefaultInvitationInfo.FileListRequestExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.DefaultInvitationInfo.InvitationAcknowledgementExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.DefaultInvitationInfo.InvitationCompleteExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.DefaultSessionInfo.UserListConfirmationExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.UserListInfo.JoinExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.XStreamExtensionProvider.XStreamPacketExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelProjectSharingExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.LeaveExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensionUtils;
import de.fu_berlin.inf.dpp.net.packet.TimedActivitiesPacket;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.ActivityUtils;
import de.fu_berlin.inf.dpp.util.CausedIOException;
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

    /**
     * Maximum retry attempts to send an activity. Retry attempts will switch to
     * prefer IBB on MAX_TRANSFER_RETRIES/2.
     */
    private static final int MAX_TRANSFER_RETRIES = 4;
    private static final int MAX_XMPP_MESSAGE_SIZE = 4096;

    protected Connection connection;

    protected ChatManager chatmanager;

    protected Map<JID, Chat> chats;

    protected Map<JID, InvitationProcess> processes;

    protected XMPPReceiver receiver;

    protected SarosNet sarosNet;

    @Inject
    protected SessionIDObservable sessionID;

    @Inject
    protected SarosSessionObservable sarosSessionObservable;

    @Inject
    protected LeaveExtension leaveExtension;

    @Inject
    protected CancelInviteExtension cancelInviteExtension;

    @Inject
    protected CancelProjectSharingExtension cancelProjectSharingExtension;

    @Inject
    protected InvitationAcknowledgementExtensionProvider invAcknowledgementExtProv;

    @Inject
    protected FileListRequestExtensionProvider fileListRequestExtProv;

    @Inject
    protected JoinExtensionProvider userListExtProv;

    @Inject
    protected UserListConfirmationExtensionProvider userListConfExtProv;

    @Inject
    protected IncomingTransferObjectExtensionProvider incomingExtProv;

    @Inject
    protected InvitationCompleteExtensionProvider invCompleteExtProv;

    @Inject
    protected DispatchThreadContext dispatchThread;

    protected DataTransferManager dataManager;

    public XMPPTransmitter(SessionIDObservable sessionID,
        DataTransferManager dataManager, SarosNet sarosNet,
        XMPPReceiver receiver) {
        sarosNet.addListener(this);
        this.dataManager = dataManager;
        this.sessionID = sessionID;
        this.sarosNet = sarosNet;
        this.receiver = receiver;
    }

    /********************************************************************************
     * Invitation process' help functions --- START
     ********************************************************************************/

    public void sendInvitationAcknowledgement(JID to, String invitationID) {
        log.trace("Sending invitation acknowledgment to " + Utils.prefix(to));
        sendMessageToUser(to,
            invAcknowledgementExtProv.create(new DefaultInvitationInfo(
                sessionID, invitationID)));
    }

    public void sendFileListRequest(JID to, String invitationID) {
        log.trace("Sending request for FileList to " + Utils.prefix(to));
        sendMessageToUser(to,
            fileListRequestExtProv.create(new DefaultInvitationInfo(sessionID,
                invitationID)));
    }

    public InputStream receiveArchive(String processID, final JID peer,
        final SubMonitor monitor, boolean forceWait) throws IOException,
        SarosCancellationException {

        monitor.beginTask("Receiving archive file", 1);
        log.debug("Receiving archive");
        final PacketFilter filter = PacketExtensionUtils
            .getIncomingTransferObjectFilter(incomingExtProv, sessionID,
                processID, TransferDescription.ARCHIVE_TRANSFER);

        SarosPacketCollector collector = installReceiver(filter);

        monitor
            .subTask("Host is compressing project files. Waiting for the archive file...");
        while (!collector.hasReceived()) {
            if (monitor.isCanceled()) {
                throw new LocalCancellationException();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new LocalCancellationException();
            }
        }

        monitor.subTask("Receiving archive file...");
        try {
            IncomingTransferObject result = incomingExtProv.getPayload(receive(
                monitor.newChild(0), collector, 10000, forceWait));

            if (monitor.isCanceled()) {
                result.reject();
                throw new LocalCancellationException();
            }
            byte[] data = result.accept(monitor.newChild(1));

            return new ByteArrayInputStream(data);
        } finally {
            monitor.done();
        }
    }

    /**
     * Helper for receiving a Packet via XMPPReceiver using
     * SarosPacketCollector.
     */
    public SarosPacketCollector installReceiver(PacketFilter filter) {
        return receiver.createCollector(filter);
    }

    public Packet receive(IProgressMonitor monitor,
        SarosPacketCollector collector, long timeout, boolean forceWait)
        throws LocalCancellationException, IOException {

        if (isConnectionInvalid())
            return null;

        try {
            Packet result;
            do {
                if (monitor.isCanceled())
                    throw new LocalCancellationException();
                monitor.worked(1);
                // Wait up to [timeout] seconds for a result.
                result = collector.nextResult(timeout);
            } while (forceWait && result == null);

            if (result != null)
                return result;
            throw new IOException("Collector timeout: no packet received.");

        } finally {
            collector.cancel();
        }
    }

    public void sendUserList(JID to, String invitationID, Collection<User> users) {
        log.trace("Sending buddy list to " + Utils.prefix(to));
        sendMessageToUser(to, userListExtProv.create(new UserListInfo(
            sessionID, invitationID, users)), true);
    }

    public void sendUserListConfirmation(JID to) {
        log.trace("Sending buddy list confirmation to " + Utils.prefix(to));
        sendMessageToUser(to,
            userListConfExtProv.create(new DefaultSessionInfo(sessionID)), true);
    }

    // FIXME move to XMPPReceiver
    public SarosPacketCollector getUserListConfirmationCollector() {

        PacketFilter filter = PacketExtensionUtils.getSessionIDFilter(
            userListConfExtProv, sessionID);

        return installReceiver(filter);
    }

    // FIXME move to XMPPReceiver
    public boolean receiveUserListConfirmation(SarosPacketCollector collector,
        List<User> fromUsers, SubMonitor monitor)
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

    public void sendInvitationCompleteConfirmation(JID to, String invitationID) {
        sendMessageToUser(to,
            invCompleteExtProv.create(new DefaultInvitationInfo(sessionID,
                invitationID)), true);
    }

    /* *********************************************
     * Invitation process' help functions --- END *
     * *********************************************
     */

    public void sendCancelInvitationMessage(JID user, String errorMsg) {
        log.debug("Send request to cancel Invitation to "
            + Utils.prefix(user)
            + (errorMsg == null ? "on user request" : "with message: "
                + errorMsg));
        sendMessageToUser(user,
            cancelInviteExtension.create(sessionID.getValue(), errorMsg));
    }

    public void sendCancelSharingProjectMessage(JID user, String errorMsg) {
        log.debug("Send request to cancel project sharing to "
            + Utils.prefix(user)
            + (errorMsg == null ? "on user request" : "with message: "
                + errorMsg));
        sendMessageToUser(user, cancelProjectSharingExtension.create(
            sessionID.getValue(), errorMsg));
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendLeaveMessage(ISarosSession sarosSession) {
        sendMessageToAll(sarosSession, leaveExtension.create());
    }

    public void sendTimedActivities(JID recipient,
        List<TimedActivityDataObject> timedActivities) {

        if (recipient == null || recipient.equals(sarosNet.getMyJID())) {
            throw new IllegalArgumentException(
                "Recipient may not be null or equal to the local user");
        }
        if (timedActivities == null || timedActivities.size() == 0) {
            throw new IllegalArgumentException(
                "TimedActivities may not be null or empty");
        }

        String sID = sessionID.getValue();

        de.fu_berlin.inf.dpp.net.packet.Packet packet = new TimedActivitiesPacket(
            new TimedActivities(sID, timedActivities));
        packet.setReceiver(recipient);

        try {
            dataManager.sendPacket(packet);
        } catch (IOException e) {
            log.error("Failed to sent activityDataObjects: " + timedActivities,
                e);
            return;
        }

        String msg = "Sent (" + String.format("%03d", timedActivities.size())
            + ") " + Utils.prefix(recipient) + timedActivities;

        // only log on debug level if there is more than a checksum
        if (ActivityUtils.containsChecksumsOnly(timedActivities))
            log.trace(msg);
        else
            log.debug(msg);
    }

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
     * Note: Does NOT ensure that peers receive messages in order because there
     * may be two completely different communication ways. See
     * {@link de.fu_berlin.inf.dpp.net.internal.ActivitySequencer} for details.
     * </p>
     * 
     * @param recipient
     * @param extension
     * @throws IOException
     *             if sending by bytestreams fails and the extension raw data is
     *             longer than {@value #MAX_XMPP_MESSAGE_SIZE}
     */

    // TODO WHITEBOARD
    public void sendToProjectUser(JID recipient, PacketExtension extension)
        throws IOException {
        /*
         * The TransferDescription can be created out of the session, the name
         * and namespace of the packet extension and standard values and thus
         * transparent to users of this method.
         */
        TransferDescription result = new TransferDescription()
            .setRecipient(recipient)
            .setSender(
                sarosSessionObservable.getValue().getLocalUser().getJID())
            .setType(extension.getElementName())
            .setNamespace(extension.getNamespace())
            .setSessionID(this.sessionID.getValue()).setCompressContent(false);

        sendToProjectUser(recipient, extension, result);
    }

    /**
     * <p>
     * Sends the given {@link PacketExtension} to the given {@link JID}. The
     * recipient has to be in the session or the extension will not be sent.
     * </p>
     * 
     * <p>
     * Callers may provide a {@link TransferDescription}.</br>
     * 
     * Then, if the extension's raw data (bytes) is longer than
     * {@value #MAX_XMPP_MESSAGE_SIZE} or if there is a peer-to-peer bytestream
     * to the recipient the extension will be sent using this bytestream. Else
     * it will be sent by chat.
     * </p>
     * 
     * @param recipient
     * @param extension
     * @param transferDescription
     *            if sent by bytestreams, this data is used to coordinate the
     *            streaming. May be null.
     * @throws IOException
     *             if sending by bytestreams fails and the extension raw data is
     *             longer than {@value #MAX_XMPP_MESSAGE_SIZE}
     */

    public void sendToProjectUser(JID recipient, PacketExtension extension,
        TransferDescription transferDescription) throws IOException {
        sendToProjectUser(recipient, extension, transferDescription, true);
    }

    public void sendToProjectUser(JID recipient, PacketExtension extension,
        TransferDescription transferDescription, boolean onlyInSession)
        throws IOException {

        byte[] data = null;

        try {
            data = extension.toXML().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("UTF-8 is unsupported", e);
        }

        int retry = 0;
        do {

            if (data == null
                || transferDescription == null
                || (!dataManager.getTransferMode(recipient).isP2P() && data.length < MAX_XMPP_MESSAGE_SIZE)) {

                sendMessageToUser(recipient, extension, onlyInSession);
                break;

            } else {
                try {

                    sendByBytestreamToProjectUser(recipient, data,
                        transferDescription);
                    break;

                } catch (IOException e) {
                    // else send by chat if applicable
                    if (data.length < MAX_XMPP_MESSAGE_SIZE) {
                        log.info("Retry failed bytestream transfer by chat.");
                        sendMessageToUser(recipient, extension, true);
                        break;
                    } else {

                        log.error("Failed to sent packet extension by bytestream ("
                            + Utils.formatByte(data.length)
                            + "): "
                            + e.getMessage());

                        if (retry == MAX_TRANSFER_RETRIES / 2) {
                            // set bytestream connections prefer IBB
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

    /**
     * Tries to send the passed byte array to the given {@link JID}.
     * 
     * @param recipient
     * @param data
     * @param transferDescription
     *            to coordinate the the bytestream using XMPP packets
     * @throws IOException
     *             if sending by bytestream fails
     */
    protected void sendByBytestreamToProjectUser(JID recipient, byte[] data,
        TransferDescription transferDescription) throws IOException {
        String user = connection.getUser();

        if (user == null) {
            log.warn("Local user is not logged in to the connection, yet.");
            return;
        }

        try {
            dataManager.sendData(transferDescription, data,
                SubMonitor.convert(new NullProgressMonitor()));
        } catch (SarosCancellationException e) {
            log.error("Cancellation cannot occur, because NullProgressMonitors"
                + " are used on both sides!", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void sendFileLists(JID recipient, String processID,
        List<FileList> fileLists, SubMonitor progress) throws IOException,
        SarosCancellationException {

        String user = connection.getUser();
        if (user == null) {
            log.warn("Local user is not logged in to the connection, yet.");
            return;
        }
        progress.beginTask("Sending file list", 100);

        TransferDescription data = TransferDescription
            .createFileListTransferDescription(recipient, new JID(user),
                sessionID.getValue(), processID);

        log.debug("fileLists.size(): " + fileLists.size());

        int length = fileLists.size();

        List<String> xmlFileLists = new ArrayList<String>(length);

        for (FileList fileList : fileLists)
            xmlFileLists.add(fileList.toXML());

        String xml = Utils.join("---next---", xmlFileLists);

        byte[] content = xml.getBytes("UTF-8");

        data.setSize(content.length);

        // Not showing detailed progress here, because it is going fast, and the
        // user would probably not understand what is happening here...
        dataManager.sendData(data, content,
            progress.newChild(100, SubMonitor.SUPPRESS_ALL_LABELS));

        progress.done();
    }

    public void sendProjectArchive(JID recipient, String invitationID,
        File archive, SubMonitor progress) throws SarosCancellationException,
        IOException {

        progress.beginTask("Sending Archive", 100);

        progress.subTask("Reading archive to memory");

        byte[] content;

        int archiveSize = (int) archive.length();
        content = new byte[archiveSize];
        FileInputStream in = new FileInputStream(archive);
        FileChannel channel = in.getChannel();
        ByteBuffer buffer = ByteBuffer.wrap(content);

        while (!progress.isCanceled() && archiveSize > 0)
            archiveSize -= channel.read(buffer);

        channel.close();
        in.close();

        progress.worked(10);

        TransferDescription transfer = TransferDescription
            .createArchiveTransferDescription(recipient, /* set by DTM */null,
                sessionID.getValue(), invitationID, content.length);

        progress.subTask("Sending project(s) archive ("
            + Utils.formatByte(content.length) + ")");
        dataManager.sendData(transfer, content, progress.newChild(90));
    }

    /**
     * Convenience method for sending the given {@link PacketExtension} to all
     * participants of the given {@link ISarosSession}.
     */
    private void sendMessageToAll(ISarosSession sarosSession,
        PacketExtension extension) {

        JID myJID = sarosNet.getMyJID();

        for (User participant : sarosSession.getParticipants()) {

            if (participant.getJID().equals(myJID))
                continue;
            sendMessageToUser(participant.getJID(), extension, true);
        }
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
    public void sendMessageToUser(JID jid, PacketExtension extension,
        boolean sessionMembersOnly) {
        Message message = new Message();
        message.addExtension(extension);
        message.setTo(jid.toString());
        sendMessageToUser(jid, message, sessionMembersOnly);
    }

    /**
     * Sends a message to a user who is not necessarily in the session.
     */
    public void sendMessageToUser(JID jid, PacketExtension extension) {
        this.sendMessageToUser(jid, extension, false);
    }

    /**
     * Sends the given {@link Message} to the given {@link JID}. The recipient
     * has to be in the session or the message will not be sent. It queues the
     * Message if the participant is OFFLINE.
     * 
     * @param sessionMembersOnly
     *            TODO
     */
    public void sendMessageToUser(JID jid, Message message,
        boolean sessionMembersOnly) {

        ISarosSession session = sarosSessionObservable.getValue();

        User user = null;

        if (session != null)
            user = session.getUser(jid);

        if (sessionMembersOnly) {
            if (session == null) {
                log.error("could not send message because there is no active session");
                return;
            }

            if (user == null) {
                log.warn("user is not in the current session:"
                    + Utils.prefix(jid));
                return;
            }
        }

        try {
            sendMessageWithoutQueueing(jid, message);
        } catch (IOException e) {
            // FIXME the session should do that
            log.error("could not send message to user: " + jid, e);
            if (user != null && session != null) {
                log.info("removing user " + user + " from the current session");
                session.removeUser(user);
            }
        }
    }

    public <T> T sendQuery(JID rqJID, XStreamExtensionProvider<T> provider,
        T payload, long timeout) {
        if (isConnectionInvalid())
            return null;

        // Request the version from a buddy
        IQ request = provider.createIQ(payload);

        request.setType(IQ.Type.GET);
        request.setTo(rqJID.toString());

        // Create a packet collector to listen for a response.
        PacketCollector collector = connection
            .createPacketCollector(new PacketIDFilter(request.getPacketID()));

        try {
            connection.sendPacket(request);

            // Wait up to 5 seconds for a result.
            return provider.getPayload(collector.nextResult(timeout));

        } finally {
            collector.cancel();
        }
    }

    /**
     * Send the given packet to the given user.
     * 
     * If no connection is set or sending fails, this method fails by throwing
     * an IOException
     */
    protected void sendMessageWithoutQueueing(JID jid, Message message)
        throws IOException {

        if (isConnectionInvalid()) {
            throw new IOException("Connection is not open");
        }

        try {
            Chat chat = getChat(jid);
            chat.sendMessage(message);
        } catch (XMPPException e) {
            throw new CausedIOException("Failed to send message", e);
        }
    }

    /**
     * Determines if the connection can be used. Helper method for error
     * handling.
     * 
     * @return false if the connection can be used, true otherwise.
     */
    protected boolean isConnectionInvalid() {
        return connection == null || !connection.isConnected();
    }

    private void putIncomingChat(JID jid, String thread) {

        synchronized (this.chats) {
            if (!this.chats.containsKey(jid)) {
                Chat chat = this.chatmanager.getThreadChat(thread);
                this.chats.put(jid, chat);
            }
        }
    }

    protected Chat getChat(JID jid) {

        if (this.connection == null) {
            throw new NullPointerException("Connection can't be null.");
        }

        synchronized (this.chats) {
            Chat chat = this.chats.get(jid);

            if (chat == null) {
                chat = this.chatmanager.createChat(jid.toString(),
                    new MessageListener() {
                        public void processMessage(Chat arg0, Message arg1) {
                            /*
                             * We don't care about the messages here, because we
                             * are registered as a PacketListener
                             */
                        }
                    });
                this.chats.put(jid, chat);
            }
            return chat;
        }
    }

    protected void prepareConnection(final Connection connection) {

        // Create Containers
        this.chats = new HashMap<JID, Chat>();
        this.processes = Collections
            .synchronizedMap(new HashMap<JID, InvitationProcess>());

        this.connection = connection;

        this.chatmanager = connection.getChatManager();

        // Register a PacketListener which takes care of decoupling the
        // processing of Packets from the Smack thread
        this.connection.addPacketListener(new PacketListener() {

            protected PacketFilter sessionFilter = PacketExtensionUtils
                .getSessionIDPacketFilter(sessionID);

            public void processPacket(final Packet packet) {
                dispatchThread.executeAsDispatch(new Runnable() {
                    public void run() {
                        if (sessionFilter.accept(packet)) {
                            try {
                                Message message = (Message) packet;

                                JID fromJID = new JID(message.getFrom());

                                // Change the input method to get the right
                                // chats
                                putIncomingChat(fromJID, message.getThread());
                            } catch (Exception e) {
                                log.error("An internal error occurred "
                                    + "while processing packets", e);
                            }
                        }
                        receiver.processPacket(packet);
                    }
                });
            }
        }, null);
    }

    protected void disposeConnection() {
        if (connection == null) {
            log.error("disposeConnection() called twice.");
            return;
        }
        chats.clear();
        processes.clear();
        chatmanager = null;
        connection = null;
    }

    public void connectionStateChanged(Connection connection,
        ConnectionState newState) {
        if (newState == ConnectionState.CONNECTED)
            prepareConnection(connection);
        else if (this.connection != null)
            disposeConnection();
    }

    public void sendMessageToUser(JID peer,
        XStreamPacketExtension<DefaultInvitationInfo> create) {
        sendMessageToUser(peer, create, false);
    }
}
