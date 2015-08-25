package de.fu_berlin.inf.dpp.session.internal;

import java.util.concurrent.CopyOnWriteArrayList;

import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;

// TODO rename this class along with the ISharedProjectListener interface
public class SharedProjectListenerDispatch implements ISharedProjectListener {

    private final CopyOnWriteArrayList<ISharedProjectListener> listeners = new CopyOnWriteArrayList<ISharedProjectListener>();

    @Override
    public void permissionChanged(User user) {
        for (ISharedProjectListener listener : listeners)
            listener.permissionChanged(user);
    }

    @Override
    public void userJoined(User user) {
        for (ISharedProjectListener listener : listeners)
            listener.userJoined(user);
    }

    @Override
    public void userStartedQueuing(User user) {
        for (ISharedProjectListener listener : listeners)
            listener.userStartedQueuing(user);
    }

    @Override
    public void userFinishedProjectNegotiation(User user) {
        for (ISharedProjectListener listener : listeners)
            listener.userFinishedProjectNegotiation(user);
    }

    @Override
    public void userLeft(User user) {
        for (ISharedProjectListener listener : listeners)
            listener.userLeft(user);
    }

    public void add(ISharedProjectListener listener) {
        listeners.addIfAbsent(listener);
    }

    public void remove(ISharedProjectListener listener) {
        listeners.remove(listener);
    }
}
