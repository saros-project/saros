package de.fu_berlin.inf.dpp.observables;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.internal.JingleTransport;
import de.fu_berlin.inf.dpp.util.ObservableValue;

/**
 * This ObservableValue contains the current JingleTransport2 or null if no
 * manager exists.
 * 
 */
@Component(module = "observables")
public class JingleFileTransferManagerObservable extends
    ObservableValue<JingleTransport> {

    public JingleFileTransferManagerObservable() {
        super(null);
    }

}
