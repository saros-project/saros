package de.fu_berlin.inf.dpp.observables;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.util.ObservableValue;

/**
 * This observable contains the ISarosSession that is currently open or null if
 * no session is open.
 * 
 * The observable value is set to a new session before ISarosSession.start() is
 * called and before the ISessionListeners are notified that the session has
 * started.
 * 
 * The observable value is set to null after ISarosSession.stop() is called but
 * before the ISessionListeners are notified that the session has ended.
 */
@Component(module = "observables")
public class SarosSessionObservable extends ObservableValue<ISarosSession> {

    public SarosSessionObservable() {
        super(null);
    }

}
