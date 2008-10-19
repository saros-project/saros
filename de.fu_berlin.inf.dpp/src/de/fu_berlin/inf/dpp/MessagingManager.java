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
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
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
 * @author rdjemili,chjacob(chris_fu)
 */
public class MessagingManager implements PacketListener, IConnectionListener {

	private static Logger log = Logger.getLogger(MessagingManager.class.getName());

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

	
	public class MultiChatSession implements SessionProvider, PacketListener {
		
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
			log.debug("processPacket called");

			final Message message = (Message) packet;

			log.debug("Received Message from " + message.getFrom() + ": " +
					message.getBody());
		}

		/*
		 * @see de.fu_berlin.inf.dpp.MessagingManager.SessionProvider
		 */
		public void sendMessage(String text) {
			try {

				Message msg = muc.createMessage();
				msg.setBody(text);
				if (muc != null) {
					log.debug("Sending Message..");
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
			//initMultiChatListener();
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

		if (message.getBody() == null) return;
		else {
			//TODO handle Messages with no session
		}
		
	}

	/**
	 * Adds the chat listener.
	 */
	public void addChatListener(IChatListener listener) {
		chatListeners.add(listener);
	}

	public void showMultiChatMessagingView(JID remoteUser) throws XMPPException {

	if (!Saros.getDefault().isConnected())
		throw new XMPPException("No connection ");

	MultiUserChat muc = multitrans.getMUC();
	if (muc == null) {
			multitrans.initMUC(Saros.getDefault().getConnection(), Saros
					.getDefault().getConnection().getUser());
			muc = multitrans.getMUC();
	}


	if (multiSession == null) {
		log.debug("Creating MUC session..");
		multiSession = new MultiChatSession(muc);
		//multiSession.openView();
	} 
	else {
		//multiSession.openView();
	}
	multiSession.sendMessage("Hello");
		
	}
}
