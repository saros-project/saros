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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.jivesoftware.smack.RosterEntry;

import de.fu_berlin.inf.dpp.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.IOutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.ISharedProject;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.IOutgoingInvitationProcess.ICallback;
import de.fu_berlin.inf.dpp.listeners.ISessionListener;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.xmpp.JID;

public class InviteAction extends SelectionProviderAction implements ISessionListener, ICallback {
    private RosterEntry selectedEntry;

    private class SynchronizeJob extends Job {
        private final IOutgoingInvitationProcess outgoingInvitationProcess;

        public SynchronizeJob(IOutgoingInvitationProcess process) {
            super("Synchronizing");
            setUser(true);
            this.outgoingInvitationProcess = process;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            outgoingInvitationProcess.startSynchronization(monitor);
            return Status.OK_STATUS;
        }
    }
    
    public InviteAction(ISelectionProvider provider) {
        super(provider, "Invite user to shared project..");
        
        setToolTipText("Start a IM messaging session with this user");
        setImageDescriptor(SarosUI.getImageDescriptor("icons/transmit_blue.png"));
        
        Saros.getDefault().getSessionManager().addSessionListener(this);
    }
    
    @Override
    public void run() {
        JID jid = new JID(selectedEntry.getUser());
        ISharedProject project = Saros.getDefault().getSessionManager().getSharedProject();
        
        IOutgoingInvitationProcess process = project.invite(jid, project.getProject().getName());
        process.setCallback(this);
    }
    
    @Override
    public void selectionChanged(IStructuredSelection selection) {
        if (selection.size() == 1 && selection.getFirstElement() instanceof RosterEntry) {
            selectedEntry = (RosterEntry)selection.getFirstElement();
        } else {
            selectedEntry = null;
        }
        
        updateEnablement();
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void sessionStarted(ISharedProject session) {
        updateEnablement();
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void sessionEnded(ISharedProject session) {
        updateEnablement();
    }

    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.listeners.ISessionListener
     */
    public void invitationReceived(IIncomingInvitationProcess process) {
        // ignore
    }
    
    /* (non-Javadoc)
     * @see de.fu_berlin.inf.dpp.IOutgoingInvitationProcess.ICallback
     */
    public void outgoingInvitationAccepted(IOutgoingInvitationProcess process) {
        SynchronizeJob job = new SynchronizeJob(process);
        job.setPriority(Job.LONG);
        job.schedule();
    }

    private void updateEnablement() {
        setEnabled(getSharedProject() != null && selectedEntry != null && 
            getSharedProject().isHost());
    }
    
    private ISharedProject getSharedProject() {
        return Saros.getDefault().getSessionManager().getSharedProject();
    }
}
