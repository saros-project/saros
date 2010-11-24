package de.fu_berlin.inf.dpp.communication.muc.events;

import de.fu_berlin.inf.dpp.communication.muc.session.MUCSession;

/**
 * Listener for {@link MUCSession} events.
 */
public class MUCManagerAdapter implements IMUCManagerListener {

    public void mucSessionCreated(MUCSession mucSession) {
        // do nothing
    }

    public void mucSessionJoined(MUCSession mucSession) {
        // do nothing
    }

    public void mucSessionLeft(MUCSession mucSession) {
        // do nothing
    }

    public void mucSessionDestroyed(MUCSession mucSession) {
        // do nothing
    }

}