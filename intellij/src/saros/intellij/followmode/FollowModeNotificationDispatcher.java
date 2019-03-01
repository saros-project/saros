package saros.intellij.followmode;

import java.text.MessageFormat;
import org.apache.log4j.Logger;
import saros.editor.FollowModeManager;
import saros.editor.IFollowModeListener;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.NotificationPanel;
import saros.session.User;

/** Displays user notifications about follow mode status changes. */
public class FollowModeNotificationDispatcher implements IFollowModeListener {

  private static final Logger log = Logger.getLogger(FollowModeNotificationDispatcher.class);

  private User followedUser;

  public FollowModeNotificationDispatcher(FollowModeManager followModeManager) {
    followModeManager.addListener(this);
  }

  @Override
  public void stoppedFollowing(Reason reason) {
    String shownReason;

    switch (reason) {
      case FOLLOWEE_LEFT_SESSION:
        shownReason = Messages.FollowModeNotificationDispatcher_end_reason_FOLLOWEE_LEFT_SESSION;
        break;
      case FOLLOWER_CLOSED_OR_SWITCHED_EDITOR:
        shownReason =
            Messages.FollowModeNotificationDispatcher_end_reason_FOLLOWER_CLOSED_OR_SWITCHED_EDITOR;
        break;
      case FOLLOWER_CLOSED_EDITOR:
        shownReason = Messages.FollowModeNotificationDispatcher_end_reason_FOLLOWER_CLOSED_EDITOR;
        break;
      case FOLLOWER_STOPPED:
        shownReason = Messages.FollowModeNotificationDispatcher_end_reason_FOLLOWER_STOPPED;
        break;
      case FOLLOWER_SWITCHES_FOLLOWEE:
        shownReason =
            Messages.FollowModeNotificationDispatcher_end_reason_FOLLOWER_SWITCHES_FOLLOWEE;
        break;

      default:
        shownReason = reason.name();
        log.warn("Encountered unknown reason for follow mode end. reason: " + reason);
    }

    NotificationPanel.showInformation(
        MessageFormat.format(
            Messages.FollowModeNotificationDispatcher_stopped_following_message,
            followedUser,
            shownReason),
        Messages.FollowModeNotificationDispatcher_stopped_following_title);

    followedUser = null;
  }

  @Override
  public void startedFollowing(User target) {
    NotificationPanel.showInformation(
        MessageFormat.format(
            Messages.FollowModeNotificationDispatcher_started_following_message, target),
        Messages.FollowModeNotificationDispatcher_started_following_title);

    followedUser = target;
  }
}
