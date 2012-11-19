package de.fu_berlin.inf.dpp.ui.util;

import java.text.MessageFormat;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Version;

import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.VersionManager;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

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

    /**
     * Asks the user for confirmation to proceed.
     * 
     * @return <code>true</code> if the user confirms to proceed,
     *         <code>false</code> otherwise.
     * 
     * 
     * @nonReentrant In order to avoid a mass of question dialogs the same time.
     *               TODO: is this the right way?
     */
    public static synchronized boolean confirmUnsupportedSaros(final JID peer) {

        try {
            return Utils.runSWTSync(new Callable<Boolean>() {
                public Boolean call() {
                    return MessageDialog.openConfirm(getAShell(),
                        Messages.InvitationWizard_invite_no_support,
                        MessageFormat.format(
                            Messages.InvitationWizard_invite_no_support_text,
                            peer));
                }
            });
        } catch (Exception e) {
            log.error(
                "An error ocurred while trying to open the confirm dialog.", e); //$NON-NLS-1$
            return false;
        }
    }

    /**
     * Asks the user for confirmation to proceed. This method should only be
     * user if the versions are incompatible.
     * 
     * @param remoteVersionInfo
     *            a {@link VersionInfo} object with the local
     *            {@link VersionInfo#version} and the ultimate
     *            {@link VersionInfo#compatibility}. You can get this
     *            {@link VersionInfo} object by the method
     *            {@link VersionManager#determineCompatibility(JID)}.
     *            <code>null</code> is allowed.
     * 
     * @return <code>true</code> if the user confirms to proceed,
     *         <code>false</code> otherwise.
     * 
     *         TODO: is this the right way?
     * @nonReentrant In order to avoid a mass of question dialogs the same time.
     */
    public static synchronized boolean confirmVersionConflict(
        VersionInfo remoteVersionInfo, JID peer, Version localVersion) {

        final String title = MessageFormat.format(
            Messages.InvitationWizard_version_conflict, peer.getBase());
        final String message;
        if (remoteVersionInfo == null) {
            message = MessageFormat.format(
                Messages.InvitationWizard_version_request_failed, peer,
                peer.getBase());
        } else {
            switch (remoteVersionInfo.compatibility) {
            case TOO_OLD:
                message = MessageFormat.format(
                    Messages.InvitationWizard_version_too_old, localVersion,
                    remoteVersionInfo.version, peer.getBase());
                break;
            case TOO_NEW:
                message = MessageFormat.format(
                    Messages.InvitationWizard_version_too_new, localVersion,
                    remoteVersionInfo.version, peer.getBase());
                break;
            default:
                log.warn(
                    "Warning message requested when no warning is in place!", //$NON-NLS-1$
                    new StackTrace());
                // No warning to display
                message = MessageFormat.format(
                    Messages.InvitationWizard_invite_error, peer);
                break;
            }
        }

        try {
            return Utils.runSWTSync(new Callable<Boolean>() {
                public Boolean call() {
                    return MessageDialog.openQuestion(getAShell(), title,
                        message);
                }
            });
        } catch (Exception e) {
            log.error(
                "An error ocurred while trying to open the confirm dialog.", e); //$NON-NLS-1$
            return false;
        }
    }

    public static boolean confirmUnknownVersion(JID peer, Version localVersion) {

        final String title = MessageFormat.format(
            Messages.InvitationWizard_is_compatible, peer.getBase());
        final String message = MessageFormat.format(
            Messages.InvitationWizard_version_check_failed_text,
            peer.getBase(), localVersion.toString());

        try {
            return Utils.runSWTSync(new Callable<Boolean>() {
                public Boolean call() {
                    return MessageDialog.openQuestion(getAShell(), title,
                        message);
                }
            });
        } catch (Exception e) {
            log.error(
                "An error ocurred while trying to open the confirm dialog.", e); //$NON-NLS-1$
            return false;
        }
    }

    public static void notifyUserOffline(JID peer) {
        Utils.popUpFailureMessage(Messages.InvitationWizard_buddy_offline,
            MessageFormat.format(Messages.InvitationWizard_buddy_offline_text,
                peer), false);
    }

    private static Shell getAShell() {
        Shell shell = EditorAPI.getShell();
        if (shell == null)
            shell = new Shell();
        return shell;
    }
}
