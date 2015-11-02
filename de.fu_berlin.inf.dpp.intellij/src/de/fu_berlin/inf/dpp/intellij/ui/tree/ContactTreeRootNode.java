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
        DefaultMutableTreeNode node;

        Roster roster = connectionService.getRoster();
        //add contacts
        for (RosterEntry contactEntry : roster.getEntries()) {
            ContactInfo contactInfo = new ContactInfo(contactEntry);
            Presence presence = roster.getPresence(contactEntry.getUser());

            if (presence.getType() == Presence.Type.available) {
                contactInfo.setOnline(true);
            } else {
                contactInfo.setOnline(false);
            }

            node = new DefaultMutableTreeNode(contactInfo);

            add(node);

            contactMap.put(contactInfo.getTitle(), contactInfo);
        }
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

        contactInfo.setHidden(true);
        for (int i = 0; i < getChildCount(); i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) getChildAt(
                i);
            if (contactInfo.equals(node.getUserObject())) {
                treeModel.removeNodeFromParent(node);
                node.setUserObject(null);
                break;
            }
        }

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
        //NOP
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

        ContactInfo info = contactMap.get(user);
        if (info != null) {
            if (presence.getType() == Presence.Type.available) {
                info.setOnline(true);
            } else {
                info.setOnline(false);
            }

            Runnable action = new Runnable() {
                @Override
                public void run() {
                    treeView.collapseRow(2);
                    treeView.expandRow(2);
                }
            };

            UIUtil.invokeAndWaitIfNeeded(action);
        }
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
