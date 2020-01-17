package saros.intellij.ui.util;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
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
   * @param selection the input range that is selected by default
   * @return the <code>String</code> entered by the user or <code>null</code> if the dialog did not
   *     finish with the exit code 0 (it was not closed by pressing the "OK" button)
   * @throws IllegalAWTContextException if the calling thread is currently inside a write safe
   *     context
   * @see Messages.InputDialog#getInputString()
   * @see com.intellij.openapi.ui.DialogWrapper#OK_EXIT_CODE
   */
  public static String showInputDialog(
      Project project,
      final String message,
      final String initialValue,
      final String title,
      InputValidator inputValidator,
      TextRange selection)
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
                  project,
                  message,
                  title,
                  Messages.getQuestionIcon(),
                  initialValue,
                  inputValidator,
                  selection);
          if (option != null) {
            response.set(option);
          }
        },
        ModalityState.defaultModalityState());

    return response.get();
  }

  /**
   * Calls {@link #showInputDialog(Project, String, String, String, InputValidator, TextRange)} with
   * <code>inputValidator=null</code>.
   *
   * @see #showInputDialog(Project, String, String, String, TextRange)
   */
  public static String showInputDialog(
      Project project,
      final String message,
      final String initialValue,
      final String title,
      TextRange selection)
      throws IllegalAWTContextException {

    return showInputDialog(project, message, initialValue, title, null, selection);
  }

  /**
   * Calls {@link #showInputDialog(Project, String, String, String, InputValidator, TextRange)} with
   * <code>inputValidator=null</code> and <code>selection=null</code>.
   *
   * @see #showInputDialog(Project, String, String, String, TextRange)
   */
  public static String showInputDialog(
      Project project, final String message, final String initialValue, final String title)
      throws IllegalAWTContextException {

    return showInputDialog(project, message, initialValue, title, null);
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
   * @return <code>true</code> if {@link Messages#YES} is chosen or <code>false</code> if {@link
   *     Messages#NO} is chosen or the dialog is closed
   * @throws IllegalAWTContextException if the calling thread is currently inside a write safe
   *     context
   * @throws IllegalStateException if no response value was received from the dialog or the response
   *     was not {@link Messages#YES} or {@link Messages#NO}.
   */
  public static boolean showYesNoDialog(Project project, final String message, final String title)
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

    Integer result = response.get();

    switch (result) {
      case Messages.YES:
        return true;
      case Messages.NO:
        return false;
      default:
        throw new IllegalStateException("Encountered unknown dialog answer " + result);
    }
  }

  /**
   * Shows a non-blocking yes/no dialog. The passed <code>runnable</code> can be used to run code
   * after the dialog is finished. However, it is <b>only</b> run if the user finishes the dialog
   * with the option {@link Messages#YES}.
   *
   * @param project the project used as a reference to generate and position the dialog
   * @param message the text displayed as the message of the dialog
   * @param title the text displayed as the title of the dialog
   * @param runAfter the runnable to execute if the user chooses {@link Messages#YES}
   * @throws IllegalStateException if the response from the dialog was not {@link Messages#YES} or
   *     {@link Messages#NO}.
   */
  public static void showYesNoDialog(
      @NotNull Project project,
      @NotNull String message,
      @NotNull String title,
      @NotNull Runnable runAfter) {

    LOG.info("Showing non-blocking yes/no dialog: " + title + " - " + message);

    application.invokeLater(
        () -> {
          int option =
              Messages.showYesNoDialog(project, message, title, Messages.getQuestionIcon());

          if (option == Messages.YES) {
            runAfter.run();

          } else if (option != Messages.NO) {
            throw new IllegalStateException("Encountered unknown dialog answer " + option);
          }
        },
        ModalityState.defaultModalityState());
  }
}
