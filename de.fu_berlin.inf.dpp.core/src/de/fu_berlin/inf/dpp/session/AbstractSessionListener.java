package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.filesystem.IProject;

/**
 * Abstract {@link ISessionListener} that does nothing by default. Clients can
 * extend this class to override only those event methods that they want to act
 * upon.
 */
public abstract class AbstractSessionListener implements ISessionListener {

    @Override
    public void permissionChanged(User user) {
        // Do nothing.
    }

    @Override
    public void userJoined(User user) {
        // Do nothing.
    }

    @Override
    public void userStartedQueuing(User user) {
        // Do nothing.
    }

    @Override
    public void userFinishedProjectNegotiation(User user) {
        // Do nothing
    }

    @Override
    public void userColorChanged(User user) {
        // Do nothing.
    }

    @Override
    public void userLeft(User user) {
        // Do nothing.
    }

    @Override
    public void projectAdded(IProject project) {
        // Do nothing.
    }

    @Override
    public void projectRemoved(IProject project) {
        // Do nothing.
    }

    @Override
    public void resourcesAdded(IProject project) {
        // Do nothing.
    }

}
