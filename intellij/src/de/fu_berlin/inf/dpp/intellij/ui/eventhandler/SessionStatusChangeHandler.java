package de.fu_berlin.inf.dpp.intellij.ui.eventhandler;

import de.fu_berlin.inf.dpp.intellij.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.ui.util.NotificationPanel;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.session.User;
import java.text.MessageFormat;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/** Notifies the local user when a session is started or the current session ended. */
public class SessionStatusChangeHandler {

  public SessionStatusChangeHandler(@NotNull ISarosSessionManager sarosSessionManager) {

    ISessionLifecycleListener lifecycleListener =
        new ISessionLifecycleListener() {

          @Override
          public void sessionStarted(ISarosSession session) {

            notifySessionStart(session);
          }

          @Override
          public void sessionEnded(ISarosSession session, SessionEndReason reason) {

            notifySessionEnd(reason);
          }
        };

    sarosSessionManager.addSessionLifecycleListener(lifecycleListener);
  }

  /**
   * Notifies the user that a new session has started. If the local user is the host, this message
   * includes a list of the current participants in the session (if present). If the local user is a
   * client, this message includes the host whose session the user joined.
   *
   * @param session the session that was started
   */
  private void notifySessionStart(@NotNull ISarosSession session) {

    String sessionStartMessage;

    if (session.isHost()) {
      List<User> participants = session.getRemoteUsers();

      if (participants.isEmpty()) {
        sessionStartMessage =
            Messages.SessionStatusChangeHandler_session_started_host_empty_message;

      } else {
        sessionStartMessage =
            MessageFormat.format(
                Messages.SessionStatusChangeHandler_session_started_host_message, participants);
      }

    } else {
      sessionStartMessage =
          MessageFormat.format(
              Messages.SessionStatusChangeHandler_session_started_client_message,
              session.getHost());
    }

    NotificationPanel.showInformation(
        sessionStartMessage, Messages.SessionStatusChangeHandler_session_started_title);
  }

  /**
   * Notifies the local user that the current session has ended. The displayed message is dependent
   * on the reason the session ended.
   *
   * @param reason the reason the session ended
   */
  private void notifySessionEnd(@NotNull SessionEndReason reason) {

    String sessionEndExplanation;

    switch (reason) {
      case LOCAL_USER_LEFT:
        sessionEndExplanation = Messages.SessionStatusChangeHandler_local_user_left;
        break;
      case HOST_LEFT:
        sessionEndExplanation = Messages.SessionStatusChangeHandler_host_left;
        break;
      case KICKED:
        sessionEndExplanation = Messages.SessionStatusChangeHandler_kicked;
        break;
      case CONNECTION_LOST:
        sessionEndExplanation = Messages.SessionStatusChangeHandler_connection_lost;
        break;
      default:
        sessionEndExplanation = reason.name();
    }

    String message =
        MessageFormat.format(
            Messages.SessionStatusChangeHandler_session_ended_message, sessionEndExplanation);

    NotificationPanel.showInformation(
        message, Messages.SessionStatusChangeHandler_session_ended_title);
  }
}
