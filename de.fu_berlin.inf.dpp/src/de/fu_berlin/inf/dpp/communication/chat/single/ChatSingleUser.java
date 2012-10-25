package de.fu_berlin.inf.dpp.communication.chat.single;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.ChatState;
import org.jivesoftware.smackx.ChatStateListener;
import org.jivesoftware.smackx.ChatStateManager;

import de.fu_berlin.inf.dpp.communication.chat.AbstractChat;
import de.fu_berlin.inf.dpp.communication.chat.ChatElement;
import de.fu_berlin.inf.dpp.communication.chat.ChatHistory;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * This object represents a chat with a single user.
 */
public class ChatSingleUser extends AbstractChat {

    private static final Logger LOG = Logger.getLogger(ChatSingleUser.class);

    private final ChatStateListener chatStateListener = new ChatStateListener() {

        @Override
        public void processMessage(Chat chat, Message message) {
            LOG.trace(this + " : received message from: " + message.getFrom()
                + " : " + message.getBody());

            if (message.getFrom() == null || message.getBody() == null)
                return;

            addHistoryEntry(new ChatElement(message, new Date(
                System.currentTimeMillis())));

            notifyJIDMessageReceived(new JID(message.getFrom()),
                message.getBody());
        }

        @Override
        public void stateChanged(Chat chat, ChatState state) {
            notifyJIDStateChanged(new JID(chat.getParticipant()), state);
        }

    };

    private ChatStateManager chatStateManager;
    private Chat chat;

    private String userJID;

    private boolean isConnected;

    /**
     * Initializes the chat so that it is possible to exchange messages with the
     * participant.
     * 
     * @param userJID
     *            {@link JID} of the local user
     * @param chat
     *            {@link Chat} object from Smack, contains the recipient
     * @param chatStateManager
     *            {@link ChatStateManager} of the current connection
     */
    synchronized void initChat(String userJID, Chat chat,
        ChatStateManager chatStateManager) {
        if (this.chat != null)
            this.chat.removeMessageListener(chatStateListener);

        this.chat = chat;
        this.chat.addMessageListener(chatStateListener);
        this.chatStateManager = chatStateManager;
        this.userJID = userJID;
    }

    /**
     * Returns the chat's {@link MessageListener}.
     * 
     * @return the chat's {@link MessageListener}
     */
    synchronized MessageListener getMessagerListener() {
        return chatStateListener;
    }

    /**
     * Connect/disconnect the chat which notifies the listeners what eventually
     * leads to new {@link ChatElement}s in its {@link ChatHistory} representing
     * this event.
     * 
     * @param isConnected
     */
    void setConnected(boolean isConnected) {
        LOG.trace("new connection state, connected=" + isConnected);
        this.isConnected = isConnected;

        if (isConnected) {
            notifyJIDConnected(new JID(chat.getParticipant()));
        } else {
            notifyJIDDisconnected(new JID(chat.getParticipant()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized JID getJID() {
        return new JID(userJID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Set<JID> getParticipants() {
        /* TODO: get all participants for MUC or SingleChat */
        return Collections.singleton(new JID(chat.getParticipant()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String getThreadID() {
        return chat.getThreadID();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(Message message) throws XMPPException {
        Chat currentChat;

        synchronized (ChatSingleUser.this) {
            currentChat = chat;

            chat.sendMessage(message);
            message.setFrom(userJID);
        }

        chatStateListener.processMessage(currentChat, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(String text) throws XMPPException {
        Message message = new Message(chat.getParticipant(), Message.Type.chat);
        message.setThread(chat.getThreadID());
        message.setBody(text);
        sendMessage(message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setCurrentState(ChatState newState)
        throws XMPPException {
        chatStateManager.setCurrentState(newState, chat);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return chat.getParticipant();
    }

    /**
     * This method does nothing as {@link ChatSingleUser}s are stateless despite
     * the global connection status.
     * 
     * @return <code>true</code>
     */
    @Override
    public boolean connect() {
        return true;
    }

    /**
     * This method does nothing as {@link ChatSingleUser}s are stateless despite
     * the global connection status.
     * 
     * @return <code>true</code>
     */
    @Override
    public boolean disconnect() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean isConnected() {
        return isConnected;
    }

}
