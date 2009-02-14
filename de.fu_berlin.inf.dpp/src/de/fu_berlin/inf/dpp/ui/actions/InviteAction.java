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
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;

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

    private static final Logger log = Logger.getLogger(InviteAction.class
        .getName());

    private RosterEntry selectedEntry;

    public InviteAction(ISelectionProvider provider) {
        super(provider, "Invite user to shared project..");
        selectionChanged((IStructuredSelection) provider.getSelection());

        setToolTipText("Invite user to shared project..");
        setImageDescriptor(SarosUI
            .getImageDescriptor("icons/transmit_blue.png"));

        Saros.getDefault().getSessionManager().addSessionListener(this);
        updateEnablement();
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

    public void sessionStarted(ISharedProject session) {
        updateEnablement();
    }

    public void sessionEnded(ISharedProject session) {
        updateEnablement();
    }

    public void invitationReceived(IIncomingInvitationProcess process) {
        // ignore
    }

    protected void updateEnablement() {

        try {
            JID jid = (this.selectedEntry == null) ? null : new JID(
                this.selectedEntry.getUser());
            if (jid == null) {
                setEnabled(false);
                return;
            }
            Presence presence = Saros.getDefault().getConnection().getRoster()
                .getPresence(jid.toString());

            setEnabled(getSharedProject() != null
                && getSharedProject().getParticipant(jid) == null
                && getSharedProject().isHost() && presence.isAvailable()
                && Saros.getDefault().hasSarosSupport(jid.toString()));

        } catch (RuntimeException e) {
            log.error("Internal Error while updating InviteAction:", e);
        }
    }

    private ISharedProject getSharedProject() {
        return Saros.getDefault().getSessionManager().getSharedProject();
    }
}
