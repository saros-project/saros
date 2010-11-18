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
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;

import de.fu_berlin.inf.dpp.communication.multiUserChat.MessagingManager;
import de.fu_berlin.inf.dpp.ui.RosterView.TreeItem;
import de.fu_berlin.inf.dpp.ui.SarosUI;

public class MessagingAction extends SelectionProviderAction {

    private static final Logger log = Logger.getLogger(MessagingAction.class
        .getName());

    protected MessagingManager messagingManager;

    public MessagingAction(MessagingManager messagingManager,
        ISelectionProvider provider) {
        super(provider, "Send instant message..");

        this.messagingManager = messagingManager;
        selectionChanged((IStructuredSelection) provider.getSelection());

        setToolTipText("Start a IM session with this user");
        setImageDescriptor(SarosUI.getImageDescriptor("icons/comment.png"));
    }

    @Override
    public void run() {
        log.error("Messaging action not implemented");
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {

        setEnabled(selection.size() == 1
            && ((TreeItem) selection.getFirstElement()).getRosterEntry() != null);

        // TODO disable if user == self
    }
}
