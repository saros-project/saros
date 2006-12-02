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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.jivesoftware.smack.Chat;
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

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess;
import de.fu_berlin.inf.dpp.net.IFileTransferCallback;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;

/**
 * An ITransmitter implementation which uses Smack Chat objects. It currently
 * only supports two users at max.
 * 
 * @author rdjemili
 */
public class XMPPChatTransmitter implements ITransmitter, PacketListener, FileTransferListener {
    private static Logger log = Logger.getLogger(XMPPChatTransmitter.class.getName());
    
    private static final int         MAX_PARALLEL_SENDS            = 10;
    private static final int         MAX_TRANSFER_RETRIES          = 5;

    /*
     * the following string descriptions are used to differentiate between
     * transfers that are for invitations and transfers that are an activity for
     * the current project.
     */
    private static final String      RESOURCE_TRANSFER_DESCRIPTION = 
        "resourceAddActivity";
    private static final String      FILELIST_TRANSFER_DESCRIPTION = 
        "filelist";

    private final XMPPConnection     connection;
    private Map<JID, Chat>           chats = new HashMap<JID, Chat>();
    private FileTransferManager      fileTransferManager;

    // TODO use ListenerList instead
    private List<IInvitationProcess> processes = new CopyOnWriteArrayList<IInvitationProcess>();

    private List<FileTransfer>       fileTransferQueue = new LinkedList<FileTransfer>();
    private int                      runningFileTransfers = 0;
    
    /**
     * A simple struct that is used to queue file transfers.
     */
    private class FileTransfer {
        public JID                   recipient;
        public IPath                 path;
        public int                   timestamp;
        public IFileTransferCallback callback;
        public int                   retries = 0;
    }

    public XMPPChatTransmitter(XMPPConnection connection) {
        this.connection = connection;
        fileTransferManager = new FileTransferManager(connection);
        fileTransferManager.addFileTransferListener(this);
        
        setProxyPort(connection);
        
        // TODO always preserve threads
        this.connection.addPacketListener(this, 
            new MessageTypeFilter(Message.Type.CHAT)); // HACK
    }
    
    public void addInvitationProcess(IInvitationProcess process) {
        processes.add(process);
    }
    
    public void removeInvitationProcess(IInvitationProcess process) {
        processes.remove(process);
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.net.ITransmitter
     */
    public void sendCancelInvitationMessage(JID user, String errorMsg) {
    	sendMessage(user, PacketExtensions.createCancelInviteExtension(errorMsg));
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendRequestForFileListMessage(JID user) {
        sendMessage(user, PacketExtensions.createRequestForFileListExtension());
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendInviteMessage(ISharedProject sharedProject, JID guest, 
        String description) {
        
        sendMessage(guest, PacketExtensions.createInviteExtension(description));
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendJoinMessage(ISharedProject sharedProject) {
        sendMessageToAll(sharedProject, PacketExtensions.createJoinExtension());
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendLeaveMessage(ISharedProject sharedProject) {
        sendMessageToAll(sharedProject, PacketExtensions.createLeaveExtension());
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendActivities(ISharedProject sharedProject, 
        List<TimedActivity> timedActivities) {
        
        // timer that calls sendActivities is called before setting chat
        
        for (TimedActivity timedActivity : timedActivities) {
            IActivity activity = timedActivity.getActivity();
            
            if (activity instanceof FileActivity) {
                FileActivity fileAdd = (FileActivity)activity;
                
                if (fileAdd.getType().equals(FileActivity.Type.Created)) {
                    JID myJID = Saros.getDefault().getMyJID();
                    
                    for (User participant : sharedProject.getParticipants()) {
                        JID jid = participant.getJid();
                        if (jid.equals(myJID))
                            continue;
                        
                        // TODO use callback
                        int time = timedActivity.getTimestamp();
                        sendFile(jid, fileAdd.getPath(), time, null);
                    }
                    
                    // TODO remove activity and let this be handled by
                    // ActivitiesProvider instead
                    
                    // don't remove file activity so that it still bumps the
                    // time stamp when being received
                }
            }
        }
        
        log.info("Sent activities: "+timedActivities);
        
        if (timedActivities != null && connection != null) {
            sendMessageToAll(sharedProject, new ActivitiesPacketExtension(timedActivities));
        }
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendFileList(JID recipient, FileList fileList) 
        throws XMPPException {
        
        log.fine("Establishing file list transfer");
        
        String xml = fileList.toXML();

        OutgoingFileTransfer transfer = 
            fileTransferManager.createOutgoingFileTransfer(recipient.toString());
        
        OutputStream out = transfer.sendFile(FILELIST_TRANSFER_DESCRIPTION, 
            xml.getBytes().length, FILELIST_TRANSFER_DESCRIPTION);
        
        log.fine("Sending file list");

        if (out == null)
            throw new XMPPException(transfer.getException());
            
        try {
            BufferedWriter writer = new BufferedWriter(new PrintWriter(out));
            writer.write(xml);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new XMPPException(e);
        }
            
        log.info("Sent file list");
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.net.ITransmitter
     */
    public void sendFile(JID to, IPath path, IFileTransferCallback callback) {
        sendFile(to, path, -1, callback);
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.net.ITransmitter
     */
    public void sendFile(JID to, IPath path, int timestamp, 
        IFileTransferCallback callback) {
        
        FileTransfer transfer = new FileTransfer();
        transfer.recipient = to;
        transfer.path = path;
        transfer.timestamp = timestamp;
        transfer.callback = callback;
        
        fileTransferQueue.add(transfer);
        sendNextFile();
    }
    
    // TODO replace dependencies by more generic listener interfaces
    /* (non-Javadoc)
     * @see org.jivesoftware.smack.PacketListener
     */
    public void processPacket(Packet packet) {
        Message message = (Message)packet;
        
        JID fromJID = new JID(message.getFrom());
        putIncomingChat(fromJID, message.getThread());
        ISharedProject project = Saros.getDefault().getSessionManager().getSharedProject();

        ActivitiesPacketExtension activitiesPacket = 
            PacketExtensions.getActvitiesExtension(message);
        
        if (activitiesPacket != null) {
            List<TimedActivity> timedActivities = activitiesPacket.getActivities();
            
            log.info("Received activities: "+timedActivities);
            
            for (TimedActivity timedActivity : timedActivities) {
                
                /*
                 * incoming fileActivities that add files are only used as
                 * placeholder to bump the timestamp. the real fileActivity
                 * will be processed by using a file transfer.
                 */
                IActivity activity = timedActivity.getActivity();
                if (!(activity instanceof FileActivity) || 
                    !((FileActivity)activity).getType().equals(FileActivity.Type.Created)) {
                    
                    
                    project.getSequencer().exec(timedActivity);
                }
            }
        }

        if (PacketExtensions.getJoinExtension(message) != null) {
            for (IInvitationProcess process : processes) {
                if (process.getPeer().equals(fromJID))
                    process.joinReceived(fromJID);
            }
        }
        
        if (PacketExtensions.getLeaveExtension(message) != null) {
            if (project != null)
                project.removeUser(new User(fromJID)); // HACK
        }
        
        if (PacketExtensions.getRequestExtension(message)!= null) {
            for (IInvitationProcess process : processes) {
                if (process.getPeer().equals(fromJID))
                    process.fileListRequested(fromJID);
            }
        }
        
        DefaultPacketExtension inviteExtension = 
            PacketExtensions.getInviteExtension(message);
        
        if (inviteExtension != null) {
            String desc = inviteExtension.getValue(PacketExtensions.DESCRIPTION);
            
            SessionManager sm = Saros.getDefault().getSessionManager();
            sm.invitationReceived(fromJID, desc);
        }
        
        DefaultPacketExtension cancelInviteExtension = 
            PacketExtensions.getCancelInviteExtension(message);
        
        if (cancelInviteExtension != null) {
            String errorMsg = cancelInviteExtension.getValue(PacketExtensions.ERROR);
            
            for (IInvitationProcess process : processes) {
                if (process.getPeer().equals(fromJID))
                    process.cancel(errorMsg, true);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.jivesoftware.smackx.filetransfer.FileTransferListener
     */
    public void fileTransferRequest(FileTransferRequest request) {
        String fileDescription = request.getDescription();

        if (fileDescription.equals(FILELIST_TRANSFER_DESCRIPTION)) {
            FileList fileList = receiveFileList(request);
            JID fromJID = new JID(request.getRequestor());
            
            for (IInvitationProcess process : processes) {
                if (process.getPeer().equals(fromJID))
                    process.fileListReceived(fromJID, fileList);
            }
            
        } else if (fileDescription.startsWith(RESOURCE_TRANSFER_DESCRIPTION, 0)) {
            receiveResource(request);
        }
    }
    
    private void sendMessageToAll(ISharedProject sharedProject, 
        PacketExtension extension) { // HACK
        
        JID myJID = Saros.getDefault().getMyJID();
        
        for (User participant : sharedProject.getParticipants()) {
            if (participant.getJid().equals(myJID))
                continue;
            
            sendMessage(participant.getJid(), extension);
        }
    }

    private void sendMessage(JID jid, PacketExtension extension) {
        try {
            Chat chat = getChat(jid);
            
            Message message = chat.createMessage();
            message.addExtension(extension);
            chat.sendMessage(message);
        } catch (XMPPException e) {
        	Saros.getDefault().getLog().log(
				new Status(IStatus.ERROR, Saros.SAROS, IStatus.ERROR,
					"Could not send message", e));
        }
    }

    private void receiveResource(FileTransferRequest request) {
        try {
            log.fine("Receiving resource "+request.getFileName());
            
            JID from = new JID(request.getRequestor());
            Path path = new Path(request.getFileName());
            InputStream in = request.accept().recieveFile();
            
            boolean handledByInvitation = false;
            for (IInvitationProcess process : processes) {
                if (process.getPeer().equals(from)) {
                    process.resourceReceived(from, path, in);
                    handledByInvitation = true;
                }
            }
            
            if (!handledByInvitation) {
                FileActivity activity = new FileActivity(
                    FileActivity.Type.Created, path, in
                );
                
                int time;
                String description = request.getDescription();
                try {
                    time = Integer.parseInt(description.substring(
                        RESOURCE_TRANSFER_DESCRIPTION.length() + 1));
                } catch (NumberFormatException e) {
                    Saros.log("Could not parse time from description: " + description, e);
                    time = 0; // HACK
                }
                
                TimedActivity timedActivity = new TimedActivity(activity, time);
                
                SessionManager sm = Saros.getDefault().getSessionManager();
                sm.getSharedProject().getSequencer().exec(timedActivity);    
            }
            
            log.info("Received resource "+request.getFileName());
            
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to receive "+request.getFileName(), e);
        }        
    }

    private void sendNextFile() {
        if (fileTransferQueue.size() == 0 || runningFileTransfers > MAX_PARALLEL_SENDS)
            return;
        
        final FileTransfer transfer = fileTransferQueue.remove(0);
        
        new Thread(new Runnable() {
            
            /* (non-Javadoc)
             * @see java.lang.Runnable#run()
             */
            public void run() {
                try {
                    runningFileTransfers++;
                    transferFile(transfer);
                    
                } catch (Exception e) {
                    if (transfer.retries >= MAX_TRANSFER_RETRIES) {
                        log.log(Level.WARNING, "Failed to send "+transfer.path, e);
                        if (transfer.callback != null)
                            transfer.callback.fileTransferFailed(transfer.path, e);
                        
                    } else {
                        transfer.retries++;
                        fileTransferQueue.add(transfer);
                    }
                    
                } finally {
                    runningFileTransfers--;
                    sendNextFile();
                }
            }
            
        }).start();
    }
    
    private void transferFile(FileTransfer transferData) 
        throws CoreException, XMPPException, IOException {
        
        log.fine("Sending file "+transferData.path);
        
        JID recipient = transferData.recipient;
        
        SessionManager sm = Saros.getDefault().getSessionManager();
        IProject project = sm.getSharedProject().getProject();
        InputStream in = project.getFile(transferData.path).getContents();
        
        OutgoingFileTransfer transfer = 
            fileTransferManager.createOutgoingFileTransfer(recipient.toString());
        
        String description = RESOURCE_TRANSFER_DESCRIPTION;
        if (transferData.timestamp >= 0) {
            description = description + ':' + transferData.timestamp;
        }
        
        // HACK file size
        OutputStream out = transfer.sendFile(transferData.path.toString(), 
            1, description);
        
        if (out == null || transfer.getException() != null)
            throw new XMPPException(transfer.getException());
        
        byte[] buffer = new byte[1000];
        int length = -1;
        
        while ((length = in.read(buffer)) >= 0) {
            out.write(buffer, 0, length);
        }
        
        out.close();
        in.close();
        
        log.info("Sent file "+transferData.path);
        
        if (transferData.callback != null)
            transferData.callback.fileSent(transferData.path);
    }

    private FileList receiveFileList(FileTransferRequest request) {
        log.fine("Receiving file list");
        
        FileList fileList = null;
        final IncomingFileTransfer transfer = request.accept();
        
        try {
            InputStream in = transfer.recieveFile();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuffer sb = new StringBuffer();
            
            try {
                String line = null;
                while((line=reader.readLine()) != null) {
                    sb.append(line+"\n");
                }
            } catch(Exception e) {
            	Saros.log("Error while receiving file list", e);
            } finally {
                reader.close();
            }   
            
            fileList = new FileList(sb.toString());
            
            log.info("Received file list");
            
        } catch (Exception e) {
        	Saros.log("Exception while receiving file list", e);
        }
        
        return fileList;
    }

    private void putIncomingChat(JID jid, String thread) {
        if (!chats.containsKey(jid))
            chats.put(jid, new Chat(connection, jid.toString(), thread));
    }

    private Chat getChat(JID jid) {
        if (connection == null)
            throw new NullPointerException("Connection can't be null.");
        
        Chat chat = chats.get(jid);
        
        if (chat == null) {
            chat = new Chat(connection, jid.toString());
            chats.put(jid, chat);
        }
        
        return chat;
    }

    private void setProxyPort(XMPPConnection connection) {
        /*
         * Not supported!
         *  
         IPreferenceStore preferenceStore = Saros.getDefault().getPreferenceStore();
        
        fileTransferManager.getProperties().setProperty(
            Socks5TransferNegotiator.PROPERTIES_PORT, 
            preferenceStore.getString(PreferenceConstants.FILE_TRANSFER_PORT)
        );
        */
    }
}
