package de.fu_berlin.inf.dpp.concurrent.watchdog;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.observables.ObservableValue;

/**
 * This observable contains whether the ConsistencyWatchdogClient has detected that there are files
 * which are inconsistent with regards to the checksums sent by the server.
 */
@Component(module = "observables")
public class IsInconsistentObservable extends ObservableValue<Boolean> {

  public IsInconsistentObservable() {
    super(false);
  }
}
