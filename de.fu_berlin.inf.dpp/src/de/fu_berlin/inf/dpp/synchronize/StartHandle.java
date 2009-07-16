package de.fu_berlin.inf.dpp.synchronize;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.User;

public class StartHandle {

    private static Logger log = Logger.getLogger(StartHandle.class.getName());

    protected User user;
    protected StopManager stopManager;
    protected boolean startCalled = false;
    protected String id;

    public StartHandle(User user, StopManager stopManager, String id) {
        this.user = user;
        this.stopManager = stopManager;
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

        log.debug("Called start on " + user);

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StartHandle other = (StartHandle) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (user == null) {
            if (other.user != null)
                return false;
        } else if (!user.equals(other.user))
            return false;
        return true;
    }

    @Override
    public String toString() {
        String out = "StartHandle (" + user + ", " + id + ", ";
        if (startCalled)
            out += "not ";
        return out + "startable)";
    }
}
