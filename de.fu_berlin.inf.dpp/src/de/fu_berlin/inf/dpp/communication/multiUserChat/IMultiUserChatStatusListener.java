package de.fu_berlin.inf.dpp.communication.multiUserChat;

import org.jivesoftware.smackx.ChatState;

/**
 * Events for state changes of a user in a multi user chat.
 */
public interface IMultiUserChatStatusListener {

    /**
     * Fires when the state of a user changes.
     * 
     * @param sender
     *            the sender who changed the state.
     * @param state
     *            the new state of the sender.
     */
    void stateChanged(String sender, ChatState state);

}