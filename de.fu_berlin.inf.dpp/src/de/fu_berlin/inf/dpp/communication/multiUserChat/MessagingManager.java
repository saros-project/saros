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
package de.fu_berlin.inf.dpp.communication.multiUserChat;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.ChatState;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
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
 * Add a {@link IMultiUserChatListener} to get notified of chat events.
 * 
 * @author rdjemili
 * @author ahaferburg
 * @author mariaspg
 */
@Component(module = "net")
public class MessagingManager implements IConnectionListener,
    InvitationListener {

    private static final Logger log = Logger.getLogger(MessagingManager.class);

    protected Saros saros;

    protected SessionManager sessionManager;

    @Inject
    protected CommunicationNegotiatingManager comNegotiatingManager;

    /** True iff we're in a Saros session. */
    private boolean sarosSessionRunning = false;

    /** True iff we're currently in the chat room. */
    private boolean chatJoined = false;

    public final List<IMultiUserChatListener> chatListeners = new ArrayList<IMultiUserChatListener>();

    private MultiUserChatSession mucSession;

    /** Stores the last connection used so that we know if it changes. */
    private Connection currentConnection;

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
    public void addChatListener(IMultiUserChatListener listener) {
        chatListeners.add(listener);
        checkChatState();
    }

    /**
     * Removes the chat listener.
     */
    public void removeChatListener(IMultiUserChatListener listener) {
        chatListeners.remove(listener);
        checkChatState();
    }

    /**
     * Returns the current chat session.
     * 
     * @return The current chat session.
     */
    public IMultiUserChatSessionProvider getSession() {
        return this.mucSession;
    }

    /**
     * Notifies local listeners that a user has joined the session. TODO Event
     * Listeners need to be registered on the MultiUserChat and not on the
     * MessagingManager
     * 
     * @param user
     */
    public void notifyChatJoined(User user) {
        for (IMultiUserChatListener listener : chatListeners) {
            listener.userJoined(user);
        }
    }

    /**
     * Notifies local listeners that a user has left the session. TODO Event
     * Listeners need to be registered on the MultiUserChat and not on the
     * MessagingManager
     * 
     * @param user
     */
    public void notifyChatLeft(User user) {
        for (IMultiUserChatListener listener : chatListeners) {
            listener.userLeft(user);
        }
    }

    /**
     * Notifies all chat state listeners of the changed state. TODO Event
     * Listeners need to be registered on the MultiUserChat and not on the
     * MessagingManager
     */
    public void chatStateUpdated(final User sender, final ChatState state) {
        log.debug("Notifying Listeners.");
        for (IMultiUserChatListener l : chatListeners) {
            l.stateChanged(sender, state);
            log.debug("Notified Listener.");
        }
    }

    /**
     * Notifies all chat listeners of a new message. TODO Event Listeners need
     * to be registered on the MultiUserChat and not on the MessagingManager
     */
    public void chatMessageAdded(final User sender, final String message) {
        log.debug("Notifying Listeners.");
        for (IMultiUserChatListener l : chatListeners) {
            l.messageReceived(sender, message);
            log.debug("Notified Listener.");
        }
    }

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
        CommunicationPreferences comPrefs = getCommunicationPreferences();
        return comPrefs.chatroom;
    }

    protected String getRoomPassword() {
        CommunicationPreferences comPrefs = getCommunicationPreferences();
        return comPrefs.password;
    }

    protected CommunicationPreferences getCommunicationPreferences() {
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
                && saros.isConnected() && getCommunicationPreferences() != null) {
                connectMultiUserChat();
            }
        }
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
            mucSession = new MultiUserChatSession(this.saros,
                this.sessionManager.getSarosSession(),
                getCommunicationPreferences(), this);
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
}
