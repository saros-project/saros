package de.fu_berlin.inf.dpp.project.internal;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.util.StackTrace;

public class SharedProjectListenerDispatch implements ISharedProjectListener {

    private static final Logger log = Logger
        .getLogger(SharedProjectListenerDispatch.class);

    protected List<ISharedProjectListener> listeners = new CopyOnWriteArrayList<ISharedProjectListener>();

    /**
     * Currently executed method
     */
    protected String concurrentModificationChecker;

    protected synchronized void enter(String methodName) {
        if (concurrentModificationChecker != null)
            log.warn("Threading violation: About to enter " + methodName
                + ", but still in " + concurrentModificationChecker,
                new StackTrace());
        else
            concurrentModificationChecker = methodName;
    }

    protected synchronized void leave(String methodName) {
        if (!concurrentModificationChecker.equals(methodName))
            log.warn("Threading violation: About to leave " + methodName
                + ", but other method currently being processed "
                + concurrentModificationChecker, new StackTrace());
        else
            concurrentModificationChecker = null;
    }

    public void invitationCompleted(User user) {
        enter("invitationCompleted");
        for (ISharedProjectListener listener : this.listeners) {
            listener.invitationCompleted(user);
        }
        leave("invitationCompleted");
    }

    public void roleChanged(User user) {
        enter("roleChanged");
        for (ISharedProjectListener listener : this.listeners) {
            listener.roleChanged(user);
        }
        leave("roleChanged");
    }

    public void userJoined(User user) {
        enter("userJoined");
        for (ISharedProjectListener listener : this.listeners) {
            listener.userJoined(user);
        }
        leave("userJoined");
    }

    public void userLeft(User user) {
        enter("userLeft");
        for (ISharedProjectListener listener : this.listeners) {
            listener.userLeft(user);
        }
        leave("userLeft");
    }

    public void add(ISharedProjectListener listener) {
        enter("add");
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
        leave("add");
    }

    public void remove(ISharedProjectListener listener) {
        enter("remove");
        this.listeners.remove(listener);
        leave("remove");
    }

}
