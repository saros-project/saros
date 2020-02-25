package saros.intellij.runtime;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.ThrowableComputable;
import org.jetbrains.annotations.NotNull;

/**
 * Provides centralized methods for synchronized write and read actions.
 *
 * <p>Write actions are executed on the event dispatcher thread (EDT).
 *
 * @see EDTExecutor
 */
public class FilesystemRunner {

  private static final Application application;

  static {
    application = ApplicationManager.getApplication();
  }

  private FilesystemRunner() {
    // NOP
  }

  /**
   * Ensures that the given write action is executed in the correct thread context. This methods
   * waits until the action was executed.
   *
   * <p>If an exception occurs during the execution it is thrown back to the caller, including
   * <i>RuntimeException<i> and <i>Error</i>.
   *
   * @see Application#runWriteAction(Runnable)
   */
  public static <T, E extends Throwable> T runWriteAction(
      @NotNull ThrowableComputable<T, E> computation, @NotNull ModalityState modalityState)
      throws E {

    return EDTExecutor.invokeAndWait(
        (ThrowableComputable<T, E>) () -> application.runWriteAction(computation), modalityState);
  }

  /**
   * Ensures that the given write action is executed in the correct thread context. This methods
   * waits until the action was executed.
   *
   * <p><b>Note:</b> Exceptions are <b>NOT</b> logged. It is up to the caller to log any error
   * during the execution of the <i>runnable</i>.
   *
   * @see Application#runWriteAction(Runnable)
   */
  public static void runWriteAction(
      @NotNull Runnable runnable, @NotNull ModalityState modalityState) {

    EDTExecutor.invokeAndWait(() -> application.runWriteAction(runnable), modalityState);
  }

  /** @see Application#runReadAction(Computable computation) */
  public static <T> T runReadAction(@NotNull Computable<T> computation) {
    return application.runReadAction(computation);
  }
}
