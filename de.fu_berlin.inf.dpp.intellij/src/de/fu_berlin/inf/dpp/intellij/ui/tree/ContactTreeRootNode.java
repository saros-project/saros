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

import com.intellij.util.ui.UIUtil;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.intellij.ui.util.IconManager;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.net.xmpp.roster.IRosterListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;
import org.picocontainer.annotations.Inject;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Root node for the tree of contacts, that a user has in their contact list.
 * <p/>
 * It registers as {@link IRosterListener} and keeps the displayed
 * contacts in sync with the server.
 */
public class ContactTreeRootNode extends DefaultMutableTreeNode
    implements IRosterListener {
    public static final String TREE_TITLE = "Contacts";

    private final SessionAndContactsTreeView treeView;
    private final Map<String, ContactInfo> contactMap = new HashMap<String, ContactInfo>();
    private final DefaultTreeModel treeModel;

    @Inject
    private XMPPConnectionService connectionService;

    public ContactTreeRootNode(SessionAndContactsTreeView treeView) {
        super(treeView);
        SarosPluginContext.initComponent(this);

        this.treeView = treeView;
        treeModel = (DefaultTreeModel) this.treeView.getModel();
        setUserObject(TREE_TITLE);
    }

    /**
     * Creates a tree root node and adds a {@link DefaultMutableTreeNode} containing
     * a {@link de.fu_berlin.inf.dpp.intellij.ui.tree.ContactTreeRootNode.ContactInfo}
     * as content.
     */
    public void createContactNodes() {
        removeAllChildren();

        Roster roster = connectionService.getRoster();
        //add contacts
        for (RosterEntry contactEntry : roster.getEntries()) {
            addContact(contactEntry,
                roster.getPresence(contactEntry.getUser()));
        }
    }

    private void addContact(RosterEntry contactEntry, Presence presence) {
        ContactInfo contactInfo = new ContactInfo(contactEntry);

        if (presence.getType() == Presence.Type.available) {
            contactInfo.setOnline(true);
        } else {
            contactInfo.setOnline(false);
        }

        contactMap.put(contactInfo.getTitle(), contactInfo);

        final DefaultMutableTreeNode node = new DefaultMutableTreeNode(
            contactInfo);

        final MutableTreeNode parent = this;

        UIUtil.invokeAndWaitIfNeeded(new Runnable() {

            @Override
            public void run() {
                treeModel.insertNodeInto(node, parent, getChildCount());
            }
        });
    }

    /**
     * Adds the node of the given contact to the tree model.
     */
    public void showContact(final String name) {
        ContactInfo contactInfo = contactMap.get(name);
        if (contactInfo == null || !contactInfo.isHidden()) {
            return;
        }

        contactInfo.setHidden(false);
        MutableTreeNode node = new DefaultMutableTreeNode(contactInfo);

        add(node);
    }

    /**
     * Removes the node of the given contact from the tree.
     */
    public void hideContact(final String name) {
        ContactInfo contactInfo = contactMap.get(name);

        if (contactInfo == null || contactInfo.isHidden()) {
            return;
        }

        MutableTreeNode node = getContactNode(contactInfo);

        if (node == null) {
            return;
        }

        treeModel.removeNodeFromParent(node);
        node.setUserObject(null);

        contactInfo.setHidden(true);
    }

    private MutableTreeNode getContactNode(final ContactInfo contact) {
        for (int i = 0; i < getChildCount(); i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) getChildAt(
                i);
            if (contact.equals(node.getUserObject())) {
                return node;
            }
        }

        return null;
    }

    /**
     * Removes all contacts.
     */
    public void removeContacts() {
        removeAllChildren();
        contactMap.clear();
    }

    @Override
    public void rosterChanged(Roster roster) {
        //NOP
    }

    @Override
    public void entriesAdded(Collection<String> addresses) {
        Roster roster = connectionService.getRoster();

        for (String address : addresses) {
            addContact(roster.getEntry(address), roster.getPresence(address));
        }
    }

    @Override
    public void entriesUpdated(Collection<String> addresses) {
        //NOP
    }

    @Override
    public void entriesDeleted(Collection<String> addresses) {
        //NOP
    }

    /**
     * Updates online / offline status of a contact and updates the tree view.
     */
    @Override
    public void presenceChanged(Presence presence) {
        String user = new JID(presence.getFrom()).getBareJID().toString();
        final ContactInfo info = contactMap.get(user);

        if (info == null)
            return;

        if (presence.getType() == Presence.Type.available) {
            info.setOnline(true);
        } else {
            info.setOnline(false);
        }

        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
            @Override
            public void run() {
                treeModel.nodeChanged(getContactNode(info));
            }
        });
    }

    /**
     * Class to keep contact info
     */
    protected class ContactInfo extends LeafInfo {

        private final String status;
        private final RosterEntry rosterEntry;
        private boolean isOnline;
        private boolean isHidden = false;

        private ContactInfo(RosterEntry rosterEntry) {
            super(rosterEntry.getUser());
            this.rosterEntry = rosterEntry;
            status = rosterEntry.getStatus() == null ?
                null :
                rosterEntry.getStatus().toString();
        }

        public RosterPacket.ItemStatus getStatus() {
            return rosterEntry.getStatus();
        }

        public RosterEntry getRosterEntry() {
            return rosterEntry;
        }

        public boolean isOnline() {
            return isOnline;
        }

        public boolean isHidden() {
            return isHidden;
        }

        public void setHidden(boolean isHidden) {
            this.isHidden = isHidden;
        }

        public void setOnline(boolean isOnline) {
            this.isOnline = isOnline;
            if (isOnline) {
                setIcon(IconManager.CONTACT_ONLINE_ICON);
            } else {
                setIcon(IconManager.CONTACT_OFFLINE_ICON);
            }
        }

        @Override
        public String toString() {
            return status == null ? title : title + " (" + status + ")";
        }
    }

}
