package de.fu_berlin.inf.dpp.observables;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.internal.StreamSession;
import de.fu_berlin.inf.dpp.util.ObservableValue;

/**
 * This ObservableValue contains the current session of a VoIPSession or null if
 * no session exists.
 * 
 */
@Component(module = "observables")
public class VoIPSessionObservable extends ObservableValue<StreamSession> {

    public VoIPSessionObservable() {
        super(null);
    }

}
