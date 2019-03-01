package saros.ui.eventhandler;

import saros.concurrent.watchdog.IsInconsistentObservable;
import saros.observables.ValueChangeListener;
import saros.ui.util.SWTUtils;
import saros.ui.util.ViewUtils;

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
