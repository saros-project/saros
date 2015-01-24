/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.intellij.ui.tree;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.session.User;
import org.picocontainer.annotations.Inject;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Session pop-up menu that displays the option to follow a participant.
 */
class SessionPopMenu extends JPopupMenu {

    @Inject
    private EditorManager editorManager;

    public SessionPopMenu(final User user) {
        SarosPluginContext.initComponent(this);
        JMenuItem menuItemFollowParticipant = new JMenuItem(
            "Follow participant");
        menuItemFollowParticipant.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorManager.setFollowing(user);
            }
        });
        add(menuItemFollowParticipant);
    }
}
