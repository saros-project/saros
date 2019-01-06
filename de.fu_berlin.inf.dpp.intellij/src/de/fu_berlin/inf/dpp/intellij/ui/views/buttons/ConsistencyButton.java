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

  private static final String IN_SYNC_ICON_PATH = "/icons/famfamfam/in_sync.png";
  private static final String OUT_SYNC_ICON_PATH = "/icons/famfamfam/out_sync.png";

  private boolean previouslyInConsistentState = true;

  private final ActionListener actionListener =
      new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          if (!isEnabled() || sessionInconsistencyState == null) return;

          if (!sessionInconsistencyState.isInconsistent) return;

          setEnabledFromUIThread(false);

          final Set<SPath> paths =
              new HashSet<SPath>(
                  sessionInconsistencyState.watchdogClient.getPathsWithWrongChecksums());

          String inconsistentFiles = createConfirmationMessage(paths);

          if (!DialogUtils.showQuestion(
              null, Messages.ConsistencyAction_confirm_dialog_title, inconsistentFiles)) {

            setEnabledFromUIThread(true);
            return;
          }

          sessionInconsistencyState.action.execute();
        }
      };

  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
          setSarosSession(newSarosSession);
          setEnabledFromUIThread(true);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {
          setSarosSession(null);
          setEnabledFromUIThread(false);
        }
      };

  private final ValueChangeListener<Boolean> isConsistencyListener =
      new ValueChangeListener<Boolean>() {

        @Override
        public void setValue(Boolean newValue) {
          handleConsistencyChange(newValue);
        }
      };

  @Inject private ISarosSessionManager sessionManager;

  @Inject private IsInconsistentObservable inconsistentObservable;

  private volatile SessionInconsistencyState sessionInconsistencyState;

  /** Creates a Consistency button, adds a sessionListener and disables the button. */
  public ConsistencyButton() {
    super(
        ConsistencyAction.NAME,
        "Recover inconsistencies",
        IN_SYNC_ICON_PATH,
        "Files are consistent");
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
    public SessionInconsistencyState(ISarosSession sarosSession) {

      watchdogClient = sarosSession.getComponent(ConsistencyWatchdogClient.class);

      action = new ConsistencyAction(watchdogClient);
      action.addActionListener(consistencyActionListener);
    }
  }

  public void setInconsistent(boolean isInconsistent) {
    sessionInconsistencyState.isInconsistent = isInconsistent;

    if (isInconsistent) {
      setEnabledFromUIThread(true);
      /*
       * TODO tooltip should be set using messages.properties
       * use ConsistencyAction_tooltip_inconsistency_detected
       * this also requires the inconsistent files
       */
      setIcon(OUT_SYNC_ICON_PATH, "Files are NOT consistent");
    } else {
      setEnabledFromUIThread(false);
      setIcon(IN_SYNC_ICON_PATH, Messages.ConsistencyAction_tooltip_no_inconsistency);
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

    LOG.debug("Inconsistency indicator goes: " + (isInconsistent ? "on" : "off"));

    ApplicationManager.getApplication()
        .invokeLater(
            new Runnable() {
              @Override
              public void run() {
                setInconsistent(isInconsistent);
              }
            });

    if (!isInconsistent) {
      if (!previouslyInConsistentState) {
        previouslyInConsistentState = true;

        NotificationPanel.showInformation(
            "No inconsistencies remaining", "Inconsistencies resolved");
      }

      return;
    }

    if (previouslyInConsistentState) {
      previouslyInConsistentState = false;
    }

    final Set<SPath> paths =
        new HashSet<SPath>(sessionInconsistencyState.watchdogClient.getPathsWithWrongChecksums());

    final String files = createInconsistentPathsMessage(paths);

    ApplicationManager.getApplication()
        .invokeLater(
            new Runnable() {
              @Override
              public void run() {
                if (files.isEmpty()) {
                  NotificationPanel.showWarning(
                      Messages.ConsistencyAction_message_inconsistency_detected_no_files,
                      Messages.ConsistencyAction_title_inconsistency_detected);

                  return;
                }

                NotificationPanel.showWarning(
                    MessageFormat.format(
                        Messages.ConsistencyAction_message_inconsistency_detected, files),
                    Messages.ConsistencyAction_title_inconsistency_detected);
              }
            });
  }

  private String createConfirmationMessage(Set<SPath> paths) {
    StringBuilder sbInconsistentFiles = new StringBuilder();
    for (SPath path : paths) {
      sbInconsistentFiles.append("project: ");
      sbInconsistentFiles.append(path.getReferencePoint());
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
