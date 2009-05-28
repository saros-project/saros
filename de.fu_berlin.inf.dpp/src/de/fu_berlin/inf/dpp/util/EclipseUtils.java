package de.fu_berlin.inf.dpp.util;

import org.eclipse.jface.window.Window;

public class EclipseUtils {

    /**
     * Calls open() on the given window (and returns the result), but before it
     * dispatches a call to forceActive (which gives a visual hint on the task
     * bar that the application wants focus).
     */
    public static int openWindow(Window wd) {

        if (wd.getShell() == null || wd.getShell().isDisposed()) {
            wd.create();
        }
        wd.getShell().open();
        wd.getShell().forceActive();
        return wd.open();

    }

}
