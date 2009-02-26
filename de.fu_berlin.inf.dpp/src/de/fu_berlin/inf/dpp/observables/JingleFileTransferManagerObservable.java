package de.fu_berlin.inf.dpp.observables;

import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferManager;
import de.fu_berlin.inf.dpp.util.ObservableValue;

/**
 * This ObservableValue contains the current JingleFileTransferManager or null
 * if no manager exists.
 */
public class JingleFileTransferManagerObservable extends
    ObservableValue<JingleFileTransferManager> {

    public JingleFileTransferManagerObservable() {
        super(null);
    }

}
