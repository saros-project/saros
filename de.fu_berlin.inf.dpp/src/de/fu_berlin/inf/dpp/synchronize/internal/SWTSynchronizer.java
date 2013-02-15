package de.fu_berlin.inf.dpp.synchronize.internal;

import org.eclipse.swt.widgets.Display;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;

@Component(module = "eclipse")
public class SWTSynchronizer implements UISynchronizer {

    @Override
    public void asyncExec(Runnable runnable) {
        Display display = getDisplay();

        if (display.isDisposed())
            return;

        display.asyncExec(runnable);
    }

    @Override
    public void syncExec(Runnable runnable) {
        Display display = getDisplay();

        if (display.isDisposed())
            return;

        display.syncExec(runnable);
    }

    @Override
    public boolean isUIThread() {
        return Thread.currentThread() == getDisplay().getThread();
    }

    /*
     * always return the default display as some OS like Windows have the
     * capability to support multiple displays.
     */
    private Display getDisplay() {
        return Display.getDefault();
    }
}
