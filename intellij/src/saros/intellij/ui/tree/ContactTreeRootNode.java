package saros.intellij.ui.tree;

import com.intellij.util.ui.UIUtil;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.intellij.ui.util.IconManager;
import saros.net.xmpp.JID;
import saros.net.xmpp.roster.IRosterListener;
import saros.net.xmpp.roster.RosterTracker;

/**
 * Root node for the tree of contacts, that a user has in their contact list.
 *
 * <p>It registers as {@link IRosterListener} and keeps the displayed contacts in sync with the
 * server.
 */

/*
 * TODO the visual representation of the roster has
 * the same issue as the Eclipse counterpart, it cannot
 * handle multiple JIDs with different resources
 */
public class ContactTreeRootNode extends DefaultMutableTreeNode implements IRosterListener {

  private static final long serialVersionUID = 1L;

  public static final String TREE_TITLE = "Contacts";

  private final SessionAndContactsTreeView treeView;

  private final Map<JID, ContactInfo> visibleContactsMap = new HashMap<>();
  private final Map<JID, ContactInfo> hiddenContactsMap = new HashMap<>();

  private final DefaultTreeModel treeModel;

  @Inject private RosterTracker rosterTracker;

  private Roster roster;

  public ContactTreeRootNode(SessionAndContactsTreeView treeView) {
    super(treeView);
    SarosPluginContext.initComponent(this);

    this.treeView = treeView;
    treeModel = (DefaultTreeModel) this.treeView.getModel();
    setUserObject(TREE_TITLE);

    rosterTracker.addRosterListener(this);
    rosterChanged(rosterTracker.getRoster());
  }

  /**
   * Adds the node of the given contact to the tree model. The contact will be visible. Does nothing
   * if the contact is already present(either visible, or hidden).
   */
  private void addContact(String jid) {
    ContactInfo contactInfo = getContact(jid);

    if (contactInfo != null) return;

    if (roster == null) return;

    // Smack BUG, does not work with RQJIDs
    RosterEntry entry = roster.getEntry(toBareJID(jid).toString());

    if (entry == null) return;

    contactInfo = new ContactInfo(entry);

    visibleContactsMap.put(toBareJID(jid), contactInfo);

    Presence presence = roster.getPresence(jid);

    contactInfo.setOnline(presence.getType() == Presence.Type.available);

    MutableTreeNode node = new DefaultMutableTreeNode(contactInfo);

    add(node);
  }

  private ContactInfo getContact(final String jid) {
    ContactInfo info;

    info = visibleContactsMap.get(toBareJID(jid));

    if (info == null) info = hiddenContactsMap.get(toBareJID(jid));

    return info;
  }

  /**
   * Removes the node of the given contact from the tree. If the contact is already not visible it
   * will still be removed.
   *
   * <p>Does nothing if the contact is not present(either visible, or hidden).
   */
  private void removeContact(final String jid) {
    ContactInfo contactInfo;

    contactInfo = hiddenContactsMap.remove(toBareJID(jid));

    // the info is already not in the tree
    if (contactInfo != null) return;

    contactInfo = visibleContactsMap.remove(toBareJID(jid));

    if (contactInfo == null) return;

    for (int i = 0; i < getChildCount(); i++) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) getChildAt(i);

      if (contactInfo.equals(node.getUserObject())) {
        treeModel.removeNodeFromParent(node);
        node.setUserObject(null);
        break;
      }
    }
  }

  /**
   * Shows the contact in the current tree, i.e it is displayed.
   *
   * @param jid
   */
  public void showContact(JID jid) {
    ContactInfo info = hiddenContactsMap.remove(jid.getBareJID());

    if (info == null) return;

    addContact(jid.toString());
  }

  /**
   * Hides the contact in the current tree, i.e it is not displayed.
   *
   * @param jid
   */
  public void hideContact(JID jid) {
    ContactInfo info = visibleContactsMap.get(jid.getBareJID());

    if (info == null) return;

    removeContact(jid.toString());

    hiddenContactsMap.put(jid.getBareJID(), info);
  }

  /** Removes all contacts. */
  public void removeContacts() {
    removeAllChildren();
    visibleContactsMap.clear();
    hiddenContactsMap.clear();
  }

  @Override
  public void rosterChanged(final Roster roster) {
    UIUtil.invokeLaterIfNeeded(
        new Runnable() {
          @Override
          public void run() {
            ContactTreeRootNode.this.roster = roster;

            if (roster == null) {
              removeContacts();
              return;
            }

            for (RosterEntry entry : roster.getEntries()) addContact(entry.getUser());
          }
        });
  }

  @Override
  public void entriesAdded(final Collection<String> addresses) {
    UIUtil.invokeLaterIfNeeded(
        new Runnable() {
          @Override
          public void run() {
            for (String address : addresses) addContact(address);
          }
        });
  }

  @Override
  public void entriesUpdated(Collection<String> addresses) {
    // TODO
  }

  @Override
  public void entriesDeleted(final Collection<String> addresses) {
    UIUtil.invokeLaterIfNeeded(
        new Runnable() {
          @Override
          public void run() {
            for (String address : addresses) removeContact(address);
          }
        });
  }

  /** Updates online / offline status of a contact and updates the tree view. */
  @Override
  public void presenceChanged(final Presence presence) {

    Runnable action =
        new Runnable() {
          @Override
          public void run() {

            String user = presence.getFrom();

            if (user == null) return;

            ContactInfo info = getContact(user);

            if (info == null) return;

            info.setOnline(presence.getType() == Presence.Type.available);
            treeView.collapseRow(2);
            treeView.expandRow(2);
          }
        };

    UIUtil.invokeLaterIfNeeded(action);
  }

  /** Class to keep contact info */
  static class ContactInfo extends LeafInfo {

    private final String status;
    private final RosterEntry rosterEntry;
    private boolean isOnline;

    private boolean visible = true;

    private ContactInfo(RosterEntry rosterEntry) {
      super(rosterEntry.getUser());
      this.rosterEntry = rosterEntry;

      status = rosterEntry.getStatus() == null ? null : rosterEntry.getStatus().toString();
    }

    public RosterEntry getRosterEntry() {
      return rosterEntry;
    }

    public boolean isOnline() {
      return isOnline;
    }

    private boolean isVisible() {
      return visible;
    }

    private void setVisible(boolean visible) {
      this.visible = visible;
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

  private static JID toBareJID(String jid) {
    return new JID(jid).getBareJID();
  }
}
