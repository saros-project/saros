package de.fu_berlin.inf.dpp.ui.util;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.util.Utils;

public class DialogUtils {

    private DialogUtils() {
        // no instantiation allowed
    }

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

    /**
     * Opens a MessageDialog of the type {@link MessageDialog#ERROR} and
     * dispatches a call to forceActive (which gives a visual hint on the
     * taskbar that the application wants focus).
     * 
     * @param shell
     *            the parent shell
     * @param dialogTitle
     *            the dialog title, or <code>null</code> if none
     * @param dialogMessage
     *            the dialog message
     * @return
     */
    public static int openErrorMessageDialog(Shell shell, String dialogTitle,
        String dialogMessage) {
        MessageDialog md = new MessageDialog(shell, dialogTitle, null,
            dialogMessage, MessageDialog.ERROR,
            new String[] { IDialogConstants.OK_LABEL }, 0);
        return openWindow(md);
    }

    /**
     * Shows an error window and sets monitors subTask to <code>message</code>
     * or exceptions message.
     * 
     * @param title
     *            Title of error window
     * @param message
     *            Message of error window
     * @param e
     *            Exception caused this error, may be <code>null</code>
     * @param monitor
     *            May be <code>null</code>
     */
    public static void showErrorPopup(final Logger log, final String title,
        final String message, Exception e, IProgressMonitor monitor) {
        Utils.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                DialogUtils.openErrorMessageDialog(EditorAPI.getShell(), title,
                    message);
            }
        });
        if (monitor != null) {
            if (e != null && e.getMessage() != null
                && !(e.getMessage().length() == 0))
                monitor.subTask(e.getMessage());
            else
                monitor.subTask(message);
        }
    }

    /**
     * Opens a MessageDialog of the type {@link MessageDialog#INFORMATION} and
     * dispatches a call to forceActive (which gives a visual hint on the
     * taskbar that the application wants focus).
     * 
     * @param shell
     *            the parent shell
     * @param dialogTitle
     *            the dialog title, or <code>null</code> if none
     * @param dialogMessage
     *            the dialog message
     * @return
     */
    public static int openInformationMessageDialog(Shell shell,
        String dialogTitle, String dialogMessage) {
        MessageDialog md = new MessageDialog(shell, dialogTitle, null,
            dialogMessage, MessageDialog.INFORMATION,
            new String[] { IDialogConstants.OK_LABEL }, 0);
        return openWindow(md);
    }

    /**
     * Opens a MessageDialog of the type {@link MessageDialog#WARNING} and
     * dispatches a call to forceActive (which gives a visual hint on the
     * taskbar that the application wants focus).
     * 
     * @param shell
     *            the parent shell
     * @param dialogTitle
     *            the dialog title, or <code>null</code> if none
     * @param dialogMessage
     *            the dialog message
     * @return
     */
    public static int openWarningMessageDialog(Shell shell, String dialogTitle,
        String dialogMessage) {
        MessageDialog md = new MessageDialog(shell, dialogTitle, null,
            dialogMessage, MessageDialog.WARNING,
            new String[] { IDialogConstants.OK_LABEL }, 0);
        return openWindow(md);
    }

    /**
     * Opens a MessageDialog of the type {@link MessageDialog#QUESTION} and
     * dispatches a call to forceActive (which gives a visual hint on the
     * task-bar that the application wants focus).
     * 
     * @param shell
     *            the parent shell
     * @param dialogTitle
     *            the dialog title, or <code>null</code> if none
     * @param dialogMessage
     *            the dialog message
     * @return true if the user answered with YES
     */
    public static boolean openQuestionMessageDialog(Shell shell,
        String dialogTitle, String dialogMessage) {
        MessageDialog md = new MessageDialog(shell, dialogTitle, null,
            dialogMessage, MessageDialog.QUESTION, new String[] {
                IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
        return openWindow(md) == 0;
    }

}
