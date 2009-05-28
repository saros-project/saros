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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Leaves the current Saros session. Is deactivated if there is no running
 * session.
 * 
 * @author rdjemili
 * @author oezbek
 */
@Component(module = "action")
public class LeaveSessionAction extends Action {

    private static final Logger log = Logger.getLogger(LeaveSessionAction.class
        .getName());

    protected SessionManager sessionManager;

    public LeaveSessionAction(SessionManager sessionManager) {

        this.sessionManager = sessionManager;

        setToolTipText("Leave the session");
        setImageDescriptor(SarosUI.getImageDescriptor("/icons/door_open.png"));

        sessionManager.addSessionListener(new AbstractSessionListener() {
            @Override
            public void sessionStarted(ISharedProject sharedProject) {
                updateEnablement();
            }

            @Override
            public void sessionEnded(ISharedProject sharedProject) {
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
        Shell shell = EditorAPI.getShell();
        if (shell == null) {
            return;
        }

        ISharedProject sharedProject = sessionManager.getSharedProject();

        assert sharedProject != null;

        boolean reallyLeave;

        if (sharedProject.isHost()) {
            if (sharedProject.getParticipants().size() == 1) {
                // Do not ask when host is alone...
                reallyLeave = true;
            } else {
                reallyLeave = MessageDialog
                    .openQuestion(
                        shell,
                        "Confirm Closing Session",
                        "Are you sure that you want to close this Saros session? Since you are the host of this session, it will be closed for all participants.");
            }
        } else {
            reallyLeave = MessageDialog.openQuestion(shell,
                "Confirm Leaving Session",
                "Are you sure that you want to leave this Saros session?");
        }

        if (!reallyLeave)
            return;

        Util.runSafeAsync(log, new Runnable() {
            public void run() {
                runLeaveSession();
            }
        });
    }

    protected void runLeaveSession() {
        try {
            sessionManager.stopSharedProject();
        } catch (Exception e) {
            log.error("Session could not be left: ", e);
        }
    }

    protected void updateEnablement() {
        setEnabled(sessionManager.getSharedProject() != null);
    }

}
