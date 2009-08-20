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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
import org.osgi.framework.Version;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserConnectionState;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.concurrent.management.DocumentChecksum;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess;
import de.fu_berlin.inf.dpp.net.IFileTransferCallback;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.ChecksumErrorExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.ChecksumExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InviteExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.JoinExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.LeaveExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensionUtils;
import de.fu_berlin.inf.dpp.net.internal.extensions.RequestActivityExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.RequestForFileListExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserListExtension;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.observables.SharedProjectObservable;
import de.fu_berlin.inf.dpp.project.ConnectionSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.util.CausedIOException;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * The one ITransmitter implementation which uses Smack Chat objects.
 * 
 * Hides the complexity of dealing with changing XMPPConnection objects and
 * provides convenience functions for sending messages.
 */
@Component(module = "net")
public class XMPPChatTransmitter implements ITransmitter,
    ConnectionSessionListener, IXMPPTransmitter {

    private static Logger log = Logger.getLogger(XMPPChatTransmitter.class
        .getName());

    public static final int MAX_PARALLEL_SENDS = 10;
    public static final int MAX_TRANSFER_RETRIES = 5;
    public static final int FORCEDPART_OFFLINEUSER_AFTERSECS = 60;
    public static final int MAX_XMPP_MESSAGE_SIZE = 16378;

    protected XMPPConnection connection;

    protected ChatManager chatmanager;

    protected Map<JID, Chat> chats;

    protected Map<JID, IInvitationProcess> processes;

    protected List<MessageTransfer> messageTransferQueue;

    @Inject
    protected XMPPChatReceiver receiver;

    @Inject
    protected Saros saros;

    @Inject
    protected SessionIDObservable sessionID;

    @Inject
    protected SharedProjectObservable sharedProject;

    @Inject
    protected ChecksumErrorExtension checksumErrorExtension;

    @Inject
    protected ChecksumExtension checksumExtension;

    @Inject
    protected InviteExtension inviteExtension;

    @Inject
    protected LeaveExtension leaveExtension;

    @Inject
    protected JoinExtension joinExtension;

    @Inject
    protected RequestActivityExtension requestActivityExtension;

    @Inject
    protected UserListExtension userListExtension;

    @Inject
    protected CancelInviteExtension cancelInviteExtension;

    @Inject
    protected RequestForFileListExtension requestForFileListExtension;

    @Inject
    protected ActivitiesExtensionProvider activitiesProvider;

    protected DataTransferManager dataManager;

    public XMPPChatTransmitter(SessionIDObservable sessionID,
        DataTransferManager dataManager) {

        this.dataManager = dataManager;
        this.sessionID = sessionID;

        // TODO Use DI better
        dataManager.chatTransmitter = this;
    }

    protected ExecutorService dispatch = Executors
        .newSingleThreadExecutor(new NamedThreadFactory(
            "XMPPChatTransmitter-Dispatch-"));

    public void executeAsDispatch(Runnable runnable) {
        dispatch.submit(Util.wrapSafe(log, runnable));
    }

    /**
     * A simple struct that is used to queue message transfers.
     */
    protected static class MessageTransfer {
        public JID receipient;
        public Message message;
    }

    public void sendCancelInvitationMessage(JID user, String errorMsg) {
        log.debug("Send request to cancel Invititation to ["
            + user.getBase()
            + "] "
            + (errorMsg == null ? "on user request" : "with message: "
                + errorMsg));
        sendMessage(user, cancelInviteExtension.create(sessionID.getValue(),
            errorMsg));
    }

    public void sendRequestForFileListMessage(JID toJID) {
        log.debug("Send request for FileList to " + toJID);

        sendMessage(toJID, requestForFileListExtension.create());
    }

    public void awaitJingleManager(JID peer) {
        // If other user supports Jingle, make sure that we are done starting
        // the JingleManager
        dataManager.awaitJingleManager(peer);

    }

    public void sendRequestForActivity(ISharedProject sharedProject,
        Map<JID, Integer> expectedSequenceNumbers, boolean andup) {

        // TODO this method is currently not used. Probably they interfere with
        // Jupiter
        if (true) {
            log
                .error(
                    "Unexpected Call to Request for Activity, which is currently disabled:",
                    new StackTrace());
            return;
        }

        for (Entry<JID, Integer> entry : expectedSequenceNumbers.entrySet()) {
            JID recipient = entry.getKey();
            int expectedSequenceNumber = entry.getValue();
            log.info("Requesting old activity (sequence number="
                + expectedSequenceNumber + "," + andup + ") from " + recipient);
            sendMessage(recipient, requestActivityExtension.create(
                expectedSequenceNumber, andup));
        }
    }

    public void sendInviteMessage(ISharedProject sharedProject, JID guest,
        String description, int colorID, Version sarosVersion) {
        log.debug("Send invitation to [" + guest.getBase()
            + "] with description '" + description + "' and Saros version "
            + sarosVersion.toString());
        sendMessage(guest, inviteExtension.create(sharedProject.getProject()
            .getName(), description, colorID, sarosVersion));
    }

    public void sendJoinMessage(ISharedProject sharedProject) {
        try {
            /*
             * HACK sleep process for 1000 millis to ensure invitation state
             * process on host.
             */
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error("Code not designed to be interruptable", e);
            Thread.currentThread().interrupt();
            return;
        }
        sendMessageToAll(sharedProject, joinExtension.create(sharedProject
            .getLocalUser().getColorID()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendLeaveMessage(ISharedProject sharedProject) {
        sendMessageToAll(sharedProject, leaveExtension.create());
    }

    /**
     * Send the given list of timed activities to the given recipient.
     * 
     * If the total size in byte of the timedActivities exceeds
     * MAX_XMPP_MESSAGE_SIZE, the message is not send using XMPP Chat Messages
     * but rather using the DataTransferManager.
     * 
     * TODO: Add Progress
     * 
     * TODO: Add some heuristics for splitting very large lists of
     * timeActivities
     */
    public void sendTimedActivities(JID recipient,
        List<TimedActivity> timedActivities) {

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
            log.error("UTF-8 is unsupported", e); // send as XMPP Message
        }

        if (data == null || data.length < MAX_XMPP_MESSAGE_SIZE) {
            sendMessage(recipient, extensionToSend);
        } else {
            try {
                TransferDescription transferData = TransferDescription
                    .createActivityTransferDescription(recipient, new JID(
                        connection.getUser()), sID);

                // TODO Move to Utils and/or switch to Util.deflate/inflate
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                GZIPOutputStream gos = new GZIPOutputStream(bos);
                IOUtils.write(data, gos);
                gos.finish();
                IOUtils.closeQuietly(gos);

                dataManager.sendData(transferData, bos.toByteArray(),
                    SubMonitor.convert(new NullProgressMonitor()));
            } catch (IOException e) {
                log.error("Failed to sent activities:" + timedActivities, e);
                return;
            }
        }

        log.debug("Sent Activities to " + recipient + ": " + timedActivities);
    }

    /**
     * {@inheritDoc}
     */
    public void sendFileList(JID recipient, FileList fileList,
        SubMonitor progress) throws IOException {

        progress.beginTask("Sending FileList", 100);

        TransferDescription data = TransferDescription
            .createFileListTransferDescription(recipient, new JID(connection
                .getUser()), sessionID.getValue());
        /*
         * TODO [MR] Not portable because String#getBytes() uses the platform's
         * default encoding.
         */
        dataManager.sendData(data, fileList.toXML().getBytes(), progress
            .newChild(100));

        progress.done();
    }

    public void sendFile(JID to, IProject project, IPath path,
        int sequenceNumber, SubMonitor progress) throws IOException {

        progress.beginTask("Sending " + path.lastSegment(), 100);

        TransferDescription transfer = TransferDescription
            .createFileTransferDescription(to, new JID(connection.getUser()),
                path, sessionID.getValue());

        File f = new File(project.getFile(path).getLocation().toOSString());
        if (!f.isFile())
            throw new IOException("No file found for given path: " + path);

        progress.subTask("Reading file " + path.lastSegment());

        // TODO Use Eclipse to read file?
        byte[] content = FileUtils.readFileToByteArray(f);
        progress.newChild(10).done();

        progress.subTask("Sending file " + path.lastSegment());
        dataManager.sendData(transfer, content, progress.newChild(90));

        progress.done();
    }

    public void sendProjectArchive(JID recipient, IProject project,
        File archive, SubMonitor progress) throws IOException {

        progress.beginTask("Sending Archive", 100);

        TransferDescription transfer = TransferDescription
            .createArchiveTransferDescription(recipient, new JID(connection
                .getUser()), sessionID.getValue());

        progress.subTask("Reading archive");
        byte[] content = FileUtils.readFileToByteArray(archive);
        progress.newChild(10).done();

        progress.subTask("Sending archive");
        dataManager.sendData(transfer, content, progress.newChild(90));

        progress.done();
    }

    public void sendUserListTo(JID to, Collection<User> participants) {
        XMPPChatTransmitter.log.debug("Sending user list to " + to.toString());

        sendMessage(to, userListExtension.create(participants));
    }

    public void sendFileChecksumErrorMessage(List<JID> recipients,
        Set<IPath> paths, boolean resolved) {

        XMPPChatTransmitter.log.debug("Sending checksum message ("
            + (resolved ? "resolved" : "error") + ") message of files "
            + Util.toOSString(paths) + " to " + recipients);

        for (JID recipient : recipients) {
            sendMessage(recipient, checksumErrorExtension.create(paths,
                resolved));
        }
    }

    public void sendDocChecksumsToClients(List<JID> recipients,
        Collection<DocumentChecksum> checksums) {

        if (!connection.isConnected())
            return;

        // TODO: Assert on the client side that this message was send by the
        // host
        // assert project.isHost() :
        // "This message should only be called from the host";

        for (JID jid : recipients) {
            try {
                Message message = new Message();
                message.addExtension(checksumExtension.create(checksums));
                sendMessageWithoutQueueing(jid, message);
            } catch (IOException e) {
                // If checksums are failed to be sent, this is not a big problem
                log.warn("Sending Checksum to " + jid + " failed: ", e);
            }
        }
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
            sendMessage(pex.receipient, pex.message);
        }
    }

    /**
     * TODO use sendMessage
     * 
     * @param sharedProject
     * @param extension
     */
    protected void sendMessageToAll(ISharedProject sharedProject,
        PacketExtension extension) {

        JID myJID = saros.getMyJID();

        for (User participant : sharedProject.getParticipants()) {

            if (participant.getJID().equals(myJID))
                continue;

            Message message = new Message();
            message.addExtension(extension);

            // TODO Why is this here and not in sendMessage()!?
            if (participant.getPresence() == UserConnectionState.OFFLINE) {

                /*
                 * TODO [CO] 2009-02-07 This probably does not work anymore! See
                 * Bug #2577390
                 * https://sourceforge.net/tracker2/?func=detail&aid
                 * =2577390&group_id=167540&atid=843359
                 */

                // Offline for too long
                if (participant.getOfflineSeconds() > XMPPChatTransmitter.FORCEDPART_OFFLINEUSER_AFTERSECS) {
                    XMPPChatTransmitter.log
                        .info("Removing offline user from session...");
                    sharedProject.removeUser(participant);
                } else {
                    queueMessage(participant.getJID(), message);
                    XMPPChatTransmitter.log
                        .info("User known as offline - Message queued!");
                }

                continue;
            }

            sendMessage(participant.getJID(), message);
        }
    }

    private void queueMessage(JID jid, Message message) {
        MessageTransfer msg = new MessageTransfer();
        msg.receipient = jid;
        msg.message = message;
        this.messageTransferQueue.add(msg);
    }

    public void sendMessage(JID jid, PacketExtension extension) {
        Message message = new Message();
        message.addExtension(extension);
        sendMessage(jid, message);
    }

    public void sendMessage(JID jid, Message message) {

        // TODO Also queue like in sendMessageToAll if user is offline
        if (this.connection == null || !this.connection.isConnected()) {
            queueMessage(jid, message);
            return;
        }

        try {
            sendMessageWithoutQueueing(jid, message);
        } catch (IOException e) {
            log.info("Could not send message, thus queuing", e);
            queueMessage(jid, message);
        }
    }

    public <T> T sendQuery(JID rqJID, XStreamExtensionProvider<T> provider,
        T payload, long timeout) {
        if (connection == null || !connection.isConnected())
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

        if (!this.connection.isConnected()) {
            throw new IOException("Connection is not open");
        }

        try {
            Chat chat = getChat(jid);
            chat.sendMessage(message);
        } catch (XMPPException e) {
            throw new CausedIOException("Failed to send message", e);
        }
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
                            // We don't care about the messages here, because we
                            // are registered as a PacketListener
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

        final TransferDescription transfer = TransferDescription
            .createFileTransferDescription(recipient, new JID(connection
                .getUser()), path, sessionID.getValue());

        File f = new File(project.getFile(path).getLocation().toOSString());

        final byte[] content = FileUtils.readFileToByteArray(f);

        sendAsyncExecutor.execute(new Runnable() {
            public void run() {
                try {
                    // To test if asynchronously arriving file transfers work:
                    // Thread.sleep(10000);
                    dataManager.sendData(transfer, content, progress);
                    callback.fileSent(path);
                } catch (Exception e) {
                    callback.fileTransferFailed(path, e);
                }
            }
        });
    }

    public void disposeConnection() {
        if (connection == null) {
            log.error("Contract violation: "
                + "ConnectionSessionListener.disposeConnection()"
                + " is called twice");
            return;
        }
        sendAsyncExecutor.shutdownNow();
        chats.clear();
        processes.clear();
        messageTransferQueue.clear();
        chatmanager = null;
        connection = null;
    }

    public void prepareConnection(final XMPPConnection connection) {

        // Create Containers
        this.chats = new HashMap<JID, Chat>();
        this.processes = Collections
            .synchronizedMap(new HashMap<JID, IInvitationProcess>());
        this.messageTransferQueue = Collections
            .synchronizedList(new LinkedList<MessageTransfer>());

        this.sendAsyncExecutor = Executors
            .newFixedThreadPool(MAX_PARALLEL_SENDS);

        this.connection = connection;

        this.chatmanager = connection.getChatManager();

        // Register a packetlistener which takes care of decoupling the
        // processing of Packets from the Smack thread
        this.connection.addPacketListener(new PacketListener() {

            protected PacketFilter sessionFilter = PacketExtensionUtils
                .getSessionIDPacketFilter(sessionID);

            public void processPacket(final Packet packet) {
                executeAsDispatch(new Runnable() {
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

    public void startConnection() {
        // TODO start sending only now, queue otherwise
    }

    public void stopConnection() {
        // TODO stop sending, but queue rather
    }

    /**
     * Get the ExecutorService under which all incoming activities should be
     * executed.
     * 
     * @return
     */
    public ExecutorService getDispatchExecutor() {
        return dispatch;
    }

}
