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
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.jivesoftware.smack.RosterEntry;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Renames the nickname of the selected roster entry.
 * 
 * @author rdjemili
 */
public class RenameContactAction extends Action {

    private static final Logger log = Logger
        .getLogger(RenameContactAction.class.getName());

    protected Saros saros;

    public RenameContactAction(Saros saros) {
        super("Rename...");
        this.saros = saros;

        SelectionUtils.getSelectionService().addSelectionListener(
            new ISelectionListener() {
                public void selectionChanged(IWorkbenchPart part,
                    ISelection selection) {
                    List<JID> buddies = SelectionRetrieverFactory
                        .getSelectionRetriever(JID.class).getSelection();
                    setEnabled(buddies.size() == 1);
                }
            });
        setToolTipText("Set the nickname of this buddy.");
    }

    @Override
    public void run() {
        Utils.runSafeSync(log, new Runnable() {
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
                        .equals(saros.getMyJID())) {
                        rosterEntry = selectedRosterEntries.get(0);
                    }
                }

                if (rosterEntry == null) {
                    log.error("RosterEntry should not be null at this point!");
                    return;
                }

                Shell shell = EditorAPI.getShell();

                assert shell != null : "Action should not be run if the display is disposed";

                String message = "Enter the new nickname of this buddy '"
                    + rosterEntry.getUser() + "'";
                if (rosterEntry.getName() != null) {
                    message += " with current nickname '"
                        + rosterEntry.getName() + "'";
                }
                message += ":";

                InputDialog dialog = new InputDialog(shell, "Set new nickname",
                    message, rosterEntry.getName(), null);

                if (dialog.open() == Window.OK) {
                    String newName = dialog.getValue();
                    rosterEntry.setName(newName.length() == 0 ? null : newName);
                }
            }
        });
    }
}
