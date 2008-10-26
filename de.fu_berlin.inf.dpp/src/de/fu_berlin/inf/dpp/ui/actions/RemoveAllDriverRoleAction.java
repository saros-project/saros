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

import org.eclipse.jface.action.Action;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.SarosUI;

/**
 * this action remove all remote driver from project. Only the project host has
 * the driver role after this action is executed.
 * 
 * @author orieger
 * 
 */
public class RemoveAllDriverRoleAction extends Action implements
	ISharedProjectListener, ISessionListener {

    public RemoveAllDriverRoleAction() {
	super("Take remote user driver roles");
	setImageDescriptor(SarosUI.getImageDescriptor("icons/user_edit.png"));
	setToolTipText("Take remote driver roles");

	Saros.getDefault().getSessionManager().addSessionListener(this);
	updateEnablement();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void driverChanged(JID driver, boolean replicated) {
	updateEnablement();
    }

    private ISharedProject getSharedProject() {
	return Saros.getDefault().getSessionManager().getSharedProject();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void invitationReceived(IIncomingInvitationProcess process) {
	// ignore
    }

    @Override
    public void run() {
	// getSharedProject().setDriver(getSharedProject().getHost(), false);
	ISharedProject project = Saros.getDefault().getSessionManager()
		.getSharedProject();
	for (User user : project.getParticipants()) {

	    if ((user.getUserRole() == UserRole.DRIVER)
		    && !project.getHost().equals(user)) {
		project.removeDriver(user, false);
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void sessionEnded(ISharedProject session) {
	session.removeListener(this);
	updateEnablement();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void sessionStarted(ISharedProject session) {
	session.addListener(this);
	updateEnablement();
    }

    private void updateEnablement() {
	ISharedProject project = getSharedProject();
	boolean enabled = ((project != null) && project.isHost());
	setEnabled(enabled);
	// setEnabled(project != null && project.isHost() &&
	// !project.isDriver());
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void userJoined(JID user) {
	// ignore
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
     */
    public void userLeft(JID user) {
	// ignore
    }
}
