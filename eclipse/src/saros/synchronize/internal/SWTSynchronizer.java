package de.fu_berlin.inf.dpp.synchronize.internal;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.util.StackTrace;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

@Component(module = "eclipse")
// TODO rename to EclipseSWTSynchronizer
public class SWTSynchronizer implements UISynchronizer {

  private static final Logger LOG = Logger.getLogger(SWTSynchronizer.class);

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
    // On Win32 it is possible to have multiple displays
    // return Thread.currentThread() == getDisplay().getThread();
    return Display.getCurrent() != null;
  }

  private void exec(Runnable runnable, boolean async) {
    try {
      Display display = getDisplay();

      /*
       * this will not work, although the chance is really small, it is
       * possible that the device is disposed after this check and before
       * the a(sync)Exec call
       */
      // if (display.isDisposed())
      // return;

      if (async) display.asyncExec(runnable);
      else display.syncExec(runnable);

    } catch (SWTException e) {

      if (PlatformUI.getWorkbench().isClosing()) {
        LOG.warn(
            "could not execute runnable " + runnable + ", UI thread is not available",
            new StackTrace());
      } else {
        LOG.error(
            "could not execute runnable "
                + runnable
                + ", workbench display was disposed before workbench shutdown",
            e);
      }
    }
  }

  private Display getDisplay() {
    return PlatformUI.getWorkbench().getDisplay();
  }
}
