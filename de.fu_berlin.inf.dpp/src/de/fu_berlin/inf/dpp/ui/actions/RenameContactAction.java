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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.jivesoftware.smack.RosterEntry;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.ui.RosterView.TreeItem;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Renames the nickname of the selected roster entry.
 * 
 * @author rdjemili
 */
public class RenameContactAction extends SelectionProviderAction {

    private static final Logger log = Logger
        .getLogger(RenameContactAction.class.getName());

    private RosterEntry rosterEntry;

    protected Saros saros;

    public RenameContactAction(Saros saros, ISelectionProvider provider) {
        super(provider, "Rename...");

        this.saros = saros;
        selectionChanged((IStructuredSelection) provider.getSelection());

        setToolTipText("Set the nickname of this buddy.");
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Utils.runSafeSync(log, new Runnable() {
            public void run() {
                runRename();
            }
        });
    }

    protected void runRename() {
        RosterEntry entry = rosterEntry;

        assert entry != null : "Action should only be run if a rosterEntry is selected";

        Shell shell = EditorAPI.getShell();

        assert shell != null : "Action should not be run if the display is disposed";

        String message = "Enter the new nickname of this buddy '"
            + entry.getUser() + "'";
        if (entry.getName() != null) {
            message += " with current nickname '" + entry.getName() + "'";
        }
        message += ":";

        InputDialog dialog = new InputDialog(shell, "Set new nickname",
            message, entry.getName(), null);

        if (dialog.open() == Window.OK) {
            String newName = dialog.getValue();
            entry.setName(newName.length() == 0 ? null : newName);
        }

        TreeViewer parent = (TreeViewer) getSelectionProvider();
        parent.refresh(true);
    }

    protected RosterEntry getSelectedForRename(IStructuredSelection selection) {

        if (selection.size() != 1)
            return null;

        TreeItem selected = (TreeItem) selection.getFirstElement();
        RosterEntry result = selected.getRosterEntry();
        /*
         * TODO Why forbid renaming self? Is the own entry displayed at all?
         */
        // Compare the plain-JID portion of the XMPP address
        if (result != null
            && !new JID(result.getUser()).equals(saros.getMyJID())) {

            return result;
        }
        return null;
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        this.rosterEntry = getSelectedForRename(selection);
        setEnabled(this.rosterEntry != null);
    }
}
