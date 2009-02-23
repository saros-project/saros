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
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.jivesoftware.smack.RosterEntry;

import de.fu_berlin.inf.dpp.util.Util;

/**
 * Renames the nickname of the selected roster entry.
 * 
 * @author rdjemili
 */
public class RenameContactAction extends SelectionProviderAction {

    private static final Logger log = Logger
        .getLogger(RenameContactAction.class.getName());

    private class InputValidator implements IInputValidator {
        public String isValid(String newText) {
            // TODO Review this
            return null;
        }
    }

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
        Shell shell = Display.getDefault().getActiveShell();
        if ((shell == null) || (this.rosterEntry == null)) {
            return;
        }

        InputDialog dialog = new InputDialog(shell, "Set new nickname",
            "Enter the new nickname of this contact '"
                + this.rosterEntry.getName() + "' ('"
                + this.rosterEntry.getUser() + "'):", this.rosterEntry
                .getName(), new InputValidator());

        if (dialog.open() == Window.OK) {
            String name = (dialog.getValue().length() == 0) ? "" : dialog
                .getValue();
            this.rosterEntry.setName(name);
        }
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();

        if ((selection.size() == 1) && (selected instanceof RosterEntry)) {
            this.rosterEntry = (RosterEntry) selected;
            setEnabled(true);
        } else {
            this.rosterEntry = null;
            setEnabled(false);
        }

        // TODO disable if user == self
    }
}
