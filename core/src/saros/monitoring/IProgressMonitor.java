package saros.monitoring;

import java.util.concurrent.Future;
import saros.exceptions.SarosCancellationException;

/**
 * This interface is under development. It currently equals its Eclipse counterpart. If not
 * mentioned otherwise all offered methods are equivalent to their Eclipse counterpart.
 */
public interface IProgressMonitor {

  /** Constant indicating an unknown amount of work. */
  public static final int UNKNOWN = -1;

  public void done();

  public void subTask(String name);

  public void setTaskName(String name);

  public void worked(int amount);

  public void setCanceled(boolean canceled);

  public boolean isCanceled();

  public void beginTask(String name, int size);

  /**
   * Wait till the provided future is done, while continuously checking if this monitor is canceled.
   *
   * @param future Future to wait for completion
   * @throws SarosCancellationException when the monitor is cancelled or the thread interrupted
   */
  public default void waitForCompletion(Future<?> future) throws SarosCancellationException {
    try {
      while (!future.isDone()) {
        if (isCanceled()) {
          throw new SarosCancellationException("Canceled waiting.");
        }

        Thread.sleep(200);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new SarosCancellationException("Interrupted waiting.");
    }
  }
}
