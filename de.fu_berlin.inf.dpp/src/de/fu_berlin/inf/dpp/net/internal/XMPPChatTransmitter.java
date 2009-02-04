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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorPart;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.xmlpull.v1.XmlPullParserException;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.PreferenceConstants;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.management.DocumentChecksum;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.TransferMode;
import de.fu_berlin.inf.dpp.net.IFileTransferCallback;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.net.jingle.IJingleFileTransferListener;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferData;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferManager;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferData.FileTransferType;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferManager.JingleConnectionState;
import de.fu_berlin.inf.dpp.project.ISessionManager;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.ui.ErrorMessageDialog;

/**
 * The one ITransmitter implementation which uses Smack Chat objects.
 * 
 * @author rdjemili
 */
public class XMPPChatTransmitter implements ITransmitter, PacketListener,
        MessageListener, FileTransferListener, IJingleFileTransferListener {
    private static Logger log = Logger.getLogger(XMPPChatTransmitter.class
            .getName());

    private static final int MAX_PARALLEL_SENDS = 10;
    private static final int MAX_TRANSFER_RETRIES = 5;
    private static final int FORCEDPART_OFFLINEUSER_AFTERSECS = 60;

    /*
     * the following string descriptions are used to differentiate between
     * transfers that are for invitations and transfers that are an activity for
     * the current project.
     */
    private static final String RESOURCE_TRANSFER_DESCRIPTION = "resourceAddActivity";

    private static final String FILELIST_TRANSFER_DESCRIPTION = "filelist";

    private static final String PROJECT_ARCHIVE_DESCRIPTION = "projectArchiveFile";

    private XMPPConnection connection;

    /*
     * old version of chatmanager. TODO: exchange this with private manager.
     */
    private ChatManager chatmanager;

    private MultiUserChatManager mucmanager;

    private final Map<JID, Chat> chats = new HashMap<JID, Chat>();

    private FileTransferManager fileTransferManager;

    // TODO use ListenerList instead
    private final List<IInvitationProcess> processes = new CopyOnWriteArrayList<IInvitationProcess>();

    private final List<FileTransferData> fileTransferQueue = new LinkedList<FileTransferData>();
    private final List<MessageTransfer> messageTransferQueue = new LinkedList<MessageTransfer>();
    private final Map<String, IncomingFile> incomingFiles = new HashMap<String, IncomingFile>();
    private int runningFileTransfers = 0;

    private boolean m_bFileTransferByChat = false; // to switch to

    private JingleFileTransferManager jingleManager;

    public JingleFileTransferManager getJingleManager() {
        try {
            startingJingleThread.join();
        } catch (InterruptedException e) {
            // do nothing
        }
        return jingleManager;
    }

    private Thread startingJingleThread;

    protected long lastReceivedActivityTime;

    /**
     * A simple struct that is used to queue file transfers.
     */
    public class FileTransferData {
        public JID recipient;
        public IPath path;
        public int timestamp;
        public IFileTransferCallback callback;
        public int retries = 0;
        public byte[] content;
        public long filesize;
        public IProject project;
    }

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

    /**
     * A simple struct that is used to queue message transfers.
     */
    private class MessageTransfer {
        public JID receipient;
        public PacketExtension packetextension;
    }

    public XMPPChatTransmitter(XMPPConnection connection) {
        setXMPPConnection(connection);
    }

    public void setXMPPConnection(final XMPPConnection connection) {
        this.connection = connection;
        this.chatmanager = connection.getChatManager();
        this.fileTransferManager = new FileTransferManager(connection);
        this.fileTransferManager.addFileTransferListener(this);

        this.chats.clear();

        // Register as PacketListener
        this.connection.addPacketListener(this, new MessageTypeFilter(Message.Type.chat));
        
        // Start Jingle Manager asynchronous
        this.startingJingleThread = new Thread(new Runnable() {
            public void run() {
                XMPPChatTransmitter.this.jingleManager = new JingleFileTransferManager(
                        connection, XMPPChatTransmitter.this);
                log.debug("Jingle Manager started");
            }
        });
        this.startingJingleThread.start();
    }

    public void addInvitationProcess(IInvitationProcess process) {
        this.processes.add(process);
    }

    public void removeInvitationProcess(IInvitationProcess process) {
        this.processes.remove(process);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.net.ITransmitter
     */
    public void sendCancelInvitationMessage(JID user, String errorMsg) {
        sendMessage(user, PacketExtensions
                .createCancelInviteExtension(errorMsg));
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendRequestForFileListMessage(JID user) {

        // Make sure JingleManager has started
        getJingleManager();

        sendMessage(user, PacketExtensions.createRequestForFileListExtension());

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendRequestForActivity(ISharedProject sharedProject,
            int timestamp, boolean andup) {

        // log.info("Requesting old activity (timestamp=" + timestamp + ", "
        // + andup + ") from all...");
        //
        // sendMessageToAll(sharedProject, PacketExtensions
        // .createRequestForActivityExtension(timestamp, andup));

    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendInviteMessage(ISharedProject sharedProject, JID guest,
            String description, int colorID) {
        sendMessage(guest, PacketExtensions.createInviteExtension(sharedProject
                .getProject().getName(), description, colorID));
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendJoinMessage(ISharedProject sharedProject) {
        try {
            /* sleep process for 500 millis to ensure invitation state process. */
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            XMPPChatTransmitter.log.error(e);
        }
        sendMessageToAll(sharedProject, PacketExtensions
                .createJoinExtension(Saros.getDefault().getLocalUser()
                        .getColorID()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendLeaveMessage(ISharedProject sharedProject) {
        sendMessageToAll(sharedProject, PacketExtensions.createLeaveExtension());
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendActivities(ISharedProject sharedProject,
            List<TimedActivity> timedActivities) {

        // timer that calls sendActivities is called before setting chat

        for (TimedActivity timedActivity : timedActivities) {
            IActivity activity = timedActivity.getActivity();

            if (activity instanceof FileActivity) {
                FileActivity fileAdd = (FileActivity) activity;

                // /* send file checksum error message to exclusive recipient */
                // if (fileAdd.getType().equals(FileActivity.Type.Error)) {
                // sendFileChecksumErrorMessage(fileAdd.getPath(), false);
                // }
                /* send file to solve checksum error to single recipient */
                // if (fileAdd.getType().equals(FileActivity.Type.Created)
                // && (fileAdd.getRecipient() != null)) {
                // int time = timedActivity.getTimestamp();
                // sendFile(fileAdd.getRecipient(),
                // sharedProject.getProject(), fileAdd.getPath(),
                // time, null);
                // return;
                // }
                if (fileAdd.getType().equals(FileActivity.Type.Created)) {
                    JID myJID = Saros.getDefault().getMyJID();

                    for (User participant : sharedProject.getParticipants()) {
                        JID jid = participant.getJID();
                        if (jid.equals(myJID)) {
                            continue;
                        }

                        // TODO use callback
                        int time = timedActivity.getTimestamp();
                        /* send file with other send method. */
                        sendFile(jid, sharedProject.getProject(), fileAdd
                                .getPath(), time, null);
                    }

                    // TODO remove activity and let this be handled by
                    // ActivitiesProvider instead

                    // don't remove file activity so that it still bumps the
                    // time stamp when being received
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

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendFileList(JID recipient, FileList fileList)
            throws XMPPException {

        String xml = fileList.toXML();

        if (getFileTransferModeViaChat()) {
            // send file with IBB File Transfer
            sendFileListWithIBB(xml, recipient);

        } else {
            // transfer file list with jingle
            try {
                JingleFileTransferData data = new JingleFileTransferData();
                data.file_list_content = xml;
                data.type = FileTransferType.FILELIST_TRANSFER;
                data.recipient = recipient;
                data.sender = new JID(connection.getUser());
                data.file_project_path = FileTransferType.FILELIST_TRANSFER
                        .toString();

                getJingleManager().send(recipient, data);

            } catch (Exception e) {
                log.info("Failed to send file list with jingle, fall back to IBB", e);
                sendFileListWithIBB(xml, recipient);
            }
        }
    }

    /**
     * TODO CO should use sendFileWithIBB(...), because this just creates a stream.
     * 
     */
    private void sendFileListWithIBB(String xml, JID recipient)
            throws XMPPException {
        try {

            /* Write xml datas to temp file for transfering. */
            File newfile = new File(
                    XMPPChatTransmitter.FILELIST_TRANSFER_DESCRIPTION + "."
                            + new JID(this.connection.getUser()).getName());
            if (newfile.exists()) {
                newfile.delete();
            }
            XMPPChatTransmitter.log
                    .debug("file : " + newfile.getAbsolutePath());

            FileWriter writer = new FileWriter(
                    XMPPChatTransmitter.FILELIST_TRANSFER_DESCRIPTION + "."
                            + new JID(this.connection.getUser()).getName());
            writer.append(xml);
            writer.close();

            OutgoingFileTransfer
                    .setResponseTimeout(XMPPChatTransmitter.MAX_TRANSFER_RETRIES * 1000);
            OutgoingFileTransfer transfer = this.fileTransferManager
                    .createOutgoingFileTransfer(recipient.toString());

            XMPPChatTransmitter.log.info("Sending file list");
            FileTransferProcessMonitor monitor = new FileTransferProcessMonitor(
                    transfer);
            // TODO CO Use sendStream instead
            transfer.sendFile(newfile,
                    XMPPChatTransmitter.FILELIST_TRANSFER_DESCRIPTION);

            /* wait for complete transfer. */
            while (monitor.isAlive() && monitor.isRunning()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            monitor.closeMonitor(true);

            if (newfile.exists()) {
                newfile.delete();
            }
            XMPPChatTransmitter.log.info("File list sent via IBB");
        } catch (IOException e) {

            // fall back to ChatTransfer
            sendChatTransfer(XMPPChatTransmitter.FILELIST_TRANSFER_DESCRIPTION,
                    "", xml.getBytes(), recipient);

            log.info("File list sent via ChatTransfer.");
        }
    }

    /**
     * Sends a data buffer to a recipient using chat messages. The buffer is
     * transmitted Base64 encoded and split into blocks of size MAX_MSG_LENGTH.
     * 
     * @param name
     *            name of the data buffer. e.g. the filename of the transmitted
     *            file
     * @param desc
     *            description String of the transfer. e.g. encoded timestamp for
     *            file activity
     * @param data
     *            a byte buffer to be transmitted. it will be base64 encoded
     * @param recipient
     *            JID of the user to send this data to
     * 
     * @return <code>true</code> if the message was send successfully
     */
    boolean sendChatTransfer(String name, String desc, byte[] data,
            JID recipient) {

        final int maxMsgLen = Saros.getDefault().getPreferenceStore().getInt(
                PreferenceConstants.CHATFILETRANSFER_CHUNKSIZE);

        // Convert byte array to base64 string
        String data64 = new sun.misc.BASE64Encoder().encode(data);

        // send large data sets in several messages
        int tosend = data64.length();
        int pcount = (tosend / maxMsgLen) + ((tosend % maxMsgLen == 0) ? 0 : 1);
        int start = 0;
        try {
            for (int i = 1; i <= pcount; i++) {
                int psize = Math.min(tosend, maxMsgLen);
                int end = start + psize;

                PacketExtension extension = PacketExtensions
                        .createDataTransferExtension(name, desc, i, pcount,
                                data64.substring(start, end));

                sendMessage(recipient, extension);

                start = end;
                tosend -= psize;

            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Receives a data buffer sent by a chat message. The data will be decoded
     * from base64 encoding. Splitted transfer will be buffered until all chunks
     * are received. Then the file will be reconstructed and processed as a
     * whole.
     * 
     * @param message
     *            Message containing the data as extension.
     * 
     * @return <code>true</code> if the message was handled successfully.
     */
    boolean receiveChatTransfer(Message message) {
        DefaultPacketExtension dt = PacketExtensions
                .getDataTransferExtension(message);
        String sName = dt.getValue(PacketExtensions.DT_NAME);
        String sData = dt.getValue(PacketExtensions.DT_DATA);

        String sSplit = dt.getValue(PacketExtensions.DT_SPLIT);
        try {
            // is this a multipart transfer?
            if ((sSplit != null) && (sSplit.equals("1/1") == false)) {
                // parse split information (index and chunk count)
                int i = sSplit.indexOf('/');
                int cur = Integer.parseInt(sSplit.substring(0, i));
                int max = Integer.parseInt(sSplit.substring(i + 1));

                XMPPChatTransmitter.log.debug("Received chunk " + cur + " of "
                        + max + " of file " + sName);

                // check for previous chunks
                IncomingFile ifile = this.incomingFiles.get(sName);
                if (ifile == null) {
                    // this is the first received chunk->create incoming file
                    // object
                    ifile = new IncomingFile();
                    ifile.receivedChunks++;
                    ifile.chunkCount = max;
                    ifile.name = sName;
                    for (i = 0; i < max; i++) {
                        ifile.messageBuffer.add(null);
                    }
                    ifile.messageBuffer.set(cur - 1, sData);
                    this.incomingFiles.put(sName, ifile);
                    return true;
                } else {
                    // this is a following chunk
                    ifile.receivedChunks++;
                    ifile.messageBuffer.set(cur - 1, sData);

                    if (ifile.isComplete() == false) {
                        return true;
                    }

                    // join the buffers to restore the file from chunks
                    sData = "";
                    for (i = 0; i < max; i++) {
                        sData += ifile.messageBuffer.get(i);
                    }
                    this.incomingFiles.remove(ifile);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;

        }

        byte[] dataOrg = null;
        try {
            dataOrg = new sun.misc.BASE64Decoder().decodeBuffer(sData);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // File list received
        if (sName.equals(XMPPChatTransmitter.FILELIST_TRANSFER_DESCRIPTION)) {
            FileList fileList = null;
            IInvitationProcess myProcess = null;
            try {
                JID fromJID = new JID(message.getFrom());
                for (IInvitationProcess process : this.processes) {
                    if (process.getPeer().equals(fromJID)) {
                        myProcess = process;
                        fileList = new FileList(new String(dataOrg));
                        process.fileListReceived(fromJID, fileList);
                    }
                }
                XMPPChatTransmitter.log
                        .info("Received file list via ChatTransfer");
            } catch (Exception e) {
                if (myProcess != null) {
                    myProcess.cancel("Error receiving file list", false);
                }
            }

        } else {
            // receiving file (resource)

            try {

                JID from = new JID(message.getFrom());
                Path path = new Path(sName);

                ByteArrayInputStream in = new ByteArrayInputStream(dataOrg);

                XMPPChatTransmitter.log.debug("Receiving resource from "
                        + from.toString() + ": " + sName + " (ChatTransfer)");

                boolean handledByInvitation = false;
                for (IInvitationProcess process : this.processes) {
                    if (process.getPeer().equals(from)) {
                        process.resourceReceived(from, path, in);
                        handledByInvitation = true;
                    }
                }

                if (!handledByInvitation) {

                    if (Saros.getDefault().getSessionManager()
                            .getSharedProject() == null) {
                        // receiving resource without a running session? not
                        // accepted
                        return false;
                    }

                    FileActivity activity = new FileActivity(
                            FileActivity.Type.Created, path, in);

                    int time;
                    String description = dt.getValue(PacketExtensions.DT_DESC);
                    try {
                        time = Integer
                                .parseInt(description
                                        .substring(XMPPChatTransmitter.RESOURCE_TRANSFER_DESCRIPTION
                                                .length() + 1));
                    } catch (Exception e) {
                        Saros.log("Could not parse time from description: "
                                + description, e);
                        time = 0; // HACK
                    }

                    TimedActivity timedActivity = new TimedActivity(activity,
                            time);

                    ISessionManager sm = Saros.getDefault().getSessionManager();
                    sm.getSharedProject().getSequencer().exec(timedActivity);
                }

                XMPPChatTransmitter.log.info("Received resource " + sName);

            } catch (Exception e) {
                XMPPChatTransmitter.log.warn("Failed to receive " + sName, e);
            }
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.net.ITransmitter
     */
    public void sendFile(JID to, IProject project, IPath path,
            IFileTransferCallback callback) {
        sendFile(to, project, path, -1, callback);
    }

    /**
     * Reads a files content into a buffer.
     * 
     * @param transfer
     *            Object containing file path and a buffer (among other) to read
     *            from and to.
     * 
     * @return <code>true</code> if the file was read successfully
     */
    boolean readFile(FileTransferData transfer) {
        // SessionManager sm = Saros.getDefault().getSessionManager();
        // IProject project = sm.getSharedProject().getProject();

        File f = new File(transfer.project.getFile(transfer.path).getLocation()
                .toOSString());
        transfer.filesize = f.length();
        transfer.content = new byte[(int) transfer.filesize];

        try {
            InputStream in = transfer.project.getFile(transfer.path)
                    .getContents();
            in.read(transfer.content, 0, (int) transfer.filesize);
        } catch (Exception e) {
            e.printStackTrace();
            transfer.content = null;
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.net.ITransmitter
     */
    public void sendFile(JID to, IProject project, IPath path, int timestamp,
            IFileTransferCallback callback) {

        FileTransferData transfer = new FileTransferData();
        transfer.recipient = to;
        transfer.path = path;
        transfer.timestamp = timestamp;
        transfer.callback = callback;
        transfer.project = project;
        transfer.filesize = project.getFile(path).getLocation().toFile()
                .length();

        // if transfer will be delayed, we need to buffer the file
        // to not send modified versions later
        if (!this.connection.isConnected()) {
            readFile(transfer);
        } else {
            transfer.content = null;
        }

        this.fileTransferQueue.add(transfer);
        sendNextFile();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.fu_berlin.inf.dpp.net.ITransmitter#sendProjectArchive(de.fu_berlin
     * .inf.dpp.net.JID, org.eclipse.core.resources.IProject, java.io.File,
     * de.fu_berlin.inf.dpp.net.IFileTransferCallback)
     */
    public void sendProjectArchive(JID recipient, IProject project,
            File archive, IFileTransferCallback callback) {
        OutgoingFileTransfer
                .setResponseTimeout(XMPPChatTransmitter.MAX_TRANSFER_RETRIES * 1000);
        OutgoingFileTransfer transfer = this.fileTransferManager
                .createOutgoingFileTransfer(recipient.toString());

        try {
            transfer.sendFile(archive,
                    XMPPChatTransmitter.PROJECT_ARCHIVE_DESCRIPTION);

            FileTransferProcessMonitor monitor = new FileTransferProcessMonitor(
                    transfer, callback);
            /* wait for complete transfer. */
            while (monitor.isAlive() && monitor.isRunning()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            monitor.closeMonitor(true);

            if (transfer
                    .getStatus()
                    .equals(
                            org.jivesoftware.smackx.filetransfer.FileTransfer.Status.complete)) {
                XMPPChatTransmitter.log.debug("transfer complete");
                callback.fileSent(new Path(archive.getName()));
            }

            /* delete temp archive file. */
            archive.delete();

        } catch (Exception e) {

            XMPPChatTransmitter.log.warn("Failed to send archive file", e);
            if (callback != null) {
                callback.fileTransferFailed(null, e);
            }
        }
    }

    private void sendNextFile() {
        if ((this.fileTransferQueue.size() == 0)
                || (this.runningFileTransfers > XMPPChatTransmitter.MAX_PARALLEL_SENDS)) {
            XMPPChatTransmitter.log.debug("No file to send in queue.");
            return;
        }

        final FileTransferData transfer = this.fileTransferQueue.remove(0);

        new Thread(new Runnable() {

            /*
             * (non-Javadoc)
             * 
             * @see java.lang.Runnable#run()
             */
            public void run() {
                try {
                    XMPPChatTransmitter.this.runningFileTransfers++;
                    XMPPChatTransmitter.log.debug("try to send file "
                            + transfer.path);
                    transferFile(transfer);

                } catch (Exception e) {
                    if (transfer.retries >= XMPPChatTransmitter.MAX_TRANSFER_RETRIES) {
                        XMPPChatTransmitter.log.warn("Failed to send "
                                + transfer.path, e);
                        if (transfer.callback != null) {
                            transfer.callback.fileTransferFailed(transfer.path,
                                    e);
                        }

                    } else {
                        transfer.retries++;
                        XMPPChatTransmitter.this.fileTransferQueue
                                .add(transfer);
                    }

                } finally {
                    XMPPChatTransmitter.this.runningFileTransfers--;
                    sendNextFile();
                }
            }

        }).start();
    }

    public void sendUserListTo(JID to, Collection<User> participants) {
        XMPPChatTransmitter.log.debug("Sending user list to " + to.toString());

        sendMessage(to, PacketExtensions.createUserListExtension(participants));
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
            sendMessage(user.getJID(), PacketExtensions
                    .createChecksumErrorExtension(path, resolved));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.fu_berlin.inf.dpp.net.ITransmitter#sendDocChecksumsToClients(org.eclipse
     * .core.runtime.IPath)
     */
    public void sendDocChecksumsToClients(Collection<DocumentChecksum> checksums) {
        // send checksums to all clients
        ISharedProject project = Saros.getDefault().getSessionManager()
                .getSharedProject();
        if (project != null) {
            Collection<User> participants = project.getParticipants();
            if (participants != null) {
                for (User participant : participants) {
                    if (!Saros.getDefault().getSessionManager()
                            .getSharedProject().getHost().getJID().equals(
                                    participant.getJID())) {
                        JID jid = participant.getJID();
                        XMPPChatTransmitter.log.debug("Sending checksums to "
                                + jid);
                        Message m = new Message();
                        m.addExtension(PacketExtensions
                                .createChecksumsExtension(checksums));
                        try {
                            getChat(jid).sendMessage(m);
                        } catch (XMPPException e) {
                            log.error("Can't send checksums to " + jid);
                        }
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.fu_berlin.inf.dpp.net.ITransmitter#sendJupiterTransformationError(
     * de.fu_berlin.inf.dpp.net.JID, org.eclipse.core.runtime.IPath)
     */
    public void sendJupiterTransformationError(JID to, IPath path) {
        XMPPChatTransmitter.log
                .debug("Sending jupiter transformation error message to " + to
                        + " of file " + path.lastSegment());
        sendMessage(to, PacketExtensions.createJupiterErrorExtension(path));
    }

    public void sendRemainingFiles() {

        if (this.fileTransferQueue.size() > 0) {
            sendNextFile();
        }
    }

    public void sendRemainingMessages() {

        try {
            while (this.messageTransferQueue.size() > 0) {
                final MessageTransfer pex = this.messageTransferQueue.remove(0);

                Chat chat = getChat(pex.receipient);
                Message message;
                // TODO: Änderung für Smack 3
                message = new Message();
                // message = chat.createMessage();
                message.addExtension(pex.packetextension);
                chat.sendMessage(message);
                XMPPChatTransmitter.log.info("Resending message");
            }
        } catch (Exception e) {
            Saros.getDefault().getLog().log(
                    new Status(IStatus.ERROR, Saros.SAROS, IStatus.ERROR,
                            "Could not send message", e));
        }
    }

    public boolean resendActivity(JID jid, int timestamp, boolean andup) {

        boolean sent = false;

        ISharedProject project = Saros.getDefault().getSessionManager()
                .getSharedProject();

        try {
            List<TimedActivity> tempActivities = new LinkedList<TimedActivity>();
            for (TimedActivity tact : project.getSequencer()
                    .getActivityHistory()) {

                if (((andup == false) && (tact.getTimestamp() != timestamp))
                        || ((andup == true) && (tact.getTimestamp() < timestamp))) {
                    continue;
                }

                tempActivities.add(tact);
                sent = true;

                if (andup == false) {
                    break;
                }
            }

            if (sent) {
                PacketExtension extension = new ActivitiesPacketExtension(Saros
                        .getDefault().getSessionManager().getSessionID(),
                        tempActivities);
                sendMessage(jid, extension);
            }

        } catch (Exception e) {
            Saros.getDefault().getLog().log(
                    new Status(IStatus.ERROR, Saros.SAROS, IStatus.ERROR,
                            "Could not resend message", e));
        }

        return sent;
    }

    public void processMessage(Chat chat, Message message) {
        // TODO: new method für smack 3
        // log.debug("incomming message : " + message.getBody());
        // processPacket(message);

    }

    public void processPacket(Chat chat, Packet packet) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.fu_berlin.inf.dpp.net.ITransmitter#sendActivitiyTo(de.fu_berlin.inf
     * .dpp.project.ISharedProject, java.util.List,
     * de.fu_berlin.inf.dpp.net.JID)
     */
    public void sendJupiterRequest(ISharedProject sharedProject,
            Request request, JID jid) {
        XMPPChatTransmitter.log.info("send request to : " + jid + " request: "
                + request);
        sendMessage(jid, new RequestPacketExtension(Saros.getDefault()
                .getSessionManager().getSessionID(), request));
    }

    /*
     * All Smack Packets are dispatched here
     * 
     * TODO replace dependencies by more generic listener interfaces
     */
    public void processPacket(final Packet packet) {

        Message message = (Message) packet;

        JID fromJID = new JID(message.getFrom());

        if (PacketExtensions.getInviteExtension(message) != null) {
            processInviteExtension(message, fromJID);
            return;
        }

        if (!Saros.getDefault().getSessionManager().getSessionID().equals(
                PacketExtensions.getSessionID(message))) {
            log.debug("received message with wrong session id ("
                    + PacketExtensions.getSessionID(message) + "), drop it..");
            return;
        }

        final ISharedProject project = Saros.getDefault().getSessionManager()
                .getSharedProject();

        // Change the input method to get the right chats
        putIncomingChat(fromJID, message.getThread());

        if (PacketExtensions.getActvitiesExtension(message) != null) {
            processActivitiesExtension(message, fromJID, project);
        }

        if (PacketExtensions.getChecksumExtension(message) != null) {
            processChecksumExtension(message, project);
        }

        if (PacketExtensions.getJoinExtension(message) != null) {
            processJoinExtension(message, fromJID, project);
        }

        // TODO CJ: Leave Project Message must be handled better
        if (PacketExtensions.getLeaveExtension(message) != null) {
            processLeaveExtension(fromJID, project);
        }

        if (PacketExtensions.getRequestActivityExtension(message) != null) {
            processActivityRequestExtension(message, project, fromJID);
        }

        if (PacketExtensions.getDataTransferExtension(message) != null) {
            receiveChatTransfer(message);
        }

        if (PacketExtensions.getRequestExtension(message) != null) {
            processRequestExtension(fromJID);
        }

        if (PacketExtensions.getUserlistExtension(message) != null) {
            processUserListExtension(message, fromJID, project);
        }

        if (PacketExtensions.getCancelInviteExtension(message) != null) {
            processCancelInviteExtension(message, fromJID);
        }

        if (PacketExtensions.getChecksumErrorExtension(message) != null) {
            processChecksumErrorExtension(message);
        }
    }
    
    private void processActivitiesExtension(final Message message, JID fromJID,
            final ISharedProject project) {
        ActivitiesPacketExtension activitiesPacket = PacketExtensions
                .getActvitiesExtension(message);

        List<TimedActivity> timedActivities = activitiesPacket.getActivities();

        boolean isProjectParticipant = false;
        if (project != null) {
            isProjectParticipant = (project.getParticipant(fromJID) != null);
        }

        XMPPChatTransmitter.log.debug("Received activities from "
                + fromJID.toString() + ": " + timedActivities);

        if (!isProjectParticipant) {
            XMPPChatTransmitter.log.info("user not member!");
            return;
        }

        for (TimedActivity timedActivity : timedActivities) {

            IActivity activity = timedActivity.getActivity();
            activity.setSource(fromJID.toString());

            /*
             * incoming fileActivities that add files are only used as
             * placeholder to bump the timestamp. the real fileActivity will be
             * processed by using a file transfer.
             */
            if (!(activity instanceof FileActivity)
                    || !((FileActivity) activity).getType().equals(
                            FileActivity.Type.Created)) {

                project.getSequencer().exec(timedActivity);

            }
        }
    }

    private void processChecksumErrorExtension(final Message message) {
        DefaultPacketExtension checksumErrorExtension = PacketExtensions
                .getChecksumErrorExtension(message);

        final String path = checksumErrorExtension
                .getValue(PacketExtensions.FILE_PATH);

        final boolean resolved = Boolean.parseBoolean(checksumErrorExtension
                .getValue("resolved"));

        if (resolved) {
            log.debug("synchronisation completed, inconsistency resolved");
            ErrorMessageDialog.closeChecksumErrorMessage();
            return;
        }

        ErrorMessageDialog.showChecksumErrorMessage(path);

        // Host
        if (Saros.getDefault().getSessionManager().getSharedProject().isHost()) {
            log.warn("Checksum Error for " + path);

            IEditorPart editor = (IEditorPart) EditorManager.getDefault()
                    .getEditors(new Path(path)).toArray()[0];

            new Thread() {

                public void run() {
                    // wait until no more activities are received

                    // TODO lastReceivedActivity is not set at the moment
                    
                    while (System.currentTimeMillis()
                            - XMPPChatTransmitter.this.lastReceivedActivityTime < 1500) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    EditorManager.getDefault().saveText(new Path(path), true);

                    // IPath fullPath =
                    // Saros.getDefault().getSessionManager()
                    // .getSharedProject().getProject().findMember(
                    // path).getFullPath();
                    // ITextFileBuffer fileBuff = FileBuffers
                    // .getTextFileBufferManager().getTextFileBuffer(
                    // fullPath, LocationKind.IFILE);
                    //
                    // if (fileBuff == null) {
                    // log.error("Can't get File Buffer");
                    // }
                    // if (fileBuff.isDirty())
                    // try {
                    // fileBuff
                    // .commit(new NullProgressMonitor(), true);
                    // } catch (CoreException e) {
                    // // TODO Auto-generated catch block
                    // e.printStackTrace();
                    // }

                    // TODO CJ: thinking about a better solution with
                    // activity sequencer and jupiter

                    // Saros.getDefault().getSessionManager()
                    // .getSharedProject()
                    // .getConcurrentDocumentManager()
                    // .resetJupiterDocument(new Path(path));

                    log.debug("Sending file to clients");
                    sendFile(new JID(message.getFrom()), Saros.getDefault()
                            .getSessionManager().getSharedProject()
                            .getProject(), new Path(path), null);
                }
            }.start();
        }
    }

    private void processCancelInviteExtension(final Message message, JID fromJID) {
        DefaultPacketExtension cancelInviteExtension = PacketExtensions
                .getCancelInviteExtension(message);

        String errorMsg = cancelInviteExtension
                .getValue(PacketExtensions.ERROR);

        for (IInvitationProcess process : this.processes) {
            if (process.getPeer().equals(fromJID)) {
                process.cancel(errorMsg, true);
            }
        }
    }

    private void processUserListExtension(final Message message, JID fromJID,
            final ISharedProject project) {
        DefaultPacketExtension userlistExtension = PacketExtensions
                .getUserlistExtension(message);

        // My inviter sent a list of all session participants
        // I need to adapt the order for later case of driver leaving the
        // session
        XMPPChatTransmitter.log.debug("Received user list from " + fromJID);

        int count = 0;
        while (true) {
            String jidS = userlistExtension.getValue("User" + count);
            if (jidS == null) {
                break;
            }
            JID jid = new JID(jidS);
            XMPPChatTransmitter.log.debug("   *:" + jidS);
            int color = Integer.parseInt(userlistExtension.getValue("UserColor"
                    + count));
            XMPPChatTransmitter.log.debug("   color: " + color);

            User user = project.getParticipant(jid);

            if (user == null) {
                // This user is new, we have to send him a message later
                // and add him to the project
                user = new User(jid, color);
            } else {
                // TODO [MR] The User constructor takes a color argument, so
                // this should be unnecessary.
                user.setColorID(color);
            }

            String userRole = userlistExtension.getValue("UserRole" + count);
            user.setUserRole(UserRole.valueOf(userRole));

            if (project.getParticipant(jid) == null) {
                project.addUser(user);

                sendMessage(jid, PacketExtensions.createJoinExtension(Saros
                        .getDefault().getLocalUser().getColorID()));
            }

            count++;
        }
    }

    /**
     *  invitee request for project file list (state.INVITATION_SEND 
     */
    private void processRequestExtension(final JID fromJID) {
        new Thread(new Runnable() {
            public void run() {
                for (IInvitationProcess process : processes) {
                    if (process.getPeer().equals(fromJID)) {
                        process.invitationAccepted(fromJID);
                    }
                }
            }
        }).start();
    }

    private void processActivityRequestExtension(final Message message,
            ISharedProject project, JID fromJID) {

        if (project == null || project.getParticipant(fromJID) == null) {
            return;
        }

        DefaultPacketExtension rae = PacketExtensions
                .getRequestActivityExtension(message);

        String sID = rae.getValue("ID");
        String sIDandup = rae.getValue("ANDUP");

        int ts = -1;
        if (sID != null) {
            ts = (new Integer(sID)).intValue();
            // get that activity from history (if it was mine) and send it
            boolean sent = resendActivity(fromJID, ts, (sIDandup != null));

            String info = "Received Activity request for timestamp=" + ts + ".";
            if (sIDandup != null) {
                info += " (andup) ";
            }
            if (sent) {
                info += " I sent response.";
            } else {
                info += " (not for me)";
            }

            XMPPChatTransmitter.log.info(info);
        }
    }

    private void processLeaveExtension(JID fromJID, final ISharedProject project) {
        if (project != null) {
            project.removeUser(project.getParticipant(fromJID)); // HACK
        }
    }

    private void processJoinExtension(final Message message, JID fromJID,
            final ISharedProject project) {
        int colorID = Integer.parseInt(PacketExtensions.getJoinExtension(
                message).getValue("ColorID"));

        log.debug("Join: ColorID: " + colorID);

        for (IInvitationProcess process : this.processes) {
            if (process.getPeer().equals(fromJID)) {
                process.joinReceived(fromJID);
                return;
            }
        }
        if (project != null) {
            project.addUser(new User(fromJID, colorID));
            // a new user
            // joined this
            // session
        }
    }

    private void processChecksumExtension(final Message message,
            final ISharedProject project) {
        final DefaultPacketExtension ext = PacketExtensions
                .getChecksumExtension(message);
        log.debug("Received checksums");

        new Thread() {
            public void run() {
                int count = Integer.parseInt(ext.getValue("quantity"));
                DocumentChecksum[] checksums = new DocumentChecksum[count];

                for (int i = 1; i <= count; i++) {
                    IPath path = Path.fromPortableString(ext.getValue("path"
                            + i));
                    int length = Integer.parseInt(ext.getValue("length" + i));
                    int hash = Integer.parseInt(ext.getValue("hash" + i));
                    checksums[i - 1] = new DocumentChecksum(path, length, hash);
                }
                project.getConcurrentDocumentManager().checkConsistency(
                        checksums);
            }
        }.start();
    }

    private void processInviteExtension(final Message message, JID fromJID) {
        DefaultPacketExtension inviteExtension = PacketExtensions
                .getInviteExtension(message);
        String desc = inviteExtension.getValue(PacketExtensions.DESCRIPTION);
        String pName = inviteExtension.getValue(PacketExtensions.PROJECTNAME);
        String sessionID = inviteExtension
                .getValue(PacketExtensions.SESSION_ID);
        int colorID = Integer.parseInt(inviteExtension
                .getValue(PacketExtensions.COLOR_ID));

        ISessionManager sm = Saros.getDefault().getSessionManager();
        log.debug("Received invitation with session id " + sessionID);
        log.debug("and ColorID: " + colorID + ", i'm "
                + Saros.getDefault().getMyJID());
        sm.invitationReceived(fromJID, sessionID, pName, desc, colorID);
        return;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jivesoftware.smackx.filetransfer.FileTransferListener
     */
    public void fileTransferRequest(FileTransferRequest incommingRequest) {

        final FileTransferRequest request = incommingRequest;

        new Thread(new Runnable() {

            public void run() {
                try {
                    String fileDescription = request.getDescription();
                    XMPPChatTransmitter.log.debug("1. incomming file transfer "
                            + request.getFileName());
                    if (fileDescription
                            .equals(XMPPChatTransmitter.PROJECT_ARCHIVE_DESCRIPTION)) {
                        XMPPChatTransmitter.log
                                .debug(" incoming project archive file.");
                        receiveArchiveFile(request);
                    }
                    if (fileDescription
                            .equals(XMPPChatTransmitter.FILELIST_TRANSFER_DESCRIPTION)) {
                        FileList fileList = receiveFileListBufferByteArray(request);
                        JID fromJID = new JID(request.getRequestor());

                        XMPPChatTransmitter.log
                                .debug("2. inform invitation process...");
                        for (IInvitationProcess process : XMPPChatTransmitter.this.processes) {
                            if (process.getPeer().equals(fromJID)) {
                                process.fileListReceived(fromJID, fileList);
                                /*
                                 * incoming IBB transfer. cancel jingle transfer
                                 * mode.
                                 */
                                process.setTransferMode(TransferMode.IBB);
                            }
                        }

                    } else if (fileDescription.startsWith(
                            XMPPChatTransmitter.RESOURCE_TRANSFER_DESCRIPTION,
                            0)) {
                        receiveResource(request);
                    }
                } catch (Exception e) {
                    XMPPChatTransmitter.log.error(
                            "Incoming File Transfer Thread: ", e);
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

    /**
     * read incoming file and open inputstream to IInvitationProcess.
     * 
     * @param request
     * @throws Exception
     */
    private void receiveArchiveFile(FileTransferRequest request)
            throws Exception {
        // try{
        File archive = receiveFile(request);

        ZipFile zip = new ZipFile(archive);
        Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            XMPPChatTransmitter.log.debug(entry.getName());
            JID fromJID = new JID(request.getRequestor());

            XMPPChatTransmitter.log.debug("2. inform invitation process...");
            for (IInvitationProcess process : this.processes) {
                if (process.getPeer().equals(fromJID)) {
                    process.resourceReceived(fromJID,
                            new Path(entry.getName()), zip
                                    .getInputStream(entry));
                }
            }
        }
        archive.delete();
    }

    private void sendMessageToAll(ISharedProject sharedProject,
            PacketExtension extension) { // HACK

        JID myJID = Saros.getDefault().getMyJID();

        for (User participant : sharedProject.getParticipants()) {
            if (participant.getJID().equals(myJID)) {
                continue;
            }

            // if user is known to be offline, dont send but queue
            if (sharedProject != null) {

                User user = sharedProject.getParticipant(participant.getJID());
                if ((user != null)
                        && (user.getPresence() == User.UserConnectionState.OFFLINE)) {

                    // offline for too long
                    if (user.getOfflineSeconds() > XMPPChatTransmitter.FORCEDPART_OFFLINEUSER_AFTERSECS) {
                        XMPPChatTransmitter.log
                                .info("Removing offline user from session...");
                        sharedProject.removeUser(user);
                    } else {
                        queueMessage(participant.getJID(), extension);
                        XMPPChatTransmitter.log
                                .info("User known as offline - Message queued!");
                    }

                    continue;
                }
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

    private void sendMessage(JID jid, PacketExtension extension) {

        if (!this.connection.isConnected()) {
            queueMessage(jid, extension);
            return;
        }

        try {

            Chat chat = getChat(jid);
            Message message = new Message();
            message.addExtension(extension);
            chat.sendMessage(message);

        } catch (Exception e) {
            queueMessage(jid, extension);

            Saros.getDefault().getLog().log(
                    new Status(IStatus.ERROR, Saros.SAROS, IStatus.ERROR,
                            "Could not send message, message queued", e));
        }
    }

    /**
     * receive resource with file transfer.
     * 
     * @param request
     */
    private void receiveResource(FileTransferRequest request) {
        try {

            JID from = new JID(request.getRequestor());
            /* file path exists in description. */
            Path path = new Path(request.getDescription()
                    .substring(
                            XMPPChatTransmitter.RESOURCE_TRANSFER_DESCRIPTION
                                    .length() + 1));

            XMPPChatTransmitter.log.debug("Receiving resource from"
                    + from.toString() + ": " + request.getFileName());

            // InputStream in = request.accept().recieveFile();

            IncomingFileTransfer transfer = request.accept();

            // FileTransferProcessMonitor monitor = new
            // FileTransferProcessMonitor(
            // transfer);

            InputStream in = transfer.recieveFile();

            // TODO CJ: move this to business logic
            boolean handledByInvitation = false;
            for (IInvitationProcess process : this.processes) {
                if (process.getPeer().equals(from)) {
                    process.resourceReceived(from, path, in);
                    handledByInvitation = true;
                    break;
                }
            }

            if (!handledByInvitation) {
                FileActivity activity = new FileActivity(
                        FileActivity.Type.Created, path, in);

                int time;
                String description = request.getDescription();
                try {
                    time = Integer
                            .parseInt(description
                                    .substring(XMPPChatTransmitter.RESOURCE_TRANSFER_DESCRIPTION
                                            .length() + 1));
                } catch (NumberFormatException e) {
                    Saros.log("Could not parse time from description: "
                            + description, e);
                    time = 0; // HACK
                }

                TimedActivity timedActivity = new TimedActivity(activity, time);

                ISessionManager sm = Saros.getDefault().getSessionManager();
                sm.getSharedProject().getSequencer().exec(timedActivity);
            }

            // /* wait for complete transfer. */
            // while (monitor.isAlive() && monitor.isRunning()) {
            // Thread.sleep(500);
            // }
            // monitor.closeMonitor(true);

            XMPPChatTransmitter.log.info("Received resource "
                    + request.getFileName());

        } catch (Exception e) {
            XMPPChatTransmitter.log.warn("Failed to receive "
                    + request.getFileName(), e);
        }
    }

    /**
     * TODO CO merge with sendFileList
     */
    private void transferFile(FileTransferData transferData)
            throws CoreException, XMPPException, IOException {

        XMPPChatTransmitter.log.info("Sending file " + transferData.path);

        JID recipient = transferData.recipient;

        String description = XMPPChatTransmitter.RESOURCE_TRANSFER_DESCRIPTION;
        if (transferData.timestamp >= 0) {
            description = description + ':' + transferData.timestamp;
        }

        if (getFileTransferModeViaChat()) {
            sendSingleFileWithIBB(transferData);
        } else {

            if (getJingleManager().getState(recipient) != JingleConnectionState.ERROR) {
                log.info("Send file " + transferData.path + " (with Jingle)");

                /* create file transfer. */
                JingleFileTransferData data = new JingleFileTransferData();

                data.file_project_path = transferData.path.toString();
                data.project_name = transferData.project.getName();
                data.type = FileTransferType.RESOURCE_TRANSFER;
                data.recipient = recipient;
                data.sender = new JID(connection.getUser());

                /* read content data. */
                File f = new File(transferData.project.getFile(
                        transferData.path).getLocation().toOSString());
                data.filesize = f.length();
                data.content = new byte[(int) data.filesize];

                try {
                    InputStream in = transferData.project.getFile(
                            transferData.path).getContents();
                    in.read(data.content, 0, (int) data.filesize);
                } catch (Exception e) {
                    e.printStackTrace();
                    data.content = null;
                    log.error("Error during read file content for transfer!");
                }

                try {
                    // transfer files with jingle
                    getJingleManager().send(recipient, data);

                    /* inform callback. */
                    if (transferData.callback != null)
                        transferData.callback.fileSent(transferData.path);

                    return;

                } catch (Exception e) {
                    log.info("Failed to send file with jingle, fall back to IBB");
                    sendSingleFileWithIBB(transferData);
                }
            }
        }
    }

    /**
     * send single file from queue with xmpp message.
     * 
     * @param transferData
     * @throws CoreException
     * @throws XMPPException
     * @throws IOException
     */
    private void sendSingleFileWithIBB(FileTransferData transferData)
            throws CoreException, XMPPException, IOException {

        JID recipient = transferData.recipient;

        String description = RESOURCE_TRANSFER_DESCRIPTION;

        OutgoingFileTransfer.setResponseTimeout(MAX_TRANSFER_RETRIES * 1000);
        OutgoingFileTransfer transfer = fileTransferManager
                .createOutgoingFileTransfer(recipient.toString());

        IFile f = transferData.project.getFile(transferData.path);

        if (f.exists()) {
            log.debug("file exists and will be send :" + f.getName() + " "
                    + f.getLocation());
            /* set path in description */
            description = description + ":" + transferData.path;
            /* send file */
            transfer
                    .sendFile(new File(f.getLocation().toString()), description);
        } else {
            log.warn("file NOT exists. " + f.getLocation());
            throw new IOException("File not exists.");
        }

        FileTransferProcessMonitor monitor = new FileTransferProcessMonitor(
                transfer);
        /* wait for complete transfer. */
        while (monitor.isAlive() && monitor.isRunning()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        monitor.closeMonitor(true);

        if (transfer
                .getStatus()
                .equals(
                        org.jivesoftware.smackx.filetransfer.FileTransfer.Status.complete)) {
            log.debug("transfer complete");
        }

        if (transferData.callback != null)
            transferData.callback.fileSent(transferData.path);
    }

    /**
     * Receive file and save temporary.
     * 
     * @param request
     *            transfer request of incoming file.
     * @return File object of received file
     */
    private File receiveFile(FileTransferRequest request) {
        File archiveFile = new File("./incoming_archive.zip");
        XMPPChatTransmitter.log.debug("Archive file: "
                + archiveFile.getAbsolutePath());
        try {
            final IncomingFileTransfer transfer = request.accept();

            IFileTransferCallback callback = null;

            /* get IInvitationprocess for monitoring. */
            JID fromJID = new JID(request.getRequestor());
            for (IInvitationProcess process : this.processes) {
                if (process.getPeer().equals(fromJID)) {
                    /* set callback. */
                    callback = process;
                }
            }

            /* monitoring of transfer process */
            FileTransferProcessMonitor monitor = new FileTransferProcessMonitor(
                    transfer, callback);

            /* receive file. */
            transfer.recieveFile(archiveFile);

            /* wait for complete transfer. */
            while (monitor.isAlive() && monitor.isRunning()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            monitor.closeMonitor(true);

        } catch (Exception e) {
            XMPPChatTransmitter.log.error("Error in Incoming File: ", e);
            return null;
        }

        return archiveFile;
    }

    private FileList receiveFileListBufferByteArray(FileTransferRequest request) {
        FileList fileList = null;
        try {
            final IncomingFileTransfer transfer = request.accept();

            InputStream in = transfer.recieveFile();

            byte[] buffer = new byte[1024];
            int bytesRead;
            String sb = new String();
            while ((bytesRead = in.read(buffer, 0, 1024)) != -1) {
                sb += new String(buffer, 0, bytesRead).toString();
            }
            in.close();
            XMPPChatTransmitter.log.debug("Close input stream");
            fileList = new FileList(sb.toString());

        } catch (Exception e) {
            XMPPChatTransmitter.log.error("Error in Incoming File List: ", e);
            return null;
        }

        return fileList;
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
            chat = this.chatmanager.createChat(jid.toString(), this);
            this.chats.put(jid, chat);
        }

        return chat;
    }

    private boolean getFileTransferModeViaChat() {
        return this.m_bFileTransferByChat
                || Saros.getDefault().getPreferenceStore().getBoolean(
                        PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT);

    }

    /**
     * File received with jingle.
     */
    public void incomingResourceFile(JingleFileTransferData data,
            InputStream input) {
        log.info("incoming resource " + data.file_project_path);

        JID from = data.sender;
        Path path = new Path(data.file_project_path);
        int time = data.timestamp;

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
            FileActivity activity = new FileActivity(FileActivity.Type.Created,
                    path, input);

            TimedActivity timedActivity = new TimedActivity(activity, time);

            ISessionManager sm = Saros.getDefault().getSessionManager();
            sm.getSharedProject().getSequencer().exec(timedActivity);
        }
    }

    public void incomingFileList(String fileList_content, JID recipient) {
        FileList fileList = null;
        log.info("incoming file list");
        try {
            fileList = new FileList(fileList_content);
        } catch (XmlPullParserException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
        for (IInvitationProcess process : processes) {
            if (process.getPeer().equals(recipient))
                process.fileListReceived(recipient, fileList);
        }

    }

    public void connected(String protocol, String remote) {
        // TODO Auto-generated method stub

    }

}
