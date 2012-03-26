package de.fu_berlin.inf.dpp.util;

/**
 * The notifier complements the Listener construct.
 * 
 * 
 */
public interface StateChangeListener<T> {

    public void stateChangedNotification(T t);

}
