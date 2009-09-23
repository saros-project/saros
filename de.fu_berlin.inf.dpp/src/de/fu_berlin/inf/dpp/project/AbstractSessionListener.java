package de.fu_berlin.inf.dpp.project;

import de.fu_berlin.inf.dpp.invitation.IncomingInvitationProcess;

/**
 * Abstract ISessionListener that does nothing in all the methods.
 * 
 * Clients can override just the methods they want to act upon.
 */
public abstract class AbstractSessionListener implements ISessionListener {

    public void invitationReceived(IncomingInvitationProcess invitation) {
        // do nothing
    }

    public void sessionEnded(ISharedProject session) {
        // do nothing
    }

    public void sessionStarted(ISharedProject session) {
        // do nothing
    }
}
