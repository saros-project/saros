package de.fu_berlin.inf.dpp.concurrent.watchdog;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.util.ObservableValue;

/**
 * This observable contains whether the ConsistencyWatchdogClient has detected
 * that there are files which are inconsistent with regards to the checksums
 * sent by the server.
 * 
 * @component The single instance of this class per application is created by
 *            PicoContainer in the central plug-in class {@link Saros}
 */
public class IsInconsistentObservable extends ObservableValue<Boolean> {

    public IsInconsistentObservable() {
        super(false);
    }

}
