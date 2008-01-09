package de.fu_berlin.inf.dpp.net.internal;

import java.util.List;

import org.apache.log4j.Logger;
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
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.InvitationRejectionListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.IChatManager;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.project.ISharedProject;

public class MultiUserChatManager implements InvitationListener,
		InvitationRejectionListener, IChatManager {

	private static Logger log = Logger.getLogger(MultiUserChatManager.class
			.getName());

	public static String Room = "ori2007@conference.jabber.org";

	public static String JID_PROPERTY = "jid";

	/* current muc connection. */
	private MultiUserChat muc;

	/* current xmppconnection for transfer. */
	private XMPPConnection connection;

	private IReceiver receiver;
	
	private String currentJID;

	public MultiUserChatManager() {

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
				// try {
				// muc.sendConfigurationForm(getConfigForm(user));
				// } catch (Exception e) {
				// log.debug("");
				// }
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
			log.debug("configure room: ", e);
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

	public void sendActivities(ISharedProject sharedProject,
			List<TimedActivity> activities) {

		log.info("Sent muc activities: " + activities);
		try {
			/* create new message for multi chat. */
			Message newMessage = muc.createMessage();
			/* add packet extension. */
			newMessage.addExtension(new ActivitiesPacketExtension(activities));
			/* add jid property */
			newMessage.setProperty(JID_PROPERTY, Saros.getDefault().getMyJID()
					.toString());

			// newMessage.setBody("test");
			muc.sendMessage(newMessage);

		} catch (XMPPException e) {

			Saros.getDefault().getLog().log(
					new Status(IStatus.ERROR, Saros.SAROS, IStatus.ERROR,
							"Could not send message, message queued", e));
		}

	}

	public void setMUCConnection(XMPPConnection connection) {
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
			MultiUserChat.addInvitationListener(connection, this);
			/*
			 * der listener im muc reagiert nur auf chat messages, die packet
			 * extension muss über einen listener der XMPPConnection erfolgen.
			 */
			connection.addPacketListener(this, new MessageTypeFilter(
					Message.Type.groupchat));
		} catch (XMPPException xe) {
			xe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// TODO always preserve threads
		// this.connection.addPacketListener(this, new
		// MessageTypeFilter(Message.Type.chat)); // HACK
	}

	/**
	 * This method check sender of packet.
	 * 
	 * @param packet
	 *            incoming packet
	 * @param jid
	 * @return true if given jid is sender of packet.
	 */
	private boolean isMessageFromJID(Packet packet, JID jid) {
		if (packet instanceof Message) {
			Message message = (Message) packet;
			/* replace room */
			String sender = message.getFrom();
			/* replace room info */
			sender = sender.replace(MultiUserChatManager.Room + "/", "");
			if (sender.equals(jid.toString())) {
				message.setFrom(sender);
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	@Override
	public void processPacket(Packet packet) {
		log.debug("incoming packet");

		if (packet instanceof Message) {

			Message message = (Message) packet;

			/**
			 * 1. check getFrom JID. Host can send muc message and shouldn't
			 * receive the message again.
			 */
			if (isMessageFromJID(message, new JID(currentJID))) {
				log.debug("Own group message. Do nothing.");
				return;
			} else {

				/**
				 * 2. check message property. Observer can send muc messages and
				 * shouldn't receive the message again.
				 */
				String property = (String) message.getProperty(JID_PROPERTY);
				if(property.equals(currentJID)){
					log.debug("Own group message with property. Do nothing");
					return;
				}
				else{
					log.debug("Received group message with property");
					message.setFrom(property);
					
				}
				receiver.processPacket(message);
				return;
			}


		}

		if (packet instanceof Message) {
			Message msg = (Message) packet;
			// System.out.println("from " + msg.getFrom().replace(Room + "/",
			// "")
			// + " text: " + msg.getBody());
			log.info("received message : +" + msg.getBody() + " from "
					+ msg.getProperty("jid"));
		} else {
			System.out.println("other formated message received. ");
		}
	}

	@Override
	public void invitationReceived(XMPPConnection conn, String room,
			String inviter, String reason, String password, Message message) {
		/* init xmpp and muc connection. */
		setMUCConnection(conn);
		// TODO: Später besser ausbauen. Momantan wird nur ein fester Room
		// akzeptiert.

	}

	@Override
	public void invitationDeclined(String invitee, String reason) {
		// TODO: use case für ablehung aufstellen und umsetzen.
		System.out.println("Invitation declined: " + invitee + "with reason : "
				+ reason);
	}

	@Override
	public void setConnection(XMPPConnection connection, IReceiver receiver) {
		/**
		 * this method implements the connection to the muc room. To control
		 * creation and destroy process of muc room should be implements in
		 * separate class.
		 */
		this.connection = connection;
		this.currentJID = connection.getUser();
		try {
			/* init multi user chat connection. */
			initMUC(connection, Saros.getDefault().getConnection().getUser());
			/* init listener for muc messages. */
			muc.addMessageListener(this);
			MultiUserChat.addInvitationListener(connection, this);
			/*
			 * der listener im muc reagiert nur auf chat messages, die packet
			 * extension muss über einen listener der XMPPConnection erfolgen.
			 */
			connection.addPacketListener(this, new MessageTypeFilter(
					Message.Type.groupchat));
		} catch (XMPPException xe) {
			xe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		setReceiver(receiver);

		// TODO always preserve threads
		// this.connection.addPacketListener(this, new
		// MessageTypeFilter(Message.Type.chat)); // HACK

	}

	@Override
	public void setReceiver(IReceiver receiver) {
		this.receiver = receiver;

	}

}
