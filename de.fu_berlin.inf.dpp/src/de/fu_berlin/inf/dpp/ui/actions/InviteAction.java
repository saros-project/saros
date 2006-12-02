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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.progress.IProgressService;
import org.jivesoftware.smack.RosterEntry;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess.IInvitationUI;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;

/**
 * @author rdjemili
 */
public class InviteAction extends SelectionProviderAction implements ISessionListener,
	IInvitationUI {

	private RosterEntry selectedEntry;

	public InviteAction(ISelectionProvider provider) {
		super(provider, "Invite user to shared project..");

		setToolTipText("Start a IM messaging session with this user");
		setImageDescriptor(SarosUI.getImageDescriptor("icons/transmit_blue.png"));

		Saros.getDefault().getSessionManager().addSessionListener(this);
	}

	@Override
	public void run() {
		JID jid = new JID(selectedEntry.getUser());
		SessionManager sessionManager = Saros.getDefault().getSessionManager();
		ISharedProject project = sessionManager.getSharedProject();

		String name = project.getProject().getName();
		IOutgoingInvitationProcess process = project.invite(jid, name);
		process.setInvitationUI(this);
	}

	@Override
	public void selectionChanged(IStructuredSelection selection) {
		if (selection.size() == 1 && selection.getFirstElement() instanceof RosterEntry) {
			selectedEntry = (RosterEntry) selection.getFirstElement();
		} else {
			selectedEntry = null;
		}

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
	public void invitationReceived(IIncomingInvitationProcess process) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess.IInvitationUI
	 */
	public void runWithProgressBar(final IRunnableWithProgress runnable) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					IWorkbench wb = PlatformUI.getWorkbench();
					IProgressService ps = wb.getProgressService();
					ps.run(true, true, runnable);

				} catch (InvocationTargetException e) {
					e.printStackTrace();

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void updateEnablement() {
		setEnabled(getSharedProject() != null && selectedEntry != null
			&& getSharedProject().isHost());
	}

	private ISharedProject getSharedProject() {
		return Saros.getDefault().getSessionManager().getSharedProject();
	}
}
