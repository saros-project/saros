package saros.server.progress;

import java.io.PrintStream;
import saros.monitoring.IProgressMonitor;

/**
 * Progress indicator supposed to be use for terminal output.
 *
 * <p>Refreshes the same line over and it over again with the current progress until the task is
 * done to avoid flooding the output, but still be up-to-date.
 *
 * <p>Redraws in the next line if stdout is used in the meantime (e.g. by a logger). Avoid excessive
 * logging while this is in use.
 *
 * <p>Alternatively use {@link saros.monitoring.NullProgressMonitor} to disable progress reporting.
 */
public class ConsoleProgressIndicator implements IProgressMonitor {
  private PrintStream out;
  private boolean canceled = false;

  private int total = 0;
  private int worked = 0;

  private String task;
  private String subTask;

  private int animationFrameId = 0;
  private String[] animationFrames = {"[-] ", "[\\] ", "[|] ", "[/] "};

  public ConsoleProgressIndicator(PrintStream out) {
    this.out = out;
  }

  @Override
  public void done() {
    out.print('\n');
  }

  @Override
  public void subTask(String name) {
    if (!name.equals(subTask)) {
      subTask = name;
      print();
    }
  }

  @Override
  public void setTaskName(String name) {
    if (!name.equals(task)) {
      task = name;
      print();
    }
  }

  @Override
  public void worked(int amount) {
    if (amount != worked) {
      worked = amount;
      print();
    }
  }

  @Override
  public void setCanceled(boolean canceled) {
    if (canceled != this.canceled) {
      this.canceled = canceled;
      print();
    }
  }

  @Override
  public boolean isCanceled() {
    return canceled;
  }

  @Override
  public void beginTask(String name, int size) {
    task = name;
    total = size;
    print();
  }

  private void print() {
    if (canceled) {
      out.print("[CANCELED] ");
    } else if (total == IProgressMonitor.UNKNOWN) {
      out.print(animationFrames[animationFrameId]);
      animationFrameId = (animationFrameId + 1) % animationFrames.length;
    } else {
      out.printf("[%2d%%] ", worked / total);
    }
    out.print(task);
    if (subTask != null) {
      out.printf(" - %s", subTask);
    }
    out.print('\r');
  }
}
