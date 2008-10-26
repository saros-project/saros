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

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.jivesoftware.smack.RosterEntry;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISessionManager;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.ui.SarosUI;

/**
 * @author rdjemili
 */
public class InviteAction extends SelectionProviderAction implements
	ISessionListener {

    private RosterEntry selectedEntry;

    public InviteAction(ISelectionProvider provider) {
	super(provider, "Invite user to shared project..");
	selectionChanged((IStructuredSelection) provider.getSelection());

	setToolTipText("Start a IM messaging session with this user");
	setImageDescriptor(SarosUI
		.getImageDescriptor("icons/transmit_blue.png"));

	Saros.getDefault().getSessionManager().addSessionListener(this);
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
	JID jid = new JID(this.selectedEntry.getUser());
	ISessionManager sessionManager = Saros.getDefault().getSessionManager();
	ISharedProject project = sessionManager.getSharedProject();

	project.startInvitation(jid);
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
	if ((selection.size() == 1)
		&& (selection.getFirstElement() instanceof RosterEntry)) {
	    this.selectedEntry = (RosterEntry) selection.getFirstElement();
	} else {
	    this.selectedEntry = null;
	}

	updateEnablement();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void sessionEnded(ISharedProject session) {
	updateEnablement();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void sessionStarted(ISharedProject session) {
	updateEnablement();
    }

    private void updateEnablement() {
	JID jid = (this.selectedEntry == null) ? null : new JID(
		this.selectedEntry.getUser());

	setEnabled((getSharedProject() != null)
		&& (this.selectedEntry != null)
		&& (getSharedProject().getParticipant(jid) == null)
		&& (getSharedProject().isHost() || getSharedProject()
			.isDriver()));
    }
}
