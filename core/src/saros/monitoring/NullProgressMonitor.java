package saros.monitoring;

public class NullProgressMonitor implements IProgressMonitor {

  private volatile boolean canceled = false;

  @Override
  public void subTask(String task) {
    // NOP
  }

  @Override
  public void worked(int amount) {
    // NOP
  }

  @Override
  public void setCanceled(boolean canceled) {
    this.canceled = canceled;
  }

  @Override
  public boolean isCanceled() {
    return canceled;
  }

  @Override
  public void beginTask(String string, int size) {
    // NOP
  }

  @Override
  public void done() {
    // NOP
  }

  @Override
  public void setTaskName(String name) {
    // NOP
  }
}
