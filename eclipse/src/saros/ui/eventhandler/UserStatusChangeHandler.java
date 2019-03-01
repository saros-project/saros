package saros.ui.eventhandler;

import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.ISessionListener;
import saros.session.SessionEndReason;
import saros.session.User;
import saros.ui.Messages;
import saros.ui.util.ModelFormatUtils;
import saros.ui.views.SarosView;

/**
 * Simple handler that informs the local user of the status changes for users in the current
 * session.
 *
 * @author srossbach
 */
public class UserStatusChangeHandler {

  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {
        @Override
        public void sessionStarting(ISarosSession session) {
          session.addListener(sessionListener);
        }

        @Override
        public void sessionEnded(ISarosSession session, SessionEndReason reason) {
          session.removeListener(sessionListener);
        }
      };

  private ISessionListener sessionListener =
      new ISessionListener() {

        /*
         * save to call SarosView.showNotification because it uses asyncExec
         * calls
         */

        @Override
        public void permissionChanged(User user) {

          if (user.isLocal()) {
            SarosView.showNotification(
                Messages.UserStatusChangeHandler_permission_changed,
                ModelFormatUtils.format(
                    Messages.UserStatusChangeHandler_you_have_now_access,
                    user,
                    user.hasWriteAccess()
                        ? Messages.UserStatusChangeHandler_write
                        : Messages.UserStatusChangeHandler_read_only));
          } else {
            SarosView.showNotification(
                Messages.UserStatusChangeHandler_permission_changed,
                ModelFormatUtils.format(
                    Messages.UserStatusChangeHandler_he_has_now_access,
                    user,
                    user.hasWriteAccess()
                        ? Messages.UserStatusChangeHandler_write
                        : Messages.UserStatusChangeHandler_read_only));
          }
        }

        @Override
        public void userJoined(User user) {

          SarosView.showNotification(
              Messages.UserStatusChangeHandler_user_joined,
              ModelFormatUtils.format(Messages.UserStatusChangeHandler_user_joined_text, user));
        }

        @Override
        public void userLeft(User user) {
          SarosView.showNotification(
              Messages.UserStatusChangeHandler_user_left,
              ModelFormatUtils.format(Messages.UserStatusChangeHandler_user_left_text, user));
        }
      };

  public UserStatusChangeHandler(ISarosSessionManager sessionManager) {
    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
  }
}
