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
package de.fu_berlin.inf.dpp.xmpp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.IInvitationProcess;
import de.fu_berlin.inf.dpp.ISharedProject;
import de.fu_berlin.inf.dpp.ITransmitter;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SessionManager;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IncomingResourceAddActivity;
import de.fu_berlin.inf.dpp.activities.ResourceAddActivity;

/**
 * An ITransmitter implementation which uses Smack Chat objects. It currently
 * only supports two users at max.
 * 
 * @author rdjemili
 */
public class XMPPChatTransmitter implements ITransmitter, PacketListener, FileTransferListener {
    private static final int         MAX_PARALLEL_SENDS = 10;
    
    /*
     * the following string descriptions are used to differentiate between
     * transfers that are for invitations and transfers that are an activity for
     * the current project.
     */
    private static final String RESOURCE_TRANSFER_DESCRIPTION = "resourceAddActivity";
    private static final String FILELIST_TRANSFER_DESCRIPTION = "filelist";

    private final XMPPConnection     connection;
    private Map<JID, Chat>           chats              = new HashMap<JID, Chat>();
    private FileTransferManager      fileTransferManager;

    private List<IInvitationProcess> processes = new CopyOnWriteArrayList<IInvitationProcess>();

    private List<SendStruct>         sendQueue          = new LinkedList<SendStruct>();
    private int                      parallelSends      = 0;
    
    private class SendStruct {
        public JID   jid;
        public IPath path;
        public int   time;
    }

    public XMPPChatTransmitter(XMPPConnection connection) {
        this.connection = connection;
        fileTransferManager = new FileTransferManager(connection);
        fileTransferManager.addFileTransferListener(this);
        
        System.out.println("chat transmitter constructed "+this);
        
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
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendRequestForFileListMessage(JID host) {
        sendMessage(host, PacketExtensions.createRequestForFileListExtension());
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendInviteMessage(ISharedProject sharedProject, JID guest, String description) {
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
    public void sendCloseSessionMessage(ISharedProject sharedProject) {
        sendMessageToAll(sharedProject, PacketExtensions.createCloseExtension());
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendActivities(ISharedProject sharedProject, 
        List<IActivity> activities, int time) throws CoreException {
        
        // timer that calls sendActivities is called before setting chat
        
        int sendTime = time;
        List<ResourceAddActivity> resourceAdds = new ArrayList<ResourceAddActivity>();
        for (IActivity activity : activities) {
            if (activity instanceof ResourceAddActivity) {
                ResourceAddActivity resourceAdd = (ResourceAddActivity)activity;
                
                JID myJID = Saros.getDefault().getMyJID();
                for (User participant : sharedProject.getParticipants()) {
                    if (participant.getJid().equals(myJID))
                        continue;
                    
                    sendResource(participant.getJid(), resourceAdd.getPath(), sendTime);
                }
                
                resourceAdds.add(resourceAdd);
            }
            
            sendTime++;
        }
        
        System.out.println("send activities: "+activities);
        
        if (activities != null && connection != null) {
            sendMessageToAll(sharedProject, new ActivitiesPacketExtension(activities, time));
        }
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendFileList(JID to, FileList fileList) throws XMPPException, IOException {
        System.out.println("sending file list");
        
        String xml = fileList.toXML();

        OutgoingFileTransfer transfer = 
            fileTransferManager.createOutgoingFileTransfer(to.getJID());
        
        OutputStream out = transfer.sendFile(FILELIST_TRANSFER_DESCRIPTION, 
            xml.getBytes().length, FILELIST_TRANSFER_DESCRIPTION);

        if (out != null) {
            BufferedWriter writer = new BufferedWriter(new PrintWriter(out));
            writer.write(xml);
            writer.close();
            
            System.out.println("sent file list");
            
        } else {
            if (transfer.getException() != null)
                throw new XMPPException(transfer.getException());
        }
    }

    // TODO wrap CoreException in TransmitterException 
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.ITransmitter
     */
    public void sendResource(JID to, IPath path) throws CoreException {
        sendResource(to, path, -1); // HACK
    }
    
    public void sendResource(JID to, IPath path, int time) throws CoreException {
        SendStruct sendStruct = new SendStruct();
        sendStruct.jid = to;
        sendStruct.path = path;
        sendStruct.time = time;
        
        sendQueue.add(sendStruct);
        
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
            int time = activitiesPacket.getTime();
            for (IActivity activity : activitiesPacket.getActivities()) {
                /*
                 * resourceAddActivity is only used as placeholder to bump the
                 * time. the real resourceAddActivity will be processed by using
                 * a file transfer and a IncomingResourceAddActivity object.
                 */
                if (!(activity instanceof ResourceAddActivity)) {
                    project.getSequencer().exec(time, activity);
                }
                
                time++;
            }
        }

        if (PacketExtensions.getJoinExtension(message) != null) {
            for (IInvitationProcess process : processes) {
                if (process.getPeer().equals(fromJID))
                    process.joinReceived(fromJID);
            }
        }
        
        if (PacketExtensions.getLeaveExtension(message) != null) {
            project.removeUser(new User(fromJID)); // HACK
        }
        
        if (PacketExtensions.getCloseExtension(message) != null) {
            Saros.getDefault().getSessionManager().sessionClosed();
        }
        
        if (PacketExtensions.getRequestExtension(message)!= null) {
            for (IInvitationProcess process : processes) {
                if (process.getPeer().equals(fromJID))
                    process.fileListRequested(fromJID);
            }
        }
        
        DefaultPacketExtension inviteExtension = PacketExtensions.getInviteExtension(message);
        if (inviteExtension != null) {
            String description = inviteExtension.getValue(PacketExtensions.DESCRIPTION);
            
            Saros.getDefault().getSessionManager().createIncomingInvitation(
                fromJID, description);
        }
    }
    
//    public static void handle(List<IActivity> activities, int startTime) {
//        ISharedProject project = Saros.getDefault().getSessionManager().getSharedProject();
//
//        for (IActivity activity : activities) {
//            if (!(activity instanceof NoOpActivity)) {
//                project.getSequencer().exec(startTime, activity);
//            } 
//            startTime++;
//        }
//    }
    
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
            try {
                JID from = new JID(request.getRequestor());
                Path path = new Path(request.getFileName());
                InputStream in = request.accept().recieveFile();
                
                for (IInvitationProcess process : processes) {
                    if (process.getPeer().equals(from)) {
                        process.resourceReceived(from, path, in);
                        return;
                    }
                }
                
                receiveResource(request);
                
            } catch (XMPPException e) {
                e.printStackTrace();
            }
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
            // TODO
            e.printStackTrace();
        }
    }

    private void receiveResource(FileTransferRequest request) { // HACK
        try {
            InputStream in = request.accept().recieveFile();
            IPath path = new Path(request.getFileName());
            
            IncomingResourceAddActivity activity = 
                new IncomingResourceAddActivity(path, in);
            
            int time;
            try {
                time = Integer.parseInt(request.getDescription().substring(
                    RESOURCE_TRANSFER_DESCRIPTION.length() + 1));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                
                time = 0; // HACK
            }            
            
            SessionManager sessionManager = Saros.getDefault().getSessionManager();
            sessionManager.getSharedProject().getSequencer().exec(time, activity);
            
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }        
    }

    private void sendNextFile() throws CoreException {
        if (sendQueue.size() == 0 || parallelSends > MAX_PARALLEL_SENDS)
            return;
        
        final SendStruct sendStruct = sendQueue.remove(0);
        
        new Thread(new Runnable() {

            public void run() {
                try {
                    parallelSends++;
                    
                    System.out.println("sending file "+sendStruct.path);
                    
                    JID to = sendStruct.jid;
                    
                    SessionManager sessionManager = Saros.getDefault().getSessionManager();
                    IProject project = sessionManager.getSharedProject().getProject();
                    InputStream in = project.getFile(sendStruct.path).getContents();
                    
                    OutgoingFileTransfer transfer = 
                        fileTransferManager.createOutgoingFileTransfer(to.getJID());
                    
                    String description = RESOURCE_TRANSFER_DESCRIPTION;
                    if (sendStruct.time >= 0) {
                        description = description + ':' + sendStruct.time;
                    }
                    
                    // HACK file size
                    OutputStream out = transfer.sendFile(sendStruct.path.toString(), 
                        1, description);
                    
                    if (out == null || transfer.getException() != null) {
                        throw new XMPPException(transfer.getException());
                    }
                    
                    byte[] buffer = new byte[1000];
                    int length = -1;
                    
                    while ((length = in.read(buffer)) >= 0) {
                        out.write(buffer, 0, length);
                    }
                    
                    out.close();
                    in.close();
                    
                    System.out.println("sending file "+sendStruct.path + " done");
                    
                    for (IInvitationProcess process : processes) {
                        process.resourceSent(sendStruct.path);
                    }
                    
                    parallelSends--;
                    sendNextFile();
                    
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
        }).start();
    }

    private FileList receiveFileList(FileTransferRequest request) {
        FileList fileList = null;
        
        System.out.println("receiving file list");
        
        final IncomingFileTransfer transfer = request.accept();
        
        try {
            InputStream in = transfer.recieveFile();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuffer sb = new StringBuffer();
            
            try {
                String line = null;
                while((line=reader.readLine()) != null) {
                    if (transfer.getStatus().equals(Status.CANCLED)) {
                        break; // TODO
                    }
                    
                    sb.append(line+"\n");
                }
                
            } catch(Exception e) {
                e.printStackTrace();
                
            } finally {
                try {reader.close();} catch(Exception ex) {ex.printStackTrace();}
            }   
            
            fileList = new FileList(sb.toString());
            
            System.out.println("received file list");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return fileList;
    }

    private void putIncomingChat(JID jid, String thread) {
        if (!chats.containsKey(jid))
            chats.put(jid, new Chat(connection, jid.getJID(), thread));
    }

    private Chat getChat(JID jid) {
        if (connection == null)
            throw new NullPointerException("Connection can't be null.");
        
        Chat chat = chats.get(jid);
        
        if (chat == null) {
            chat = new Chat(connection, jid.getJID());
            chats.put(jid, chat);
        }
        
        return chat;
    }
}
