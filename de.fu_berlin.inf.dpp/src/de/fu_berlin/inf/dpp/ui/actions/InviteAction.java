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
import org.jivesoftware.smack.RosterEntry;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DiscoveryManager;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
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
        public void sessionStarted(ISharedProject sharedProject) {
            updateEnablement();
        }

        @Override
        public void sessionEnded(ISharedProject sharedProject) {
            updateEnablement();
        }
    };

    protected SessionManager sessionManager;

    protected Saros saros;

    public InviteAction(SessionManager sessionManager, Saros saros,
        ISelectionProvider provider, DiscoveryManager discoManager) {
        super(provider, "Invite user to shared project..");
        setToolTipText("Invite user to shared project..");

        setImageDescriptor(SarosUI
            .getImageDescriptor("icons/transmit_blue.png"));

        this.sessionManager = sessionManager;
        this.saros = saros;
        this.discoManager = discoManager;
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
                sessionManager.openInviteDialog(getSelected());
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

        for (Object o : getStructuredSelection().toList()) {
            if (!(o instanceof RosterEntry)) {
                return Collections.emptyList();
            } else {
                selected.add(new JID(((RosterEntry) o).getUser()));
            }
        }

        return selected;
    }

    public boolean canInviteSelected() {

        ISharedProject project = sessionManager.getSharedProject();

        List<JID> selected = getSelected();

        if (project == null || !project.isHost() || selected.isEmpty()) {
            return false;
        }

        for (JID jid : selected) {

            // Participant needs to be...
            // ...available
            // ...not in a session already
            // ...to have saros
            if (!saros.getRoster().getPresence(jid.toString()).isAvailable()
                || project.getResourceQualifiedJID(jid) != null
                || !discoManager.isSarosSupported(jid))
                return false;

        }

        return true;
    }
}
