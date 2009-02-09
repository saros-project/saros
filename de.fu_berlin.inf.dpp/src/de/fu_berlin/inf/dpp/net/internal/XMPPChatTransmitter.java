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
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.dnd.TransferData;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.PreferenceConstants;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserConnectionState;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.management.DocumentChecksum;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.TransferMode;
import de.fu_berlin.inf.dpp.net.IFileTransferCallback;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.net.business.InvitationHandler;
import de.fu_berlin.inf.dpp.net.business.LeaveHandler;
import de.fu_berlin.inf.dpp.net.business.RequestForActivityHandler;
import de.fu_berlin.inf.dpp.net.business.UserListHandler;
import de.fu_berlin.inf.dpp.net.internal.extensions.CancelInviteExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.ChecksumErrorExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.ChecksumExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.DataTransferExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InviteExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.JoinExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.LeaveExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions;
import de.fu_berlin.inf.dpp.net.internal.extensions.RequestActivityExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.RequestForFileListExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserListExtension;
import de.fu_berlin.inf.dpp.net.jingle.IJingleFileTransferListener;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferManager;
import de.fu_berlin.inf.dpp.net.jingle.JingleSessionException;
import de.fu_berlin.inf.dpp.project.ISessionManager;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager.ConnectionSessionListener;
import de.fu_berlin.inf.dpp.util.StackTrace;

/**
 * The one ITransmitter implementation which uses Smack Chat objects.
 * 
 * The instance of this class to use will change when the XMPP Connection is
 * disconnected.
 * 
 */
public class XMPPChatTransmitter implements ITransmitter,
    ConnectionSessionListener, IXMPPTransmitter {

    private static Logger log = Logger.getLogger(XMPPChatTransmitter.class
        .getName());

    private static final int MAX_PARALLEL_SENDS = 10;
    private static final int MAX_TRANSFER_RETRIES = 5;
    private static final int FORCEDPART_OFFLINEUSER_AFTERSECS = 60;

    private XMPPConnection connection;

    private ChatManager chatmanager;

    private final Map<JID, Chat> chats = new HashMap<JID, Chat>();

    private FileTransferManager fileTransferManager;

    private final List<IInvitationProcess> processes = new CopyOnWriteArrayList<IInvitationProcess>();

    private final ConcurrentLinkedQueue<TransferData> fileTransferQueue = new ConcurrentLinkedQueue<TransferData>();
    private final List<MessageTransfer> messageTransferQueue = Collections
        .synchronizedList(new LinkedList<MessageTransfer>());
    private final Map<String, IncomingFile> incomingFiles = new HashMap<String, IncomingFile>();

    private JingleFileTransferManager jingleManager;

    public JingleFileTransferManager getJingleManager() {
        try {
            if (startingJingleThread == null)
                return null;

            startingJingleThread.join();
        } catch (InterruptedException e) {
            // do nothing
        }
        return jingleManager;
    }

    private Thread startingJingleThread;

    protected long lastReceivedActivityTime;

    protected JingleDiscoveryManager jingleDiscovery;

    /**
     * A simple struct that is used to manage incoming chunked files via
     * chat-file transfer
     */
    private class IncomingFile {
        String name;
        int receivedChunks;
        int chunkCount;
        List<String> messageBuffer;

        IncomingFile() {
            this.messageBuffer = new LinkedList<String>();
        }

        boolean isComplete() {
            return (this.receivedChunks == this.chunkCount);
        }
    }

    private final class JingleTransferListener implements
        IJingleFileTransferListener {

        public void incomingData(TransferDescription data, InputStream input) {
            setLastUsedTransferMode(data.getSender(), TransferMode.JINGLE);
            receiveData(data, input);
        }

        public void connected(String protocol, String remote) {
            // ignore, because we only need to know when a file arrived
        }
    }

    private class IBBTransferListener implements FileTransferListener {

        public void fileTransferRequest(final FileTransferRequest request) {

            new Thread(new Runnable() {

                public void run() {
                    try {
                        TransferDescription data = TransferDescription
                            .fromBase64(request.getDescription());

                        XMPPChatTransmitter.log
                            .debug("Incoming file transfer via IBB: "
                                + data.toString());

                        IncomingFileTransfer accept = request.accept();

                        InputStream in = accept.recieveFile();

                        byte[] content;
                        try {
                            content = IOUtils.toByteArray(in);
                        } finally {
                            IOUtils.closeQuietly(in);
                        }
                        setLastUsedTransferMode(data.getSender(),
                            TransferMode.IBB);
                        receiveData(data, new ByteArrayInputStream(content));

                    } catch (Exception e) {
                        XMPPChatTransmitter.log.error(
                            "Incoming File Transfer via IBB failed: ", e);
                        for (IInvitationProcess process : XMPPChatTransmitter.this.processes) {
                            if (process.getPeer().equals(
                                new JID(request.getRequestor()))) {
                                process.cancel(e.getMessage(), false);
                            }
                        }
                    }

                }
            }).start();
        }
    }

    public static String read(InputStream input) throws IOException {

        try {
            byte[] content = IOUtils.toByteArray(input);

            try {
                return new String(content, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return new String(content);
            }
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    /**
     * TODO break this up into many individually registered Listeners
     */
    public final class GodPacketListener implements PacketListener {

        private CancelInviteExtension cancelInvite = new CancelInviteExtension() {
            @Override
            public void invitationCanceledReceived(JID sender, String errorMsg) {
                for (IInvitationProcess process : processes) {
                    if (process.getPeer().equals(sender)) {
                        process.cancel(errorMsg, true);
                    }
                }
            }
        };

        private RequestForFileListExtension requestForFileList = new RequestForFileListExtension() {

            @Override
            public void requestForFileListReceived(final JID sender) {

                XMPPChatTransmitter.log
                    .debug("Received Request for FileList from " + sender);

                new Thread(new Runnable() {
                    public void run() {
                        for (IInvitationProcess process : processes) {
                            if (process.getPeer().equals(sender)) {
                                process.invitationAccepted(sender);
                            }
                        }
                    }
                }).start();
            }
        };

        private JoinExtension join = new JoinExtension() {

            @Override
            public void joinReceived(JID sender, int colorID) {
                log.debug("Join: ColorID=" + colorID);

                for (IInvitationProcess process : processes) {
                    if (process.getPeer().equals(sender)) {
                        process.joinReceived(sender);
                        return;
                    }
                }

                ISharedProject project = Saros.getDefault().getSessionManager()
                    .getSharedProject();

                if (project != null) {
                    // a new user joined this session
                    project.addUser(new User(sender, colorID));
                }
            }
        };

        private DataTransferExtension dataTransfer = new DataTransferExtension() {

            /**
             * Receives a data buffer sent by a chat message. The data will be
             * decoded from base64 encoding. Splitted transfer will be buffered
             * until all chunks are received. Then the file will be
             * reconstructed and processed as a whole.
             */
            @Override
            public void chunkReceived(JID fromJID, String sName, String desc,
                int index, int maxIndex, String sData) {

                setLastUsedTransferMode(fromJID, TransferMode.DEFAULT);

                TransferDescription transferDescription;
                try {
                    transferDescription = TransferDescription.fromBase64(desc);
                } catch (IOException e) {
                    log
                        .error(
                            "Error while decoding TransferDescription in ChatTransfer",
                            e);
                    return;
                }

                // Is this a transfer with multiple parts?
                if (maxIndex > 1) {

                    XMPPChatTransmitter.log.debug("Received chunk " + index
                        + " of " + maxIndex + " of "
                        + transferDescription.toString());

                    // check for previous chunks
                    IncomingFile ifile = incomingFiles.get(desc);
                    if (ifile == null) {
                        /*
                         * this is the first received chunk->create incoming
                         * file object
                         */
                        ifile = new IncomingFile();
                        ifile.receivedChunks++;
                        ifile.chunkCount = maxIndex;
                        ifile.name = sName;
                        for (int i = 0; i < maxIndex; i++) {
                            ifile.messageBuffer.add(null);
                        }
                        ifile.messageBuffer.set(index - 1, sData);
                        incomingFiles.put(sName, ifile);
                        return;
                    } else {
                        // this is a following chunk
                        ifile.receivedChunks++;
                        ifile.messageBuffer.set(index - 1, sData);

                        if (ifile.isComplete() == false) {
                            return;
                        } else {

                            // join the buffers to restore the file from chunks
                            sData = "";
                            for (int i = 0; i < maxIndex; i++) {
                                sData += ifile.messageBuffer.get(i);
                            }
                            incomingFiles.remove(ifile);
                        }
                    }
                }

                byte[] dataOrg = Base64.decodeBase64(sData.getBytes());
                if (dataOrg == null)
                    return;

                receiveData(transferDescription, new ByteArrayInputStream(
                    dataOrg));
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

                dataTransfer.processPacket(packet);

                requestForFileList.processPacket(packet);

                cancelInvite.processPacket(packet);

            } catch (Exception e) {
                XMPPChatTransmitter.log.error(
                    "An internal error occurred while processing packets", e);
            }
        }

        private void processActivitiesExtension(final Message message,
            JID fromJID) {

            final ISharedProject project = Saros.getDefault()
                .getSessionManager().getSharedProject();

            ActivitiesPacketExtension activitiesPacket = PacketExtensions
                .getActvitiesExtension(message);

            List<TimedActivity> timedActivities = activitiesPacket
                .getActivities();

            String source = fromJID.toString();

            if ((project == null) || (project.getParticipant(fromJID) == null)) {
                XMPPChatTransmitter.log.info("Recevied activities from "
                    + source + " but User is no participant!");
                return;
            } else {
                XMPPChatTransmitter.log.debug("Received activities from "
                    + source + ": " + timedActivities);
            }

            for (TimedActivity timedActivity : timedActivities) {

                IActivity activity = timedActivity.getActivity();
                activity.setSource(source);

                /*
                 * incoming fileActivities that add files are only used as
                 * placeholder to bump the timestamp. the real fileActivity will
                 * be processed by using a file transfer.
                 */
                if (!(activity instanceof FileActivity)
                    || !((FileActivity) activity).getType().equals(
                        FileActivity.Type.Created)) {

                    project.getSequencer().exec(timedActivity);

                }
            }
        }
    }

    /**
     * A simple struct that is used to queue message transfers.
     */
    private class MessageTransfer {
        public JID receipient;
        public PacketExtension packetextension;
    }

    public void setXMPPConnection(final XMPPConnection connection) {

        this.connection = connection;
        this.chatmanager = connection.getChatManager();

        this.fileTransferManager = new FileTransferManager(connection);
        this.fileTransferManager
            .addFileTransferListener(new IBBTransferListener());

        OutgoingFileTransfer
            .setResponseTimeout(XMPPChatTransmitter.MAX_TRANSFER_RETRIES * 1000);

        this.chats.clear();

        // Create JingleDiscoveryManager
        jingleDiscovery = new JingleDiscoveryManager(connection);

        // Register PacketListeners
        this.connection.addPacketListener(new InvitationHandler(this),
            new AndFilter(new MessageTypeFilter(Message.Type.chat),
                InviteExtension.getDefault().getFilter()));

        LeaveHandler handler = new LeaveHandler();
        this.connection.addPacketListener(handler, handler.getFilter());

        RequestForActivityHandler activityHandler = new RequestForActivityHandler(
            this);
        this.connection.addPacketListener(activityHandler, activityHandler
            .getFilter());

        UserListHandler userListHandler = new UserListHandler(this);
        this.connection.addPacketListener(userListHandler, userListHandler
            .getFilter());

        this.connection.addPacketListener(new GodPacketListener(),
            PacketExtensions.getSessionIDPacketFilter());

        if (!getFileTransferModeViaChat()) {
            // Start Jingle Manager asynchronous
            this.startingJingleThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        jingleManager = new JingleFileTransferManager(
                            connection, new JingleTransferListener());
                        log.debug("Jingle Manager started");
                    } catch (Exception e) {
                        log.error("Jingle Manager could not be started", e);
                        jingleManager = null;
                    }
                }
            });
            this.startingJingleThread.start();
        }
    }

    public void addInvitationProcess(IInvitationProcess process) {
        this.processes.add(process);
    }

    public void removeInvitationProcess(IInvitationProcess process) {
        this.processes.remove(process);
    }

    public void sendCancelInvitationMessage(JID user, String errorMsg) {
        sendMessage(user, CancelInviteExtension.getDefault().create(
            Saros.getDefault().getSessionManager().getSessionID(), errorMsg));
    }

    public void sendRequestForFileListMessage(JID toJID) {

        XMPPChatTransmitter.log.debug("Send request for FileList to " + toJID);

        // If other user supports Jingle, make sure that we are done starting
        // the JingleManager
        if (jingleDiscovery.getCachedJingleSupport(toJID)) {
            getJingleManager();
        }

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

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendActivities(ISharedProject sharedProject,
        List<TimedActivity> timedActivities) {

        for (TimedActivity timedActivity : timedActivities) {
            IActivity activity = timedActivity.getActivity();

            if (activity instanceof TextEditActivity) {
                log.debug("sendActivities: " + activity);
                TextEditActivity textEditActivity = (TextEditActivity) activity;
                textEditActivity.setSource(Saros.getDefault().getMyJID()
                    .toString());
            }

            if (activity instanceof FileActivity) {
                FileActivity fileAdd = (FileActivity) activity;

                if (fileAdd.getType().equals(FileActivity.Type.Created)) {
                    JID myJID = Saros.getDefault().getMyJID();

                    for (User participant : sharedProject.getParticipants()) {
                        JID jid = participant.getJID();
                        if (jid.equals(myJID)) {
                            continue;
                        }

                        // TODO use callback
                        int time = timedActivity.getTimestamp();
                        try {
                            IFileTransferCallback callback = new AbstractFileTransferCallback() {
                                @Override
                                public void fileTransferFailed(IPath path,
                                    Exception e) {
                                    log.error("File could not be send:", e);
                                }
                            };
                            sendFileAsync(jid, sharedProject.getProject(),
                                fileAdd.getPath(), time, callback);
                        } catch (IOException e) {
                            log.error("File could not be send:", e);
                            // TODO This means we were really unable to send
                            // this file. No more falling back.
                        }
                    }
                }
            } else {
                sharedProject.getSequencer().getActivityHistory().add(
                    timedActivity);

                // TODO: removed very old entries
            }
        }

        XMPPChatTransmitter.log.info("Sent activities: " + timedActivities);

        if (timedActivities != null) {
            sendMessageToAll(sharedProject, new ActivitiesPacketExtension(Saros
                .getDefault().getSessionManager().getSessionID(),
                timedActivities));
        }

    }

    protected void sendData(TransferDescription jingleFileTransferData,
        byte[] content) throws IOException {

        // TODO Buffer correctly when not connected....
        // this.fileTransferQueue.offer(transfer);
        // sendNextFile();

        /*
         * TODO This is not a safe way to determine whether the user really
         * supports Jingle at this point in time - He might have left and
         * reconnected and changed his Jingle settings in between
         */
        if (getFileTransferModeViaChat()
            || !jingleDiscovery
                .getCachedJingleSupport(jingleFileTransferData.recipient)) {

            ibb.send(jingleFileTransferData, content);

        } else {

            try {
                jingle.send(jingleFileTransferData, content);
            } catch (Exception e) {
                // TODO Catch only IOException and RuntimeException
                log.error("Failed to send file with jingle, fall back to IBB",
                    e);
                ibb.send(jingleFileTransferData, content);

                // Fall back to ChatTransfer:
                // handmade.send(data)
            }
        }
    }

    public interface Transmitter {

        /**
         * Send the given data as a blocking operation.
         * 
         * If this call returns the data has been send successfully, otherwise
         * an IOException is thrown with the reason why the transfer failed.
         * 
         * @param data
         *            The data to be sent.
         * @throws IOException
         *             if the send failed
         */
        public void send(TransferDescription data, byte[] content)
            throws IOException;

    }

    /**
     * Sends a data buffer to a recipient using chat messages. The buffer is
     * transmitted Base64 encoded and split into blocks of size MAX_MSG_LENGTH.
     * 
     * This is not IBB (XEP-96)!!
     * 
     */
    Transmitter handmade = new Transmitter() {

        public void send(TransferDescription data, byte[] content)
            throws IOException {

            final int maxMsgLen = Saros.getDefault().getPreferenceStore()
                .getInt(PreferenceConstants.CHATFILETRANSFER_CHUNKSIZE);

            // Convert byte array to base64 string
            byte[] bytes64 = Base64.encodeBase64(content);

            String data64;
            try {
                data64 = new String(bytes64, "UTF-8");
            } catch (UnsupportedCharsetException e1) {
                data64 = new String(bytes64);
            }

            // send large data sets in several messages
            int tosend = data64.length();
            int pcount = (tosend / maxMsgLen)
                + ((tosend % maxMsgLen == 0) ? 0 : 1);
            int start = 0;
            try {
                for (int i = 1; i <= pcount; i++) {
                    int psize = Math.min(tosend, maxMsgLen);
                    int end = start + psize;

                    PacketExtension extension = DataTransferExtension
                        .getDefault().create("Filename managed by Description",
                            data.toBase64(), i, pcount,
                            data64.substring(start, end));

                    sendMessage(data.getRecipient(), extension);

                    start = end;
                    tosend -= psize;

                }
            } catch (Exception e) {
                throw new IOException("Sending failed", e);
            }
        }
    };

    Transmitter ibb = new Transmitter() {

        public void send(TransferDescription data, byte[] content)
            throws IOException {

            log.debug("Sending via IBB: " + data.toString());

            OutgoingFileTransfer
                .setResponseTimeout(XMPPChatTransmitter.MAX_TRANSFER_RETRIES * 1000);
            OutgoingFileTransfer transfer = fileTransferManager
                .createOutgoingFileTransfer(data.getRecipient().toString());

            FileTransferProgressMonitor monitor = new FileTransferProgressMonitor(
                transfer);

            // The file path is irrelevant
            transfer.sendStream(new ByteArrayInputStream(content),
                "Filename managed by Description", content.length, data
                    .toBase64());

            /* wait for complete transfer. */
            while (monitor.isRunning()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }

            if (monitor.getMonitoringException() != null) {
                throw new IOException("RuntimeError in IBB-FileTransfer: ",
                    monitor.getMonitoringException());
            }

            if (transfer.getStatus() == Status.error) {
                throw new IOException("XMPPError in IBB-FileTransfer: "
                    + transfer.getError());
            }

            if (transfer.getStatus() != Status.complete) {
                throw new IOException("Error in IBB-FileTransfer wrong state: "
                    + transfer.getStatus());
            }
        }
    };

    Transmitter jingle = new Transmitter() {

        public void send(TransferDescription data, byte[] content)
            throws IOException {
            try {
                JingleFileTransferManager jftm = getJingleManager();
                if (jftm == null)
                    throw new IOException("Jingle is disabled");

                jftm.send(data, content);
            } catch (JingleSessionException e) {
                throw new IOException(e);
            }
        }
    };

    public void sendFileList(JID recipient, FileList fileList)
        throws IOException {

        TransferDescription data = TransferDescription
            .createFileListTransferDescription(recipient, new JID(connection
                .getUser()));

        sendData(data, fileList.toXML().getBytes());
    }

    public void sendFile(JID to, IProject project, IPath path, int timestamp)
        throws IOException {

        TransferDescription transfer = TransferDescription
            .createFileTransferDescription(to, new JID(connection.getUser()),
                path, timestamp);

        File f = new File(project.getFile(path).getLocation().toOSString());

        sendData(transfer, FileUtils.readFileToByteArray(f));
    }

    public void sendProjectArchive(JID recipient, IProject project,
        File archive, IFileTransferCallback callback) {

        TransferDescription transfer = TransferDescription
            .createArchiveTransferDescription(recipient, new JID(connection
                .getUser()));

        // TODO monitor progress
        try {
            sendData(transfer, FileUtils.readFileToByteArray(archive));
            if (callback != null)
                callback.fileSent(new Path(archive.getName()));

        } catch (IOException e) {
            if (callback != null)
                callback.fileTransferFailed(null, e);
        }
    }

    //
    // private void sendNextFile() {
    //
    // final TransferData transfer = this.fileTransferQueue.poll();
    //
    // if (transfer == null) {
    // XMPPChatTransmitter.log.debug("No file to send in queue.");
    // return;
    // }
    //
    // executor.execute(new Runnable() {
    // public void run() {
    // try {
    // sendData(transfer);
    // } catch (Exception e) {
    // transfer.callback.fileTransferFailed(transfer.path, e);
    // } finally {
    // sendNextFile();
    // }
    // }
    // });
    // }

    public void sendUserListTo(JID to, Collection<User> participants) {
        XMPPChatTransmitter.log.debug("Sending user list to " + to.toString());

        sendMessage(to, UserListExtension.getDefault().create(participants));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.fu_berlin.inf.dpp.net.ITransmitter#sendFileChecksumErrorMessage(org
     * .eclipse .core.runtime.IPath)
     */
    public void sendFileChecksumErrorMessage(IPath path, boolean resolved) {

        Collection<User> participants = Saros.getDefault().getSessionManager()
            .getSharedProject().getParticipants();

        XMPPChatTransmitter.log.debug("Sending checksum error message of file "
            + path.lastSegment() + " to all");
        for (User user : participants) {
            sendMessage(user.getJID(), ChecksumErrorExtension.getDefault()
                .create(path, resolved));
        }
    }

    /**
     * 
     * @see de.fu_berlin.inf.dpp.net.ITransmitter
     */
    public void sendDocChecksumsToClients(Collection<DocumentChecksum> checksums) {
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
            if (project.getHost().getJID().equals(participant.getJID())) {
                continue;
            }

            JID jid = participant.getJID();

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

        if (this.fileTransferQueue.size() > 0) {
            // sendNextFile();
        }
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
        XMPPChatTransmitter.log.info("send request to : " + jid + " request: "
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
        PacketExtension extension) { // HACK

        JID myJID = Saros.getDefault().getMyJID();

        for (User participant : sharedProject.getParticipants()) {

            if (participant.getJID().equals(myJID)) {
                continue;
            }

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

    public static boolean getFileTransferModeViaChat() {
        return Saros.getDefault().getPreferenceStore().getBoolean(
            PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT);

    }

    ExecutorService executor = Executors.newFixedThreadPool(MAX_PARALLEL_SENDS);

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
                    sendData(transfer, content);
                    callback.fileSent(path);
                } catch (Exception e) {
                    callback.fileTransferFailed(path, e);
                }
            }
        });
    }

    public void dispose() {
        executor.shutdownNow();
    }

    public void prepare(XMPPConnection connection) {
        setXMPPConnection(connection);
    }

    public void start() {
        // TODO start sending only now, queue otherwise
    }

    public void stop() {
        // TODO stop sending, but queue rather
    }

    public void setLastUsedTransferMode(JID from, TransferMode mode) {
        for (IInvitationProcess process : processes) {
            if (process.getPeer().equals(from))
                process.setTransferMode(mode);
        }
    }

    protected void receiveData(TransferDescription data, InputStream input) {

        try {
            switch (data.type) {
            case ARCHIVE_TRANSFER:
                receivedArchive(data, input);
                break;
            case FILELIST_TRANSFER:
                receivedFileList(data, input);
                break;
            case RESOURCE_TRANSFER:
                receivedResource(data.sender, new Path(data.file_project_path),
                    input, data.timestamp);
                break;
            }
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    protected void receivedResource(JID from, Path path, InputStream input,
        int time) {

        log.info("Incoming resource from " + from.toString() + ": " + path);

        // TODO CJ: move this to business logic
        boolean handledByInvitation = false;
        for (IInvitationProcess process : processes) {
            if (process.getPeer().equals(from)) {
                process.resourceReceived(from, path, input);
                handledByInvitation = true;
                break;
            }
        }

        if (!handledByInvitation) {
            TimedActivity timedActivity = new TimedActivity(new FileActivity(
                FileActivity.Type.Created, path, input), time);

            ISessionManager sm = Saros.getDefault().getSessionManager();
            sm.getSharedProject().getSequencer().exec(timedActivity);
        }

    }

    protected void receivedFileList(TransferDescription data, InputStream input) {

        String fileListAsString;
        try {
            fileListAsString = read(input);
        } catch (IOException e) {
            log.error("Error receiving FileList", e);
            return;
        }

        FileList fileList = null;

        if (fileListAsString != null) {
            try {
                fileList = new FileList(fileListAsString);
            } catch (Exception e) {

                for (IInvitationProcess process : processes) {
                    if (process.getPeer().equals(data.getSender()))
                        process.cancel("Could not parse your FileList", false);
                }
                log.error("Could not parse FileList", e);
            }
        }
        for (IInvitationProcess process : processes) {
            if (process.getPeer().equals(data.getSender()))
                process.fileListReceived(data.getSender(), fileList);
        }
    }

    protected void receivedArchive(TransferDescription data, InputStream input) {

        File archiveFile = new File("./incoming_archive.zip");
        XMPPChatTransmitter.log.debug("Archive file: "
            + archiveFile.getAbsolutePath());

        try {
            FileUtils.writeByteArrayToFile(archiveFile, IOUtils
                .toByteArray(input));

            ZipFile zip = new ZipFile(archiveFile);
            @SuppressWarnings("unchecked")
            Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip
                .entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                XMPPChatTransmitter.log.debug(entry.getName());

                receivedResource(data.getSender(), new Path(entry.getName()),
                    zip.getInputStream(entry), data.timestamp);
            }
            archiveFile.delete();
        } catch (IOException e) {
            log.error("Failed to receive and unpack archive");
        }
    }
}
