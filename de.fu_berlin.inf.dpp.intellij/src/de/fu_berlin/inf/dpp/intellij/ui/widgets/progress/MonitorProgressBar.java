package de.fu_berlin.inf.dpp.intellij.ui.widgets.progress;

import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/** Progress bar with autoincrement to use as COMPONENT in UI */
public class MonitorProgressBar implements IProgressMonitor {
  public static final int MIN_VALUE = 0;
  public static final int MAX_VALUE = 100;
  protected String taskName;
  private JProgressBar progressBar;
  private JLabel label;
  protected boolean isCanceled = false;

  private FinishListener finishListener;

  /**
   * Creates progress bar w/o additional information
   *
   * @param progressBar
   */
  public MonitorProgressBar(JProgressBar progressBar) {
    this(progressBar, null);
  }

  /**
   * Creates progress bar with additional information
   *
   * @param progressBar JProgressBar - progress information
   * @param infoLabel JLabel - additional information
   */
  public MonitorProgressBar(JProgressBar progressBar, JLabel infoLabel) {
    this.progressBar = progressBar;
    this.label = infoLabel;
  }

  /**
   * Sets real progress to UI
   *
   * @param progress progress
   */
  public void setProgress(final int progress) {
    SwingUtilities.invokeLater(
        new Runnable() {
          @Override
          public void run() {
            progressBar.setValue(progress);
          }
        });
  }

  @Override
  public boolean isCanceled() {
    return isCanceled;
  }

  @Override
  public void setCanceled(boolean cancel) {
    this.isCanceled = cancel;
    done();
  }

  @Override
  public void worked(int delta) {
    setProgress(progressBar.getValue() + delta);
  }

  @Override
  public void subTask(String subTaskName) {
    setTaskName(subTaskName);
  }

  @Override
  public void setTaskName(String name) {
    if (taskName == null) {
      return;
    }

    SwingUtilities.invokeLater(
        new Runnable() {
          @Override
          public void run() {
            if (label == null) {
              progressBar.setString(taskName);
            } else {
              label.setText(taskName);
            }
          }
        });
  }

  @Override
  public void done() {
    SwingUtilities.invokeLater(
        new Runnable() {
          @Override
          public void run() {
            progressBar.setIndeterminate(false);
            progressBar.setValue(MAX_VALUE);
          }
        });
    if (finishListener != null) {
      finishListener.finished();
    }
  }

  public void setIndeterminate(final boolean isIndeterminate) {
    SwingUtilities.invokeLater(
        new Runnable() {
          @Override
          public void run() {
            progressBar.setIndeterminate(isIndeterminate);
          }
        });
  }

  @Override
  public void beginTask(String taskName, int progress) {
    setProgress(progress);
    setTaskName(taskName);
    this.taskName = taskName;

    if (progress == IProgressMonitor.UNKNOWN) {
      setIndeterminate(true);
    }
  }

  /** @param finishListener FinishListener */
  public void setFinishListener(FinishListener finishListener) {
    this.finishListener = finishListener;
  }

  /** Interface creates structure to listen progress bar events */
  public interface FinishListener {
    /** Fires when progress monitor is finished */
    void finished();
  }
}
