package saros.ui.util;

import java.util.concurrent.Callable;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

public class DialogUtils {

  private static Logger log = Logger.getLogger(DialogUtils.class);

  private DialogUtils() {
    // no instantiation allowed
  }

  /** Calls open() on the given window and returns the result. */
  public static int openWindow(Window wd) {

    if (wd.getShell() == null || wd.getShell().isDisposed()) {
      wd.create();
    }
    wd.getShell().open();
    return wd.open();
  }

  /**
   * Opens a MessageDialog of the type {@link MessageDialog#ERROR} and dispatches a call to
   * forceActive (which gives a visual hint on the taskbar that the application wants focus).
   *
   * @param shell the parent shell
   * @param dialogTitle the dialog title, or <code>null</code> if none
   * @param dialogMessage the dialog message
   * @return
   */
  public static int openErrorMessageDialog(Shell shell, String dialogTitle, String dialogMessage) {
    MessageDialog md =
        new MessageDialog(
            shell,
            dialogTitle,
            null,
            dialogMessage,
            MessageDialog.ERROR,
            new String[] {IDialogConstants.OK_LABEL},
            0);
    return openWindow(md);
  }

  /**
   * Opens a MessageDialog of the type {@link MessageDialog#INFORMATION} and dispatches a call to
   * forceActive (which gives a visual hint on the taskbar that the application wants focus).
   *
   * @param shell the parent shell
   * @param dialogTitle the dialog title, or <code>null</code> if none
   * @param dialogMessage the dialog message
   * @return
   */
  public static int openInformationMessageDialog(
      Shell shell, String dialogTitle, String dialogMessage) {
    MessageDialog md =
        new MessageDialog(
            shell,
            dialogTitle,
            null,
            dialogMessage,
            MessageDialog.INFORMATION,
            new String[] {IDialogConstants.OK_LABEL},
            0);
    return openWindow(md);
  }

  /**
   * Opens a MessageDialog of the type {@link MessageDialog#WARNING} and dispatches a call to
   * forceActive (which gives a visual hint on the taskbar that the application wants focus).
   *
   * @param shell the parent shell
   * @param dialogTitle the dialog title, or <code>null</code> if none
   * @param dialogMessage the dialog message
   * @return
   */
  public static int openWarningMessageDialog(
      Shell shell, String dialogTitle, String dialogMessage) {
    MessageDialog md =
        new MessageDialog(
            shell,
            dialogTitle,
            null,
            dialogMessage,
            MessageDialog.WARNING,
            new String[] {IDialogConstants.OK_LABEL},
            0);
    return openWindow(md);
  }

  /**
   * Opens a MessageDialog of the type {@link MessageDialog#QUESTION} and dispatches a call to
   * forceActive (which gives a visual hint on the task-bar that the application wants focus).
   *
   * @param shell the parent shell
   * @param dialogTitle the dialog title, or <code>null</code> if none
   * @param dialogMessage the dialog message
   * @return true if the user answered with YES
   */
  public static boolean openQuestionMessageDialog(
      Shell shell, String dialogTitle, String dialogMessage) {
    MessageDialog md =
        new MessageDialog(
            shell,
            dialogTitle,
            null,
            dialogMessage,
            MessageDialog.QUESTION,
            new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL},
            0);
    return openWindow(md) == 0;
  }

  /**
   * Ask the User a given question. It pops up a QuestionDialog with given title and message.
   *
   * @return boolean indicating whether the user said Yes or No
   */
  public static boolean popUpYesNoQuestion(
      final String title, final String message, boolean failSilently) {
    if (failSilently) return false;

    try {
      return SWTUtils.runSWTSync(
          new Callable<Boolean>() {
            @Override
            public Boolean call() {
              return MessageDialog.openQuestion(SWTUtils.getShell(), title, message);
            }
          });
    } catch (Exception e) {
      log.error("An internal error occurred while trying" + " to open the question dialog.");
      return false;
    }
  }

  /**
   * Indicate the User that there was an error. It pops up an ErrorDialog with given title and
   * message.
   */
  public static void popUpFailureMessage(
      final String title, final String message, boolean failSilently) {
    if (failSilently) return;

    SWTUtils.runSafeSWTSync(
        log,
        new Runnable() {
          @Override
          public void run() {
            MessageDialog.openError(SWTUtils.getShell(), title, message);
          }
        });
  }
}
