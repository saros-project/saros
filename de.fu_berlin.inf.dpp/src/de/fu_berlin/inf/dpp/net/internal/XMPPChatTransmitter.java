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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserConnectionState;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
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
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions;
import de.fu_berlin.inf.dpp.net.internal.extensions.RequestActivityExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.RequestForFileListExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserListExtension;
import de.fu_berlin.inf.dpp.project.ConnectionSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * The one ITransmitter implementation which uses Smack Chat objects.
 * 
 * Hides the complexity of dealing with changing XMPPConnection objects and
 * provides convenience functions for sending messages.
 * 
 * @Component The single instance of this class per application is managed by
 *            PicoContainer
 */
public class XMPPChatTransmitter implements ITransmitter,
    ConnectionSessionListener, IXMPPTransmitter {

    private static Logger log = Logger.getLogger(XMPPChatTransmitter.class
        .getName());

    public static final int MAX_PARALLEL_SENDS = 10;
    public static final int MAX_TRANSFER_RETRIES = 5;
    public static final int FORCEDPART_OFFLINEUSER_AFTERSECS = 60;

    protected XMPPConnection connection;

    protected ChatManager chatmanager;

    protected Map<JID, Chat> chats;

    protected Map<JID, IInvitationProcess> processes;

    protected List<MessageTransfer> messageTransferQueue;

    @Inject
    protected XMPPChatReceiver receiver;

    protected DataTransferManager dataManager;

    public XMPPChatTransmitter(DataTransferManager dataManager) {
        // TODO Use DI better
        this.dataManager = dataManager;
        dataManager.chatTransmitter = this;
    }

    ExecutorService dispatch = Executors
        .newSingleThreadExecutor(new NamedThreadFactory(
            "XMPPChatTransmitter-Dispatch"));

    public class XMPPChatTransmitterPacketListener implements PacketListener {

        GodPacketListener godListener = new GodPacketListener();

        PacketFilter sessionFilter = PacketExtensions
            .getSessionIDPacketFilter();

        public void processPacket(final Packet packet) {
            executeAsDispatch(new Runnable() {
                public void run() {
                    if (sessionFilter.accept(packet)) {
                        godListener.processPacket(packet);
                    }
                    receiver.processPacket(packet);
                }
            });
        }
    }

    public void executeAsDispatch(Runnable runnable) {
        dispatch.submit(Util.wrapSafe(log, runnable));
    }

    /**
     * TODO break this up into many individually registered Listeners
     */
    public final class GodPacketListener implements PacketListener {

        private CancelInviteExtension cancelInvite = new CancelInviteExtension() {
            @Override
            public void invitationCanceledReceived(JID sender, String errorMsg) {
                IInvitationProcess process = getInvitationProcess(sender);
                if (process != null) {
                    process.cancel(errorMsg, true);
                } else {
                    log
                        .warn("Received Invitation Canceled message from unknown user ["
                            + sender.getBase() + "]");
                }
            }
        };

        private RequestForFileListExtension requestForFileList = new RequestForFileListExtension() {

            @Override
            public void requestForFileListReceived(final JID sender) {

                XMPPChatTransmitter.log.debug("[" + sender.getName()
                    + "] Request for FileList");

                Util.runSafeAsync("XMPPChatTransmitter-RequestForFileList",
                    log, new Runnable() {
                        public void run() {
                            IInvitationProcess process = getInvitationProcess(sender);
                            if (process != null) {
                                process.invitationAccepted(sender);
                            } else {
                                log
                                    .warn("Received Invitation Acceptance from unknown user ["
                                        + sender.getBase() + "]");
                            }
                        }
                    });
            }
        };

        private JoinExtension join = new JoinExtension() {

            @Override
            public void joinReceived(final JID sender, final int colorID) {

                XMPPChatTransmitter.log.debug("[" + sender.getName()
                    + "] Join: ColorID=" + colorID);

                Util.runSafeAsync("XMPPChatTransmitter-RequestForFileList",
                    log, new Runnable() {
                        public void run() {
                            IInvitationProcess process = getInvitationProcess(sender);
                            if (process != null) {
                                process.joinReceived(sender);
                                return;
                            }

                            ISharedProject project = Saros.getDefault()
                                .getSessionManager().getSharedProject();

                            if (project != null) {
                                // a new user joined this session
                                project.addUser(new User(sender, colorID));
                            }
                        }
                    });
            }
        };

        public void processPacket(Packet packet) {

            try {
                Message message = (Message) packet;

                JID fromJID = new JID(message.getFrom());

                // Change the input method to get the right chats
                putIncomingChat(fromJID, message.getThread());

                if (PacketExtensions.getActvitiesExtension(message) != null) {
                    processActivitiesExtension(message, fromJID);
                }

                join.processPacket(packet);

                requestForFileList.processPacket(packet);

                cancelInvite.processPacket(packet);

            } catch (Exception e) {
                XMPPChatTransmitter.log.error(
                    "An internal error occurred while processing packets", e);
            }
        }

        private void processActivitiesExtension(final Message message,
            JID fromJID) {

            ActivitiesPacketExtension activitiesPacket = PacketExtensions
                .getActvitiesExtension(message);

            List<TimedActivity> timedActivities = activitiesPacket
                .getActivities();

            // FileActivities of type Create are sent via file transfer
            assert containsNoFileCreationActivities(timedActivities);

            receiveActivities(fromJID, timedActivities);
        }
    }

    /**
     * Used for asserting that the given list contains no FileActivities which
     * create files
     */
    public static boolean containsNoFileCreationActivities(
        List<TimedActivity> timedActivities) {

        for (TimedActivity timedActivity : timedActivities) {

            IActivity activity = timedActivity.getActivity();

            if (activity instanceof FileActivity
                && ((FileActivity) activity).getType().equals(
                    FileActivity.Type.Created)) {
                return false;
            }
        }
        return true;
    }

    /**
     * A simple struct that is used to queue message transfers.
     */
    private static class MessageTransfer {
        public JID receipient;
        public PacketExtension packetextension;
    }

    public IInvitationProcess getInvitationProcess(JID jid) {
        return this.processes.get(jid);
    }

    public void addInvitationProcess(IInvitationProcess process) {
        this.processes.put(process.getPeer(), process);
    }

    public void removeInvitationProcess(IInvitationProcess process) {
        this.processes.remove(process.getPeer());
    }

    public void sendCancelInvitationMessage(JID user, String errorMsg) {
        sendMessage(user, CancelInviteExtension.getDefault().create(
            Saros.getDefault().getSessionManager().getSessionID(), errorMsg));
    }

    public void sendRequestForFileListMessage(JID toJID) {

        XMPPChatTransmitter.log.debug("Send request for FileList to " + toJID);

        // If other user supports Jingle, make sure that we are done starting
        // the JingleManager
        dataManager.awaitJingleManager(toJID);

        sendMessage(toJID, RequestForFileListExtension.getDefault().create());

    }

    public void sendRequestForActivity(ISharedProject sharedProject,
        int timestamp, boolean andup) {

        log.info("Requesting old activity (timestamp=" + timestamp + ", "
            + andup + ") from all...");

        // TODO this method is currently not used. Probably they interfere with
        // Jupiter
        if (true) {
            log
                .error(
                    "Unexpected Call to Request for Activity, which is currently disabled:",
                    new StackTrace());
            return;
        }

        sendMessageToAll(sharedProject, RequestActivityExtension.getDefault()
            .create(timestamp, andup));

    }

    public void sendInviteMessage(ISharedProject sharedProject, JID guest,
        String description, int colorID) {
        sendMessage(guest, InviteExtension.getDefault().create(
            sharedProject.getProject().getName(), description, colorID));
    }

    public void sendJoinMessage(ISharedProject sharedProject) {
        try {
            /* sleep process for 1000 millis to ensure invitation state process. */
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        sendMessageToAll(sharedProject, JoinExtension.getDefault().create(
            Saros.getDefault().getLocalUser().getColorID()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendLeaveMessage(ISharedProject sharedProject) {
        sendMessageToAll(sharedProject, LeaveExtension.getDefault().create());
    }

    public void sendActivities(ISharedProject sharedProject,
        ActivitySequencer sequencer, List<IActivity> activities) {

        // Set sender of TextEditActivity's and source if it is not set yet.
        for (IActivity activity : activities) {
            if (activity instanceof TextEditActivity) {
                TextEditActivity textEditActivity = (TextEditActivity) activity;

                String sender = Saros.getDefault().getMyJID().toString();
                textEditActivity.setSender(sender);

                if (textEditActivity.getSource() == null) {
                    /*
                     * Missing source means, we handled the creation of new
                     * TextEditActivities incorrectly
                     */
                    log.error("TextEditActivities does not have a source: "
                        + textEditActivity);
                    textEditActivity.setSource(sender);
                }
            }
        }

        // Send the activities to each user.
        JID myJID = Saros.getDefault().getMyJID();
        for (User user : sharedProject.getParticipants()) {

            JID recipientJID = user.getJID();
            if (recipientJID.equals(myJID)) {
                continue;
            }

            ArrayList<TimedActivity> stillToSend = new ArrayList<TimedActivity>(
                activities.size());
            for (TimedActivity timedActivity : sequencer.createTimedActivities(
                recipientJID, activities)) {

                // Check each activity if it is a file creation which will be
                // send asynchronous, and collect all others in stillToSend.
                // TODO boolean methods with side effects are bad style
                if (!ifFileCreateSendAsync(recipientJID, sharedProject,
                    timedActivity)) {
                    stillToSend.add(timedActivity);
                }
            }

            if (stillToSend.size() > 0) {

                assert containsNoFileCreationActivities(stillToSend);

                sendMessage(recipientJID, new ActivitiesPacketExtension(Saros
                    .getDefault().getSessionManager().getSessionID(),
                    stillToSend));
            }
        }
        XMPPChatTransmitter.log.info("Sent Activities to all: " + activities);
    }

    /**
     * If given {@link TimedActivity} is a file creation, send it asynchronous
     * to given recipient.
     * 
     * @param recipientJID
     * @param sharedProject
     * @param timedActivity
     * 
     * @return <code>true</code> if timedActivity was a file creation, otherwise
     *         <code>false</code>.
     */
    private boolean ifFileCreateSendAsync(JID recipientJID,
        ISharedProject sharedProject, TimedActivity timedActivity) {

        IActivity activity = timedActivity.getActivity();
        if (activity instanceof FileActivity) {
            FileActivity fileActivity = (FileActivity) activity;
            if (fileActivity.getType().equals(FileActivity.Type.Created)) {
                try {
                    sendFileAsync(recipientJID, sharedProject.getProject(),
                        fileActivity.getPath(), timedActivity.getTimestamp(),
                        new AbstractFileTransferCallback() {
                            @Override
                            public void fileTransferFailed(IPath path,
                                Exception e) {
                                log.error("File could not be send:", e);
                            }
                        });
                } catch (IOException e) {
                    log.error("File could not be send:", e);
                    // TODO This means we were really unable to send
                    // this file. No more falling back.
                }
                return true;
            }
        }
        return false;
    }

    public void sendFileList(JID recipient, FileList fileList,
        IFileTransferCallback callback) throws IOException {

        TransferDescription data = TransferDescription
            .createFileListTransferDescription(recipient, new JID(connection
                .getUser()));

        dataManager.sendData(data, fileList.toXML().getBytes(), callback);
    }

    public void sendFile(JID to, IProject project, IPath path, int timestamp,
        IFileTransferCallback callback) throws IOException {

        TransferDescription transfer = TransferDescription
            .createFileTransferDescription(to, new JID(connection.getUser()),
                path, timestamp);

        File f = new File(project.getFile(path).getLocation().toOSString());

        dataManager.sendData(transfer, FileUtils.readFileToByteArray(f),
            callback);
    }

    public void sendProjectArchive(JID recipient, IProject project,
        File archive, IFileTransferCallback callback) {

        TransferDescription transfer = TransferDescription
            .createArchiveTransferDescription(recipient, new JID(connection
                .getUser()));

        // TODO monitor progress
        try {
            dataManager.sendData(transfer, FileUtils
                .readFileToByteArray(archive), callback);
            if (callback != null)
                callback.fileSent(new Path(archive.getName()));

        } catch (IOException e) {
            if (callback != null)
                callback.fileTransferFailed(null, e);
        }
    }

    public void sendUserListTo(JID to, Collection<User> participants) {
        XMPPChatTransmitter.log.debug("Sending user list to " + to.toString());

        sendMessage(to, UserListExtension.getDefault().create(participants));
    }

    public void sendFileChecksumErrorMessage(Set<IPath> paths, boolean resolved) {

        Collection<User> participants = Saros.getDefault().getSessionManager()
            .getSharedProject().getParticipants();

        // Concatenate paths
        StringBuilder sb = new StringBuilder();
        for (IPath path : paths) {
            if (sb.length() > 0)
                sb.append(", ");

            sb.append(path.toOSString());
        }
        String pathsOfInconsistencies = sb.toString();

        XMPPChatTransmitter.log.debug("Sending checksum "
            + (resolved ? "resolved" : "error") + " message of files "
            + pathsOfInconsistencies + " to all");

        for (User user : participants) {
            sendMessage(user.getJID(), ChecksumErrorExtension.getDefault()
                .create(paths, resolved));
        }
    }

    public void sendDocChecksumsToClients(Collection<DocumentChecksum> checksums) {

        if (!connection.isConnected())
            return;

        // send checksums to all clients
        ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();

        if (project == null) {
            return;
        }

        assert project.isHost() : "This message should only be called from the host";

        Collection<User> participants = project.getParticipants();
        if (participants == null) {
            return;
        }

        for (User participant : participants) {

            JID jid = participant.getJID();

            if (project.getHost().getJID().equals(jid)) {
                continue;
            }

            try {
                sendMessageWithoutQueueing(jid, ChecksumExtension.getDefault()
                    .create(checksums));
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
            sendMessage(pex.receipient, pex.packetextension);
        }
    }

    public void sendJupiterRequest(ISharedProject sharedProject,
        Request request, JID jid) {
        XMPPChatTransmitter.log.info("Send Jupiter [" + jid.getName() + "]: "
            + request);
        sendMessage(jid, new RequestPacketExtension(Saros.getDefault()
            .getSessionManager().getSessionID(), request));
    }

    /**
     * TODO use sendMessage
     * 
     * @param sharedProject
     * @param extension
     */
    protected void sendMessageToAll(ISharedProject sharedProject,
        PacketExtension extension) {

        JID myJID = Saros.getDefault().getMyJID();

        for (User participant : sharedProject.getParticipants()) {

            if (participant.getJID().equals(myJID)) {
                continue;
            }

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
                    queueMessage(participant.getJID(), extension);
                    XMPPChatTransmitter.log
                        .info("User known as offline - Message queued!");
                }

                continue;
            }

            sendMessage(participant.getJID(), extension);
        }
    }

    private void queueMessage(JID jid, PacketExtension extension) {
        MessageTransfer msg = new MessageTransfer();
        msg.receipient = jid;
        msg.packetextension = extension;
        this.messageTransferQueue.add(msg);
    }

    public void sendMessage(JID jid, PacketExtension extension) {

        if (!this.connection.isConnected()) {
            queueMessage(jid, extension);
            return;
        }

        try {
            sendMessageWithoutQueueing(jid, extension);
        } catch (IOException e) {
            log.info("Could not send message, thus queuing", e);
            queueMessage(jid, extension);
        }
    }

    /**
     * Send the given packet to the given user.
     * 
     * If no connection is set or sending fails, this method fails by throwing
     * an IOException
     * 
     * @param jid
     * @param extension
     */
    protected void sendMessageWithoutQueueing(JID jid, PacketExtension extension)
        throws IOException {

        if (!this.connection.isConnected()) {
            throw new IOException("Connection is not open");
        }

        try {
            Chat chat = getChat(jid);
            Message message = new Message();
            message.addExtension(extension);
            chat.sendMessage(message);
        } catch (XMPPException e) {
            throw new IOException("Failed to send message", e);
        }
    }

    private void putIncomingChat(JID jid, String thread) {
        if (!this.chats.containsKey(jid)) {
            Chat chat = this.chatmanager.getThreadChat(thread);
            this.chats.put(jid, chat);
        }
    }

    private Chat getChat(JID jid) {
        if (this.connection == null) {
            throw new NullPointerException("Connection can't be null.");
        }

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

    protected ExecutorService executor;

    public void sendFileAsync(JID recipient, IProject project,
        final IPath path, int timestamp, final IFileTransferCallback callback)
        throws IOException {

        if (callback == null)
            throw new IllegalArgumentException();

        final TransferDescription transfer = TransferDescription
            .createFileTransferDescription(recipient, new JID(connection
                .getUser()), path, timestamp);

        File f = new File(project.getFile(path).getLocation().toOSString());

        final byte[] content = FileUtils.readFileToByteArray(f);

        executor.execute(new Runnable() {
            public void run() {
                try {
                    // To test if asynchronously arriving file transfers work:
                    // Thread.sleep(10000);
                    dataManager.sendData(transfer, content, callback);
                    callback.fileSent(path);
                } catch (Exception e) {
                    callback.fileTransferFailed(path, e);
                }
            }
        });
    }

    public void dispose() {
        executor.shutdownNow();
        chats.clear();
        processes.clear();
        messageTransferQueue.clear();
        chatmanager = null;
    }

    public void prepare(final XMPPConnection connection) {

        // Create Containers
        this.chats = new HashMap<JID, Chat>();
        this.processes = Collections
            .synchronizedMap(new HashMap<JID, IInvitationProcess>());
        this.messageTransferQueue = Collections
            .synchronizedList(new LinkedList<MessageTransfer>());

        this.executor = Executors.newFixedThreadPool(MAX_PARALLEL_SENDS);

        this.connection = connection;
        this.chatmanager = connection.getChatManager();

        // Register PacketListeners
        this.connection.addPacketListener(
            new XMPPChatTransmitterPacketListener(), null);
    }

    public void start() {
        // TODO start sending only now, queue otherwise
    }

    public void stop() {
        // TODO stop sending, but queue rather
    }

    /**
     * This method is called from all the different transfer methods, when an
     * activity arrives. This method puts the activity into the
     * ActivitySequencer which will execute it.
     * 
     * @param fromJID
     *            The JID which sent these activities (the source in the
     *            activities might be different!)
     * @param timedActivities
     *            The received activities including timestamps.
     */
    protected void receiveActivities(JID fromJID,
        List<TimedActivity> timedActivities) {
        String source = fromJID.toString();

        final ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();

        if ((project == null) || (project.getParticipant(fromJID) == null)) {
            XMPPChatTransmitter.log.info("Recevied activities from " + source
                + " but User is no participant!");
            return;
        } else {
            XMPPChatTransmitter.log.debug("Received activities from " + source
                + ": " + timedActivities);
        }

        for (TimedActivity timedActivity : timedActivities) {

            IActivity activity = timedActivity.getActivity();

            /*
             * Some activities save space in the message by not setting the
             * source
             */
            if (activity.getSource() == null) {
                activity.setSource(source);
            }

            // Ask sequencer to execute or queue until missing activities arrive
            project.getSequencer().exec(timedActivity);
        }
    }

}
