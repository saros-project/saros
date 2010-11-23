package de.fu_berlin.inf.dpp.communication.muc.session.events;

import de.fu_berlin.inf.dpp.communication.muc.session.MUCSession;
import de.fu_berlin.inf.dpp.communication.muc.session.states.IMUCStateListener;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * Listener for {@link MUCSession} events.
 */
public interface IMUCSessionListener extends IMUCStateListener {

    /**
     * Gets called whenever a {@link JID} has joined the session.
     * 
     * @param jid
     *            {@link JID} who has joined the session
     */
    public void joined(JID jid);

    /**
     * Gets called whenever a {@link JID} has left the session.
     * 
     * @param jid
     *            {@link JID} who has left the session
     */
    public void left(JID jid);

    /**
     * Gets called whenever a message has been received
     * 
     * @param sender
     *            {@link JID} who has sent the message
     * @param message
     *            the received message
     */
    public void messageReceived(JID sender, String message);

}