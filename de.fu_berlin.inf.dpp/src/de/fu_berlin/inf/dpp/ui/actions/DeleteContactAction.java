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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.util.Util;

public class DeleteContactAction extends SelectionProviderAction {

    private static final Logger log = Logger
        .getLogger(DeleteContactAction.class.getName());

    private RosterEntry rosterEntry;

    public DeleteContactAction(ISelectionProvider provider) {
        super(provider, "Delete");
        selectionChanged((IStructuredSelection) provider.getSelection());

        setToolTipText("Delete this contact.");

        IWorkbench workbench = Saros.getDefault().getWorkbench();
        setImageDescriptor(workbench.getSharedImages().getImageDescriptor(
            ISharedImages.IMG_TOOL_DELETE));
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
        Util.runSafeSync(log, new Runnable() {
            public void run() {
                runDeleteAction();
            }
        });
    }

    public void runDeleteAction() {

        Shell shell = Display.getDefault().getActiveShell();
        if ((shell == null) || (this.rosterEntry == null)) {
            return;
        }

        if (MessageDialog.openQuestion(shell, "Confirm Delete",
            "Are you sure you want to delete " + toString(this.rosterEntry)
                + " from your roster?")) {

            try {
                Saros.getDefault().removeContact(this.rosterEntry);
            } catch (XMPPException e) {
                log.error("Could not delete contact "
                    + toString(this.rosterEntry) + ":", e);
            }
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
    }
}
