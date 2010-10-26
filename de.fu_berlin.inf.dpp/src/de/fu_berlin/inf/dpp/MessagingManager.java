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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.ChatState;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.ChatStatusManager;
import de.fu_berlin.inf.dpp.MultiUserChatManager;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.util.CommunicationNegotiatingManager;
import de.fu_berlin.inf.dpp.util.CommunicationNegotiatingManager.CommunicationPreferences;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * MessagingManager manages the multi user chat (MUC). It's responsible for
 * creating and maintaining the connection to the chat room. <br>
 * Add a {@link MessagingManager.IChatListener} to get notified of chat events.
 * 
 * @author rdjemili
 * @author ahaferburg
 * @author mariaspg
 */
@Component(module = "net")
public class MessagingManager implements IConnectionListener,
    InvitationListener {

    /**
     * Listener for incoming chat messages.
     */
    public interface IChatListener {
        public void chatMessageAdded(User sender, String message);

        public void chatStateUpdated(User sender, ChatState state);

        public void chatJoined(User joinedUser);

        public void chatLeft(User leftUser);
    }

    private static final Logger log = Logger.getLogger(MessagingManager.class);

    protected Saros saros;

    protected SessionManager sessionManager;

    protected ChatStatusManager chatStatusManager;

    @Inject
    protected CommunicationNegotiatingManager comNegotiatingManager;

    /** True iff we're in a Saros session. */
    private boolean sarosSessionRunning = false;

    /** True iff we're currently in the chat room. */
    private boolean chatJoined = false;

    private final List<IChatListener> chatListeners = new ArrayList<IChatListener>();

    private MultiChatSession mucSession;

    /** Stores the last connection used so that we know if it changes. */
    private Connection currentConnection;

    public static class ChatLine {
        public String sender;

        public String text;

        public Date date;

        public String packedID;
    }

    protected ISessionListener sessionListener = new AbstractSessionListener() {
        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            sarosSessionRunning = false;
            checkChatState();
        }

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            sarosSessionRunning = true;
            // If the [possibly user specified] chat server is unreachable,
            // checkChatState() will block, causing a ~5s delay when using the
            // "Share project" command.
            // TODO Should we always run "join chat" in the background?
            Util.runSafeAsync("SarosJoinChat", log, new Runnable() {
                public void run() {
                    checkChatState();
                }
            });

        }
    };

    /**
     * The public interface of a chat session.
     */
    public interface SessionProvider {
        public List<ChatLine> getHistory();

        public String getName();

        /**
         * Sends a message and/ or a state to all other participants in the
         * multi user chat.
         * 
         * @param msg
         *            message to send to other chat participants; can be null
         * @param state
         *            current state send to other participants; can be null
         */
        public void sendMessage(String msg, ChatState state);
    }

    /**
     * Events for state changes of a user in a multi user chat.
     * 
     * @author mariaspg
     */
    public interface ChatStatusListener {

        /**
         * Fired when the state of a user changes.
         * 
         * @param sender
         *            the sender who changed the state.
         * @param state
         *            the new state of the sender.
         */
        void stateChanged(String sender, ChatState state);

    }

    public class MultiChatSession implements SessionProvider, PacketListener,
        MessageListener, ChatStatusListener {
        private final Logger logCH = Logger.getLogger(MultiChatSession.class
            .getName());

        private final String name;

        private final List<ChatLine> history = new ArrayList<ChatLine>();

        /** The chat room corresponding to the current session. */
        private MultiUserChatManager muc;

        private Set<User> chatUsers = new HashSet<User>();

        public MultiChatSession() {
            name = "Multi User Chat (" + saros.getMyJID().getName() + ")";
        }

        public String getName() {
            return name;
        }

        public List<ChatLine> getHistory() {
            return history;
        }

        public void processPacket(Packet packet) {
            logCH.debug("processPacket called");

            final Message message = (Message) packet;
            if (message.getBody() == null || message.getBody().equals("")) {
                return;
            }

            ISarosSession sarosSession = sessionManager.getSarosSession();
            JID jid = getJID(message.getFrom());
            User user = sarosSession.getUser(jid);

            if (user == null)
                return;

            chatMessageAdded(user, message.getBody());
        }

        public void processMessage(Chat chat, Message message) {
            logCH.debug("processMessage called.");
            processPacket(message);
        }

        /**
         * @see de.fu_berlin.inf.dpp.MessagingManager.SessionProvider
         */
        public void sendMessage(String text, ChatState state) {
            if (muc == null) {
                logCH.error("MUC does not exist");
                return;
            }

            try {
                /*
                 * Sends a message if there is one. If a message was composed
                 * then the ChatState is always active.
                 */
                chatStatusManager.setCurrentState(
                    (state == null) ? ChatState.active : state, muc);
                if (text != null) {
                    Message msg = muc.createMessage();
                    msg.setBody(text);
                    muc.sendMessage(msg);
                }
            } catch (Exception e1) {
                e1.printStackTrace();
                addChatLine("error",
                    "Couldn't send message (" + e1.getMessage() + ")");
            }
        }

        /**
         * Fired when the state of another user changes.
         * 
         * @see ChatStatusListener
         */
        public void stateChanged(String sender, ChatState state) {
            logCH.debug("stateChanged got fired with state: "
                + state.toString());

            ISarosSession sarosSession = sessionManager.getSarosSession();
            JID jid = getJID(sender);
            User user = sarosSession.getUser(jid);

            if (!chatUsers.contains(user) && state == ChatState.active) {
                chatUsers.add(user);
                /*
                 * Manually notified on local user in connectMultiUserChat.
                 */
                if (!user.equals(sarosSession.getLocalUser()))
                    notifyChatJoined(user);
            }

            if (chatUsers.contains(user) && state == ChatState.gone) {
                chatUsers.remove(user);
                /*
                 * TODO: Remove notification as soon as TODO in
                 * disconnectMultiUserChat is fixed
                 */
                notifyChatLeft(user);
            }

            chatStateUpdated(user, state);
        }

        private void addChatLine(String sender, String text) {
            ChatLine chatLine = new ChatLine();
            chatLine.sender = sender;
            chatLine.text = text;
            chatLine.date = new Date();

            this.history.add(chatLine);

            ISarosSession sarosSession = sessionManager.getSarosSession();
            JID jid = getJID(sender);
            User user = sarosSession.getUser(jid);

            for (IChatListener chatListener : MessagingManager.this.chatListeners) {
                chatListener.chatMessageAdded(user, text);
            }
        }

        /*
         * Gets User who sent the message.
         */
        private JID getJID(String sender) {
            return new JID(sender.substring(sender.indexOf('/') + 1,
                sender.length()));
        }

        /**
         * Creates and joins the chat room.
         * 
         * @param connection
         * @param user
         * @throws XMPPException
         */
        private void initMUC(Connection connection, String user)
            throws XMPPException {

            CommunicationPreferences comPrefs = getComPrefs();
            if (comPrefs == null)
                throw new IllegalStateException("No comPrefs found!");

            /* create room domain of current connection. */
            // JID(connection.getUser()).getDomain();
            String host = comPrefs.chatroom + "@" + comPrefs.chatserver;

            // Create a MultiUserChat using an XMPPConnection for a room
            MultiUserChatManager muc = new MultiUserChatManager(connection,
                host);

            // try to join to room
            boolean joined = false;
            try {
                muc.join(user, comPrefs.password);
                joined = true;
            } catch (XMPPException e) {
                log.debug(e);
            }

            boolean createdRoom = false;
            if (!joined) {
                try {
                    // Create the room
                    muc.create(user);
                    createdRoom = true;
                    // try to join to room
                    muc.join(user, comPrefs.password);
                } catch (XMPPException e) {
                    log.debug(e);
                }
            }

            if (createdRoom) {
                try {
                    // Get the the room's configuration form
                    Form form = muc.getConfigurationForm();

                    // Create a new form to submit based on the original form
                    Form submitForm = form.createAnswerForm();

                    // Add default answers to the form to submit
                    for (Iterator<FormField> fields = form.getFields(); fields
                        .hasNext();) {
                        FormField field = fields.next();
                        if (!FormField.TYPE_HIDDEN.equals(field.getType())
                            && (field.getVariable() != null)) {
                            // Sets the default value as the answer
                            submitForm.setDefaultAnswer(field.getVariable());
                        }
                    }

                    // set configuration, see XMPP Specs
                    submitForm.setAnswer("muc#roomconfig_moderatedroom", false);
                    submitForm.setAnswer("muc#roomconfig_publicroom", false);
                    submitForm.setAnswer(
                        "muc#roomconfig_passwordprotectedroom", true);
                    submitForm.setAnswer("muc#roomconfig_roomsecret",
                        comPrefs.password);
                    submitForm.setAnswer("muc#roomconfig_allowinvites", true);
                    submitForm
                        .setAnswer("muc#roomconfig_persistentroom", false);

                    // Send the completed form (with default values) to the
                    // server to configure the room
                    muc.sendConfigurationForm(submitForm);
                } catch (XMPPException e) {
                    log.debug(e);
                }
            }
            muc.addMessageListener(this);

            log.debug("MUC joined. Server: " + comPrefs.chatserver + " Room: "
                + comPrefs.chatroom + " Password " + comPrefs.password);

            this.muc = muc;
        }

        public void disconnect() {
            assert muc != null;
            if (muc.isJoined() && currentConnection.isConnected()) {
                muc.leave();
            }
            muc = null;
        }

        public void connect() throws XMPPException {
            XMPPConnection connection = saros.getConnection();
            String user = connection.getUser();
            initMUC(connection, user);
            // listen to changes of users ChatState
            chatStatusManager = ChatStatusManager.getInstance(connection, muc);
        }
    }

    public MessagingManager(Saros saros, SessionManager sessionManager) {
        log.setLevel(Level.DEBUG);
        saros.addListener(this);
        sessionManager.addSessionListener(sessionListener);
        this.sessionManager = sessionManager;
        this.saros = saros;
        if (saros.isConnected()) {
            initMultiChatListener();
        }
    }

    public void connectionStateChanged(XMPPConnection connection,
        ConnectionState newState) {
        checkChatState();

        if (newState == ConnectionState.ERROR && mucSession != null) {
            mucSession = null;
            log.debug("Session interrupted, will reconnect chat later.");
        }

        if (connection == null)
            return;

        if (newState == ConnectionState.CONNECTED) {
            initMultiChatListener();
        }
    }

    /**
     * Adds the chat listener.
     */
    public void addChatListener(IChatListener listener) {
        chatListeners.add(listener);
        checkChatState();
    }

    /**
     * Removes the chat listener.
     */
    public void removeChatListener(IChatListener listener) {
        chatListeners.remove(listener);
        checkChatState();
    }

    /**
     * Returns the current chat session.
     * 
     * @return The current chat session.
     */
    public SessionProvider getSession() {
        return this.mucSession;
    }

    /**
     * Notifies local listeners that a user has joined the session.
     * 
     * @param user
     */
    public void notifyChatJoined(User user) {
        for (IChatListener listener : chatListeners) {
            listener.chatJoined(user);
        }
    }

    /**
     * Notifies local listeners that a user has left the session.
     * 
     * @param user
     */
    public void notifyChatLeft(User user) {
        for (IChatListener listener : chatListeners) {
            listener.chatLeft(user);
        }
    }

    /**
     * Notifies all chat state listeners of the changed state.
     */
    private void chatStateUpdated(final User sender, final ChatState state) {
        log.debug("Notifying Listeners.");
        for (IChatListener l : chatListeners) {
            l.chatStateUpdated(sender, state);
            log.debug("Notified Listener.");
        }
    }

    /**
     * Notifies all chat listeners of a new message.
     */
    private void chatMessageAdded(final User sender, final String message) {
        log.debug("Notifying Listeners.");
        for (IChatListener l : chatListeners) {
            l.chatMessageAdded(sender, message);
            log.debug("Notified Listener.");
        }
    }

    // TODO CJ Rework needed
    public void invitationReceived(Connection conn, String room,
        String inviter, String reason, String password, Message message) {
        assert false : "When is this ever called? Please update the JavaDoc.";
        if (!chatJoined) {
            connectMultiUserChat();
        }
    }

    /**
     * Invitation listener for multi chat invitations.<br>
     * TODO This doesn't seem to work at all.
     */
    protected void initMultiChatListener() {
        // listens for MUC invitations
        MultiUserChat.addInvitationListener(saros.getConnection(), this);
    }

    protected String getRoomName() {
        CommunicationPreferences comPrefs = getComPrefs();
        return comPrefs.chatroom;
    }

    protected String getRoomPassword() {
        CommunicationPreferences comPrefs = getComPrefs();
        return comPrefs.password;
    }

    protected CommunicationPreferences getComPrefs() {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        if (sarosSession == null)
            return null;
        if (sarosSession.isHost()) {
            return comNegotiatingManager.getOwnPrefs();
        } else {
            return comNegotiatingManager.getSessionPrefs();
        }
    }

    /**
     * This method calls connectMultiUserChat() or disconnectMultiUserChat(),
     * but only if that's possible and necessary.
     */
    private void checkChatState() {
        if (chatJoined) {
            if (!sarosSessionRunning || chatListeners.isEmpty()
                || currentConnection == null
                || !currentConnection.isConnected()) {
                disconnectMultiUserChat();
            }
        } else {
            if (sarosSessionRunning && !chatListeners.isEmpty()
                && saros.isConnected() && getComPrefs() != null) {
                connectMultiUserChat();
            }
        }
    }

    /**
     * Ends the current chat session. Sends a leave message to the room if
     * that's still possible.
     * 
     * TODO: Saros session end should wait for the termination of the chat room
     * to really all participants get notified of a left chat room.
     */
    private void disconnectMultiUserChat() {
        log.debug("Leaving chat.");
        assert mucSession != null;

        ISarosSession sarosSession = sessionManager.getSarosSession();
        notifyChatLeft(sarosSession.getLocalUser());

        mucSession.sendMessage(null, ChatState.gone);
        mucSession.disconnect();
        chatJoined = false;
    }

    /**
     * Joins a chat session.
     */
    private void connectMultiUserChat() {
        if (!saros.isConnected()) {
            log.error("Can't join chat: Not connected.");
            return;
        }
        log.debug("Joining chat.");
        XMPPConnection connection = saros.getConnection();
        if (mucSession == null || !connection.equals(currentConnection)) {
            log.debug("Creating MUC session.");
            mucSession = new MultiChatSession();
            currentConnection = connection;
            initMultiChatListener();
        }
        try {
            mucSession.connect();
        } catch (XMPPException e) {
            log.error("Couldn't join chat.", e);
            return;
        }
        chatJoined = true;

        ISarosSession sarosSession = sessionManager.getSarosSession();
        notifyChatJoined(sarosSession.getLocalUser());
        mucSession.sendMessage(null, ChatState.active);
    }
}
