package de.fu_berlin.inf.dpp.communication.multiUserChat;

import org.jivesoftware.smackx.ChatState;

import de.fu_berlin.inf.dpp.User;

/**
 * Listener for incoming chat messages.
 */
public interface IMultiUserChatListener {

    /**
     * Gets called whenever a {@link User} has joined the session.
     * 
     * @param joinedUser
     *            {@link User} who has joined the session
     */
    public void userJoined(User joinedUser);

    /**
     * Gets called whenever a {@link User} has left the session.
     * 
     * @param leftUser
     *            {@link User} who has left the session
     */
    public void userLeft(User leftUser);

    /**
     * Gets called whenever a message has been received
     * 
     * @param sender
     *            {@link User} who has sent the message
     * @param message
     *            the received message
     */
    public void messageReceived(User sender, String message);

    /**
     * Gets called whenever a {@link User}'s {@link ChatState} has been changed
     * 
     * @param user
     *            who's {@link ChatState} has been changed
     * @param state
     *            new {@link ChatState}
     */
    public void stateChanged(User user, ChatState state);

}