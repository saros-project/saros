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

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.jivesoftware.smack.RosterEntry;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.ui.SarosUI;

public class MessagingAction extends SelectionProviderAction {

    public MessagingAction(ISelectionProvider provider) {
        super(provider, "Send instant message..");
        selectionChanged((IStructuredSelection) provider.getSelection());

        setToolTipText("Start a IM session with this user");
        setImageDescriptor(SarosUI.getImageDescriptor("icons/comment.png"));
    }

    @Override
    public void run() {
        // TODO This cannot work
        Saros.getDefault().getMessagingManager();
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();

        if ((selection.size() == 1) && (selected instanceof RosterEntry)) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }

        // TODO disable if user == self
    }
}
