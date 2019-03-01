package saros.monitoring;

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
}
