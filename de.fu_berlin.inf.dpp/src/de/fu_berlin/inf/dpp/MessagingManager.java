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
package de.fu_berlin.inf.dpp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.ui.MessagingWindow;

/**
 * MessagingManager handles all instant messaging related communications.
 * 
 * @author rdjemili
 */
public class MessagingManager implements PacketListener, IConnectionListener {

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

	/**
	 * Sessions are one-to-one IM chat sessions. Sessions are responsible for
	 * sending and receiving their messages. They also handle their IM chat
	 * window and save their history, even when their chat windows are disposed
	 * and reopened again.
	 */
	public class ChatSession implements SessionProvider, PacketListener {
		private String name;

		private Chat chat;

		private JID participant;

		private MessagingWindow window; // is null if disposed

		private List<ChatLine> history = new ArrayList<ChatLine>();

		public ChatSession(Chat chat, String name) {
			this.chat = chat;
			this.name = name;
			this.participant = new JID(chat.getParticipant());

			chat.addMessageListener(this); // HACK
			openWindow();
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
			final Message message = (Message) packet;

			if (message.getBody() == null)
				return;

			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					openWindow();

					addChatLine(name, message.getBody());
				}
			});
		}

		/**
		 * Opens the chat window for this chat session. Refocuses the window if
		 * it is already opened.
		 */
		public void openWindow() {
			if (window == null) {
				window = new MessagingWindow(this);
			}

			window.open();

			window.getShell().addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					window = null;
				}
			});
		}

		/*
		 * @see de.fu_berlin.inf.dpp.MessagingManager.SessionProvider
		 */
		public void sendMessage(String text) {
			try {
				Message msg = chat.createMessage();
				msg.setBody(text);
				chat.sendMessage(msg);

				addChatLine(Saros.getDefault().getMyJID().getName(), text);
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
			window.addChatLine(chatLine);

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

	private List<ChatSession> sessions = new ArrayList<ChatSession>();

	private List<IChatListener> chatListeners = new ArrayList<IChatListener>();

	public MessagingManager() {
		Saros.getDefault().addListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.listeners.IConnectionListener
	 */
	public void connectionStateChanged(XMPPConnection connection, ConnectionState newState) {
		if (newState == ConnectionState.NOT_CONNECTED && connection != null)
			connection.removePacketListener(this);

		if (newState == ConnectionState.CONNECTED)
			connection.addPacketListener(this, new MessageTypeFilter(Message.Type.CHAT));
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
		final Message message = (Message) packet;
		final JID jid = new JID(message.getFrom());

		if (message.getBody() == null)
			return;

		for (ChatSession session : sessions) {
			if (jid.equals(session.getParticipant())) {
				return; // gets already handled by message handler in session
			}
		}

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try {
					ChatSession session = showMessagingWindow(jid, message.getThread());

					// do this so that current message wont be lost
					session.processPacket(message);
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * @param thread
	 *            ID of thread or <code>null</code> if chat should start a new
	 *            thread.
	 * @throws XMPPException
	 */
	public ChatSession showMessagingWindow(JID remoteUser, String thread) throws XMPPException {
		if (!Saros.getDefault().isConnected())
			throw new XMPPException("No connection");

		for (ChatSession session : sessions) {
			if (remoteUser.equals(session.getParticipant())) {
				session.openWindow();
				return session;
			}
		}

		// create chat and open window
		XMPPConnection connection = Saros.getDefault().getConnection();
		Chat chat = (thread != null) ? new Chat(connection, remoteUser.toString(), thread)
			: new Chat(connection, remoteUser.toString());

		// try to get name from roster
		RosterEntry rosterEntry = connection.getRoster().getEntry(remoteUser.getBase());

		String name;
		if (rosterEntry != null) {
			name = "Talking to "
				+ (rosterEntry.getName() != null ? rosterEntry.getName() : rosterEntry.getUser());
		} else {
			name = "Chat";
		}

		ChatSession session = new ChatSession(chat, name);
		sessions.add(session);

		return session;
	}

	/**
	 * Adds the chat listener.
	 */
	public void addChatListener(IChatListener listener) {
		chatListeners.add(listener);
	}
}
