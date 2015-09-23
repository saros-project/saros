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

package de.fu_berlin.inf.dpp.intellij.ui.actions;

import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.NullSessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.util.ModelFormatUtils;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.picocontainer.annotations.Inject;

import java.util.ArrayList;
import java.util.List;

/**
 * Action to activateor deactivate follow mode.
 */
public class FollowModeAction extends AbstractSarosAction {

    public static final String NAME = "follow";

    private final ISessionLifecycleListener sessionLifecycleListener = new NullSessionLifecycleListener() {
        @Override
        public void sessionStarted(final ISarosSession session) {
            ThreadUtils.runSafeAsync(LOG, new Runnable() {

                @Override
                public void run() {
                    FollowModeAction.this.session = session;
                }
            });
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            ThreadUtils.runSafeAsync(LOG, new Runnable() {

                @Override
                public void run() {
                    session = null;
                }
            });
        }
    };

    @Inject
    public ISarosSessionManager sessionManager;

    @Inject
    public EditorManager editorManager;

    private ISarosSession session;

    public FollowModeAction() {
        sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    }

    @Override
    public String getActionName() {
        return NAME;
    }

    public void execute(String userName) {
        if (session == null) {
            return;
        }

        editorManager.setFollowing(findUser(userName));

        actionPerformed();
    }

    @Override
    public void execute() {
        //never called
    }

    public User getCurrentlyFollowedUser() {
        return editorManager.getFollowedUser();
    }

    public List<User> getCurrentRemoteSessionUsers() {
        if (session == null)
            return new ArrayList<User>();

        return session.getRemoteUsers();

    }

    private User findUser(String userName) {
        if (userName == null) {
            return null;
        }

        for (User user : getCurrentRemoteSessionUsers()) {
            String myUserName = ModelFormatUtils.getDisplayName(user);
            if (myUserName.equalsIgnoreCase(userName)) {
                return user;
            }
        }

        return null;
    }
}
