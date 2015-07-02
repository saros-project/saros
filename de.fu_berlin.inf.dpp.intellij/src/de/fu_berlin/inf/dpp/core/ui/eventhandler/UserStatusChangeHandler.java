/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.core.ui.eventhandler;

import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.intellij.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.ui.util.NotificationPanel;
import de.fu_berlin.inf.dpp.session.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionListener;
import de.fu_berlin.inf.dpp.session.ISharedProjectListener;
import de.fu_berlin.inf.dpp.session.NullSarosSessionListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.util.ModelFormatUtils;

/**
 * Simple handler that informs the local user of the status changes for users in
 * the current session.
 */
public class UserStatusChangeHandler {

    private final ISharedProjectListener userStatusListener = new AbstractSharedProjectListener() {

        @Override
        public void permissionChanged(User user) {

            if (user.isLocal()) {
                NotificationPanel.showNotification(
                    Messages.UserStatusChangeHandler_permission_changed,
                    ModelFormatUtils
                        .format(
                            Messages.UserStatusChangeHandler_you_have_now_access,
                            user,
                            user.hasWriteAccess() ? Messages.UserStatusChangeHandler_write
                                : Messages.UserStatusChangeHandler_read_only));
            } else {
                NotificationPanel.showNotification(
                    Messages.UserStatusChangeHandler_permission_changed,
                    ModelFormatUtils.format(
                        Messages.UserStatusChangeHandler_he_has_now_access,
                        user,
                        user.hasWriteAccess() ? Messages.UserStatusChangeHandler_write
                            : Messages.UserStatusChangeHandler_read_only));

            }
        }

        @Override
        public void userJoined(User user) {

            NotificationPanel
                .showNotification(Messages.UserStatusChangeHandler_user_joined, ModelFormatUtils
                    .format(Messages.UserStatusChangeHandler_user_joined_text,
                        user));
        }

        @Override
        public void userLeft(User user) {
            NotificationPanel
                .showNotification(Messages.UserStatusChangeHandler_user_left, ModelFormatUtils
                    .format(Messages.UserStatusChangeHandler_user_left_text,
                        user));
        }
    };
    private final ISarosSessionListener sessionListener = new NullSarosSessionListener() {
        @Override
        public void sessionStarting(ISarosSession session) {
            session.addListener(userStatusListener);
        }

        @Override
        public void sessionEnded(ISarosSession session) {
            session.removeListener(userStatusListener);
        }

    };

    public UserStatusChangeHandler(ISarosSessionManager sessionManager) {
        sessionManager.addSarosSessionListener(sessionListener);
    }
}
