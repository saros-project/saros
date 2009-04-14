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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.MessageEventManager;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.MultiUserChatManager;
import de.fu_berlin.inf.dpp.ui.MessagingWindow;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * MessagingManager handles all instant messaging related communications.
 * 
 * @author rdjemili
 * 
 *         TODO Needs Review and Clean-up
 * 
 * @component The single instance of this class per application is created by
 *            PicoContainer in the central plug-in class {@link Saros}
 */
public class MessagingManager implements PacketListener, MessageListener,
    IConnectionListener, InvitationListener {

    private static Logger log = Logger.getLogger(MessagingManager.class
        .getName());

    MessageEventManager messageEventManager;

    MultiUserChatManager multitrans = null;

    private final String CHAT_ROOM = "saros";

    public static class ChatLine {
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
     * Sessions are one-to-one IM chat sessions. Sessions are responsible for *
     * Since all sessions already handle their incoming messages, this method is
     * only to handle incoming chat messages for which no sessions have been yet
     * created. If it finds a chat message that belongs to no current session,
     * it creates a new session. sending and receiving their messages. They also
     * handle their IM chat window and save their history, even when their chat
     * windows are disposed and reopened again.
     * 
     * TODO CJ: Rework needed, we don't want one-to-one chats anymore
     * 
     * wanted: messages to all developers of programming session use this class
     * as fallback if muc fails?
     */
    public class ChatSession implements SessionProvider, MessageListener {
        private final Logger logCH = Logger.getLogger(ChatSession.class
            .getName());

        private final String name;

        private final Chat chat;

        private final JID participant;

        private MessagingWindow window; // is null if disposed

        private final List<ChatLine> history = new ArrayList<ChatLine>();

        public ChatSession(Chat chat, String name) {
            this.chat = chat;
            this.name = name;
            this.participant = new JID(chat.getParticipant());

            chat.addMessageListener(this);
            openWindow();
        }

        public String getName() {
            return this.name;
        }

        public List<ChatLine> getHistory() {
            return this.history;
        }

        /**
         * @return the participant associated to the chat object.
         */
        public JID getParticipant() {
            return this.participant;
        }

        public void processPacket(Packet packet) {
            this.logCH.debug("processPacket called");

            final Message message = (Message) packet;

            if (message.getBody() == null) {
                return;
            }

            Util.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    openWindow();

                    addChatLine(ChatSession.this.name, message.getBody());
                }
            });
        }

        public void processMessage(Chat chat, Message message) {
            // TODO new Method for messagelistener

            this.logCH.debug("processMessage called.");
            processPacket(message);

        }

        /**
         * Opens the chat window for this chat session. Refocuses the window if
         * it is already opened.
         */
        public void openWindow() {
            if (this.window == null) {
                this.window = new MessagingWindow(this);
                this.window.open();
                this.window.getShell().addDisposeListener(
                    new DisposeListener() {
                        public void widgetDisposed(DisposeEvent e) {
                            ChatSession.this.window = null;
                        }
                    });
            }
            this.window.getShell().forceActive();
            this.window.getShell().forceFocus();
        }

        /*
         * @see de.fu_berlin.inf.dpp.MessagingManager.SessionProvider
         */
        public void sendMessage(String text) {
            try {

                Message msg = new Message();
                msg.setBody(text);
                // send via muc process
                this.chat.sendMessage(msg);

                // for Testing
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

            this.history.add(chatLine);
            this.window.addChatLine(chatLine);

            for (IChatListener chatListener : MessagingManager.this.chatListeners) {
                chatListener.chatMessageAdded(sender, text);
            }
        }
    }

    public class MultiChatSession implements SessionProvider, PacketListener,
        MessageListener {
        private final Logger logCH = Logger.getLogger(ChatSession.class
            .getName());

        private final String name;

        private final MultiUserChat muc;

        private JID participant;

        private MessagingWindow window; // is null if disposed

        private final List<ChatLine> history = new ArrayList<ChatLine>();

        public MultiChatSession(MultiUserChat muc) {
            this.muc = muc;
            this.name = "Multi User Chat ("
                + Saros.getDefault().getMyJID().getName() + ")";
            muc.addMessageListener(this);
        }

        public String getName() {
            return this.name;
        }

        public List<ChatLine> getHistory() {
            return this.history;
        }

        /**
         * @return the participant associated to the chat object.
         */
        public JID getParticipant() {
            return this.participant;
        }

        public void processPacket(Packet packet) {
            this.logCH.debug("processPacket called");

            final Message message = (Message) packet;

            if (message.getBody() == null) {
                return;
            }

            // notify chat listener
            MessagingManager.log.debug("Notify Listener..");
            for (IChatListener l : MessagingManager.this.chatListeners) {
                l.chatMessageAdded(message.getFrom(), message.getBody());
                MessagingManager.log.debug("Notified Listener");
            }
        }

        public void processMessage(Chat chat, Message message) {
            this.logCH.debug("processMessage called.");
            processPacket(message);
        }

        /*
         * @see de.fu_berlin.inf.dpp.MessagingManager.SessionProvider
         */
        public void sendMessage(String text) {

            if (this.muc == null) {
                log.error("MUC does not exist");
                return;
            }

            try {
                Message msg = this.muc.createMessage();
                msg.setBody(text);
                this.muc.sendMessage(msg);
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

            this.history.add(chatLine);
            this.window.addChatLine(chatLine);

            for (IChatListener chatListener : MessagingManager.this.chatListeners) {
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

    private final List<ChatSession> sessions = new ArrayList<ChatSession>();

    private MultiChatSession multiSession;

    private final List<IChatListener> chatListeners = new ArrayList<IChatListener>();

    private MultiChatSession session;

    public MessagingManager() {
        Saros.getDefault().addListener(this);

        this.multitrans = new MultiUserChatManager(this.CHAT_ROOM);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.IConnectionListener
     */
    public void connectionStateChanged(XMPPConnection connection,
        ConnectionState newState) {
        if ((connection != null) && (newState == ConnectionState.NOT_CONNECTED)) {
            // TODO CJ Review: connection.removePacketListener(this);
            log.debug("unconnect");
        }

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
        MessagingManager.log.trace("processPacket called");
        final Message message = (Message) packet;
        final JID jid = new JID(message.getFrom());

        if (message.getBody() == null) {
            return;
        }

        /* check for multi or single chat. */
        if (message.getFrom().contains(this.multitrans.getRoomName())) {

            if (this.multiSession == null) {
                this.multiSession = new MultiChatSession(this.multitrans
                    .getMUC());
                this.multiSession.processPacket(message);
            }
        } else {
            /* old chat based message communication. */
            for (ChatSession session : this.sessions) {
                if (jid.equals(session.getParticipant())) {
                    return; // gets already handled by message handler in
                    // session
                }
            }
        }
    }

    public void processMessage(Chat chat, Message message) {
        // TODO new method for message notify
        MessagingManager.log.debug("processMessage called.");
        processPacket(message);
    }

    /**
     * Adds the chat listener.
     */
    public void addChatListener(IChatListener listener) {
        this.chatListeners.add(listener);
    }

    // TODO CJ Rework needed
    public void invitationReceived(XMPPConnection conn, String room,
        String inviter, String reason, String password, Message message) {
        try {
            // System.out.println(conn.getUser());
            if (this.multitrans.getMUC() == null) {
                // this.muc = XMPPMultiChatTransmitter.joinMuc(conn,
                // Saros.getDefault().getConnection().getUser(), room);
                this.multitrans.initMUC(conn, conn.getUser());
            }
            // muc.addMessageListener(mucl);
            // TODO Check if still connected
            if ((this.multiSession == null)
                && (this.multitrans.getMUC() != null)) {
                // muc.removeMessageListener(mucl);
                MultiChatSession session = new MultiChatSession(this.multitrans
                    .getMUC());
                this.multiSession = session;
            }
        } catch (XMPPException e) {
            // TODO SS Handle exception correctly
            e.printStackTrace();
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

    public MultiChatSession getSession() {
        return this.session;
    }

    public void connectMultiUserChat() throws XMPPException {
        if (!Saros.getDefault().isConnected()) {
            throw new XMPPException("No connection ");
        }
        String user = Saros.getDefault().getConnection().getUser();
        if (this.session == null) {
            MultiUserChat muc = this.multitrans.getMUC();
            if (muc == null) {
                this.multitrans.initMUC(Saros.getDefault().getConnection(),
                    user);
                muc = this.multitrans.getMUC();
            }
            MessagingManager.log.debug("Creating MUC session..");
            this.session = new MultiChatSession(muc);
        } else {
            this.multitrans.getMUC().join(user);
        }
    }

    public void disconnectMultiUserChat() throws XMPPException {
        MessagingManager.log.debug("Leaving MUC session..");
        this.multitrans.getMUC().leave();
    }

}
