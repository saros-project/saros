package de.fu_berlin.inf.dpp.communication.muc.events;

import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.communication.muc.session.MUCSession;

/**
 * Listener for {@link MUCSession} events.
 */
public abstract class MUCManagerAdapter implements IMUCManagerListener {

    @Override
    public void mucSessionCreated(MUCSession mucSession) {
        // NOP
    }

    @Override
    public void mucSessionJoined(MUCSession mucSession) {
        // NOP
    }

    public void mucSessionLeft(MUCSession mucSession) {
        // NOP
    }

    @Override
    public void mucSessionDestroyed(MUCSession mucSession) {
        // NOP
    }

    @Override
    public void mucSessionConnectionError(MUCSession mucSession,
        XMPPException exception) {
        // NOP
    }
}