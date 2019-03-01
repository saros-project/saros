package saros.synchronize.internal;

import java.awt.EventQueue;
import org.apache.log4j.Logger;
import saros.synchronize.UISynchronizer;

public class AWTSynchronizer implements UISynchronizer {

  private static final Logger LOG = Logger.getLogger(AWTSynchronizer.class);

  @Override
  public void asyncExec(Runnable runnable) {
    EventQueue.invokeLater(runnable);
  }

  @Override
  public void syncExec(Runnable runnable) {

    try {

      if (isUIThread()) runnable.run();
      else EventQueue.invokeAndWait(runnable);

    } catch (Exception e) {

      /*
       * TODO the SWT (Display.syncExec with default
       * org.eclipse.swt.widgets.Synchronizer) Synchronizer will ignore
       * interrupted exceptions and so we should not use
       * EventQueue.invokeAndWait but should use invokeLater along with a
       * wrapper around the runnable that tells us if the runnable has
       * completed and then interrupt the current thread "!
       */

      // can only happen with EventQueue.invokeAndWait
      if (e instanceof InterruptedException) {
        LOG.warn(
            "interrupted while waiting for completion of runnable " + runnable + " on the EDT");
        Thread.currentThread().interrupt();
        return;
      }

      LOG.error(
          "running " + runnable + " failed, encountered an error while being executed in the EDT",
          e);
    }
  }

  @Override
  public boolean isUIThread() {
    return EventQueue.isDispatchThread();
  }
}
