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
package de.fu_berlin.inf.dpp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.MultiUserChatManager;

/**
 * MessagingManager handles all instant messaging related communications.
 * 
 * @author rdjemili
 */
public class MessagingManager implements PacketListener, MessageListener,
		IConnectionListener, InvitationListener {

	private static Logger log = Logger.getLogger(MessagingManager.class.getName());

	private MUCListener mucl = new MUCListener();
	private MultiUserChatManager multitrans = null;	
	private String CHAT_ROOM = "saros";

	public class ChatLine {
		public String sender;
		public String text;
		public Date date;
		public String packedID;
	}

	/**
	 * Encapsulates the interface that is needed by the MessagingWindow.
	 */
	public interface SessionProvider {
		public List<ChatLine> getHistory();
		public String getName();
		public void sendMessage(String msg);
	}

	
	public class MultiChatSession implements SessionProvider, PacketListener,
			MessageListener {
		
		private Logger logCH = Logger.getLogger(MultiChatSession.class.getName());
		private String name;
		private MultiUserChat muc;
		private JID participant;
		private List<ChatLine> history = new ArrayList<ChatLine>();

		public MultiChatSession(MultiUserChat muc) {
			this.muc = muc;
			this.name = "Multi User Chat ("+Saros.getDefault().getMyJID().getName()+")";
			muc.addMessageListener(this);
		}

		public String getName() {
			return name;
		}

		public List<ChatLine> getHistory() {
			return history;
		}

		/**
		 * @return the participant associated to the chat object.
		 */
		public JID getParticipant() {
			return participant;
		}

		public void processPacket(Packet packet) {
			logCH.debug("processPacket called");

			final Message message = (Message) packet;

			if (message.getBody() == null)
				return;
		}

		public void processMessage(Chat chat, Message message) {
			// TODO: new Method for messagelistener
			logCH.debug("processMessage called.");
			processPacket(message);

		}

		/*
		 * @see de.fu_berlin.inf.dpp.MessagingManager.SessionProvider
		 */
		public void sendMessage(String text) {
			try {
				// TODO: Ã„nderung:
				// Message msg = chat.createMessage();

				// TODO: Check connection before sending.

				// Message msg = new Message();
				// msg.setBody(text);
				// send via muc process
				// chat.sendMessage(msg);
				Message msg = muc.createMessage();
				msg.setBody(text);
				// TODO: FÃœR MUC
				if (muc != null) {
					muc.sendMessage(msg);
				}

			} catch (XMPPException e1) {
				e1.printStackTrace();
				addChatLine("error", "Couldn't send message");
			}
		}

		private void addChatLine(String sender, String text) {
			ChatLine chatLine = new ChatLine();
			chatLine.sender = sender;
			chatLine.text = text;
			chatLine.date = new Date();

			history.add(chatLine);

			for (IChatListener chatListener : chatListeners) {
				chatListener.chatMessageAdded(sender, text);
			}
		}

	}

	/**
	 * Listener for incoming chat messages.
	 */
	public interface IChatListener {
		public void chatMessageAdded(String sender, String message);
	}

	private List<MultiChatSession> sessions = new ArrayList<MultiChatSession>();

	private MultiChatSession multiSession;

	private List<IChatListener> chatListeners = new ArrayList<IChatListener>();

	public MessagingManager() {
		Saros.getDefault().addListener(this);
		
		this.multitrans = new MultiUserChatManager(CHAT_ROOM);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.listeners.IConnectionListener
	 */
	public void connectionStateChanged(XMPPConnection connection,
			ConnectionState newState) {
		if (connection != null && newState == ConnectionState.NOT_CONNECTED)
			// connection.removePacketListener(this);
			System.out.println("unconnect");

		if (newState == ConnectionState.CONNECTED) {
			connection.addPacketListener(this, new MessageTypeFilter(
					Message.Type.chat));
			initMultiChatListener();
		}
	}

	/**
	 * Since all sessions already handle their incoming messages, this method is
	 * only to handle incoming chat messages for which no sessions have been yet
	 * created. If it finds a chat message that belongs to no current session,
	 * it creates a new session.
	 * 
	 * @see org.jivesoftware.smack.PacketListener
	 */
	public void processPacket(Packet packet) {
		log.debug("messagePacket called");
		final Message message = (Message) packet;
		final JID jid = new JID(message.getFrom());

		if (message.getBody() == null)
			return;

		if (message.getFrom().contains(multitrans.getRoomName())) {
			
			if(multiSession == null){
				multiSession = new MultiChatSession(multitrans.getMUC());
				multiSession.processPacket(message);				
			}
		} else {
			/* old chat based message communication. */
			for (MultiChatSession session : sessions) {
				// System.out.println(session.getParticipant());
				if (jid.equals(session.getParticipant())) {
					return; // gets already handled by message handler in
							// session
				}
			}

			// TODO:Checken warum der Chat manchmal nicht aufgeht. !!!
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					try {
						MultiChatSession session = showMessagingWindow(jid, message
								.getThread());

						// do this so that current message wont be lost
						// session.processMessage(null, message);
						session.processPacket(message);
					} catch (XMPPException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
	}

	public void processMessage(Chat chat, Message message) {
		// TODO new method for message notify
		log.debug("processMessage called.");
		processPacket(message);

	}

	/**
	 * @param thread
	 *            ID of thread or <code>null</code> if chat should start a new
	 *            thread.
	 * @throws XMPPException
	 */
	public void showMultiChatMessagingWindow(JID remoteUser, String thread)
			throws XMPPException {
		if (!Saros.getDefault().isConnected())
			throw new XMPPException("No connection ");

		MultiUserChat muc = multitrans.getMUC();
		if (muc == null) {
//			muc = multitrans.getMUC();
//			if(muc == null){
				multitrans.initMUC(Saros.getDefault().getConnection(), Saros
						.getDefault().getConnection().getUser());
				muc = multitrans.getMUC();
//			}
		}

		Presence remoteUserPresence = muc
				.getOccupantPresence(CHAT_ROOM+"/"
						+ remoteUser.toString() + "/Smack");
		if (remoteUserPresence == null) {
			muc.invite(remoteUser.toString(), "Testing");
		}

		if (multiSession == null) {
			muc.removeMessageListener(mucl);
			try {
				multiSession = new MultiChatSession(muc);
			} catch (Exception e) {
				e.printStackTrace();
				multiSession = null;
				muc.addMessageListener(mucl);
			}
		} else {
		}
	}

	/**
	 * @param thread
	 *            ID of thread or <code>null</code> if chat should start a new
	 *            thread.
	 * @throws XMPPException
	 */
	public MultiChatSession showMessagingWindow(JID remoteUser, String thread)
			throws XMPPException {
		if (!Saros.getDefault().isConnected())
			throw new XMPPException("No connection ");

		for (MultiChatSession session : sessions) {
			// System.out.println(remoteUser);
			if (remoteUser.equals(session.getParticipant())) {
				return session;
			}
		}

		// create chat and open window
		XMPPConnection connection = Saros.getDefault().getConnection();

		// Chat chat = (thread != null) ? new Chat(connection,
		// remoteUser.toString(), thread)
		// : new Chat(connection, remoteUser.toString());

		ChatManager chatmanager = connection.getChatManager();
		Chat chat = null;
		if (thread != null) {
			// chat = chatmanager.createChat(remoteUser.toString(), thread,
			// this);
			chat = chatmanager.getThreadChat(thread);
			// chat = new Chat(connection, remoteUser.toString(), thread)
		} else {
			chat = chatmanager.createChat(remoteUser.toString(), this);
			// chat = new Chat(connection, remoteUser.toString());
		}

		// try to get name from roster
		RosterEntry rosterEntry = connection.getRoster().getEntry(
				remoteUser.getBase());

		String name;
		if (rosterEntry != null) {
			name = rosterEntry.getName() != null ? rosterEntry.getName()
					: rosterEntry.getUser();
		} else {
			name = "unknown";
		}

		MultiUserChat muc = multitrans.getMUC();
		MultiChatSession session = new MultiChatSession(muc);
		// add this chat session to message listener of this chat instance.
		chat.addMessageListener(session);
		// chat.removeMessageListener(this);
		sessions.add(session);
		return session;
	}

	/**
	 * Adds the chat listener.
	 */
	public void addChatListener(IChatListener listener) {
		chatListeners.add(listener);
	}

	/* MultiUserChat section */

	public void invitationReceived(XMPPConnection conn, String room,
			String inviter, String reason, String password, Message message) {
		log.debug("InvitationReceived");
		if (multitrans.getMUC() == null) {
			// this.muc = XMPPMultiChatTransmitter.joinMuc(conn,
			// Saros.getDefault().getConnection().getUser(), room);
			//multitrans.initMUC(conn, conn.getUser());
		}
		// muc.addMessageListener(mucl);
		// showMultiChatMessagingWindow(new JID("Multi User Chat"), null);
		// TODO: Überprüfen, ob auch noch verbunden
		if (multiSession == null && multitrans.getMUC() != null) {
			// muc.removeMessageListener(mucl);
			MultiChatSession session = new MultiChatSession(multitrans
					.getMUC());
			this.multiSession = session;
		} else {
		}

	}

	/**
	 * invitation listener for multi chat invitations.
	 */
	public void initMultiChatListener() {
		// listens for MUC invitations
		MultiUserChat.addInvitationListener(Saros.getDefault().getConnection(),
				this);
	}

	/**
	 * this class is only for testing with muc message listener.
	 * 
	 * @author rdjemili
	 * 
	 */
	class MUCListener implements MessageListener, PacketListener {

		public void processMessage(Chat chat, Message message) {
			System.out.println("jetzt gehts los");

		}

		public void processPacket(Packet packet) {
			if (packet instanceof Message) {
				Message msg = (Message) packet;
				System.out.println("from " + msg.getFrom() + " text: "
						+ msg.getBody());
			}
		}
	}
}
