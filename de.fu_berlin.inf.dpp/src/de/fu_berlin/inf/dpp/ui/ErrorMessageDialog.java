package de.fu_berlin.inf.dpp.ui;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import de.fu_berlin.inf.dpp.util.Util;

/**
 * Eclipse Dialog to show Exception Messages.
 * 
 * @author rdjemili
 * @author chjacob
 * 
 */
public class ErrorMessageDialog {

    private static final Logger log = Logger.getLogger(ErrorMessageDialog.class
        .getName());

    /**
     * show error message dialog.
     * 
     * @param exception
     */
    public static void showErrorMessage(final Exception exception) {
        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                MessageDialog.openError(Display.getDefault().getActiveShell(),
                    exception.toString(), exception.getMessage());
            }
        });
    }

    /**
     * show error message dialog.
     * 
     * @param exception
     */
    public static void showErrorMessage(final String exceptionMessage) {
        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                if ((exceptionMessage == null) || exceptionMessage.equals("")) {
                    MessageDialog.openError(Display.getDefault()
                        .getActiveShell(), "Exception", "Error occured.");
                } else {
                    MessageDialog.openError(Display.getDefault()
                        .getActiveShell(), "Exception", exceptionMessage);
                }
            }

        });
    }
}
