package de.fu_berlin.inf.dpp.observables;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.util.ObservableValue;

/**
 * Observable containing the ID of the SarosSession in which the local user is
 * currently participating or is currently joining (during an invitation).
 * 
 * If not in an invitation or shared project session the value of this
 * Observable equals {@link SessionIDObservable#NOT_IN_SESSION}.
 * 
 * If in an shared project session the value of this Observable is the string
 * representation of a random integer.
 */
@Component(module = "observables")
public class SessionIDObservable extends ObservableValue<String> {

    public final static String NOT_IN_SESSION = "NOT_IN_SESSION";

    public SessionIDObservable() {
        super(NOT_IN_SESSION);
    }

    public boolean isInASession() {
        return NOT_IN_SESSION.equals(getValue());
    }
}
