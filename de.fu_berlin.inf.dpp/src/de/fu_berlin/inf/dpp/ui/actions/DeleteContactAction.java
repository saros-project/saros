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

import java.text.MessageFormat;
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
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.util.RosterUtils;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.util.Utils;

public class DeleteContactAction extends Action implements Disposable {

    private static final Logger log = Logger
        .getLogger(DeleteContactAction.class.getName());

    protected IConnectionListener connectionListener = new IConnectionListener() {
        @Override
        public void connectionStateChanged(Connection connection,
            final ConnectionState newState) {
            updateEnablement();
        }
    };

    protected ISelectionListener selectionListener = new ISelectionListener() {
        @Override
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            updateEnablement();
        }
    };

    @Inject
    protected SarosNet sarosNet;

    @Inject
    protected ISarosSessionManager sessionManager;

    protected final String DELETE_ERROR_IN_SESSION = Messages.DeleteContactAction_delete_error_in_session;

    public DeleteContactAction() {
        super(Messages.DeleteContactAction_title);
        setToolTipText(Messages.DeleteContactAction_tooltip);

        IWorkbench workbench = PlatformUI.getWorkbench();
        setImageDescriptor(workbench.getSharedImages().getImageDescriptor(
            ISharedImages.IMG_TOOL_DELETE));

        SarosPluginContext.initComponent(this);

        sarosNet.addListener(connectionListener);
        SelectionUtils.getSelectionService().addSelectionListener(
            selectionListener);
        updateEnablement();
    }

    protected void updateEnablement() {
        try {
            List<JID> buddies = SelectionRetrieverFactory
                .getSelectionRetriever(JID.class).getSelection();
            this.setEnabled(sarosNet.isConnected() && buddies.size() == 1);
        } catch (NullPointerException e) {
            this.setEnabled(false);
        } catch (Exception e) {
            if (!PlatformUI.getWorkbench().isClosing())
                log.error("Unexcepted error while updating enablement", e); //$NON-NLS-1$
        }
    }

    public static String toString(RosterEntry entry) {
        StringBuilder sb = new StringBuilder();
        String name = entry.getName();
        if (name != null && name.trim().length() > 0) {
            sb.append(Messages.DeleteContactAction_name_begin_deco)
                .append(name)
                .append(Messages.DeleteContactAction_name_end_deco);
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
            @Override
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
            log.error("RosterEntry should not be null at this point!"); //$NON-NLS-1$
            return;
        }

        if (sessionManager != null) {
            // Is the chosen user currently in the session?
            ISarosSession sarosSession = sessionManager.getSarosSession();
            String entryJid = rosterEntry.getUser();

            if (sarosSession != null) {
                for (User p : sarosSession.getUsers()) {
                    String pJid = p.getJID().getBase();

                    // If so, stop the deletion from completing
                    if (entryJid.equals(pJid)) {
                        MessageDialog.openError(null,
                            Messages.DeleteContactAction_error_title,
                            DELETE_ERROR_IN_SESSION);
                        return;
                    }
                }
            }
        }

        if (MessageDialog.openQuestion(null,
            Messages.DeleteContactAction_confirm_title, MessageFormat.format(
                Messages.DeleteContactAction_confirm_message,
                toString(rosterEntry)))) {

            try {
                RosterUtils.removeFromRoster(sarosNet.getConnection(),
                    rosterEntry);
            } catch (XMPPException e) {
                log.error("could not delete contact " + toString(rosterEntry) //$NON-NLS-1$
                    + ":", e); //$NON-NLS-1$
            }
        }
    }

    @Override
    public void dispose() {
        SelectionUtils.getSelectionService().removeSelectionListener(
            selectionListener);
        sarosNet.removeListener(connectionListener);
    }
}
