package de.fu_berlin.inf.dpp.intellij.runtime;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;

/**
 * Class implements the {@link UISynchronizer} with {@link Application#invokeLater(Runnable)} and
 * {@link Application#invokeAndWait(Runnable, ModalityState)}.
 */
public class IntelliJSynchronizer implements UISynchronizer {

  @Override
  public void asyncExec(Runnable runnable) {
    exec(runnable, true);
  }

  @Override
  public void syncExec(Runnable runnable) {
    exec(runnable, false);
  }

  @Override
  public boolean isUIThread() {
    return ApplicationManager.getApplication().isDispatchThread();
  }

  private void exec(Runnable runnable, boolean async) {
    Application application = ApplicationManager.getApplication();

    if (async) {
      application.invokeLater(runnable, ModalityState.defaultModalityState());
    } else {
      application.invokeAndWait(runnable, ModalityState.defaultModalityState());
    }
  }
}
