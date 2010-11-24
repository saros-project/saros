package de.fu_berlin.inf.dpp.communication.muc.events;

import de.fu_berlin.inf.dpp.communication.muc.session.MUCSession;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * Listener for {@link MUCSession} events.
 */
public interface IMUCManagerListener {

    /**
     * Gets called whenever a {@link MUCSession} was created.
     * 
     * @param mucSession
     *            {@link JID} who has joined the session
     */
    public void mucSessionCreated(MUCSession mucSession);

    /**
     * Gets called whenever a {@link MUCSession} was joined.
     * 
     * @param mucSession
     *            {@link JID} who has joined the session
     */
    public void mucSessionJoined(MUCSession mucSession);

    /**
     * Gets called whenever a {@link MUCSession} was left.
     * 
     * @param mucSession
     *            {@link JID} who has joined the session
     */
    public void mucSessionLeft(MUCSession mucSession);

    /**
     * Gets called whenever a {@link MUCSession} was destroyed.
     * 
     * @param mucSession
     *            {@link JID} who has left the session
     */
    public void mucSessionDestroyed(MUCSession mucSession);

}