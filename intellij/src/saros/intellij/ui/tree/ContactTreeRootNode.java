package saros.intellij.ui.tree;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ui.UIUtil;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import saros.SarosPluginContext;
import saros.intellij.ui.util.IconManager;
import saros.intellij.ui.views.SarosMainPanelView;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.IContactsUpdate;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.repackaged.picocontainer.annotations.Inject;

/**
 * Root node for the tree of contacts, that a user has in their contact list.
 *
 * <p><b>NOTE:</b>This component and any component added here must be correctly torn down when the
 * project the components belong to is closed. See {@link SarosMainPanelView}.
 */
public class ContactTreeRootNode extends DefaultMutableTreeNode implements Disposable {
  private static final long serialVersionUID = 1L;
  private static final String TREE_TITLE = "Contacts";

  private final DefaultTreeModel treeModel;
  private final SessionAndContactsTreeView treeView;

  private final Map<XMPPContact, ContactInfo> visibleContactsMap = new HashMap<>();
  private final Map<XMPPContact, ContactInfo> hiddenContactsMap = new HashMap<>();

  @Inject private XMPPContactsService contactsService;

  private final IContactsUpdate contactsUpdate =
      new IContactsUpdate() {

        @Override
        public void update(Optional<XMPPContact> contact, UpdateType updateType) {
          UIUtil.invokeLaterIfNeeded(
              () -> {
                if (updateType == UpdateType.ADDED) {
                  contact.ifPresent(ContactTreeRootNode.this::addContact);
                } else if (updateType == UpdateType.NOT_CONNECTED) {
                  removeContacts();
                } else if (updateType == UpdateType.REMOVED) {
                  contact.ifPresent(ContactTreeRootNode.this::removeContact);
                } else if (updateType == UpdateType.STATUS) {
                  contact.ifPresent(ContactTreeRootNode.this::updateContactStatus);
                }
              });
        }
      };

  public ContactTreeRootNode(SessionAndContactsTreeView treeView) {
    super(treeView);

    Disposer.register(treeView, this);
    SarosPluginContext.initComponent(this);

    this.treeView = treeView;
    treeModel = (DefaultTreeModel) this.treeView.getModel();
    setUserObject(TREE_TITLE);

    contactsService.addListener(contactsUpdate);
  }

  /**
   * Adds all contacts of the currently connected XMPP account to the contacts list. Covers cases
   * where the component is initialized when there is already a connected account.
   */
  void setInitialState() {
    contactsService.getAllContacts().forEach(this::addContact);
  }

  @Override
  public void dispose() {
    contactsService.removeListener(contactsUpdate);
  }

  /**
   * Adds the node of the given contact to the tree model. The contact will be visible. Does nothing
   * if the contact is already present(either visible, or hidden).
   *
   * <p>If a new entry is added, the list of displayed contacts is sorted.
   *
   * @param contact the added contact
   * @see #sortEntries()
   */
  private void addContact(XMPPContact contact) {
    ContactInfo contactInfo = getContact(contact);
    if (contactInfo != null) return;

    boolean noContactsToDisplayBefore = visibleContactsMap.isEmpty();

    contactInfo = new ContactInfo(contact);
    visibleContactsMap.put(contact, contactInfo);

    add(new DefaultMutableTreeNode(contactInfo));

    sortEntries();

    treeView.reloadModelNode(this);

    if (noContactsToDisplayBefore) {
      treeView.expandPath(new TreePath(getPath()));
    }
  }

  /**
   * Updates status icon of the node for the given contact and re-sorts the list of displayed
   * contacts.
   *
   * @param contact the contact whose status was updated
   * @see #sortEntries()
   */
  private void updateContactStatus(XMPPContact contact) {
    ContactInfo contactInfo = getContact(contact);

    if (contactInfo == null) {
      return;
    }

    contactInfo.updateStatusIcon();

    sortEntries();

    treeView.reloadModelNode(this);
  }

  /**
   * Sorts the nodes displayed in the contacts view. The nodes are sorted as separate groups for
   * online and offline contacts. Online contacts are displayed first, offline contacts second. The
   * contacts in each of the two groups are sorted alphabetically.
   */
  private void sortEntries() {
    ((Vector<?>) this.children)
        .sort(
            (Comparator<Object>)
                (o1, o2) -> {
                  DefaultMutableTreeNode n1 = (DefaultMutableTreeNode) o1;
                  DefaultMutableTreeNode n2 = (DefaultMutableTreeNode) o2;

                  ContactInfo c1 = (ContactInfo) n1.getUserObject();
                  ContactInfo c2 = (ContactInfo) n2.getUserObject();

                  if (c1.isOnline() && !c2.isOnline()) {
                    return -1;
                  } else if (!c1.isOnline() && c2.isOnline()) {
                    return 1;
                  }

                  return c1.title.compareTo(c2.title);
                });
  }

  private ContactInfo getContact(XMPPContact contact) {
    ContactInfo info = visibleContactsMap.get(contact);

    if (info == null) {
      info = hiddenContactsMap.get(contact);
    }

    return info;
  }

  /**
   * Removes the node of the given contact from the tree. If the contact is already not visible it
   * will still be removed.
   *
   * <p>Does nothing if the contact is not present(either visible, or hidden).
   */
  private void removeContact(XMPPContact contact) {
    ContactInfo contactInfo = hiddenContactsMap.remove(contact);

    // the info is already not in the tree
    if (contactInfo != null) return;

    contactInfo = visibleContactsMap.remove(contact);

    if (contactInfo == null) return;

    for (int i = 0; i < getChildCount(); i++) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) getChildAt(i);

      if (contactInfo.equals(node.getUserObject())) {
        treeModel.removeNodeFromParent(node);
        node.setUserObject(null);
        break;
      }
    }

    treeView.reloadModelNode(this);
  }

  /**
   * Shows the contact in the current tree, i.e it is displayed.
   *
   * @param jid
   */
  void showContact(JID jid) {
    XMPPContact contact = contactsService.getContact(jid.getBase()).orElse(null);
    if (contact == null) return;

    ContactInfo info = hiddenContactsMap.remove(contact);
    if (info == null) return;

    addContact(contact);
  }

  /**
   * Hides the contact in the current tree, i.e it is not displayed.
   *
   * @param jid
   */
  void hideContact(JID jid) {
    XMPPContact contact = contactsService.getContact(jid.getBase()).orElse(null);
    if (contact == null) return;

    ContactInfo info = visibleContactsMap.get(contact);
    if (info == null) return;

    removeContact(contact);

    hiddenContactsMap.put(contact, info);
  }

  /** Removes all contacts. */
  void removeContacts() {
    removeAllChildren();
    visibleContactsMap.clear();
    hiddenContactsMap.clear();
  }

  /** Class to keep contact info */
  static final class ContactInfo extends LeafInfo {

    private final XMPPContact xmppContact;

    private ContactInfo(XMPPContact xmppContact) {
      super(xmppContact.getDisplayableName());
      this.xmppContact = xmppContact;

      updateStatusIcon();
    }

    JID getJid() {
      return xmppContact.getBareJid();
    }

    public boolean isOnline() {
      return xmppContact.getStatus().isOnline();
    }

    private void updateStatusIcon() {
      if (isOnline()) {
        setIcon(IconManager.CONTACT_ONLINE_ICON);
      } else {
        setIcon(IconManager.CONTACT_OFFLINE_ICON);
      }
    }

    @Override
    public String toString() {
      return title;
    }
  }
}
