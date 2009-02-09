package de.fu_berlin.inf.dpp.project;

import de.fu_berlin.inf.dpp.net.JID;

/**
 * Abstract {@link ISharedProjectListener} that does nothing in all the methods.
 * 
 * Clients can override just the methods they want to act upon.
 */
public abstract class AbstractSharedProjectListener implements
    ISharedProjectListener {

    public void driverChanged(JID driver, boolean replicated) {
        // Do nothing.
    }

    public void userJoined(JID user) {
        // Do nothing.
    }

    public void userLeft(JID user) {
        // Do nothing.
    }
}
