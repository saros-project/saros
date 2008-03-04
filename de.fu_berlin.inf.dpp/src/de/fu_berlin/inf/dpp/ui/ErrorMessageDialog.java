package de.fu_berlin.inf.dpp.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
/**
 * Eclipse Dialog to show Exception Messages.
 * @author rdjemili
 *
 */
public class ErrorMessageDialog {

	/**
	 * show error message dialog.
	 * @param exception 
	 */
	public static void showErrorMessage(final Exception exception){
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(Display.getDefault().getActiveShell(),
					exception.toString(), exception.getMessage());
			}
		});
	}
}
