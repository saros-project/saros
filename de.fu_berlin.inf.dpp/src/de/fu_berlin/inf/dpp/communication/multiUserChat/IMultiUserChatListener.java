package de.fu_berlin.inf.dpp.communication.multiUserChat;

import org.jivesoftware.smackx.ChatState;

import de.fu_berlin.inf.dpp.User;

/**
 * Listener for incoming chat messages.
 */
public interface IMultiUserChatListener {

    public void chatJoined(User joinedUser);

    public void chatLeft(User leftUser);

    public void chatMessageReceived(User sender, String message);

    public void chatStateUpdated(User sender, ChatState state);

}