package de.fu_berlin.inf.dpp.intellij.ui.views.buttons;

import com.intellij.openapi.application.ApplicationManager;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.concurrent.watchdog.IsInconsistentObservable;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.ui.actions.ConsistencyAction;
import de.fu_berlin.inf.dpp.intellij.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.intellij.ui.util.IconManager;
import de.fu_berlin.inf.dpp.intellij.ui.util.NotificationPanel;
import de.fu_berlin.inf.dpp.observables.ValueChangeListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

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

          if (!DialogUtils.showQuestion(
              null, Messages.ConsistencyButton_confirm_dialog_title, inconsistentFiles)) {

            setEnabledFromUIThread(true);
            return;
          }

          sessionInconsistencyState.action.execute();
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
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {
          if (!oldSarosSession.isHost()) {
            setSarosSession(null);
          }

          setEnabledFromUIThread(false);
        }
      };

  private final ValueChangeListener<Boolean> isConsistencyListener = this::handleConsistencyChange;

  @Inject private ISarosSessionManager sessionManager;

  @Inject private IsInconsistentObservable inconsistentObservable;

  private volatile SessionInconsistencyState sessionInconsistencyState;

  /** Creates a Consistency button, adds a sessionListener and disables the button. */
  public ConsistencyButton() {
    super(
        ConsistencyAction.NAME,
        Messages.ConsistencyButton_tooltip_functionality,
        IconManager.IN_SYNC_ICON);

    SarosPluginContext.initComponent(this);

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
      /*
       * TODO tooltip should be set using messages.properties
       * use ConsistencyButton_tooltip_inconsistency_detected
       * this also requires the inconsistent files
       */
      setButtonIcon(IconManager.OUT_OF_SYNC_ICON);
    } else {
      setEnabledFromUIThread(false);
      setButtonIcon(IconManager.IN_SYNC_ICON);
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
      sbInconsistentFiles.append("project: ");
      sbInconsistentFiles.append(path.getProject().getName());
      sbInconsistentFiles.append(", file: ");
      sbInconsistentFiles.append(path.getProjectRelativePath().toOSString());
      sbInconsistentFiles.append("\n");
    }

    sbInconsistentFiles.append(
        "Please confirm project modifications.\n\n"
            + "                + The recovery process will perform changes to files and folders of the current shared project(s).\n\n"
            + "                + The affected files and folders may be either modified, created, or deleted.");
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
