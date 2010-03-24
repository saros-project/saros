package de.fu_berlin.inf.dpp.synchronize;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.util.StackTrace;

public class StartHandle {

    private static Logger log = Logger.getLogger(StartHandle.class.getName());

    protected StopManager stopManager;

    protected User user;

    protected String id;

    /**
     * Each start handle may be only started once. This boolean guards this.
     */
    protected AtomicBoolean startCalled = new AtomicBoolean(false);

    /**
     * Each start handle may be acknowledged once to have been started.
     */
    protected AtomicBoolean acknowledged = new AtomicBoolean(false);

    public StartHandle(User user, StopManager stopManager, String id) {
        this.user = user;
        this.stopManager = stopManager;
        this.id = id;
    }

    /**
     * Notifies the StopManager, that the operation for which this IStartHandle
     * was returned by a call to stop has finished.
     * 
     * This method will return immediately and return true, if the stopped user
     * continued working or false if other IStartHandles exist for this user,
     * which have not been called.
     * 
     * @nonblocking
     * 
     * @Throws IllegalStateException if start() is called twice on the same
     *         handle.
     */
    public boolean start() {

        log.debug("Called start on " + user);

        if (!startCalled.compareAndSet(false, true))
            throw new IllegalStateException(
                "start can only be called once per StartHandle");

        stopManager.removeStartHandle(this);

        stopManager.initiateUnlock(this);

        return stopManager.getStartHandles(user).isEmpty();
    }

    /**
     * Notifies the StopManager, that the operation for which this IStartHandle
     * was returned by a call to stop has finished.
     * 
     * This method returns true, if the stopped user continued working or false
     * if other IStartHandles exist for this user, which have not been called.
     * 
     * @blocking waits until the blocked user acknowledged the start
     * 
     * @Throws IllegalStateException if start() is called twice on the same
     *         handle.
     * @Throws CancellationException
     */
    public boolean startAndAwait(final SubMonitor progress) {

        log.debug("Called startAndAwait on " + user);

        if (!startCalled.compareAndSet(false, true))
            throw new IllegalStateException(
                "start can only be called once per StartHandle");

        stopManager.removeStartHandle(this);

        stopManager.initiateUnlock(this);

        if (stopManager.getStartHandles(user).isEmpty()) {
            return await(progress);
        }

        return false;
    }

    /**
     * Waits until the StartHandle is acknowledged or the waiting is canceled
     * using the given progress monitor.
     * 
     * Returns whether the handle was acknowledged or not
     */
    public boolean await(final SubMonitor progress) {
        try {
            while (!acknowledged.get() && !progress.isCanceled())
                Thread.sleep(stopManager.MILLISTOWAIT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Code not designed to be interruptable", e);
        }
        if (progress.isCanceled())
            throw new CancellationException();

        return acknowledged.get();
    }

    public User getUser() {
        return user;
    }

    public String getHandleID() {
        return id;
    }

    public void acknowledge() {
        if (!acknowledged.compareAndSet(false, true)) {
            log.warn("Acknowledge should only be called once per handle",
                new StackTrace());
        }
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
        if (startCalled.get())
            out += "not ";
        return out + "startable)";
    }
}
