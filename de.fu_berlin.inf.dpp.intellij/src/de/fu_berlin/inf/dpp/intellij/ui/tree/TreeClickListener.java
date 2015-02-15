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
import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.User;
import org.picocontainer.annotations.Inject;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Tree click listener for showing {@link ContactPopMenu} or {@link SessionPopMenu}.
 */
public class TreeClickListener extends MouseAdapter {
    private JTree tree;

    @Inject
    private ISarosSessionManager sessionManager;

    @Inject
    private Saros saros;

    public TreeClickListener(SessionAndContactsTreeView treeView) {
        SarosPluginContext.initComponent(this);
        tree = treeView;
    }

    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            doPop(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            doPop(e);
        }
    }

    private void doPop(MouseEvent e) {
        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
        if (selPath == null || selPath.getParentPath() == null) {
            return;
        }

        if (selPath.getLastPathComponent() instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath
                .getLastPathComponent();
            if (node
                .getUserObject() instanceof ContactTreeRootNode.ContactInfo) {
                ContactTreeRootNode.ContactInfo contactInfo = (ContactTreeRootNode.ContactInfo) node
                    .getUserObject();
                if (contactInfo.isOnline()) {
                    ContactPopMenu menu = new ContactPopMenu(saros,
                        contactInfo);
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            } else if (
                node.getUserObject() instanceof SessionTreeRootNode.UserInfo
                    || node
                    .getUserObject() instanceof SessionTreeRootNode.SessionInfo) {
                SessionTreeRootNode.UserInfo userInfo = (SessionTreeRootNode.UserInfo) node
                    .getUserObject();
                User user = userInfo.getUser();
                if (!user
                    .equals(sessionManager.getSarosSession().getLocalUser())) {
                    SessionPopMenu menu = new SessionPopMenu(user);
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }
    }
}
