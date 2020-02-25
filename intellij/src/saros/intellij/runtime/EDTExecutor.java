package saros.intellij.runtime;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.ThrowableComputable;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;

/** Provides centralized methods to run code synchronously on the event dispatcher thread (EDT). */
public class EDTExecutor {
  private static final Application application;

  static {
    application = ApplicationManager.getApplication();
  }

  /**
   * Runs the passed computation synchronously on the EDT and returns the result.
   *
   * <p>If an exception occurs during the execution it is thrown back to the caller, including
   * <i>RuntimeException<i> and <i>Error</i>.
   *
   * @param computation the computation to run
   * @param modalityState the modality state to use
   * @param <T> the type of the result of the computation
   * @param <E> the type of the exception that might be thrown by the computation
   * @return returns the result of the computation
   * @throws E any exception that occurs while executing the computation
   * @see Application#invokeAndWait(Runnable,ModalityState)
   */
  @SuppressWarnings("unchecked")
  public static <T, E extends Throwable> T invokeAndWait(
      @NotNull ThrowableComputable<T, E> computation, @NotNull ModalityState modalityState)
      throws E {

    AtomicReference<T> result = new AtomicReference<>();
    AtomicReference<Throwable> throwable = new AtomicReference<>();

    application.invokeAndWait(
        () -> {
          try {
            result.set(computation.compute());

          } catch (Throwable t) {
            throwable.set(t);
          }
        },
        modalityState);

    Throwable t = throwable.get();

    if (t == null) return result.get();

    if (t instanceof Error) throw (Error) t;

    if (t instanceof RuntimeException) throw (RuntimeException) t;

    throw (E) t;
  }

  /**
   * Executes the given computation synchronously on the EDT using the given modality state and
   * returns the result of the computation.
   *
   * @param computation the computation to run
   * @param modalityState the modality state to use
   * @param <T> the type of the result of the computation
   * @return the result of the computation
   * @see Application#invokeAndWait(Runnable, ModalityState)
   */
  public static <T> T invokeAndWait(
      @NotNull Computable<T> computation, @NotNull ModalityState modalityState) {

    AtomicReference<T> result = new AtomicReference<>();

    application.invokeAndWait(() -> result.set(computation.compute()), modalityState);

    return result.get();
  }

  /**
   * Calls {@link #invokeAndWait(Computable, ModalityState)} with {@link
   * ModalityState#defaultModalityState()}.
   */
  public static <T> T invokeAndWait(@NotNull Computable<T> computation) {
    return invokeAndWait(computation, ModalityState.defaultModalityState());
  }

  /**
   * Executes the given runnable synchronously on the EDT using the given modality state.
   *
   * @param runnable the runnable to execute
   * @param modalityState the modality state to use
   * @see Application#invokeAndWait(Runnable, ModalityState)
   */
  public static void invokeAndWait(
      @NotNull Runnable runnable, @NotNull ModalityState modalityState) {

    application.invokeAndWait(runnable, modalityState);
  }

  /**
   * Calls {@link #invokeAndWait(Runnable, ModalityState)} with {@link
   * ModalityState#defaultModalityState()}.
   */
  public static void invokeAndWait(@NotNull Runnable runnable) {
    invokeAndWait(runnable, ModalityState.defaultModalityState());
  }

  /**
   * Executes the runnable asynchronously on the EDT using the given modality state.
   *
   * @param runnable the runnable to execute
   * @param modalityState the modality state to use
   * @see Application#invokeAndWait(Runnable, ModalityState)
   */
  public static void invokeLater(@NotNull Runnable runnable, @NotNull ModalityState modalityState) {
    application.invokeLater(runnable, modalityState);
  }

  /**
   * Calls {@link #invokeLater(Runnable, ModalityState)} with {@link
   * ModalityState#defaultModalityState()}.
   */
  public static void invokeLater(@NotNull Runnable runnable) {
    invokeLater(runnable, ModalityState.defaultModalityState());
  }
}
