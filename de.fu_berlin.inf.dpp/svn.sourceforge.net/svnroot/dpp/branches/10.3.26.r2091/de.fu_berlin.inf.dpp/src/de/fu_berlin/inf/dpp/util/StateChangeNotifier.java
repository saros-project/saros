/*
 * Created on 17.01.2005
 *
 */
package de.fu_berlin.inf.dpp.util;

import java.util.HashSet;

public class StateChangeNotifier<T> extends HashSet<StateChangeListener<T>> {

    private static final long serialVersionUID = 1398284794530268904L;

    public void notify(T sender) {
        for (StateChangeListener<T> cl : this) {
            cl.stateChangedNotification(sender);
        }
    }

    public void addAndNotify(StateChangeListener<T> listener, T sender) {
        add(listener);
        listener.stateChangedNotification(sender);
    }

    /**
     * Forward all notification of the sender to this notifier.
     * 
     * @param sender
     */
    public void chain(StateChangeNotifier<T> sender) {
        sender.add(new StateChangeListener<T>() {
            public void stateChangedNotification(T t) {
                StateChangeNotifier.this.notify(t);
            }
        });
    }

    /**
     * Forward all notification of the sender to this notifier.
     * 
     * @param <S>
     * @param sender
     * @param toSender
     */
    public <S> void chain(StateChangeNotifier<S> sender, final T toSender) {
        sender.add(new StateChangeListener<S>() {
            public void stateChangedNotification(S s) {
                StateChangeNotifier.this.notify(toSender);
            }
        });
    }
}
