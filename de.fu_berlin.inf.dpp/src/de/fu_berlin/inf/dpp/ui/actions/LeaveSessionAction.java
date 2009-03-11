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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISessionManager;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Leaves the current Saros session. Is deactivated if there is no running
 * session.
 * 
 * @author rdjemili
 * @author oezbek
 */
public class LeaveSessionAction extends Action implements ISessionListener {

    private static final Logger log = Logger.getLogger(LeaveSessionAction.class
        .getName());

    public LeaveSessionAction() {
        setToolTipText("Leave the session");
        setImageDescriptor(SarosUI.getImageDescriptor("/icons/door_open.png"));

        LeaveSessionAction.getSessionManager().addSessionListener(this);
        updateEnablement();
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Util.runSafeSync(log, new Runnable() {
            public void run() {
                runLeaveSession();
            }
        });
    }

    public void runLeaveSession() {

        Shell shell = Display.getDefault().getActiveShell();
        if (shell == null) {
            return;
        }

        ISessionManager sessionManager = LeaveSessionAction.getSessionManager();

        boolean reallyLeave;

        if (sessionManager.getSharedProject().isHost()) {
            reallyLeave = MessageDialog
                .openQuestion(
                    shell,
                    "Confirm Closing Session",
                    "Are you sure that you want to close this Saros session? Since you are the host of this session, it will be closed for all participants.");
        } else {
            reallyLeave = MessageDialog.openQuestion(shell,
                "Confirm Leaving Session",
                "Are you sure that you want to leave this Saros session?");
        }

        if (!reallyLeave)
            return;
        try {
            sessionManager.stopSharedProject();
        } catch (Exception e) {
            ErrorDialog.openError(Display.getDefault().getActiveShell(),
                "Internal Error Leaving Session", "Session could not be left",
                new Status(IStatus.ERROR, "de.fu_berlin.inf.dpp",
                    IStatus.ERROR, e.getMessage(), e));
        }
    }

    public void sessionStarted(ISharedProject sharedProject) {
        updateEnablement();
    }

    public void sessionEnded(ISharedProject sharedProject) {
        updateEnablement();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void invitationReceived(IIncomingInvitationProcess process) {
        // ignore
    }

    private void updateEnablement() {
        setEnabled(LeaveSessionAction.getSessionManager().getSharedProject() != null);
    }

    private static ISessionManager getSessionManager() {
        return Saros.getDefault().getSessionManager();
    }
}
