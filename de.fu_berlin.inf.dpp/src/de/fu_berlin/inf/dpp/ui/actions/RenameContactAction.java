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
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.jivesoftware.smack.RosterEntry;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Renames the nickname of the selected roster entry.
 * 
 * @author rdjemili
 */
public class RenameContactAction extends SelectionProviderAction {

    private static final Logger log = Logger
        .getLogger(RenameContactAction.class.getName());

    private RosterEntry rosterEntry;

    public RenameContactAction(ISelectionProvider provider) {
        super(provider, "Rename...");
        selectionChanged((IStructuredSelection) provider.getSelection());

        setToolTipText("Set the nickname of this contact.");
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Util.runSafeSync(log, new Runnable() {
            public void run() {
                runRename();
            }
        });
    }

    public void runRename() {

        assert this.rosterEntry != null : "Action should only be run if a rosterEntry is selected";

        Shell shell = EditorAPI.getShell();

        assert shell != null : "Action should not be run if the display is disposed";

        String message;
        if (this.rosterEntry.getName() == null) {
            message = "Enter the new nickname of this contact '"
                + this.rosterEntry.getUser() + "':";
        } else {
            message = "Enter the new nickname of this contact '"
                + this.rosterEntry.getUser() + "' with current nickname '"
                + this.rosterEntry.getName() + "':";
        }

        InputDialog dialog = new InputDialog(shell, "Set new nickname",
            message, this.rosterEntry.getName(), null);

        if (dialog.open() == Window.OK) {
            String newName = dialog.getValue();
            if (newName.length() == 0) {
                this.rosterEntry.setName(null);
            } else {
                this.rosterEntry.setName(newName);
            }
        }
    }

    public RosterEntry getSelectedForRename(IStructuredSelection selection) {

        if (selection.size() != 1)
            return null;

        Object selected = selection.getFirstElement();

        if (selected instanceof RosterEntry) {
            RosterEntry result = (RosterEntry) selected;

            if (!result.getUser().equals(Saros.getDefault().getMyJID())) {
                return result;
            }
        }
        return null;

    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        this.rosterEntry = getSelectedForRename(selection);
        setEnabled(this.rosterEntry != null);
    }
}
