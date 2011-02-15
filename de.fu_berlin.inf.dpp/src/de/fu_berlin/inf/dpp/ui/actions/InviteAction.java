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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.discoveryManager.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.discoveryManager.DiscoveryManager.CacheMissException;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.project.SharedProject;
import de.fu_berlin.inf.dpp.ui.RosterView.TreeItem;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Action to start an Invitation for the currently selected RosterEntries.
 * 
 * @author rdjemili
 * @author oezbek
 */
public class InviteAction extends SelectionProviderAction {

    private static final Logger log = Logger.getLogger(InviteAction.class
        .getName());

    protected DiscoveryManager discoveryManager;

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {
        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            updateEnablement();
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            updateEnablement();
        }
    };

    protected SarosSessionManager sessionManager;
    protected Saros saros;
    protected InvitationProcessObservable invitationProcesses;

    public InviteAction(SarosSessionManager sessionManager, Saros saros,
        ISelectionProvider provider, DiscoveryManager discoManager,
        InvitationProcessObservable invitationProcesses) {
        super(provider, "Invite buddy...");
        setToolTipText("Invites the selected buddies to a Saros session. A new session will be started if none exists.");

        setImageDescriptor(SarosUI
            .getImageDescriptor("icons/elcl16/project_share_tsk.png"));

        this.sessionManager = sessionManager;
        this.saros = saros;
        this.discoveryManager = discoManager;
        this.invitationProcesses = invitationProcesses;
        sessionManager.addSarosSessionListener(sessionListener);

        updateEnablement();
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        ISarosSession currentSession = sessionManager.getSarosSession();

        if (currentSession == null) {
            // We are not in a session. We start a new session with the selected
            // user.

            // Choose project to share
            ContainerSelectionDialog dialog = new ContainerSelectionDialog(
                EditorAPI.getShell(), null, false, "Select project to share");

            dialog.open();
            Object[] result = dialog.getResult();

            IProject chosenProject = ResourcesPlugin.getWorkspace().getRoot()
                .findMember((Path) result[0]).getProject();
            List<IProject> chosenProjects = new ArrayList<IProject>();
            chosenProjects.add(chosenProject);

            // Start new Saros session, invite selected user
            try {

                sessionManager.startSession(chosenProjects, null);
                sessionManager.invite(getSelected(), makeDescription());
            } catch (final XMPPException e) {
                Utils.runSafeSWTSync(log, new Runnable() {
                    public void run() {
                        ErrorDialog.openError(EditorAPI.getShell(),
                            "Error Starting Session",
                            "Session could not be started", new Status(
                                IStatus.ERROR, "de.fu_berlin.inf.dpp",
                                IStatus.ERROR, e.getMessage(), e));
                    }
                });
            }

        } else {
            // We are in an existing session. We are adding the user.

            Utils.runSafeSync(log, new Runnable() {
                public void run() {
                    sessionManager.invite(getSelected(), makeDescription());
                }
            });
        }
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        updateEnablement();
    }

    public void updateEnablement() {

        Utils.runSafeSWTAsync(log, new Runnable() {
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

    /**
     * Checks if it is possible to invite the user currently selected in the
     * RosterView. Checks if peer:
     * <ol>
     * <li>is available
     * <li>is not the session (if a session exists)
     * <li>has Saros installed
     * <li>is not currently in an invitation
     * </ol>
     * 
     * @return true if the participant
     */
    public boolean canInviteSelected() {

        ISarosSession sarosSession = sessionManager.getSarosSession();
        List<JID> usersSelected = getSelected();

        if (usersSelected.isEmpty())
            return false;

        if (!saros.isConnected())
            return false;

        // Test if each user is reachable and available
        boolean sarosSupported = false;
        for (final JID jid : usersSelected) {
            try {
                sarosSupported = discoveryManager.isSupportedNonBlock(jid,
                    Saros.NAMESPACE);
            } catch (CacheMissException e) {
                // Saros support wasn't in cache. Update the discovery manager.
                discoveryManager.cacheSarosSupport(jid);
            }
            if (!saros.getRoster().getPresence(jid.toString()).isAvailable()
                || !sarosSupported
                || invitationProcesses.getInvitationProcess(jid) != null)
                return false;
        }

        if (sarosSession != null) {
            // Make sure I am host
            if (!sarosSession.isHost())
                return false;

            // Make sure none of them are already in the session
            for (JID jid : usersSelected) {
                if (sarosSession.getResourceQualifiedJID(jid) != null)
                    return false;
            }
        }

        return true;
    }

    private String makeDescription() {
        String result = sessionManager.getSarosSession().getHost().getJID()
            .getBase()
            + " has invited you to a Saros session";
        List<SharedProject> sharedProjects = sessionManager.getSarosSession()
            .getSharedProjects();
        if (sharedProjects.size() == 1) {
            result += " with the shared Project\n";
        } else if (sharedProjects.size() > 1) {
            result += " with the shared Projects\n";
        }
        for (SharedProject sharedProject : sharedProjects) {
            result += "\n - " + sharedProject.getName();
        }
        return result;
        // return sessionManager.getSarosSession().getHost().getJID().getBase()
        // + " has invited you to a Saros shared project session";

    }
}
