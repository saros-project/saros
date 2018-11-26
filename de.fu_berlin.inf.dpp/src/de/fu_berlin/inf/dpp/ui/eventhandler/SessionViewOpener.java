package de.fu_berlin.inf.dpp.ui.eventhandler;

import de.fu_berlin.inf.dpp.concurrent.watchdog.IsInconsistentObservable;
import de.fu_berlin.inf.dpp.observables.ValueChangeListener;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.ViewUtils;

/**
 * This handler is responsible for opening the SessionView if an inconsistency has been detected.
 */
public class SessionViewOpener {

  public SessionViewOpener(IsInconsistentObservable isInconsistentObservable) {
    isInconsistentObservable.add(
        new ValueChangeListener<Boolean>() {
          @Override
          public void setValue(Boolean inconsistency) {
            if (!inconsistency) {
              return;
            }

            SWTUtils.runSafeSWTAsync(
                null,
                new Runnable() {
                  @Override
                  public void run() {
                    ViewUtils.openSarosView();
                  }
                });
          }
        });
  }
}
