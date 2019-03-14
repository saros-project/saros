package saros.intellij.ui.views.buttons;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import saros.SarosPluginContext;
import saros.activities.SPath;
import saros.concurrent.watchdog.ConsistencyWatchdogClient;
import saros.concurrent.watchdog.IsInconsistentObservable;
import saros.filesystem.IResource;
import saros.intellij.ui.Messages;
import saros.intellij.ui.actions.ConsistencyAction;
import saros.intellij.ui.util.DialogUtils;
import saros.intellij.ui.util.IconManager;
import saros.intellij.ui.util.NotificationPanel;
import saros.observables.ValueChangeListener;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.SessionEndReason;

/**
 * Button for triggering a {@link ConsistencyAction}. Displays a different symbol when state is
 * inconsistent or not.
 *
 * <p>FIXME: Remove awkward session handling together with UI components created with session.
 */
public class ConsistencyButton extends ToolbarButton {
  private static final Logger LOG = Logger.getLogger(ConsistencyButton.class);

  private boolean previouslyInConsistentState = true;

  @SuppressWarnings("FieldCanBeLocal")
  private final ActionListener actionListener =
      new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          if (!isEnabled() || sessionInconsistencyState == null) return;

          if (!sessionInconsistencyState.isInconsistent) return;

          setEnabledFromUIThread(false);

          final Set<SPath> paths =
              new HashSet<>(sessionInconsistencyState.watchdogClient.getPathsWithWrongChecksums());

          String inconsistentFiles = createConfirmationMessage(paths);

          boolean userConfirmedRecovery =
              DialogUtils.showQuestion(
                  project,
                  Messages.ConsistencyButton_confirm_dialog_title,
                  MessageFormat.format(
                      Messages.ConsistencyButton_confirm_dialog_message, inconsistentFiles));

          if (userConfirmedRecovery) {
            sessionInconsistencyState.action.execute();
          }

          setEnabledFromUIThread(true);
        }
      };

  @SuppressWarnings("FieldCanBeLocal")
  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
          if (!newSarosSession.isHost()) {
            setSarosSession(newSarosSession);
          }

          setToolTipText(Messages.ConsistencyButton_tooltip_no_inconsistency);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {
          if (!oldSarosSession.isHost()) {
            setSarosSession(null);
          }

          setEnabledFromUIThread(false);
          setToolTipText(Messages.ConsistencyButton_tooltip_functionality);
        }
      };

  private final ValueChangeListener<Boolean> isConsistencyListener = this::handleConsistencyChange;

  private final Project project;

  @Inject private ISarosSessionManager sessionManager;

  @Inject private IsInconsistentObservable inconsistentObservable;

  private volatile SessionInconsistencyState sessionInconsistencyState;

  /** Creates a Consistency button, adds a sessionListener and disables the button. */
  public ConsistencyButton(@NotNull Project project) {
    super(
        ConsistencyAction.NAME,
        Messages.ConsistencyButton_tooltip_functionality,
        IconManager.IN_SYNC_ICON);

    SarosPluginContext.initComponent(this);

    this.project = project;

    setSarosSession(sessionManager.getSession());
    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);

    addActionListener(actionListener);
    setEnabled(false);
  }

  private class SessionInconsistencyState {

    private ConsistencyAction action;

    private final ActionListener consistencyActionListener =
        new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent actionEvent) {
            setInconsistent(!watchdogClient.getPathsWithWrongChecksums().isEmpty());
          }
        };

    private boolean isInconsistent = false;

    private ConsistencyWatchdogClient watchdogClient;

    /** Creates an object to store the inconsistency warning state for a session. */
    SessionInconsistencyState(ISarosSession sarosSession) {

      watchdogClient = sarosSession.getComponent(ConsistencyWatchdogClient.class);

      action = new ConsistencyAction(watchdogClient);
      action.addActionListener(consistencyActionListener);
    }
  }

  private void setInconsistent(boolean isInconsistent) {
    sessionInconsistencyState.isInconsistent = isInconsistent;

    if (isInconsistent) {
      setEnabledFromUIThread(true);
      setButtonIcon(IconManager.OUT_OF_SYNC_ICON);
      setToolTipText(Messages.ConsistencyButton_tooltip_inconsistency_detected);
    } else {
      setEnabledFromUIThread(false);
      setButtonIcon(IconManager.IN_SYNC_ICON);
      setToolTipText(Messages.ConsistencyButton_tooltip_no_inconsistency);
    }
  }

  private void setSarosSession(ISarosSession newSession) {
    if (sessionInconsistencyState != null) {
      inconsistentObservable.remove(isConsistencyListener);
    }

    if (newSession != null) sessionInconsistencyState = new SessionInconsistencyState(newSession);

    if (sessionInconsistencyState != null) {
      inconsistentObservable.addAndNotify(isConsistencyListener);
    }
  }

  /**
   * This method activates the consistency recovery button, if an inconsistency was detected and
   * displays a tooltip.
   */
  private void handleConsistencyChange(final Boolean isInconsistent) {
    if (sessionManager.getSession() == null) {
      return;
    }

    LOG.debug("Inconsistency indicator goes: " + (isInconsistent ? "on" : "off"));

    ApplicationManager.getApplication().invokeLater(() -> setInconsistent(isInconsistent));

    if (!isInconsistent) {
      if (!previouslyInConsistentState) {
        previouslyInConsistentState = true;

        NotificationPanel.showInformation(
            Messages.ConsistencyButton_message_no_inconsistencies_remaining,
            Messages.ConsistencyButton_title_no_inconsistencies_remaining);
      }

      return;
    }

    if (previouslyInConsistentState) {
      previouslyInConsistentState = false;
    }

    final Set<SPath> paths =
        new HashSet<>(sessionInconsistencyState.watchdogClient.getPathsWithWrongChecksums());

    final String files = createInconsistentPathsMessage(paths);

    ApplicationManager.getApplication()
        .invokeLater(
            () -> {
              if (files.isEmpty()) {
                NotificationPanel.showWarning(
                    Messages.ConsistencyButton_message_inconsistency_detected_no_files,
                    Messages.ConsistencyButton_title_inconsistency_detected);

                return;
              }

              NotificationPanel.showWarning(
                  MessageFormat.format(
                      Messages.ConsistencyButton_message_inconsistency_detected, files),
                  Messages.ConsistencyButton_title_inconsistency_detected);
            });
  }

  private String createConfirmationMessage(Set<SPath> paths) {
    StringBuilder sbInconsistentFiles = new StringBuilder();

    for (SPath path : paths) {
      sbInconsistentFiles.append("module: ");
      sbInconsistentFiles.append(path.getProject().getName());
      sbInconsistentFiles.append(", file: ");
      sbInconsistentFiles.append(path.getProjectRelativePath().toOSString());
      sbInconsistentFiles.append("\n");
    }

    return sbInconsistentFiles.toString();
  }

  private String createInconsistentPathsMessage(Set<SPath> paths) {
    StringBuilder sb = new StringBuilder();

    for (SPath path : paths) {
      IResource resource = path.getResource();

      if (resource == null) {
        LOG.warn("Inconsistent resource " + path + " could not be " + "found.");

        continue;
      }

      if (sb.length() > 0) {
        sb.append(", ");
      }

      sb.append(resource.getFullPath());
    }

    return sb.toString();
  }
}
