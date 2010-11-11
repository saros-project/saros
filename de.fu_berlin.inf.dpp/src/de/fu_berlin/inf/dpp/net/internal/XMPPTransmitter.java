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
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.joda.time.DateTime;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserConnectionState;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.InvitationProcess;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.IFileTransferCallback;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject;
import de.fu_berlin.inf.dpp.net.IncomingTransferObject.IncomingTransferObjectExtensionProvider;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivityDataObject;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.internal.DefaultInvitationInfo.FileListRequestExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.DefaultInvitationInfo.InvitationAcknowledgementExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.DefaultInvitationInfo.InvitationCompleteExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.DefaultSessionInfo.UserListConfirmationExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.TransferDescription.FileTransferType;
import de.fu_berlin.inf.dpp.net.internal.UserListInfo.JoinExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.LeaveExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensionUtils;
import de.fu_berlin.inf.dpp.net.internal.extensions.RequestActivityExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserListExtension;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.CausedIOException;
import de.fu_berlin.inf.dpp.util.CommunicationNegotiatingManager.CommunicationPreferences;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;
import de.fu_berlin.inf.dpp.util.log.LoggingUtils;

/**
 * The one ITransmitter implementation which uses Smack Chat objects.
 * 
 * Hides the complexity of dealing with changing XMPPConnection objects and
 * provides convenience functions for sending messages.
 */
@Component(module = "net")
public class XMPPTransmitter implements ITransmitter, IConnectionListener,
    IXMPPTransmitter {

    private static Logger log = Logger.getLogger(XMPPTransmitter.class);

    public static final int MAX_PARALLEL_SENDS = 10;
    public static final int MAX_TRANSFER_RETRIES = 5;
    public static final int FORCEDPART_OFFLINEUSER_AFTERSECS = 60;
    public static final int MAX_XMPP_MESSAGE_SIZE = 16378;

    protected XMPPConnection connection;

    protected ChatManager chatmanager;

    protected Map<JID, Chat> chats;

    protected Map<JID, InvitationProcess> processes;

    protected List<MessageTransfer> messageTransferQueue;

    @Inject
    protected XMPPReceiver receiver;

    protected Saros saros;

    @Inject
    protected SessionIDObservable sessionID;

    @Inject
    protected SarosSessionObservable sarosSessionObservable;

    @Inject
    protected LeaveExtension leaveExtension;

    @Inject
    protected RequestActivityExtension requestActivityExtension;

    @Inject
    protected UserListExtension userListExtension;

    @Inject
    protected CancelInviteExtension cancelInviteExtension;

    @Inject
    protected ActivitiesExtensionProvider activitiesProvider;

    @Inject
    protected InvitationInfo.InvitationExtensionProvider invExtProv;

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
        DataTransferManager dataManager, Saros saros) {
        saros.addListener(this);
        this.dataManager = dataManager;
        this.sessionID = sessionID;
        this.saros = saros;
    }

    /********************************************************************************
     * Invitation process' help functions --- START
     ********************************************************************************/

    public void sendInvitation(String projectID, JID guest, String description,
        int colorID, VersionInfo versionInfo, String invitationID,
        DateTime sessionStart, boolean doStream,
        CommunicationPreferences comPrefs) {

        log.trace("Sending invitation to " + Util.prefix(guest)
            + " with description " + description);

        InvitationInfo invInfo = new InvitationInfo(sessionID, invitationID,
            projectID, description, colorID, versionInfo, sessionStart,
            doStream, comPrefs);

        sendMessageToUser(guest, invExtProv.create(invInfo));
    }

    public SarosPacketCollector getFileListRequestCollector(String invitationID) {
        PacketFilter filter = PacketExtensionUtils.getInvitationFilter(
            fileListRequestExtProv, sessionID, invitationID);

        return installReceiver(filter);
    }

    public boolean receivedInvitationAcknowledgment(String invitationID,
        SubMonitor monitor) throws LocalCancellationException {

        PacketFilter filter = PacketExtensionUtils.getInvitationFilter(
            invAcknowledgementExtProv, sessionID, invitationID);
        SarosPacketCollector collector = installReceiver(filter);

        try {
            receive(monitor, collector, INVITATION_ACKNOWLEDGEMENT_TIMEOUT,
                false);
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public DefaultInvitationInfo receiveFileListRequest(
        SarosPacketCollector collector, String invitationID, SubMonitor monitor)
        throws LocalCancellationException, IOException {

        Packet result = receive(monitor, collector, 500, true);
        return fileListRequestExtProv.getPayload(result);
    }

    public void sendInvitationAcknowledgement(JID to, String invitationID) {
        log.trace("Sending invitation acknowledgment to " + Util.prefix(to));
        sendMessageToUser(to,
            invAcknowledgementExtProv.create(new DefaultInvitationInfo(
                sessionID, invitationID)));
    }

    public void sendFileListRequest(JID to, String invitationID) {
        log.trace("Sending request for FileList to " + Util.prefix(to));
        sendMessageToUser(to,
            fileListRequestExtProv.create(new DefaultInvitationInfo(sessionID,
                invitationID)));
    }

    public FileList receiveFileList(SarosPacketCollector collector,
        SubMonitor monitor, boolean forceWait)
        throws SarosCancellationException, IOException {

        log.trace("Waiting for FileList from ");

        IncomingTransferObject result = incomingExtProv.getPayload(receive(
            monitor, collector, 500, true));

        if (monitor.isCanceled()) {
            result.reject();
            throw new LocalCancellationException();
        }

        byte[] data = result.accept(monitor);
        String fileListAsString;
        try {
            fileListAsString = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            fileListAsString = new String(data);
        }

        // should return null if it's not parseable.
        return FileList.fromXML(fileListAsString);
    }

    public SarosPacketCollector getInvitationCollector(String invitationID,
        FileTransferType type) {

        PacketFilter filter = PacketExtensionUtils
            .getIncomingTransferObjectFilter(incomingExtProv, sessionID,
                invitationID, type);

        return installReceiver(filter);
    }

    /**
     * TODO: think about handling timeouts.
     */
    public InputStream receiveArchive(SarosPacketCollector collector,
        SubMonitor monitor, boolean forceWait) throws IOException,
        SarosCancellationException {

        monitor.beginTask("Receiving archive", 100);

        try {
            IncomingTransferObject result = incomingExtProv.getPayload(receive(
                monitor.newChild(10), collector, 1000, forceWait));

            if (monitor.isCanceled()) {
                result.reject();
                throw new LocalCancellationException();
            }
            byte[] data = result.accept(monitor.newChild(90));

            return new ByteArrayInputStream(data);
        } finally {
            monitor.done();
        }
    }

    /**
     * Helper for receiving a Packet via XMPPReceiver using
     * SarosPacketCollector.
     */
    protected SarosPacketCollector installReceiver(PacketFilter filter) {
        return receiver.createCollector(filter);
    }

    protected Packet receive(SubMonitor monitor,
        SarosPacketCollector collector, long timeout, boolean forceWait)
        throws LocalCancellationException, IOException {

        if (isConnectionInvalid())
            return null;

        try {
            Packet result;
            do {
                if (monitor.isCanceled())
                    throw new LocalCancellationException();
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
        log.trace("Sending userList to " + Util.prefix(to));
        sendMessageToProjectUser(to, userListExtProv.create(new UserListInfo(
            sessionID, invitationID, users)));
    }

    public void sendUserListConfirmation(JID to) {
        log.trace("Sending userListConfirmation to " + Util.prefix(to));
        sendMessageToProjectUser(to,
            userListConfExtProv.create(new DefaultSessionInfo(sessionID)));
    }

    public SarosPacketCollector getUserListConfirmationCollector() {

        PacketFilter filter = PacketExtensionUtils.getSessionIDFilter(
            userListConfExtProv, sessionID);

        return installReceiver(filter);
    }

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
                    log.warn("UserListConfirmation from unknown user: "
                        + Util.prefix(jid));
                } else {
                    log.debug("UserListConfirmation from: " + Util.prefix(jid));
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

    public SarosPacketCollector getInvitationCompleteCollector(
        String invitationID) {

        PacketFilter filter = PacketExtensionUtils.getInvitationFilter(
            invCompleteExtProv, sessionID, invitationID);
        return installReceiver(filter);
    }

    public void receiveInvitationCompleteConfirmation(SubMonitor monitor,
        SarosPacketCollector collector) throws LocalCancellationException,
        IOException {

        receive(monitor, collector, 500, true);
    }

    public void sendInvitationCompleteConfirmation(JID to, String invitationID) {
        sendMessageToProjectUser(to,
            invCompleteExtProv.create(new DefaultInvitationInfo(sessionID,
                invitationID)));
    }

    /********************************************************************************
     * Invitation process' help functions --- END
     ********************************************************************************/

    /**
     * A simple struct that is used to queue message transfers.
     */
    protected static class MessageTransfer {
        public JID receipient;
        public Message message;
    }

    public void sendCancelInvitationMessage(JID user, String errorMsg) {
        log.debug("Send request to cancel Invitation to "
            + Util.prefix(user)
            + (errorMsg == null ? "on user request" : "with message: "
                + errorMsg));
        sendMessageToUser(user,
            cancelInviteExtension.create(sessionID.getValue(), errorMsg));
    }

    // TODO: Remove this method.
    public void awaitJingleManager(JID peer) {
        // If other user supports Jingle, make sure that we are done starting
        // the JingleManager
        dataManager.awaitJingleManager(peer);
    }

    public void sendRequestForActivity(ISarosSession sarosSession,
        Map<JID, Integer> expectedSequenceNumbers, boolean andup) {

        // TODO this method is currently not used. Probably they interfere with
        // Jupiter
        if (true) {
            log.error("Unexpected Call to Request for Activity,"
                + " which is currently disabled:", new StackTrace());
            return;
        }

        for (Entry<JID, Integer> entry : expectedSequenceNumbers.entrySet()) {
            JID recipient = entry.getKey();
            int expectedSequenceNumber = entry.getValue();
            log.info("Requesting old activityDataObject (sequence number="
                + expectedSequenceNumber + "," + andup + ") from "
                + Util.prefix(recipient));
            sendMessageToProjectUser(recipient,
                requestActivityExtension.create(expectedSequenceNumber, andup));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendLeaveMessage(ISarosSession sarosSession) {
        sendMessageToAll(sarosSession, leaveExtension.create());
    }

    /**
     * Send the given list of timed activityDataObjects to the given recipient.
     * 
     * If the total size in byte of the timedActivities exceeds
     * MAX_XMPP_MESSAGE_SIZE, the message is not send using XMPP Chat Messages
     * but rather using the DataTransferManager.
     * 
     * TODO: Add Progress
     */
    public void sendTimedActivities(JID recipient,
        List<TimedActivityDataObject> timedActivities) {

        if (recipient == null || recipient.equals(saros.getMyJID())) {
            throw new IllegalArgumentException(
                "Recipient may not be null or equal to the local user");
        }
        if (timedActivities == null || timedActivities.size() == 0) {
            throw new IllegalArgumentException(
                "TimedActivities may not be null or empty");
        }

        String sID = sessionID.getValue();
        PacketExtension extensionToSend = activitiesProvider.create(sID,
            timedActivities);
        byte[] data = null;
        try {
            data = extensionToSend.toXML().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("UTF-8 is unsupported", e);
        }

        if (data == null || data.length < MAX_XMPP_MESSAGE_SIZE) {
            // send as XMPP Message
            sendMessageToProjectUser(recipient, extensionToSend);
        } else {
            // send using DataTransferManager
            try {
                String user = connection.getUser();
                if (user == null) {
                    log.warn("Local user is not logged in to the connection, yet.");
                    return;
                }
                TransferDescription transferData = TransferDescription
                    .createActivityTransferDescription(recipient,
                        new JID(user), sID);

                dataManager.sendData(transferData, data,
                    SubMonitor.convert(new NullProgressMonitor()));
            } catch (IOException e) {
                log.error(
                    "Failed to sent activityDataObjects ("
                        + Util.formatByte(data.length) + "): "
                        + timedActivities, e);
                return;
            } catch (SarosCancellationException e) {
                log.error(
                    "Cancellation cannot occur, because NullProgressMonitors"
                        + " are used on both sides!", e);
            }
        }
        String msg = "Sent (" + String.format("%03d", timedActivities.size())
            + ") " + Util.prefix(recipient) + timedActivities;

        // only log on debug level if there is more than a checksum
        if (LoggingUtils.containsChecksumsOnly(timedActivities))
            log.trace(msg);
        else
            log.debug(msg);
    }

    /**
     * {@inheritDoc}
     */
    public void sendFileList(JID recipient, String invitationID,
        FileList fileList, SubMonitor progress) throws IOException,
        SarosCancellationException {

        String user = connection.getUser();
        if (user == null) {
            log.warn("Local user is not logged in to the connection, yet.");
            return;
        }
        progress.beginTask("Sending FileList", 100);

        TransferDescription data = TransferDescription
            .createFileListTransferDescription(recipient, new JID(user),
                sessionID.getValue(), invitationID);

        String xml = fileList.toXML();

        byte[] content = xml.getBytes("UTF-8");

        dataManager.sendData(data, content, progress.newChild(100));

        progress.done();
    }

    public void sendFile(JID to, IProject project, IPath path,
        int sequenceNumber, SubMonitor progress) throws IOException,
        SarosCancellationException {

        String user = connection.getUser();
        if (user == null) {
            log.warn("Local user is not logged in to the connection, yet.");
            return;
        }
        progress.beginTask("Sending " + path.lastSegment(), 100);

        TransferDescription transfer = TransferDescription
            .createFileTransferDescription(to, new JID(user), path,
                sessionID.getValue());

        File f = new File(project.getFile(path).getLocation().toOSString());
        if (!f.isFile())
            throw new IOException("No file found for given path: " + path);

        progress.subTask("Reading file " + path.lastSegment());

        // TODO Use Eclipse to read file?
        byte[] content = FileUtils.readFileToByteArray(f);
        progress.worked(10);

        progress.subTask("Sending file " + path.lastSegment());
        dataManager.sendData(transfer, content, progress.newChild(90));
        progress.done();
    }

    public void sendProjectArchive(JID recipient, String invitationID,
        File archive, SubMonitor progress) throws SarosCancellationException,
        IOException {

        String user = connection.getUser();
        if (user == null) {
            log.warn("Local user is not logged in to the connection, yet.");
            return;
        }
        progress.beginTask("Sending Archive", 100);

        TransferDescription transfer = TransferDescription
            .createArchiveTransferDescription(recipient, new JID(user),
                sessionID.getValue(), invitationID);

        progress.subTask("Reading archive");

        byte[] content = archive == null ? new byte[0] : FileUtils
            .readFileToByteArray(archive);
        progress.worked(10);

        progress.subTask("Sending archive");
        dataManager.sendData(transfer, content, progress.newChild(90));

        progress.done();
    }

    public void sendRemainingFiles() {

        log.warn("Sending remaining files is not implemented!");
        //
        // if (this.fileTransferQueue.size() > 0) {
        // // sendNextFile();
        // }
    }

    public void sendRemainingMessages() {

        List<MessageTransfer> toTransfer = null;

        synchronized (messageTransferQueue) {
            toTransfer = new ArrayList<MessageTransfer>(messageTransferQueue);
            messageTransferQueue.clear();
        }

        for (MessageTransfer pex : toTransfer) {
            sendMessageToProjectUser(pex.receipient, pex.message);
        }
    }

    /**
     * Convenience method for sending the given {@link PacketExtension} to all
     * participants of the given {@link ISarosSession}.
     */
    protected void sendMessageToAll(ISarosSession sarosSession,
        PacketExtension extension) {

        JID myJID = saros.getMyJID();

        for (User participant : sarosSession.getParticipants()) {

            if (participant.getJID().equals(myJID))
                continue;
            sendMessageToProjectUser(participant.getJID(), extension);
        }
    }

    private void queueMessage(JID jid, Message message) {
        MessageTransfer msg = new MessageTransfer();
        msg.receipient = jid;
        msg.message = message;
        this.messageTransferQueue.add(msg);
    }

    /**
     * The recipient has to be in the session or the message will not be sent.
     */
    public void sendMessageToProjectUser(JID jid, PacketExtension extension) {
        Message message = new Message();
        message.addExtension(extension);
        message.setTo(jid.toString());
        sendMessageToProjectUser(jid, message);
    }

    /**
     * Sends a message to a user who is not necessarily in the session.
     */
    public void sendMessageToUser(JID jid, PacketExtension extension) {
        Message message = new Message();
        message.addExtension(extension);
        message.setTo(jid.toString());
        try {
            sendMessageWithoutQueueing(jid, message);
        } catch (IOException e) {
            log.info("Could not send message, thus queueing", e);
            queueMessage(jid, message);
        }
    }

    /**
     * Sends the given {@link Message} to the given {@link JID}. The recipient
     * has to be in the session or the message will not be sent. It queues the
     * Message if the participant is OFFLINE.
     */
    protected void sendMessageToProjectUser(JID jid, Message message) {

        User participant = sarosSessionObservable.getValue().getUser(jid);
        if (participant == null) {
            log.warn("User not in session:" + Util.prefix(jid));
            return;
        }

        if (participant.getConnectionState() == UserConnectionState.OFFLINE) {
            /*
             * TODO This probably does not work anymore! See Feature Request
             * #2577390
             */
            // remove participant if he/she is offline too long
            if (participant.getOfflineSeconds() > XMPPTransmitter.FORCEDPART_OFFLINEUSER_AFTERSECS) {
                log.info("Removing offline user from session...");
                sarosSessionObservable.getValue().removeUser(participant);
            } else {
                queueMessage(jid, message);
                log.info("User known as offline - Message queued!");
            }
            return;
        }

        try {
            sendMessageWithoutQueueing(jid, message);
        } catch (IOException e) {
            log.info("Could not send message, thus queueing", e);
            queueMessage(jid, message);
        }
    }

    public <T> T sendQuery(JID rqJID, XStreamExtensionProvider<T> provider,
        T payload, long timeout) {
        if (isConnectionInvalid())
            return null;

        // Request the version from a remote user
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

    /**
     * Executor service used by sendFileAsync to queue the sending of files.
     */
    protected ExecutorService sendAsyncExecutor;

    /**
     * Utility method for #sendFile() which makes a copy of the file and returns
     * immediately while sending in the background.
     */
    public void sendFileAsync(JID recipient, IProject project,
        final IPath path, int sequenceNumber,
        final IFileTransferCallback callback, final SubMonitor progress)
        throws IOException {

        if (callback == null)
            throw new IllegalArgumentException();

        String user = connection.getUser();
        if (user == null) {
            log.warn("Local user is not logged in to the connection, yet.");
            return;
        }
        final TransferDescription transfer = TransferDescription
            .createFileTransferDescription(recipient, new JID(user), path,
                sessionID.getValue());

        File f = new File(project.getFile(path).getLocation().toOSString());

        final byte[] content = FileUtils.readFileToByteArray(f);

        sendAsyncExecutor.execute(new Runnable() {
            public void run() {
                try {
                    // To test if asynchronously arriving file transfers work:
                    // Thread.sleep(10000);
                    dataManager.sendData(transfer, content,
                        progress.newChild(100));
                    callback.fileSent(path);
                } catch (Exception e) {
                    callback.fileTransferFailed(path, e);
                }
            }
        });
    }

    protected void prepareConnection(final XMPPConnection connection) {

        // Create Containers
        this.chats = new HashMap<JID, Chat>();
        this.processes = Collections
            .synchronizedMap(new HashMap<JID, InvitationProcess>());
        this.messageTransferQueue = Collections
            .synchronizedList(new LinkedList<MessageTransfer>());

        this.sendAsyncExecutor = Executors
            .newFixedThreadPool(MAX_PARALLEL_SENDS);

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
        sendAsyncExecutor.shutdownNow();
        chats.clear();
        processes.clear();
        messageTransferQueue.clear();
        chatmanager = null;
        connection = null;
    }

    public void connectionStateChanged(XMPPConnection connection,
        ConnectionState newState) {
        if (newState == ConnectionState.CONNECTED)
            prepareConnection(connection);
        else if (this.connection != null)
            disposeConnection();
    }
}
