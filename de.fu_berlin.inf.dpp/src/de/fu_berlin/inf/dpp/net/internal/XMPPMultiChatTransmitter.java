package de.fu_berlin.inf.dpp.net.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.InvitationRejectionListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.muc.RoomInfo;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.IFileTransferCallback;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.project.ISharedProject;

public class XMPPMultiChatTransmitter implements ITransmitter, PacketListener,
		InvitationListener, InvitationRejectionListener {

	private static Logger log = Logger.getLogger(XMPPMultiChatTransmitter.class
			.getName());

	public static String Room = "ori2007@conference.jabber.org";

	/* current muc connection. */
	private MultiUserChat muc;

	/* current xmppconnection for transfer. */
	private XMPPConnection connection;

	public XMPPMultiChatTransmitter() {

	}

	public void initMUC(XMPPConnection connection, String user)
			throws XMPPException {
		this.muc = null;
		// Create a MultiUserChat using an XMPPConnection for a roomacknowledge
		MultiUserChat muc = new MultiUserChat(connection, Room);

		if (!isJoined(muc, user)) {

			if (!isRoomExist(muc, Room)) {
				// Create the room
				muc.create(user);

				// Send an empty room configuration form which indicates that we
				// want
				// an instant room
				// muc.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
				muc.sendConfigurationForm(getConfigForm(user));
				if (muc.isJoined()) {
					this.muc = muc;
				}
			} else {
				joinMuc(muc, user);
				try {
					muc.sendConfigurationForm(getConfigForm(user));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	private Form getConfigForm(String user) throws XMPPException {
		// Get the the room's configuration form
		Form form = muc.getConfigurationForm();
		// Create a new form to submit based on the original form
		Form submitForm = form.createAnswerForm();

		// // Add default answers to the form to submit
		// for (Iterator<FormField> fields = form.getFields();
		// fields.hasNext();) {
		// FormField field = (FormField) fields.next();
		// if (!FormField.TYPE_HIDDEN.equals(field.getType())
		// && field.getVariable() != null) {
		// // Sets the default value as the answer
		// submitForm.setDefaultAnswer(field.getVariable());
		// }
		// }
		try {
			submitForm.setAnswer("muc#roomconfig_moderatedroom", true);
			// Sets the new owner of the room
			// List<String> owners = new ArrayList<String>();
			// owners.add(user);

			// FormField field = new FormField("muc#roomconfig_roomowners");
			// field.addValues(owners);
			// submitForm.addField(field);

			// submitForm.setAnswer("muc#roomconfig_roomowners", owners);

			// Collection<Affiliate> owner = muc.getOwners();
			//			
			// for(Affiliate a : owner){
			// System.out.println(a.getJid());
			// }
			// System.out.println(owners.size());
			// System.out.println(owner.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return submitForm;
	}

	public void joinMuc(MultiUserChat muc, String user) throws XMPPException {

		DiscussionHistory history = new DiscussionHistory();
		history.setSeconds(2);

		muc.join(user, null, history, SmackConfiguration
				.getPacketReplyTimeout());

		boolean isjoined = muc.isJoined();

		if (isjoined) {
			this.muc = muc;
		}
	}

	private boolean isRoomExist(MultiUserChat muc, String room) {
		try {
			String roomName = muc.getRoom();
			if (roomName != null && roomName.equals(Room)) {
				return true;
			}
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("no room exist.");
			return false;
		}
	}

	private boolean isRoomExist(XMPPConnection connection, String room) {
		try {
			RoomInfo info = MultiUserChat.getRoomInfo(connection, room);
			if (info.isPersistent()) {
				return true;
			}
		} catch (XMPPException e) {
			// TODO Auto-generated catch block
			System.out.println("no room exist.");

		}

		return false;
	}

	private boolean isJoined(MultiUserChat tmuc, String user)
			throws XMPPException {
		boolean isjoined = false;
		if (muc != null) {
			try {
				muc.isJoined();
			} catch (IllegalStateException ise) {
				/* if no logged into exception is called. */
				return false;
			}

		} else {
			try {
				// Collection<Occupant> kd = muc.getParticipants();
				tmuc.changeNickname(user + "1");
				isjoined = true;
				tmuc.changeNickname(user);
				// System.out.println(kd.size());
			} catch (IllegalStateException e) {
				System.out.println("no logged in.");

				// muc = joinMuc(connection, user, Room);
				// tmuc.changeNickname(user);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return isjoined;
	}

	/**
	 * this method returns current muc or null no muc exists.
	 * 
	 * @return
	 */
	public MultiUserChat getMUC() {
		return muc;
	}

	public boolean initIndicateForm(XMPPConnection connection, String user)
			throws XMPPException {
		// Create a MultiUserChat using an XMPPConnection for a roomacknowledge
		MultiUserChat muc = new MultiUserChat(connection, Room);

		if (isJoined(muc, user)) {

			if (!isRoomExist(connection, Room)) {
				// Create the room
				muc.create(user);

				// Send an empty room configuration form which indicates that we
				// want
				// an instant room
				muc.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
			}
			// else{
			// return joinMuc(connection, user, Room);
			// }
		}
		this.muc = muc;
		return true;
	}

	@Override
	public void addInvitationProcess(IInvitationProcess invitation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeInvitationProcess(IInvitationProcess invitation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendActivities(ISharedProject sharedProject,
			List<TimedActivity> activities) {

		log.info("Sent muc activities: " + activities);
		try {
			/* create new message for multi chat. */
			Message newMessage = muc.createMessage();
			/* add packet extension. */
			newMessage.addExtension(new ActivitiesPacketExtension(activities));
//			newMessage.setBody("test");
			muc.sendMessage(newMessage);
			
		} catch (XMPPException e) {

			Saros.getDefault().getLog().log(
					new Status(IStatus.ERROR, Saros.SAROS, IStatus.ERROR,
							"Could not send message, message queued", e));
		}

	}

	@Override
	public void sendCancelInvitationMessage(JID jid, String errorMsg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendFile(JID recipient, IPath path,
			IFileTransferCallback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendFile(JID recipient, IPath path, int timestamp,
			IFileTransferCallback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendFileList(JID jid, FileList fileList) throws XMPPException {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendInviteMessage(ISharedProject sharedProject, JID jid,
			String description) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendJoinMessage(ISharedProject sharedProject) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendLeaveMessage(ISharedProject sharedProject) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendRemainingFiles() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendRemainingMessages() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendRequestForActivity(ISharedProject sharedProject,
			int timestamp, boolean andup) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendRequestForFileListMessage(JID recipient) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendUserListTo(JID to, List<User> participants) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setXMPPConnection(XMPPConnection connection) {
		/**
		 * this method implements the connection to the muc room. To control
		 * creation and destroy process of muc room should be implements in
		 * separate class.
		 */
		this.connection = connection;
		try {
			/* init multi user chat connection. */
			initMUC(connection, Saros.getDefault().getConnection().getUser());
			/* init listener for muc messages. */
			muc.addMessageListener(this);
			MultiUserChat.addInvitationListener(connection,this);
			/* der listener im muc reagiert nur auf chat messages, die packet extension muss über
			 * einen listener der XMPPConnection erfolgen. */
			connection.addPacketListener(this, new MessageTypeFilter(Message.Type.groupchat));
		} catch (XMPPException xe) {
			xe.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}

		// TODO always preserve threads
		// this.connection.addPacketListener(this, new
		// MessageTypeFilter(Message.Type.chat)); // HACK
	}

	@Override
	public void processPacket(Packet packet) {
		if (packet instanceof Message) {
			Message msg = (Message) packet;
//			System.out.println("from " + msg.getFrom().replace(Room + "/", "")
//					+ " text: " + msg.getBody());
			log.info("received message : +"+msg);
		}
		else{
			System.out.println("other formated message received. ");
		}
	}

	@Override
	public void invitationReceived(XMPPConnection conn, String room,
			String inviter, String reason, String password, Message message) {
		/* init xmpp and muc connection. */
		setXMPPConnection(conn);
		// TODO: Später besser ausbauen. Momantan wird nur ein fester Room
		// akzeptiert.

	}

	@Override
	public void invitationDeclined(String invitee, String reason) {
		// TODO: use case für ablehung aufstellen und umsetzen.
		System.out.println("Invitation declined: " + invitee + "with reason : "
				+ reason);
	}

}
