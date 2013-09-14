package de.fu_berlin.inf.dpp.ui.util;

import java.text.MessageFormat;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.dialogs.RememberDecisionMessageDialog;

public class DialogUtils {

    private static Logger log = Logger.getLogger(DialogUtils.class);

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
        SWTUtils.runSafeSWTAsync(log, new Runnable() {
            @Override
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

    public static void notifyUserOffline(JID peer) {
        DialogUtils.popUpFailureMessage(
            Messages.InvitationWizard_buddy_offline, MessageFormat.format(
                Messages.InvitationWizard_buddy_offline_text, peer), false);
    }

    /**
     * Ask the User a given question. It pops up a {@link MessageDialog} with
     * given title and message. It stores the decision in the
     * {@link PreferenceStore} it the checkbox is selected.
     * 
     * @param saros
     *            is needed to set the selection to preference store
     * @param preferenceName
     *            constant where to store in the preference store
     * 
     * @return boolean indicating whether the user said Yes or No
     */
    public static boolean popUpRememberDecisionDialog(final String title,
        final String message, final Saros saros, final String preferenceName) {
        try {
            return SWTUtils.runSWTSync(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    RememberDecisionMessageDialog dialog = new RememberDecisionMessageDialog(
                        EditorAPI.getShell(), title, null, message,
                        MessageDialog.QUESTION, new String[] {
                            IDialogConstants.YES_LABEL,
                            IDialogConstants.NO_LABEL }, 0);
                    int result = dialog.open();
                    if (dialog.isRememberDecision()) {
                        saros.getPreferenceStore().setValue(preferenceName,
                            Boolean.toString(result == 0));
                    }

                    return result == 0;
                }
            });
        } catch (Exception e) {
            log.error("An internal error ocurred while trying"
                + " to open the question dialog.");
            return false;
        }
    }

    /**
     * Ask the User a given question. It pops up a QuestionDialog with given
     * title and message. Additionally custom button labels are applicable.
     * 
     * @param title
     *            dialog title
     * @param message
     *            displayed message
     * @param dialogButtonLabels
     *            custom button labels
     * @param failSilently
     *            don`t open the dialog
     * 
     * @return boolean indicating whether the user said Yes or No
     */
    public static boolean popUpCustomQuestion(final String title,
        final String message, final String[] dialogButtonLabels,
        boolean failSilently) {
        if (failSilently)
            return false;

        try {
            return SWTUtils.runSWTSync(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    MessageDialog md = new MessageDialog(EditorAPI.getShell(),
                        title, null, message, MessageDialog.QUESTION,
                        dialogButtonLabels, 0);
                    md.open();
                    return md.getReturnCode() == 0;
                }
            });
        } catch (Exception e) {
            log.error("An internal error ocurred while trying"
                + " to open the question dialog.");
            return false;
        }
    }

    /**
     * Ask the User a given question. It pops up a QuestionDialog with given
     * title and message.
     * 
     * @return boolean indicating whether the user said Yes or No
     */
    public static boolean popUpYesNoQuestion(final String title,
        final String message, boolean failSilently) {
        if (failSilently)
            return false;

        try {
            return SWTUtils.runSWTSync(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    return MessageDialog.openQuestion(EditorAPI.getShell(),
                        title, message);
                }
            });
        } catch (Exception e) {
            log.error("An internal error ocurred while trying"
                + " to open the question dialog.");
            return false;
        }
    }

    /**
     * Indicate the User that there was an error. It pops up an ErrorDialog with
     * given title and message.
     */
    public static void popUpFailureMessage(final String title,
        final String message, boolean failSilently) {
        if (failSilently)
            return;

        SWTUtils.runSafeSWTSync(log, new Runnable() {
            @Override
            public void run() {
                MessageDialog.openError(EditorAPI.getShell(), title, message);
            }
        });
    }
}
