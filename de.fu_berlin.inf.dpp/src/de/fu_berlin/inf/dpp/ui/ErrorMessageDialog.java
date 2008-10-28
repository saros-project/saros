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

    /**
     * show error message dialog.
     * 
     * @param exception
     */
    public static void showChecksumErrorMessage(final String fileName) {
	Display.getDefault().syncExec(new Runnable() {
	    public void run() {
		MessageDialog.openWarning(
			Display.getDefault().getActiveShell(),
			"Consistency Problem!",
			"Inconsitent file state has detected. File " + fileName
				+ " has to synchronized with project host");
	    }
	});
    }

}
