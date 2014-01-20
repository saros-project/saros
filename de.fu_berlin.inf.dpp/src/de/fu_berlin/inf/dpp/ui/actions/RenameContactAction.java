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
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.RosterEntry;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.util.ThreadUtils;

/**
 * Renames the nickname of the selected roster entry.
 * 
 * @author rdjemili
 */
public class RenameContactAction extends Action {

    private static final Logger log = Logger
        .getLogger(RenameContactAction.class.getName());

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
    protected Saros saros;

    public RenameContactAction() {
        super(Messages.RenameContactAction_title);
        setToolTipText(Messages.RenameContactAction_tooltip);
        setImageDescriptor(ImageManager.ETOOL_EDIT);

        SarosPluginContext.initComponent(this);

        saros.getSarosNet().addListener(connectionListener);
        SelectionUtils.getSelectionService().addSelectionListener(
            selectionListener);
        updateEnablement();
    }

    protected void updateEnablement() {
        try {
            List<JID> contacts = SelectionRetrieverFactory
                .getSelectionRetriever(JID.class).getSelection();
            this.setEnabled(saros.getSarosNet().isConnected()
                && contacts.size() == 1);
        } catch (NullPointerException e) {
            this.setEnabled(false);
        } catch (Exception e) {
            if (!PlatformUI.getWorkbench().isClosing())
                log.error("Unexcepted error while updating enablement", e); //$NON-NLS-1$
        }
    }

    @Override
    public void run() {
        ThreadUtils.runSafeSync(log, new Runnable() {
            @Override
            public void run() {
                RosterEntry rosterEntry = null;
                List<RosterEntry> selectedRosterEntries = SelectionRetrieverFactory
                    .getSelectionRetriever(RosterEntry.class).getSelection();
                if (selectedRosterEntries.size() == 1) {
                    /*
                     * TODO Why forbid renaming self? Is the own entry displayed
                     * at all?
                     */
                    // Compare the plain-JID portion of the XMPP address
                    if (!new JID(selectedRosterEntries.get(0).getUser())
                        .equals(saros.getSarosNet().getJID())) {
                        rosterEntry = selectedRosterEntries.get(0);
                    }
                }

                if (rosterEntry == null) {
                    log.error("RosterEntry should not be null at this point!"); //$NON-NLS-1$
                    return;
                }

                Shell shell = SWTUtils.getShell();

                assert shell != null : "Action should not be run if the display is disposed"; //$NON-NLS-1$

                String message = MessageFormat.format(
                    Messages.RenameContactAction_rename_message,
                    rosterEntry.getUser());

                if (rosterEntry.getName() != null
                    && !rosterEntry.getName().equals(rosterEntry.getUser())) {
                    message += MessageFormat
                        .format(
                            Messages.RenameContactAction_rename_current_nickname_message,
                            rosterEntry.getName());
                }
                message += ":"; //$NON-NLS-1$

                InputDialog dialog = new InputDialog(shell,
                    Messages.RenameContactAction_new_nickname_dialog_title,
                    message, rosterEntry.getName(), null);

                if (dialog.open() == Window.OK) {
                    String newName = dialog.getValue();
                    rosterEntry.setName(newName.length() == 0 ? null : newName);
                }
            }
        });
    }

    public void dispose() {
        SelectionUtils.getSelectionService().removeSelectionListener(
            selectionListener);
        saros.getSarosNet().removeListener(connectionListener);
    }
}
