package de.fu_berlin.inf.dpp.synchronize;

import de.fu_berlin.inf.dpp.User;

public class StartHandle {

    protected User user;
    protected StopManager stopManager;
    protected boolean startCalled;
    protected String id;

    public StartHandle(User user, StopManager stopManager, String id) {
        this.user = user;
        this.stopManager = stopManager;
        this.startCalled = false;
        this.id = id;
    }

    /**
     * Notifies the StopManager, that the operation for which this IStartHandle
     * was returned by a call to stop has finished.
     * 
     * This method will return immediately and return true, if the user stopped
     * by the call to stop which returned this handle continued working or false
     * if other IStartHandles exist for this user, which have not been called.
     * 
     * @Throws IllegalStateException if start() is called twice on the same
     *         handle.
     */
    public boolean start() {

        if (startCalled)
            throw new IllegalStateException(
                "start can only be called once per StartHandle");
        startCalled = true;

        stopManager.removeStartHandle(this);

        if (stopManager.noStartHandlesFor(user)) {
            stopManager.initiateUnlock(this);
            return true;
        }
        return false;
    }

    public User getUser() {
        return user;
    }

    public String getHandleID() {
        return id;
    }
}
