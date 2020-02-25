package saros.intellij.runtime;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProcessCanceledException;
import org.apache.log4j.Logger;
import saros.synchronize.UISynchronizer;

/**
 * Class implements the {@link UISynchronizer} with {@link Application#invokeLater(Runnable)} and
 * {@link Application#invokeAndWait(Runnable, ModalityState)}.
 */
public class IntellijUISynchronizer implements UISynchronizer {

  private static final Logger log = Logger.getLogger(IntellijUISynchronizer.class);

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
      try {
        application.invokeAndWait(runnable, ModalityState.defaultModalityState());
      } catch (ProcessCanceledException e) {
        log.error("Synchronous execution on EDT interrupted - " + runnable, e);
      }
    }
  }
}
