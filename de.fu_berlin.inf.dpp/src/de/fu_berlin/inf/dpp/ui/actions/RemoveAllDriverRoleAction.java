/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * this action remove all remote driver from project. Only the project host has
 * the driver role after this action is executed.
 * 
 * @author orieger
 * 
 */
@Component(module = "action")
public class RemoveAllDriverRoleAction extends Action {

    public static final String ACTION_ID = RemoveAllDriverRoleAction.class
        .getName();

    private static final Logger log = Logger
        .getLogger(RemoveAllDriverRoleAction.class.getName());

    @Inject
    protected SarosUI sarosUI;

    protected ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {
        @Override
        public void roleChanged(User user) {
            updateEnablement();
        }
    };

    protected ISessionListener sessionListener = new AbstractSessionListener() {

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            newSarosSession.addListener(sharedProjectListener);
            updateEnablement();
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            oldSarosSession.removeListener(sharedProjectListener);
            updateEnablement();
        }
    };

    protected SessionManager sessionManager;

    public RemoveAllDriverRoleAction(SessionManager sessionManager) {
        super("Remove driver roles");
        this.sessionManager = sessionManager;

        setImageDescriptor(SarosUI.getImageDescriptor("icons/user_edit.png"));
        setToolTipText("Remove all driver roles");
        setId(ACTION_ID);

        sessionManager.addSessionListener(sessionListener);
        updateEnablement();
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Util.runSafeSync(log, new Runnable() {
            public void run() {
                runRemoveAllDrivers();
            }
        });
    }

    public void runRemoveAllDrivers() {

        ISarosSession sarosSession = sessionManager.getSarosSession();
        for (User user : sarosSession.getParticipants()) {
            if (user.isDriver()) {
                sarosUI.performRoleChange(user, UserRole.OBSERVER);
            }
        }
        updateEnablement();
    }

    protected void updateEnablement() {
        ISarosSession sarosSession = sessionManager.getSarosSession();
        setEnabled((sarosSession != null && sarosSession.isHost()));
    }
}
