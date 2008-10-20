package de.fu_berlin.inf.dpp.net.internal;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.packet.DiscoverItems.Item;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.net.IChatManager;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.MUCForbiddenException;
import de.fu_berlin.inf.dpp.net.RoomNotExistException;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.util.PacketProtokollLogger;

public class MultiUserChatManager implements IChatManager {

	private static Logger log = Logger.getLogger(MultiUserChatManager.class
			.getName());

	public String room = "saros";

	public static String JID_PROPERTY = "jid";

	/* current muc connection. */
	private MultiUserChat muc;

	/* current xmppconnection for transfer. */
	private XMPPConnection connection;

	public MultiUserChatManager() {

	}

	public MultiUserChatManager(String conference_room_name) {
		room = conference_room_name;
	}

	public void initMUC(XMPPConnection connection, String user, String room)
			throws XMPPException {
		this.room = room;
		initMUC(connection, user);
	}

	public void initMUC(XMPPConnection connection, String user)
			throws XMPPException {
		this.connection = connection;

		// TODO: Room name should be configured by settings.
		/* create room domain of current connection. */
		// JID(connection.getUser()).getDomain();
		room = room + "@conference.jabber.org";

		// Create a MultiUserChat using an XMPPConnection for a room
		MultiUserChat muc = new MultiUserChat(connection, room);

		if (isRoomExist(muc, room)) {
			if (!isJoined(muc, user)) {
				joinMuc(muc, user);
			} else {
				log.debug(" already joined. ");
			}
		} else {
			// Create the room
			muc.create(user);
			muc.sendConfigurationForm(getConfigForm(user, muc));
			log.debug("create room and send configuration.");

		}

		if (muc.isJoined()) {
			this.muc = muc;
			log.debug("Has joined in muc room.");
		} else {
			throw new XMPPException("Couldn't join with MUC room.");
		}
	}

	private Form getConfigForm(String user, MultiUserChat muc)
			throws XMPPException {
		// Get the the room's configuration form
		Form form = muc.getConfigurationForm();
		// Create a new form to submit based on the original form
		Form submitForm = form.createAnswerForm();

		try {
			submitForm.setAnswer("muc#roomconfig_moderatedroom", true);
		} catch (Exception e) {
			log.debug("configure room: ", e);
		}
		return submitForm;
	}

	public void joinMuc(MultiUserChat muc, String user) throws XMPPException {

		try {
			muc.join(user);
		} catch (Exception e) {
			log.error("try to join room. " + e.getMessage());
		}
		boolean isjoined = muc.isJoined();

		if (isjoined) {
			this.muc = muc;
		}
	}

	private boolean isRoomExist(MultiUserChat muc, String room) {
		try {
			// RoomInfo info = MultiUserChat.getRoomInfo(connection, Room);
			// Iterator<String> it = muc.getJoinedRooms(connection,
			// Saros.getDefault().getMyJID().toString());

			Collection<Affiliate> t = muc.getOwners();
			if (!t.isEmpty()) {
				return true;
			}
			return false;
		} catch (Exception e) {
			if (RoomNotExistException.MUC_ERROR_MESSAGE.equals(e.getMessage())) {
				/* no room exists. */
				return false;
			}
			if (MUCForbiddenException.FORBIDDEN_ERROR_MESSAGE.equals(e
					.getMessage())) {
				/* with restricted privileges */
				String roomName = muc.getRoom();
				if (roomName != null && roomName.equals(room)) {
					return true;
				}
			}
			// HACK
			if (e.getMessage().endsWith("No response from server.")) {
				/* in some case there are no response from existing room. */
				return true;
			}
			if (e.getMessage().endsWith("remote-server-not-found(404)")) {
				log.warn("try to check room: " + e.getMessage()
						+ " for room : " + room);
				return true;
			}

			log.warn("room exists failure", e);

			return false;
		}
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
				/* find out occupants of the muc room without be joined before. */
				ServiceDiscoveryManager discoManager = ServiceDiscoveryManager
						.getInstanceFor(connection);
				DiscoverItems items = discoManager.discoverItems(room);
				for (Iterator<Item> it = items.getItems(); it.hasNext();) {
					DiscoverItems.Item item = (DiscoverItems.Item) it.next();
					if (item.getEntityID().equals(room + "/" + user)) {
						return true;
					}
				}

			} catch (XMPPException xe) {
				log.warn(xe.getMessage());
			} catch (IllegalStateException e) {
				log.warn(e.getMessage());
			} catch (Exception e) {
				log.warn(e.getMessage());
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

		// log.info("Sent muc activities: " + activities);
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
			PacketProtokollLogger.getInstance().sendPacket(newMessage);

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
			connection.addPacketListener(this, new MessageTypeFilter(
					Message.Type.groupchat));
		} catch (XMPPException xe) {
			log.warn("XMPPException during muc connection setting: ", xe);
			// xe.printStackTrace();
		} catch (Exception e) {
			log.warn("XMPPException during muc connection setting: ", e);
			// e.printStackTrace();
		}
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
			sender = sender.replace(room + "/", "");
			if (sender.equals(jid.toString())) {
				message.setFrom(sender);
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public void processPacket(Packet packet) {
		// TODO should processing here instead of MessagingManager?
	}

	/**
	 * this method implements the connection to the muc room. To control
	 * creation and destroy process of muc room should be implements in separate
	 * class.
	 */
	public void setConnection(XMPPConnection connection, IReceiver receiver) {

		this.connection = connection;
		connection.getUser();
		try {
			/* init multi user chat connection. */
			initMUC(connection, connection.getUser());
			/* init listener for muc messages. */
			muc.addMessageListener(this);

			connection.addPacketListener(this, new MessageTypeFilter(
					Message.Type.groupchat));
		} catch (XMPPException xe) {
			log.error(xe.getMessage());
			xe.printStackTrace();
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}

		setReceiver(receiver);
	}

	public void setReceiver(IReceiver receiver) {

	}

	public String getRoomName() {
		return this.room;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.net.IChatManager#isConnected()
	 */
	public boolean isConnected() {
		if (muc != null && muc.isJoined()) {
			return true;
		}
		return false;
	}

	public void sendRequest(Request request) {
		// TODO Auto-generated method stub
	}
}
