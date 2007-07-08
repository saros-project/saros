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
import java.io.File;
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
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.jivesoftware.smackx.filetransfer.Socks5TransferNegotiator;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.PreferenceConstants;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
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

	private XMPPConnection connection;

	private Map<JID, Chat> chats = new HashMap<JID, Chat>();

	private FileTransferManager fileTransferManager;

	// TODO use ListenerList instead
	private List<IInvitationProcess> processes = new CopyOnWriteArrayList<IInvitationProcess>();

	private List<FileTransfer>	  fileTransferQueue    = new LinkedList<FileTransfer>();
	private List<MessageTransfer> messageTransferQueue = new LinkedList<MessageTransfer>();

	private int runningFileTransfers = 0;

	/**
	 * A simple struct that is used to queue file transfers.
	 */
	private class FileTransfer {
		public JID recipient;

		public IPath path;

		public int timestamp;

		public IFileTransferCallback callback;

		public int retries = 0;
		
		public byte[] 	content;
		public long 	filesize;
	}

	/**
	 * A simple struct that is used to queue message transfers.
	 */
	private class MessageTransfer {
		public JID receipient;
		
		public PacketExtension packetextension;
	}
	
	public XMPPChatTransmitter(XMPPConnection connection) {
		SetXMPPConnection(connection);
	}
	
	public void SetXMPPConnection(XMPPConnection connection) {
		this.connection = connection;
		fileTransferManager = new FileTransferManager(connection);
		fileTransferManager.addFileTransferListener(this);
		
		chats.clear();

		setProxyPort(connection);

		// TODO always preserve threads
		this.connection.addPacketListener(this, new MessageTypeFilter(Message.Type.CHAT)); // HACK
	}

	public void addInvitationProcess(IInvitationProcess process) {
		processes.add(process);
	}

	public void removeInvitationProcess(IInvitationProcess process) {
		processes.remove(process);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.net.ITransmitter
	 */
	public void sendCancelInvitationMessage(JID user, String errorMsg) {
		sendMessage(user, PacketExtensions.createCancelInviteExtension(errorMsg));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.ITransmitter
	 */
	public void sendRequestForFileListMessage(JID user) {
		
		sendMessage(user, PacketExtensions.createRequestForFileListExtension());
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.ITransmitter
	 */
	public void sendRequestForActivity(ISharedProject sharedProject, int timestamp, boolean andup) {
		
		log.info("Requesting old activity (timestamp=" + timestamp+", "+andup+") from all...");

		sendMessageToAll(sharedProject, 
				PacketExtensions.createRequestForActivityExtension(timestamp,andup) );

	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.ITransmitter
	 */
	public void sendInviteMessage(ISharedProject sharedProject, JID guest, String description) {
		sendMessage(guest, PacketExtensions.createInviteExtension(sharedProject.getProject()
			.getName(), description));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.ITransmitter
	 */
	public void sendJoinMessage(ISharedProject sharedProject) {
		sendMessageToAll(sharedProject, PacketExtensions.createJoinExtension());
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
	public void sendActivities(ISharedProject sharedProject, List<TimedActivity> timedActivities) {

		// timer that calls sendActivities is called before setting chat

		for (TimedActivity timedActivity : timedActivities) {
			IActivity activity = timedActivity.getActivity();

			if (activity instanceof FileActivity) {
				FileActivity fileAdd = (FileActivity) activity;

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
			} else {
				sharedProject.getSequencer().getActivityHistory().add(timedActivity);
				
				// TODO: removed very old entries
			}
		}

		log.info("Sent activities: " + timedActivities);

		if (timedActivities != null ) {
			sendMessageToAll(sharedProject, new ActivitiesPacketExtension(timedActivities));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.ITransmitter
	 */
	public void sendFileList(JID recipient, FileList fileList) throws XMPPException {
		log.fine("Establishing file list transfer");

		String xml = fileList.toXML();

		String to = recipient.toString();
		
		int attempts = MAX_TRANSFER_RETRIES;
		
		while (true) {
			
			try {
				OutgoingFileTransfer transfer = fileTransferManager.createOutgoingFileTransfer(to);
	
				OutputStream out = transfer.sendFile(FILELIST_TRANSFER_DESCRIPTION, xml.getBytes().length,
						FILELIST_TRANSFER_DESCRIPTION);
	
				log.fine("Sending file list");
	
				if (out == null) {
					if (attempts-- > 0)
						continue;
					throw new XMPPException(transfer.getException());
				}
					
				BufferedWriter writer = new BufferedWriter(new PrintWriter(out));
				writer.write(xml);
				writer.flush();
				writer.close();
				break;

			} catch (IOException e) {
				if (attempts-- > 0)
					continue;
				throw new XMPPException(e);
			}
		}
		log.info("Sent file list");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.net.ITransmitter
	 */
	public void sendFile(JID to, IPath path, IFileTransferCallback callback) {
		sendFile(to, path, -1, callback);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.net.ITransmitter
	 */
	public void sendFile(JID to, IPath path, int timestamp, IFileTransferCallback callback)
	{

		FileTransfer transfer = new FileTransfer();
		transfer.recipient = to;
		transfer.path = path;
		transfer.timestamp = timestamp;
		transfer.callback = callback;
		
		// if transfer will be delayed, we need to buffer the file 
		// to not send modified versions later
		if (!connection.isConnected()) {
			SessionManager sm = Saros.getDefault().getSessionManager();
			IProject project = sm.getSharedProject().getProject();
			
			File f=new File(project.getFile(path).getLocation().toOSString());
			transfer.filesize=f.length();
			transfer.content=new byte[(int)transfer.filesize];
			
			try {
				InputStream in = project.getFile(path).getContents();
				in.read(transfer.content, 0, (int)transfer.filesize);
			} catch (CoreException e) {
				e.printStackTrace();
				transfer.content=null;
			} catch (IOException e) {
				e.printStackTrace();
				transfer.content=null;
			}
			
		
		} else 
			transfer.content=null;

		fileTransferQueue.add(transfer);
		sendNextFile();
	}

	private void sendNextFile() {
		if (	fileTransferQueue.size() == 0 || 
				runningFileTransfers > MAX_PARALLEL_SENDS ||
				Saros.getDefault().getConnectionState() != Saros.ConnectionState.CONNECTED )
			return;

		
		final FileTransfer transfer = fileTransferQueue.remove(0);

		new Thread(new Runnable() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				try {
					runningFileTransfers++;
					transferFile(transfer);

				} catch (Exception e) {
					if (transfer.retries >= MAX_TRANSFER_RETRIES) {
						log.log(Level.WARNING, "Failed to send " + transfer.path, e);
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
	
	public void sendUserListTo(JID to, List<User> participants) {
		log.fine("Sending user list to "+to.toString());

		sendMessage(to, PacketExtensions.createUserListExtension(participants) );		
	}
	
	public void sendRemainingFiles() {
		
		if ( fileTransferQueue.size() > 0)
			sendNextFile();		
	}
	
	public void sendRemainingMessages() {
		
		try {
			while (messageTransferQueue.size() > 0 ) {
				final MessageTransfer pex = messageTransferQueue.remove(0);
	
				Chat chat = getChat(pex.receipient);
				Message message;
				message = chat.createMessage();
				message.addExtension(pex.packetextension);
				chat.sendMessage(message);
				log.info("Resending message");
			}
		} catch (Exception e) {
			Saros.getDefault().getLog().log(
				new Status(IStatus.ERROR, Saros.SAROS, IStatus.ERROR, "Could not send message", e));
		}
	}
	
	public boolean resendActivity(JID jid, int timestamp, boolean andup) {

		boolean sent=false;
		
		ISharedProject project = Saros.getDefault().getSessionManager().getSharedProject();
			
		try {
			List<TimedActivity> tempActivities = new LinkedList<TimedActivity>();
			for (TimedActivity tact : project.getSequencer().getActivityHistory()  ) {
				
				if ((andup==false && tact.getTimestamp() != timestamp) ||
					(andup==true  && tact.getTimestamp() < timestamp ) )
					continue;
				
				tempActivities.add(tact);
				sent=true;
				
				if (andup==false )
					break;
			}

			if (sent) {
				PacketExtension extension = new ActivitiesPacketExtension(tempActivities);
				sendMessage(jid, extension);
			}
			
		} catch (Exception e) {
			Saros.getDefault().getLog().log(
					new Status(IStatus.ERROR, Saros.SAROS, IStatus.ERROR, "Could not resend message", e));
		}

		return sent;
	}

	// TODO replace dependencies by more generic listener interfaces
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jivesoftware.smack.PacketListener
	 */
	public void processPacket(Packet packet) {
		Message message = (Message) packet;

		JID fromJID = new JID(message.getFrom());
		putIncomingChat(fromJID, message.getThread());
		ISharedProject project = Saros.getDefault().getSessionManager().getSharedProject();

		ActivitiesPacketExtension activitiesPacket = PacketExtensions
			.getActvitiesExtension(message);

		boolean isProjectParticipant=false;
		if (project!=null)
			isProjectParticipant= (project.getParticipant(fromJID) != null);
		
		if (activitiesPacket != null) {
			List<TimedActivity> timedActivities = activitiesPacket.getActivities();

			log.info("Received activities from "+fromJID.toString()+": " + timedActivities);

			if (!isProjectParticipant) {
				log.info("user not member!");
				return;
			}

			for (TimedActivity timedActivity : timedActivities) {

				IActivity activity = timedActivity.getActivity();
				
				if (activity instanceof TextSelectionActivity ){
					((TextSelectionActivity)activity).setSource(fromJID.toString());
				}
				if (activity instanceof TextEditActivity ){
					((TextEditActivity)activity).setSource(fromJID.toString());
				}
				
				/*
				 * incoming fileActivities that add files are only used as
				 * placeholder to bump the timestamp. the real fileActivity will
				 * be processed by using a file transfer.
				 */
				if (!(activity instanceof FileActivity)
					|| !((FileActivity) activity).getType().equals(FileActivity.Type.Created)) {

					project.getSequencer().exec(timedActivity);
					
				}
			}
		}

		if (PacketExtensions.getJoinExtension(message) != null) {
			
			boolean iAmInviter=false;
			
			for (IInvitationProcess process : processes) {
				if (process.getPeer().equals(fromJID)){
					process.joinReceived(fromJID);
					iAmInviter=true;
				}
			}
			if (!iAmInviter && project!=null)
				project.addUser(new User(fromJID));	// a new user joined this session
				
		}

		if (PacketExtensions.getLeaveExtension(message) != null) {
			if (project != null)
				project.removeUser(new User(fromJID)); // HACK
		}

		if (PacketExtensions.getRequestActivityExtension(message) != null && isProjectParticipant) {
			DefaultPacketExtension rae=PacketExtensions.getRequestActivityExtension(message);
			String sID = rae.getValue("ID");
			String sIDandup = rae.getValue("ANDUP");
			
			int ts=-1;
			if (sID!=null) {
				ts= (new Integer(sID)).intValue();
				// get that activity from history (if it was mine) and send it
				boolean sent=resendActivity(fromJID, ts, (sIDandup!=null) );
				
				String info="Received Activity request for timestamp="+ts+".";
				if (sIDandup!=null)
					info+=" (andup) ";
				if (sent)
					info+=" I sent response.";
				else
					info+=" (not for me)";
				
				log.info(info);
			}
		}

		if (PacketExtensions.getRequestExtension(message) != null) {
			for (IInvitationProcess process : processes) {
				if (process.getPeer().equals(fromJID))
					process.invitationAccepted(fromJID);
			}
		}

		DefaultPacketExtension userlistExtension = PacketExtensions.getUserlistExtension(message);
		if (userlistExtension != null ) {
			// My inviter sent a list of all session participants
			// I need to adapt the order for later case of driver leaving the session
			log.fine( Saros.getDefault().getMyJID()+ "received user list from "+fromJID);
	
			int count=0;
			while( true ) {
				String jidS=userlistExtension.getValue( "User" + new Integer(count++).toString() );
				if (jidS==null)
					break;
				log.fine("   *:"+jidS);
				
				JID jid=new JID(jidS);
				User user=new User( jid);
				
				if (project.getParticipant(jid) == null) 
					sendMessage(jid, PacketExtensions.createJoinExtension());

				project.addUser(user, count-1 ); // add user to internal user list, maintaining the received order

			}
		}
		
		DefaultPacketExtension inviteExtension = PacketExtensions.getInviteExtension(message);

		if (inviteExtension != null) {
			String desc = inviteExtension.getValue(PacketExtensions.DESCRIPTION);
			String pName = inviteExtension.getValue(PacketExtensions.PROJECTNAME);

			SessionManager sm = Saros.getDefault().getSessionManager();
			sm.invitationReceived(fromJID, pName, desc);
		}

		DefaultPacketExtension cancelInviteExtension = PacketExtensions
			.getCancelInviteExtension(message);

		if (cancelInviteExtension != null) {
			String errorMsg = cancelInviteExtension.getValue(PacketExtensions.ERROR);

			for (IInvitationProcess process : processes) {
				if (process.getPeer().equals(fromJID))
					process.cancel(errorMsg, true);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
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

	private void sendMessageToAll(ISharedProject sharedProject, PacketExtension extension) { // HACK

		JID myJID = Saros.getDefault().getMyJID();
		
		for (User participant : sharedProject.getParticipants()) {
			if (participant.getJid().equals(myJID))
				continue;

			
			// if user is known to be offline, dont send but queue
			if (sharedProject!=null) {
			
				User user = sharedProject.getParticipant(participant.getJid());
				if (user!=null && user.getPresence() == User.UserConnectionState.OFFLINE ) {
					
					// offline for too long
					if ( user.getOfflineSecs() > FORCEDPART_OFFLINEUSER_AFTERSECS ) {
						log.info("Removing offline user from session...");
						sharedProject.removeUser(user);
					} else {
						queueMessage(participant.getJid(),extension);
						log.info("User known as offline - Message queued!");
					}
					
					continue;
				}
			}

			sendMessage(participant.getJid(), extension);
		}
	}

	private void queueMessage(JID jid, PacketExtension extension ) {
		MessageTransfer msg = new MessageTransfer();
		msg.receipient=jid;
		msg.packetextension = extension;
		messageTransferQueue.add( msg );
	}
	
	private void sendMessage(JID jid, PacketExtension extension) {
		
		if (!connection.isConnected()) {
			queueMessage(jid,extension);
			return;
		}
		
		try {
			Chat chat = getChat(jid);

			Message message;
			message = chat.createMessage();
			message.addExtension(extension);
			chat.sendMessage(message);

		} catch (Exception e) {
			queueMessage(jid,extension);
			
			Saros.getDefault().getLog().log(
				new Status(IStatus.ERROR, Saros.SAROS, IStatus.ERROR, "Could not send message, message queued", e));
		}
		
	}

	private void receiveResource(FileTransferRequest request) {
		try {

			JID from = new JID(request.getRequestor());
			Path path = new Path(request.getFileName());

			log.fine("Receiving resource from"+ from.toString()+": " + request.getFileName() );
			
			InputStream in = request.accept().recieveFile();

			boolean handledByInvitation = false;
			for (IInvitationProcess process : processes) {
				if (process.getPeer().equals(from)) {
					process.resourceReceived(from, path, in);
					handledByInvitation = true;
				}
			}

			if (!handledByInvitation) {
				FileActivity activity = new FileActivity(FileActivity.Type.Created, path, in);

				int time;
				String description = request.getDescription();
				try {
					time = Integer.parseInt(description.substring(RESOURCE_TRANSFER_DESCRIPTION
						.length() + 1));
				} catch (NumberFormatException e) {
					Saros.log("Could not parse time from description: " + description, e);
					time = 0; // HACK
				}

				TimedActivity timedActivity = new TimedActivity(activity, time);

				SessionManager sm = Saros.getDefault().getSessionManager();
				sm.getSharedProject().getSequencer().exec(timedActivity);
			}

			log.info("Received resource " + request.getFileName());

		} catch (Exception e) {
			log.log(Level.WARNING, "Failed to receive " + request.getFileName(), e);
		}
	}

	private void transferFile(FileTransfer transferData) throws CoreException, XMPPException,
		IOException {

		log.fine("Sending file " + transferData.path);

		JID recipient = transferData.recipient;

		SessionManager sm = Saros.getDefault().getSessionManager();
		IProject project = sm.getSharedProject().getProject();
		
		OutgoingFileTransfer transfer = fileTransferManager.createOutgoingFileTransfer(recipient
			.toString());

		String description = RESOURCE_TRANSFER_DESCRIPTION;
		if (transferData.timestamp >= 0) {
			description = description + ':' + transferData.timestamp;
		}

		// HACK file size
		OutputStream out = transfer.sendFile(transferData.path.toString(), 1, description);

		if (out == null || transfer.getException() != null)
			throw new XMPPException(transfer.getException());

		if (transferData.content==null) {
			byte[] buffer = new byte[1000];
			int length = -1;
			InputStream in = project.getFile(transferData.path).getContents();
			while ((length = in.read(buffer)) >= 0) {
				out.write(buffer, 0, length);
			}
			in.close();
		} else {
			out.write(transferData.content, 0, (int)transferData.filesize);
		}

		out.close();

		log.info("Sent file " + transferData.path);

		if (transferData.callback != null)
			transferData.callback.fileSent(transferData.path);
	}

	private FileList receiveFileList(FileTransferRequest request) {
		log.fine("Receiving file list");

		FileList fileList 	= null;
		try {
			final IncomingFileTransfer transfer = request.accept();

			InputStream in = transfer.recieveFile();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringBuffer sb = new StringBuffer();

			try {
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
			} catch (Exception e) {
				Saros.log("Error while receiving file list", e);
			} finally {
				reader.close();
			}

			fileList = new FileList(sb.toString());

			log.info("Received file list");

		} catch (Exception e) {
			Saros.log("Exception while receiving file list", e);
			// TODO retry? but we dont catch any exception here, 
			// smack might not throw them up
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

		IPreferenceStore preferenceStore = Saros.getDefault().getPreferenceStore();
 
		fileTransferManager.getProperties().setProperty(Socks5TransferNegotiator.PROPERTIES_PORT,
			preferenceStore.getString(PreferenceConstants.FILE_TRANSFER_PORT));

	}
}
