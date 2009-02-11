package de.fu_berlin.inf.dpp.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Eclipse Dialog to show Exception Messages.
 * 
 * @author rdjemili
 * 
 */
public class ErrorMessageDialog {

    /**
     * show error message dialog.
     * 
     * @param exception
     */
    public static void showErrorMessage(final Exception exception) {
        Display.getDefault().syncExec(new Runnable() {
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
        Display.getDefault().syncExec(new Runnable() {
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

    protected static MessageDialog actualChecksumerrorDialog = null;

    /**
     * TODO CJ: write javadoc
     * 
     */
    public static void showChecksumErrorMessage(final String fileName) {

        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                actualChecksumerrorDialog = new MessageDialog(Display
                    .getDefault().getActiveShell(), "Consistency Problem!",
                    null, "Inconsitent file state has detected. File "
                        + fileName
                        + " has to be synchronized with project host",
                    MessageDialog.WARNING, new String[0], 0);
                actualChecksumerrorDialog.open();
            }
        });
    }

    /**
     * TODO CJ: write javadoc
     * 
     */
    public static void closeChecksumErrorMessage() {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                if (actualChecksumerrorDialog != null) {
                    actualChecksumerrorDialog.close();
                    actualChecksumerrorDialog = null;
                }

            }
        });
    }
}
