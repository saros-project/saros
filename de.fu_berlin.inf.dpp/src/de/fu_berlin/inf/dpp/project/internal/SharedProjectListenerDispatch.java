package de.fu_berlin.inf.dpp.project.internal;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;

public class SharedProjectListenerDispatch implements ISharedProjectListener {
    protected List<ISharedProjectListener> listeners = new CopyOnWriteArrayList<ISharedProjectListener>();

    public void invitationCompleted(User user) {
        for (ISharedProjectListener listener : this.listeners) {
            listener.invitationCompleted(user);
        }
    }

    public void permissionChanged(User user) {
        for (ISharedProjectListener listener : this.listeners) {
            listener.permissionChanged(user);
        }
    }

    public void userJoined(User user) {
        for (ISharedProjectListener listener : this.listeners) {
            listener.userJoined(user);
        }
    }

    public void userLeft(User user) {
        for (ISharedProjectListener listener : this.listeners) {
            listener.userLeft(user);
        }
    }

    public void add(ISharedProjectListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public void remove(ISharedProjectListener listener) {
        this.listeners.remove(listener);
    }

}
