package de.fu_berlin.inf.dpp.intellij.ui.actions;

import com.intellij.openapi.application.ApplicationManager;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.intellij.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.ui.widgets.progress.MonitorProgressBar;
import de.fu_berlin.inf.dpp.intellij.ui.widgets.progress.ProgressFrame;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.util.ThreadUtils;

/** Performs project recovery, when inconsistency was detected. */
public class ConsistencyAction extends AbstractSarosAction {
  public static final String NAME = "consistency";

  @Override
  public String getActionName() {
    return NAME;
  }

  public ConsistencyAction(ConsistencyWatchdogClient watchdogClient) {
    this.watchdogClient = watchdogClient;
  }

  private ConsistencyWatchdogClient watchdogClient;

  /** This method starts {@link ConsistencyWatchdogClient#runRecovery(IProgressMonitor)}. */
  @Override
  public void execute() {
    LOG.debug("user activated CW recovery.");

    final ProgressFrame progress = new ProgressFrame("Consistency action");
    progress.setFinishListener(
        new MonitorProgressBar.FinishListener() {
          @Override
          public void finished() {
            ApplicationManager.getApplication()
                .invokeLater(
                    new Runnable() {
                      @Override
                      public void run() {
                        actionPerformed();
                      }
                    });
          }
        });

    ThreadUtils.runSafeAsync(
        LOG,
        new Runnable() {
          @Override
          public void run() {
            progress.beginTask(
                Messages.ConsistencyAction_progress_perform_recovery, IProgressMonitor.UNKNOWN);
            watchdogClient.runRecovery(progress);
          }
        });
  }
}
