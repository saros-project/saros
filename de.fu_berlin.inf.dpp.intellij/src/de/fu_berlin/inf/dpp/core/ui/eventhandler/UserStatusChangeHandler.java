package de.fu_berlin.inf.dpp.core.ui.eventhandler;

import de.fu_berlin.inf.dpp.intellij.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.ui.util.NotificationPanel;
import de.fu_berlin.inf.dpp.session.AbstractSessionListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.ISessionListener;
import de.fu_berlin.inf.dpp.session.NullSessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.util.ModelFormatUtils;

/**
 * Simple handler that informs the local user of the status changes for users in
 * the current session.
 */
public class UserStatusChangeHandler {

    private final ISessionListener sessionListener = new AbstractSessionListener() {

        @Override
        public void permissionChanged(User user) {

            if (user.isLocal()) {
                NotificationPanel.showInformation(ModelFormatUtils.format(
                    Messages.UserStatusChangeHandler_you_have_now_access, user,
                    user.hasWriteAccess() ?
                        Messages.UserStatusChangeHandler_write :
                        Messages.UserStatusChangeHandler_read_only),
                    Messages.UserStatusChangeHandler_permission_changed);
            } else {
                NotificationPanel.showInformation(ModelFormatUtils
                    .format(Messages.UserStatusChangeHandler_he_has_now_access,
                        user, user.hasWriteAccess() ?
                        Messages.UserStatusChangeHandler_write :
                        Messages.UserStatusChangeHandler_read_only),
                    Messages.UserStatusChangeHandler_permission_changed);

            }
        }

        @Override
        public void userJoined(User user) {

            NotificationPanel.showInformation(ModelFormatUtils
                .format(Messages.UserStatusChangeHandler_user_joined_text,
                    user), Messages.UserStatusChangeHandler_user_joined);
        }

        @Override
        public void userLeft(User user) {
            NotificationPanel.showInformation(ModelFormatUtils
                    .format(Messages.UserStatusChangeHandler_user_left_text,
                        user), Messages.UserStatusChangeHandler_user_left);
        }
    };
    private final ISessionLifecycleListener sessionLifecycleListener = new NullSessionLifecycleListener() {
        @Override
        public void sessionStarting(ISarosSession session) {
            session.addListener(sessionListener);
        }

        @Override
        public void sessionEnded(ISarosSession session,
            SessionEndReason reason) {
            session.removeListener(sessionListener);
        }

    };

    public UserStatusChangeHandler(ISarosSessionManager sessionManager) {
        sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    }
}
