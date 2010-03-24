package de.fu_berlin.inf.dpp.observables;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.internal.SharedProject;
import de.fu_berlin.inf.dpp.util.ObservableValue;

/**
 * This observable contains the SharedProject that is currently open or null if
 * no project is open.
 * 
 * The observable is set to a new project, before ISharedProject.start() has
 * been called and before the ISessionListeners are notified that the Session
 * has started.
 * 
 * The proxy is set to null, after ISharedProject.stop() has been called but
 * before the ISessionListeners are notified that the Session is has ended.
 * 
 */
@Component(module = "observables")
public class SharedProjectObservable extends ObservableValue<SharedProject> {

    public SharedProjectObservable() {
        super(null);
    }

}
