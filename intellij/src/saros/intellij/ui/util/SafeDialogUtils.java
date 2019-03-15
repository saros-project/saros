package saros.intellij.ui.util;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.log4j.Logger;
import saros.SarosPluginContext;
import saros.exceptions.IllegalAWTContextException;

/**
 * Dialog helper used to show messages in safe manner by starting it on the AWT event dispatcher
 * thread.
 *
 * <p><b>NOTE:</b> Synchronous dialogs must not be triggered while inside a write safe context. This
 * applies to all input dialogs as they need to be executed synchronously to return the input value.
 * Such dialogs should check whether they are executed in a write safe context with {@link
 * Application#isWriteAccessAllowed()} and then throw an {@link
 * saros.exceptions.IllegalAWTContextException}.
 *
 * <p>Asynchronous dialogs can still be safely executed from any context with {@link
 * Application#invokeLater(Runnable,ModalityState)}.
 */
public class SafeDialogUtils {
  private static final Logger LOG = Logger.getLogger(SafeDialogUtils.class);

  private static final Application application;

  static {
    application = ApplicationManager.getApplication();

    SarosPluginContext.initComponent(new SafeDialogUtils());
  }

  private SafeDialogUtils() {}

  /**
   * Synchronously shows an input dialog. This method must not be called from a write safe context
   * as it needs to be executed synchronously and AWT actions are not allowed from a write safe
   * context.
   *
   * @param project the project used as a reference to generate and position the dialog
   * @param message the text displayed as the message of the dialog
   * @param initialValue the initial value contained in the text field of the input dialog
   * @param title the text displayed as the title of the dialog
   * @return the <code>String</code> entered by the user or <code>null</code> if the dialog did not
   *     finish with the exit code 0 (it was not closed by pressing the "OK" button)
   * @throws IllegalAWTContextException if the calling thread is currently inside a write safe
   *     context
   * @see Messages.InputDialog#getInputString()
   * @see com.intellij.openapi.ui.DialogWrapper#OK_EXIT_CODE
   */
  public static String showInputDialog(
      Project project, final String message, final String initialValue, final String title)
      throws IllegalAWTContextException {

    if (application.isWriteAccessAllowed()) {
      throw new IllegalAWTContextException("AWT events are not allowed " + "inside write actions.");
    }

    LOG.info("Showing input dialog: " + title + " - " + message + " - " + initialValue);

    final AtomicReference<String> response = new AtomicReference<>();

    application.invokeAndWait(
        () -> {
          String option =
              Messages.showInputDialog(
                  project, message, title, Messages.getQuestionIcon(), initialValue, null);
          if (option != null) {
            response.set(option);
          }
        },
        ModalityState.defaultModalityState());

    return response.get();
  }

  /**
   * Asynchronously shows an error dialog.
   *
   * @param project the project used as a reference to generate and position the dialog
   * @param message the text displayed as the message of the dialog
   * @param title the text displayed as the title of the dialog
   */
  public static void showError(Project project, final String message, final String title) {
    LOG.info("Showing error dialog: " + title + " - " + message);

    application.invokeLater(
        () -> Messages.showErrorDialog(project, message, title),
        ModalityState.defaultModalityState());
  }

  /**
   * Synchronously shows a password dialog. This method must not be called from a write safe context
   * as it needs to be executed synchronously and AWT actions are not allowed from a write safe
   * context.
   *
   * @param project the project used as a reference to generate and position the dialog
   * @param message the text displayed as the message of the dialog
   * @param title the text displayed as the title of the dialog
   * @return the <code>String</code> entered by the user or <code>null</code> if the dialog did not
   *     finish with the exit code 0 (it was not closed by pressing the "OK" button)
   * @throws IllegalAWTContextException if the calling thread is currently inside a write safe
   *     context
   * @see Messages.InputDialog#getInputString()
   * @see com.intellij.openapi.ui.DialogWrapper#OK_EXIT_CODE
   */
  public static String showPasswordDialog(Project project, final String message, final String title)
      throws IllegalAWTContextException {

    if (application.isWriteAccessAllowed()) {
      throw new IllegalAWTContextException("AWT events are not allowed " + "inside write actions.");
    }

    LOG.info("Showing password dialog: " + title + " - " + message);

    final AtomicReference<String> response = new AtomicReference<>();

    application.invokeAndWait(
        () -> {
          String option =
              Messages.showPasswordDialog(project, message, title, Messages.getQuestionIcon());

          if (option != null) {
            response.set(option);
          }
        },
        ModalityState.defaultModalityState());

    return response.get();
  }

  /**
   * Synchronously shows a yes/no dialog. This method must not be called from a write safe context
   * as it needs to be executed synchronously and AWT actions are not allowed from a write safe
   * context.
   *
   * @param project the project used as a reference to generate and position the dialog
   * @param message the text displayed as the message of the dialog
   * @param title the text displayed as the title of the dialog
   * @return the value {@link Messages#YES} if "Yes" is chosen and {@link Messages#NO} if "No" is
   *     chosen or the dialog is closed
   * @throws IllegalAWTContextException if the calling thread is currently inside a write safe
   *     context
   */
  public static Integer showYesNoDialog(Project project, final String message, final String title)
      throws IllegalAWTContextException {

    if (application.isWriteAccessAllowed()) {
      throw new IllegalAWTContextException("AWT events are not allowed inside write actions.");
    }

    LOG.info("Showing yes/no dialog: " + title + " - " + message);

    final AtomicReference<Integer> response = new AtomicReference<>();

    application.invokeAndWait(
        () -> {
          Integer option =
              Messages.showYesNoDialog(project, message, title, Messages.getQuestionIcon());

          response.set(option);
        },
        ModalityState.defaultModalityState());

    return response.get();
  }
}
