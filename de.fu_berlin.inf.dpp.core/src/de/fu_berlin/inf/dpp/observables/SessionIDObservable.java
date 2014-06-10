package de.fu_berlin.inf.dpp.observables;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.session.ISarosSession;

/**
 * Observable containing the ID of the SarosSession in which the local user is
 * currently participating or is currently joining (during an invitation).
 * 
 * If not in an invitation or shared project session the value of this
 * Observable equals {@link SessionIDObservable#NOT_IN_SESSION}.
 * 
 * If in a shared project session the value of this Observable is the string
 * representation of a random integer.
 * 
 * @deprecated The common usage in Saros is to use this observable as a global
 *             variable to perform global state programming which was not
 *             intended. Use {@link ISarosSession#getID()} instead which will be
 *             almost equivalent in most cases.
 */
@Deprecated
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
