package de.fu_berlin.inf.dpp.observables;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.jingle.JingleFileTransferManager;
import de.fu_berlin.inf.dpp.util.ObservableValue;

/**
 * This ObservableValue contains the current JingleFileTransferManager or null
 * if no manager exists.
 * 
 * @component The single instance of this class per application is created by
 *            PicoContainer in the central plug-in class {@link Saros}
 */
public class JingleFileTransferManagerObservable extends
    ObservableValue<JingleFileTransferManager> {

    public JingleFileTransferManagerObservable() {
        super(null);
    }

}
