package saros.monitoring;

/**
 * Adapter class which turns a Saros core progress monitor into an Eclipse one.
 *
 * @see saros.monitoring.IProgressMonitor
 * @see org.eclipse.core.runtime.IProgressMonitor
 */
public class CoreToEclipseMonitorAdapter
    implements org.eclipse.core.runtime.IProgressMonitor, saros.monitoring.IProgressMonitor {

  private final saros.monitoring.IProgressMonitor monitor;
  private double collectedWork;

  /**
   * Creates an Eclipse wrapper around a Saros core {@link IProgressMonitor}.
   *
   * @param monitor Saros core progress monitor to wrap
   */
  public CoreToEclipseMonitorAdapter(saros.monitoring.IProgressMonitor monitor) {
    this.monitor = monitor;
  }

  @Override
  public void beginTask(String name, int totalWork) {
    monitor.beginTask(name, totalWork);
  }

  @Override
  public void done() {
    monitor.done();
  }

  /**
   * Implementation of {@link org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)}
   * which delegates to {@link #worked(int)}, keeping track of passed fractions until whole full
   * units of work are reached. This compensates for the lack of an <code>internalWorked()</code>
   * method in the Saros core version of {@link IProgressMonitor}.
   */
  @Override
  public void internalWorked(double work) {
    collectedWork += work;

    int floorOfCollectedWork = (int) Math.floor(collectedWork);
    if (floorOfCollectedWork > 0) {
      worked(floorOfCollectedWork);
    }

    collectedWork -= floorOfCollectedWork;
  }

  @Override
  public boolean isCanceled() {
    return monitor.isCanceled();
  }

  @Override
  public void setCanceled(boolean value) {
    monitor.setCanceled(value);
  }

  @Override
  public void setTaskName(String name) {
    monitor.setTaskName(name);
  }

  @Override
  public void subTask(String name) {
    monitor.subTask(name);
  }

  @Override
  public void worked(int work) {
    monitor.worked(work);
  }
}
