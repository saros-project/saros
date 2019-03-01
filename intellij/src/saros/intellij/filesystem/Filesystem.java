package de.fu_berlin.inf.dpp.intellij.filesystem;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.ThrowableComputable;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Provides centralized methods for synchronized write and read actions. */
public class Filesystem {

  private static final Application application;

  static {
    application = ApplicationManager.getApplication();
  }

  private Filesystem() {
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
  @Nullable
  @SuppressWarnings("unchecked")
  public static <T, E extends Throwable> T runWriteAction(
      @NotNull final ThrowableComputable<T, E> computation,
      @Nullable final ModalityState modalityState)
      throws E {

    final ModalityState chosenModalityState =
        modalityState != null ? modalityState : ModalityState.defaultModalityState();

    final AtomicReference<T> result = new AtomicReference<>();
    final AtomicReference<Throwable> throwable = new AtomicReference<>();

    application.invokeAndWait(
        new Runnable() {

          @Override
          public void run() {
            try {
              result.set(application.runWriteAction(computation));

            } catch (Throwable t) {
              throwable.set(t);
            }
          }
        },
        chosenModalityState);

    final Throwable t = throwable.get();

    if (t == null) return result.get();

    if (t instanceof Error) throw (Error) t;

    if (t instanceof RuntimeException) throw (RuntimeException) t;

    throw (E) t;
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
      @NotNull final Runnable runnable, @Nullable final ModalityState modalityState) {

    final ModalityState chosenModalityState =
        modalityState != null ? modalityState : ModalityState.defaultModalityState();

    application.invokeAndWait(
        new Runnable() {

          @Override
          public void run() {
            application.runWriteAction(runnable);
          }
        },
        chosenModalityState);
  }

  /** @see Application#runReadAction(Computable computation) */
  @Nullable
  public static <T> T runReadAction(@NotNull final Computable<T> computation) {
    return application.runReadAction(computation);
  }

  /** @see Application#runReadAction(Runnable runnable) */
  public static void runReadAction(@NotNull final Runnable runnable) {
    application.runReadAction(runnable);
  }
}
