package de.fu_berlin.inf.dpp.observables;

import de.fu_berlin.inf.dpp.util.ObservableValue;

public class SessionIDObservable extends ObservableValue<String> {

    public final static String NOT_IN_SESSION = "NOT_IN_SESSION";

    public SessionIDObservable() {
        super(NOT_IN_SESSION);
    }

    public boolean isInASession() {
        return NOT_IN_SESSION.equals(getValue());
    }

}
