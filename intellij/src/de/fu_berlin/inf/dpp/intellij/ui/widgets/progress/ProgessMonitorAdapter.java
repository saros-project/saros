package de.fu_berlin.inf.dpp.intellij.ui.widgets.progress;

import com.intellij.openapi.progress.ProgressIndicator;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;

/**
 * Adapter class for {@link ProgressIndicator} objects to be used as an {@link IProgressMonitor}.
 */
public final class ProgessMonitorAdapter implements IProgressMonitor {

  private final ProgressIndicator delegate;

  private volatile boolean isCanceled;

  private int totalWork;
  private int worked;

  private int percentCompleted = 0;

  public ProgessMonitorAdapter(final ProgressIndicator progessIndicator) {
    delegate = progessIndicator;
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

  @Override
  public void setCanceled(final boolean canceled) {
    isCanceled = canceled;

    // TODO there is no way to uncancel the progress
    if (canceled) delegate.cancel();
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
