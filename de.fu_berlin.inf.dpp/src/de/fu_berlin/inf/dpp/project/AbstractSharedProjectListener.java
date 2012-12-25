package de.fu_berlin.inf.dpp.project;

import de.fu_berlin.inf.dpp.User;

/**
 * Abstract {@link ISharedProjectListener} that does nothing in all the methods.
 * 
 * Clients can override just the methods they want to act upon.
 */
public abstract class AbstractSharedProjectListener implements
    ISharedProjectListener {

    @Override
    public void permissionChanged(User user) {
        // Do nothing.
    }

    @Override
    public void userJoined(User user) {
        // Do nothing.
    }

    @Override
    public void userLeft(User user) {
        // Do nothing.
    }

    @Override
    public void invitationCompleted(User user) {
        // Do nothing.
    }
}
