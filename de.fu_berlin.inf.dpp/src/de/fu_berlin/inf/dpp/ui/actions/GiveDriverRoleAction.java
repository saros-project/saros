/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
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

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.SarosUI;

public class GiveDriverRoleAction extends SelectionProviderAction implements
	ISharedProjectListener, ISessionListener {

	private User selectedUser;

	public GiveDriverRoleAction(ISelectionProvider provider) {
		super(provider, "Give driver role");
		setImageDescriptor(SarosUI.getImageDescriptor("icons/user_edit.png"));
		setToolTipText("Give the driver role to this user");

		Saros.getDefault().getSessionManager().addSessionListener(this);
		updateEnablemnet();
	}

	@Override
	public void run() {
		ISharedProject project = Saros.getDefault().getSessionManager().getSharedProject();
		project.setDriver(selectedUser, false);
	}

	@Override
	public void selectionChanged(IStructuredSelection selection) {
		selectedUser = (selection.size() == 1) ? (User) selection.getFirstElement() : null;

		updateEnablemnet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
	 */
	public void sessionStarted(ISharedProject session) {
		session.addListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
	 */
	public void sessionEnded(ISharedProject session) {
		session.removeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
	 */
	public void invitationReceived(IIncomingInvitationProcess process) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.listeners.ISharedProjectListener
	 */
	public void driverChanged(JID driver, boolean replicated) {
		updateEnablemnet();
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

	private void updateEnablemnet() {
		ISharedProject project = Saros.getDefault().getSessionManager().getSharedProject();

		setEnabled(project != null && (project.isDriver() /*|| project.isHost()*/)
			&& selectedUser != null && !project.getDriver().equals(selectedUser));
	}
}
