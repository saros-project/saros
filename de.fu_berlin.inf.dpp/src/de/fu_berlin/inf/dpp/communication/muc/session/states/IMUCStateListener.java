package de.fu_berlin.inf.dpp.communication.muc.session.states;

import org.jivesoftware.smackx.ChatState;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * Listener for {@link ChatState} events.
 */
public interface IMUCStateListener {

    /**
     * Gets called whenever a {@link JID}'s {@link ChatState} has been changed
     * 
     * @param jid
     *            who's {@link ChatState} has been changed
     * @param state
     *            new {@link ChatState}
     */
    public void stateChanged(JID jid, ChatState state);

}