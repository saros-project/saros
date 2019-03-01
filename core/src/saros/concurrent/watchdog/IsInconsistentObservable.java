package saros.concurrent.watchdog;

import saros.annotations.Component;
import saros.observables.ObservableValue;

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
