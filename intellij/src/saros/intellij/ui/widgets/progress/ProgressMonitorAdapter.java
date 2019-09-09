package saros.intellij.ui.widgets.progress;

import com.intellij.openapi.progress.ProgressIndicator;
import org.apache.log4j.Logger;
import saros.monitoring.IProgressMonitor;

/**
 * Adapter class for {@link ProgressIndicator} objects to be used as an {@link IProgressMonitor}.
 */
public final class ProgressMonitorAdapter implements IProgressMonitor {

  private static final Logger log = Logger.getLogger(ProgressMonitorAdapter.class);

  private final ProgressIndicator delegate;

  private volatile boolean isCanceled;

  private int totalWork;
  private int worked;

  private int percentCompleted = 0;

  public ProgressMonitorAdapter(final ProgressIndicator progressIndicator) {
    delegate = progressIndicator;
  }

  @Override
  public void done() {
    setTaskName("");
    subTask("");
    worked = 0;
    totalWork = 0;
    percentCompleted = 0;

    delegate.setIndeterminate(false);
    delegate.setFraction(1.0D);
  }

  @Override
  public void subTask(final String name) {
    delegate.setText2(name);
  }

  @Override
  public void setTaskName(final String name) {
    delegate.setText(name);
  }

  @Override
  public void worked(final int amount) {
    assert amount >= 0;

    if (totalWork < 0) return;

    worked += amount;

    if (worked > totalWork) worked = totalWork;

    double fraction = (worked + 0.0D) / totalWork;
    int currentPercent = (int) Math.round(fraction * 100D);

    if (percentCompleted == currentPercent) return;

    percentCompleted = currentPercent;
    delegate.setFraction(fraction);
  }

  /**
   * {@inheritDoc}
   *
   * <p><b>Note:</b> This used Intellij implementation does not allow to un-cancel a progress
   * monitor. Calls with <code>canceled=false</code> do not have any affect.
   *
   * @param canceled <code>true</code> to cancel the progress monitor, <code>false</code> for a NOP
   */
  @Override
  public void setCanceled(final boolean canceled) {
    if (!canceled) {
      if (isCanceled) {
        log.warn("Tried to un-cancel progress monitor. This is not supported!");
      }

      return;
    }

    isCanceled = true;

    if (!delegate.isCanceled()) {
      delegate.cancel();
    }
  }

  @Override
  public boolean isCanceled() {
    return isCanceled || delegate.isCanceled();
  }

  @Override
  public void beginTask(final String name, final int size) {

    if (!delegate.isRunning()) delegate.start();

    setTaskName(name);

    worked = 0;
    totalWork = size;
    percentCompleted = 0;

    delegate.setFraction(0D);
    delegate.setIndeterminate(totalWork < 0);
  }
}
