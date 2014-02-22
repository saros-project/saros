package de.fu_berlin.inf.dpp.ui.eventhandler;

import java.text.MessageFormat;

import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.views.SarosView;

/**
 * Simple handler that informs the local user of the status changes for users in
 * the current session.
 * 
 * @author srossbach
 */
public class UserStatusChangeHandler {

    private final ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarting(ISarosSession session) {
            session.addListener(userStatusListener);
        }

        @Override
        public void sessionEnded(ISarosSession session) {
            session.removeListener(userStatusListener);
        }

    };

    private ISharedProjectListener userStatusListener = new AbstractSharedProjectListener() {

        /*
         * save to call SarosView.showNotification because it uses asyncExec
         * calls
         */

        @Override
        public void permissionChanged(User user) {

            if (user.isLocal()) {
                SarosView
                    .showNotification(
                        Messages.UserStatusChangeHandler_permission_changed,
                        MessageFormat
                            .format(
                                Messages.UserStatusChangeHandler_you_have_now_access,
                                user.getHumanReadableName(),
                                user.hasWriteAccess() ? Messages.UserStatusChangeHandler_write
                                    : Messages.UserStatusChangeHandler_read_only));
            } else {
                SarosView
                    .showNotification(
                        Messages.UserStatusChangeHandler_permission_changed,
                        MessageFormat.format(
                            Messages.UserStatusChangeHandler_he_has_now_access,
                            user.getHumanReadableName(),
                            user.hasWriteAccess() ? Messages.UserStatusChangeHandler_write
                                : Messages.UserStatusChangeHandler_read_only));

            }
        }

        @Override
        public void userJoined(User user) {

            SarosView.showNotification(
                Messages.UserStatusChangeHandler_user_joined, MessageFormat
                    .format(Messages.UserStatusChangeHandler_user_joined_text,
                        user.getHumanReadableName()));
        }

        @Override
        public void userLeft(User user) {
            SarosView.showNotification(
                Messages.UserStatusChangeHandler_user_left, MessageFormat
                    .format(Messages.UserStatusChangeHandler_user_left_text,
                        user.getHumanReadableName()));
        }
    };

    public UserStatusChangeHandler(ISarosSessionManager sessionManager) {
        sessionManager.addSarosSessionListener(sessionListener);
    }
}
