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
import java.io.ByteArrayInputStream;
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
 * An ITransmitter implementation which uses Smack Chat objects.
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

	private List<FileTransfer>	  fileTransferQueue		= new LinkedList<FileTransfer>();
	private List<MessageTransfer> messageTransferQueue	= new LinkedList<MessageTransfer>();
	private Map<String, IncomingFile> incomingFiles		= new HashMap<String,IncomingFile>();
	private int runningFileTransfers = 0;
	
	private boolean m_bFileTransferByChat = false;	// to switch to chat-filetransfer as fallback 
	

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
	 * A simple struct that is used to manage incoming chunked files via chat-file transfer
	 */
	private class IncomingFile {
		String	name;
		int		receivedChunks;
		int		chunkCount;
		List<String>	messageBuffer;

		IncomingFile() 		{ messageBuffer = new LinkedList<String>(); }
		boolean isComplete(){ return (receivedChunks==chunkCount); }
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
	
	public void setXMPPConnection(XMPPConnection connection) {
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
		
		String xml = fileList.toXML();
		String to = recipient.toString();

		if ( getFileTransferModeViaChat()) {

			if (sendChatTransfer(FILELIST_TRANSFER_DESCRIPTION, "", xml.getBytes(), recipient))
				log.fine("Sent file list via ChatTransfer");
			else
				log.fine("Error sending file list via ChatTransfer");
		}
		else {
			log.fine("Establishing file list transfer");

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
					
					log.info("File list sent");
					
					break;
	
				} catch (IOException e) {
					if (attempts-- > 0)
						continue;
					
					m_bFileTransferByChat = true;
					sendChatTransfer(FILELIST_TRANSFER_DESCRIPTION, "", xml.getBytes(), recipient);
					// TODO errorhandling

					log.info("File list sent via ChatTransfer. File transfer mode is set to ChatTransfer.");
				}
			}
		}
		
	}

	/**
	 * Sends a data buffer to a recipient using chat messages.
	 * The buffer is transmitted Base64 encoded and split into blocks of size MAX_MSG_LENGTH. 
	 * 
	 * @param name
	 *            name of the data buffer. e.g. the filename of the transmitted file
	 * @param desc
	 * 			  description String of the transfer. e.g. encoded timestamp for file activity
	 * @param data
	 * 			  a byte buffer to be transmitted. it will be base64 encoded
	 * @param recipient
	 * 			  JID of the user to send this data to
	 * 
	 * @return <code>true</code> if the message was send successfully
	 */
	boolean sendChatTransfer(String name, String desc, byte[] data, JID recipient) {

		final int maxMsgLen = Saros.getDefault().getPreferenceStore()
		.getInt(PreferenceConstants.CHATFILETRANSFER_CHUNKSIZE); 

		// Convert byte array to base64 string
		String data64 = new sun.misc.BASE64Encoder().encode(data);
			
		// send large data sets in several messages
		int tosend=data64.length();
		int pcount = (tosend/maxMsgLen) + ((tosend % maxMsgLen ==0)?0:1);
		int start = 0;
		try {
			for (int i=1;i<=pcount;i++) {
				int psize = Math.min(tosend, maxMsgLen);
				int end   = start + psize;
		
				PacketExtension extension = 
					PacketExtensions.createDataTransferExtension(
						name, desc, i, pcount, 
						data64.substring(start, end));
	
				sendMessage(recipient, extension);

				start=end;
				tosend-=psize;
				
			}
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Receives a data buffer sent by a chat message.
	 * The data will be decoded from base64 encoding.
	 * Splitted transfer will be buffered until all chunks are received. Then the file
	 * will be reconstructed and processed as a whole.
	 * 
	 * @param message
	 *            Message containing the data as extension.
	 * 
	 * @return <code>true</code> if the message was handled successfully.
	 */
	boolean receiveChatTransfer(Message message) {
		DefaultPacketExtension dt=PacketExtensions.getDataTransferExtension(message);
		String sName = dt.getValue(PacketExtensions.DT_NAME);
		String sData = dt.getValue(PacketExtensions.DT_DATA);

		String sSplit = dt.getValue(PacketExtensions.DT_SPLIT);
		try {		
			// is this a multipart transfer?
			if (sSplit!=null && sSplit.equals("1/1")==false) {
				// parse split information (index and chunk count)
				int i=sSplit.indexOf('/');
				int cur = Integer.parseInt( sSplit.substring(0, i) );
				int max = Integer.parseInt( sSplit.substring(i+1) );

				log.fine("Received chunk "+cur+" of " + max + " of file "+sName);
	
				// check for previous chunks
				IncomingFile ifile = incomingFiles.get(sName);
				if (ifile==null) {
					// this is the first received chunk->create incoming file object
					ifile = new IncomingFile();
					ifile.receivedChunks++;
					ifile.chunkCount=max;
					ifile.name=sName;
					for (i=0;i<max;i++)	ifile.messageBuffer.add(null);
					ifile.messageBuffer.set(cur-1, sData);
					incomingFiles.put(sName, ifile);
					return true;
				} else {
					// this is a following chunk
					ifile.receivedChunks++;
					ifile.messageBuffer.set(cur-1, sData);				
					
					if (ifile.isComplete()==false)
						return true;
					
					// join the buffers to restore the file from chunks
					sData = "";
					for (i=0;i<max;i++)
						sData += ifile.messageBuffer.get(i);
					incomingFiles.remove(ifile);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
			
		}
		
		byte[] dataOrg=null;
		try {
			dataOrg = new sun.misc.BASE64Decoder().decodeBuffer(sData);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		// File list received
		if (sName.equals(FILELIST_TRANSFER_DESCRIPTION)) {
			FileList fileList=null;
			IInvitationProcess myProcess=null;
			try {
				JID fromJID = new JID(message.getFrom());
				for (IInvitationProcess process : processes) {
					if (process.getPeer().equals(fromJID)) {
						myProcess = process;
						fileList = new FileList(new String(dataOrg) );
						process.fileListReceived(fromJID, fileList);
					}
				}
				log.info("Received file list via ChatTransfer");
			} catch (Exception e) {
				if(myProcess!=null)
					myProcess.cancel("Error receiving file list",false);
			}
		
		} else { 
			// receiving file (resource)
						
			try {

				JID from = new JID(message.getFrom());
				Path path = new Path(sName);
				
				ByteArrayInputStream in = new ByteArrayInputStream(dataOrg);

				log.fine("Receiving resource from "+ from.toString()+": " + sName + " (ChatTransfer)");
				
				boolean handledByInvitation = false;
				for (IInvitationProcess process : processes) {
					if (process.getPeer().equals(from)) {
						process.resourceReceived(from, path, in);
						handledByInvitation = true;
					}
				}

				if (!handledByInvitation) {
					
					if (Saros.getDefault().getSessionManager().getSharedProject()==null) {
						// receiving resource without a running session? not accepted
						return false;
					}

					
					FileActivity activity = new FileActivity(FileActivity.Type.Created, path, in);

					int time;
					String description = dt.getValue(PacketExtensions.DT_DESC);
					try {
						time = Integer.parseInt(description.substring(RESOURCE_TRANSFER_DESCRIPTION
							.length() + 1));
					} catch (Exception e) {
						Saros.log("Could not parse time from description: " + description, e);
						time = 0; // HACK
					}

					TimedActivity timedActivity = new TimedActivity(activity, time);

					SessionManager sm = Saros.getDefault().getSessionManager();
					sm.getSharedProject().getSequencer().exec(timedActivity);
				}

				log.info("Received resource " + sName);

			} catch (Exception e) {
				log.log(Level.WARNING, "Failed to receive " + sName, e);
			}
		}
		
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.net.ITransmitter
	 */
	public void sendFile(JID to, IPath path, IFileTransferCallback callback) {
		sendFile(to, path, -1, callback);
	}

	/**
	 * Reads a files content into a buffer.
	 * 
	 * @param transfer
	 *            Object containing file path and a buffer (among other) to read from and to.
	 *            
	 * @return <code>true</code> if the file was read successfully
	 */
	boolean readFile(FileTransfer transfer) {
		SessionManager sm = Saros.getDefault().getSessionManager();
		IProject project = sm.getSharedProject().getProject();
		
		File f=new File(project.getFile(transfer.path).getLocation().toOSString());
		transfer.filesize = f.length();
		transfer.content = new byte[(int) transfer.filesize];
		
		try {
			InputStream in = project.getFile(transfer.path).getContents();
			in.read(transfer.content, 0, (int)transfer.filesize);
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
	public void sendFile(JID to, IPath path, int timestamp, IFileTransferCallback callback)
	{

		FileTransfer transfer = new FileTransfer();
		transfer.recipient = to;
		transfer.path = path;
		transfer.timestamp = timestamp;
		transfer.callback = callback;
		
		// if transfer will be delayed, we need to buffer the file 
		// to not send modified versions later
		if (!connection.isConnected())
			readFile(transfer);		
		else 
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

		else if (PacketExtensions.getLeaveExtension(message) != null) {
			if (project != null)
				project.removeUser(new User(fromJID)); // HACK
		}

		else if (PacketExtensions.getRequestActivityExtension(message) != null && isProjectParticipant) {
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
		
		else if (PacketExtensions.getDataTransferExtension(message)!=null) {
			receiveChatTransfer(message);
		}

		else if (PacketExtensions.getRequestExtension(message) != null) {
			for (IInvitationProcess process : processes) {
				if (process.getPeer().equals(fromJID))
					process.invitationAccepted(fromJID);
			}
		}
		
		else if (PacketExtensions.getUserlistExtension(message) != null ) {
			DefaultPacketExtension userlistExtension = PacketExtensions.getUserlistExtension(message);
			
			// My inviter sent a list of all session participants
			// I need to adapt the order for later case of driver leaving the session
			log.fine( "Received user list from "+fromJID);
	
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
		
		else if (PacketExtensions.getInviteExtension(message) != null) {
			DefaultPacketExtension inviteExtension = PacketExtensions.getInviteExtension(message);
			String desc = inviteExtension.getValue(PacketExtensions.DESCRIPTION);
			String pName = inviteExtension.getValue(PacketExtensions.PROJECTNAME);

			SessionManager sm = Saros.getDefault().getSessionManager();
			sm.invitationReceived(fromJID, pName, desc);
		}

		else if (PacketExtensions.getCancelInviteExtension(message) != null) {
			DefaultPacketExtension cancelInviteExtension = PacketExtensions.getCancelInviteExtension(message);

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
		String description = RESOURCE_TRANSFER_DESCRIPTION;
		if (transferData.timestamp >= 0) {
			description = description + ':' + transferData.timestamp;
		}
	
		if (getFileTransferModeViaChat()) {

			if (transferData.content==null)
				readFile(transferData);
			
			sendChatTransfer(transferData.path.toString(),
					description,
					transferData.content, 
					recipient);

			log.info("Sent file " + transferData.path + " (by ChatTransfer)");

		} else {
			
			try {
			
			OutgoingFileTransfer transfer = fileTransferManager.createOutgoingFileTransfer(recipient
				.toString());
		
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
	
			} catch (Exception e) {
				
			}
		}
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
		if (!chats.containsKey(jid)){
			chats.put(jid, new Chat(connection, jid.toString(), thread));
		}

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
	
	private boolean getFileTransferModeViaChat() {
		return m_bFileTransferByChat || 
			Saros.getDefault().getPreferenceStore().
				getBoolean(PreferenceConstants.FORCE_FILETRANSFER_BY_CHAT);
	
	}
}
