package saros.intellij.runtime;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import org.apache.log4j.Logger;
import saros.synchronize.UISynchronizer;

/**
 * Class implements the {@link UISynchronizer} by executing the given runnable on the event
 * dispatcher thread (EDT).
 *
 * @see EDTExecutor
 */
public class IntellijUISynchronizer implements UISynchronizer {

  private static final Logger log = Logger.getLogger(IntellijUISynchronizer.class);

  @Override
  public void asyncExec(Runnable runnable) {
    EDTExecutor.invokeLater(runnable);
  }

  @Override
  public void syncExec(Runnable runnable) {
    try {
      EDTExecutor.invokeAndWait(runnable);

    } catch (ProcessCanceledException e) {
      log.error("Synchronous execution on EDT interrupted - " + runnable, e);
    }
  }

  @Override
  public boolean isUIThread() {
    return ApplicationManager.getApplication().isDispatchThread();
  }
}
