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

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.AbstractSharedProjectListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * this action remove all remote driver from project. Only the project host has
 * the driver role after this action is executed.
 * 
 * @author orieger
 * 
 */
public class RemoveAllDriverRoleAction extends Action {

    public static final String ACTION_ID = RemoveAllDriverRoleAction.class
        .getName();

    private static final Logger log = Logger
        .getLogger(RemoveAllDriverRoleAction.class.getName());

    private ISharedProjectListener sharedProjectListener = new AbstractSharedProjectListener() {
        @Override
        public void roleChanged(User user, boolean replicated) {
            updateEnablement();
        }
    };

    public RemoveAllDriverRoleAction() {
        super("Remove driver roles");
        setImageDescriptor(SarosUI.getImageDescriptor("icons/user_edit.png"));
        setToolTipText("Remove all driver roles");
        setId(ACTION_ID);

        Saros.getDefault().getSessionManager().addSessionListener(
            new AbstractSessionListener() {

                @Override
                public void sessionStarted(ISharedProject sharedProject) {
                    sharedProject.addListener(sharedProjectListener);
                    updateEnablement();
                }

                @Override
                public void sessionEnded(ISharedProject sharedProject) {
                    sharedProject.removeListener(sharedProjectListener);
                    updateEnablement();
                }
            });
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

        ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();
        for (User user : project.getParticipants()) {
            if (user.isDriver()) {
                project.setUserRole(user, UserRole.OBSERVER, false);
            }
        }
    }

    private void updateEnablement() {
        ISharedProject project = Saros.getDefault().getSessionManager()
            .getSharedProject();
        setEnabled((project != null && project.isHost()));
    }
}
