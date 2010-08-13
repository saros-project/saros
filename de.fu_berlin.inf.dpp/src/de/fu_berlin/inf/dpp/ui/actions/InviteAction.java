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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DiscoveryManager;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.RosterView.TreeItem;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Action to start an Invitation for the currently selected RosterEntries.
 * 
 * @author rdjemili
 * @author oezbek
 */
public class InviteAction extends SelectionProviderAction {

    private static final Logger log = Logger.getLogger(InviteAction.class
        .getName());

    protected DiscoveryManager discoManager;

    protected ISessionListener sessionListener = new AbstractSessionListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            updateEnablement();
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            updateEnablement();
        }
    };

    protected SessionManager sessionManager;
    protected Saros saros;
    protected InvitationProcessObservable invitationProcesses;

    public InviteAction(SessionManager sessionManager, Saros saros,
        ISelectionProvider provider, DiscoveryManager discoManager,
        InvitationProcessObservable invitationProcesses) {
        super(provider, "Invite user to shared project..");
        setToolTipText("Invite user to shared project..");

        setImageDescriptor(SarosUI.getImageDescriptor("icons/invites.png"));

        this.sessionManager = sessionManager;
        this.saros = saros;
        this.discoManager = discoManager;
        this.invitationProcesses = invitationProcesses;
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
                sessionManager.invite(getSelected(),
                    "You have been invited to a Saros session by "
                        + sessionManager.getSarosSession().getHost().getJID()
                            .getBase());
            }
        });
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        updateEnablement();
    }

    public void updateEnablement() {

        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                setEnabled(canInviteSelected());
            }
        });

    }

    public List<JID> getSelected() {
        ArrayList<JID> selected = new ArrayList<JID>();

        for (Object object : getStructuredSelection().toList()) {
            JID jid = ((TreeItem) object).getJID();
            if (jid == null) {
                return Collections.emptyList();
            } else {
                selected.add(jid);
            }
        }

        return selected;
    }

    public boolean canInviteSelected() {

        ISarosSession sarosSession = sessionManager.getSarosSession();

        List<JID> selected = getSelected();

        if (sarosSession == null || !sarosSession.isHost()
            || selected.isEmpty()) {
            return false;
        }

        for (JID jid : selected) {

            // Participant needs to be...
            // ...available
            // ...not in a session already
            // ...to have saros
            // ...not currently in a invitation
            if (!saros.getRoster().getPresence(jid.toString()).isAvailable()
                || sarosSession.getResourceQualifiedJID(jid) != null
                || !discoManager.isSarosSupported(jid)
                || invitationProcesses.getInvitationProcess(jid) != null)
                return false;

        }

        return true;
    }
}
