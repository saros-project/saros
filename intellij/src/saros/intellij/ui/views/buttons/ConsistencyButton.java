package saros.intellij.ui.views.buttons;

import com.intellij.openapi.project.Project;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import saros.concurrent.watchdog.ConsistencyWatchdogClient;
import saros.concurrent.watchdog.IsInconsistentObservable;
import saros.filesystem.IFile;
import saros.intellij.runtime.EDTExecutor;
import saros.intellij.ui.Messages;
import saros.intellij.ui.actions.ConsistencyAction;
import saros.intellij.ui.util.DialogUtils;
import saros.intellij.ui.util.IconManager;
import saros.intellij.ui.util.NotificationPanel;
import saros.observables.ValueChangeListener;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.SessionEndReason;

/**
 * Session button for triggering a {@link ConsistencyAction}. Displays a different symbol when state
 * is inconsistent or not.
 *
 * <p><b>NOTE:</b>This component and any component added here must be correctly torn down when the
 * project the components belong to is closed. See {@link AbstractSessionToolbarButton}.
 *
 * <p>FIXME: Remove awkward session handling together with UI components created with session.
 */
public class ConsistencyButton extends AbstractSessionToolbarButton {
  private static final Logger log = Logger.getLogger(ConsistencyButton.class);

  private boolean previouslyInConsistentState = true;

  @SuppressWarnings("FieldCanBeLocal")
  private final ActionListener actionListener =
      new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          if (!isEnabled() || sessionInconsistencyState == null) return;

          if (!sessionInconsistencyState.isInconsistent) return;

          setEnabled(false);

          final Set<IFile> files =
              sessionInconsistencyState.watchdogClient.getFilesWithWrongChecksums();

          String inconsistentFiles = createConfirmationMessage(files);

          boolean userConfirmedRecovery =
              DialogUtils.showQuestion(
                  project,
                  Messages.ConsistencyButton_confirm_dialog_title,
                  MessageFormat.format(
                      Messages.ConsistencyButton_confirm_dialog_message, inconsistentFiles));

          if (userConfirmedRecovery) {
            sessionInconsistencyState.action.execute();
          }

          setEnabled(true);
        }
      };

  private final ValueChangeListener<Boolean> isConsistencyListener = this::handleConsistencyChange;

  @Inject private IsInconsistentObservable inconsistentObservable;

  private volatile SessionInconsistencyState sessionInconsistencyState;

  /** Creates a Consistency button, adds a sessionListener and disables the button. */
  public ConsistencyButton(@NotNull Project project) {
    super(
        project,
        ConsistencyAction.NAME,
        Messages.ConsistencyButton_tooltip_functionality,
        IconManager.IN_SYNC_ICON);

    setSarosSession(sarosSessionManager.getSession());

    addActionListener(actionListener);
    setEnabled(false);

    setInitialState();
  }

  @Override
  public void dispose() {
    super.dispose();

    inconsistentObservable.remove(isConsistencyListener);
  }

  @Override
  void sessionStarted(ISarosSession newSarosSession) {
    setToolTipText(Messages.ConsistencyButton_tooltip_no_inconsistency);

    if (!newSarosSession.isHost()) {
      setSarosSession(newSarosSession);

      Boolean isInconsistent = inconsistentObservable.getValue();
      if (isInconsistent != null && isInconsistent) handleConsistencyChange(Boolean.TRUE);
    }
  }

  @Override
  void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {
    if (!oldSarosSession.isHost()) {
      setSarosSession(null);
    }

    setEnabled(false);
    setToolTipText(Messages.ConsistencyButton_tooltip_functionality);
    setButtonIcon(IconManager.IN_SYNC_ICON);
  }

  private class SessionInconsistencyState {

    private ConsistencyAction action;

    private final ActionListener consistencyActionListener =
        new ActionListener() {

          @Override
          public void actionPerformed(ActionEvent actionEvent) {
            setInconsistent(!watchdogClient.getFilesWithWrongChecksums().isEmpty());
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
      setEnabled(true);
      setButtonIcon(IconManager.OUT_OF_SYNC_ICON);
      setToolTipText(Messages.ConsistencyButton_tooltip_inconsistency_detected);
    } else {
      setEnabled(false);
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
    if (sarosSessionManager.getSession() == null) {
      return;
    }

    log.debug("Inconsistency indicator goes: " + (isInconsistent ? "on" : "off"));

    EDTExecutor.invokeLater(() -> setInconsistent(isInconsistent));

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

    final Set<IFile> files = sessionInconsistencyState.watchdogClient.getFilesWithWrongChecksums();

    final String inconsistentFilesMessage = createInconsistentFilesMessage(files);

    if (inconsistentFilesMessage.isEmpty()) {
      NotificationPanel.showWarning(
          Messages.ConsistencyButton_message_inconsistency_detected_no_files,
          Messages.ConsistencyButton_title_inconsistency_detected);

    } else {
      NotificationPanel.showWarning(
          MessageFormat.format(
              Messages.ConsistencyButton_message_inconsistency_detected, inconsistentFilesMessage),
          Messages.ConsistencyButton_title_inconsistency_detected);
    }
  }

  private String createConfirmationMessage(Set<IFile> files) {
    StringBuilder sbInconsistentFiles = new StringBuilder();

    for (IFile file : files) {
      sbInconsistentFiles
          .append(Messages.ConsistencyButton_inconsistent_list_reference_point)
          .append(": ");
      sbInconsistentFiles.append(file.getReferencePoint().getName()).append(", ");
      sbInconsistentFiles.append(Messages.ConsistencyButton_inconsistent_list_file).append(": ");
      sbInconsistentFiles.append(file.getReferencePointRelativePath());
      sbInconsistentFiles.append("\n");
    }

    return sbInconsistentFiles.toString();
  }

  private String createInconsistentFilesMessage(Set<IFile> files) {
    StringBuilder sb = new StringBuilder();

    for (IFile file : files) {
      if (sb.length() > 0) {
        sb.append(", ");
      }

      sb.append(file.getReferencePoint().getName())
          .append(" - ")
          .append(file.getReferencePointRelativePath());
    }

    return sb.toString();
  }
}
