package de.fu_berlin.inf.dpp.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

public class WarningMessageDialog {

    /**
     * show error message dialog.
     * 
     * @param exception
     */
    public static void showWarningMessage(final String title,
        final String message) {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                MessageDialog.openWarning(
                    Display.getDefault().getActiveShell(), title, message);
            }
        });
    }

}
