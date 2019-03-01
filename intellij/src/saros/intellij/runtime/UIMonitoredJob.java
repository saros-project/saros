package saros.intellij.runtime;

import saros.core.monitoring.IStatus;
import saros.intellij.ui.widgets.progress.ProgressFrame;
import saros.monitoring.IProgressMonitor;

/** Class designed to start long lasting job with progress indicator */
public abstract class UIMonitoredJob extends Thread {

  private IProgressMonitor monitor;

  /**
   * Creates a new UIMonitoredJob with the given name monitored by monitor.
   *
   * @param name progress window name
   */
  public UIMonitoredJob(String name, IProgressMonitor monitor) {
    super(name);
    if (monitor == null) {
      this.monitor = new ProgressFrame();
    } else {
      this.monitor = monitor;
    }
    this.monitor.setTaskName(name);
  }

  /**
   * Creates job with named progress window
   *
   * @param name progress window name
   */
  public UIMonitoredJob(final String name) {
    this(name, null);
  }

  public void schedule() {
    start();
  }

  @Override
  public final void run() {
    run(monitor);
  }

  /**
   * Implement job business logic here. IProgressMonitor is passed internally. Implementation is
   * responsible to pass information about progress for progress monitor
   *
   * @param monitor
   * @return
   */
  protected abstract IStatus run(IProgressMonitor monitor);
}
