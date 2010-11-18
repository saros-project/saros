package de.fu_berlin.inf.dpp.communication.multiUserChat;

import java.util.List;

import org.jivesoftware.smackx.ChatState;

/**
 * The public interface of a chat session.
 */
public interface IMultiUserChatSessionProvider {
    public List<ChatLine> getHistory();

    /**
     * Sends a message and/ or a state to all other participants in the multi
     * user chat.
     * 
     * @param msg
     *            message to send to other chat participants; can be null
     * @param state
     *            current state send to other participants; can be null
     */
    public void sendMessage(String msg, ChatState state);
}