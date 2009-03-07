package de.fu_berlin.inf.dpp.ui;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import de.fu_berlin.inf.dpp.util.Util;

public class WarningMessageDialog {

    private static final Logger log = Logger
        .getLogger(WarningMessageDialog.class.getName());

    /**
     * show error message dialog.
     * 
     * @param exception
     */
    public static void showWarningMessage(final String title,
        final String message) {
        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                MessageDialog.openWarning(
                    Display.getDefault().getActiveShell(), title, message);
            }
        });
    }

}
