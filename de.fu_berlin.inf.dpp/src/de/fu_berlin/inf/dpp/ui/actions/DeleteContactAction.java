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

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.util.RosterUtils;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.util.Utils;

public class DeleteContactAction extends Action {

    private static final Logger log = Logger
        .getLogger(DeleteContactAction.class.getName());

    protected Saros saros;

    protected SarosSessionManager sessionManager;

    protected final String DELETE_ERROR_IN_SESSION = "You cannot delete this buddy "
        + "because they are currently in your Saros session.";

    public DeleteContactAction(SarosSessionManager sessionManager, Saros saros) {
        super("Delete");

        SelectionUtils.getSelectionService().addSelectionListener(
            new ISelectionListener() {
                public void selectionChanged(IWorkbenchPart part,
                    ISelection selection) {
                    List<JID> buddies = SelectionRetrieverFactory
                        .getSelectionRetriever(JID.class).getSelection();
                    setEnabled(buddies.size() == 1);
                }
            });
        setToolTipText("Delete this buddy.");

        IWorkbench workbench = PlatformUI.getWorkbench();
        setImageDescriptor(workbench.getSharedImages().getImageDescriptor(
            ISharedImages.IMG_TOOL_DELETE));

        this.sessionManager = sessionManager;
        this.saros = saros;
    }

    public static String toString(RosterEntry entry) {
        StringBuilder sb = new StringBuilder();
        String name = entry.getName();
        if (name != null && name.trim().length() > 0) {
            sb.append("'").append(name).append("' ");
        }
        sb.append(entry.getUser());
        return sb.toString();
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Utils.runSafeSync(log, new Runnable() {
            public void run() {
                runDeleteAction();
            }
        });
    }

    public void runDeleteAction() {
        RosterEntry rosterEntry = null;
        List<RosterEntry> selectedRosterEntries = SelectionRetrieverFactory
            .getSelectionRetriever(RosterEntry.class).getSelection();
        if (selectedRosterEntries.size() == 1) {
            rosterEntry = selectedRosterEntries.get(0);
        }

        if (rosterEntry == null) {
            log.error("RosterEntry should not be null at this point!");
            return;
        }

        if (sessionManager != null) {
            // Is the chosen user currently in the session?
            ISarosSession sarosSession = sessionManager.getSarosSession();
            String entryJid = rosterEntry.getUser();

            if (sarosSession != null) {
                for (User p : sarosSession.getParticipants()) {
                    String pJid = p.getJID().getBase();

                    // If so, stop the deletion from completing
                    if (entryJid.equals(pJid)) {
                        MessageDialog.openError(null,
                            "Cannot delete a buddy in the session",
                            DELETE_ERROR_IN_SESSION);
                        return;
                    }
                }
            }
        }

        if (MessageDialog.openQuestion(null, "Confirm Delete",
            "Are you sure you want to delete " + toString(rosterEntry)
                + " from your buddies?")) {

            try {
                RosterUtils
                    .removeFromRoster(saros.getConnection(), rosterEntry);
            } catch (XMPPException e) {
                log.error("Could not delete buddy " + toString(rosterEntry)
                    + ":", e);
            }
        }
    }

}
