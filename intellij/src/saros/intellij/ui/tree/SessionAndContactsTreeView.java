package saros.intellij.ui.tree;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ui.UIUtil;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.jetbrains.annotations.NotNull;
import saros.SarosPluginContext;
import saros.account.XMPPAccountStore;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectionStateListener;
import saros.intellij.ui.util.IconManager;
import saros.intellij.ui.views.SarosMainPanelView;
import saros.net.ConnectionState;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.IContactsUpdate;
import saros.net.xmpp.contact.IContactsUpdate.UpdateType;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.repackaged.picocontainer.annotations.Inject;

/**
 * Saros tree view for contacts and sessions.
 *
 * <p><b>NOTE:</b>This component and any component added here must be correctly torn down when the
 * project the components belong to is closed. See {@link SarosMainPanelView}.
 */
public class SessionAndContactsTreeView extends JTree implements Disposable {

  private final SessionTreeRootNode sessionTreeRootNode;
  private final ContactTreeRootNode contactTreeRootNode;

  private final Project project;

  @Inject private XMPPAccountStore accountStore;

  @Inject private ConnectionHandler connectionHandler;
  @Inject private XMPPContactsService xmppContactsService;

  /**
   * A cell renderer that sets the node icon according to the node type (root, session, contact, or
   * other).
   */
  private final TreeCellRenderer renderer =
      new DefaultTreeCellRenderer() {
        @Override
        public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean selected,
            boolean expanded,
            boolean isLeaf,
            int row,
            boolean focused) {
          Component c =
              super.getTreeCellRendererComponent(
                  tree, value, selected, expanded, isLeaf, row, focused);

          TreePath path = tree.getPathForRow(row);
          if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

            if (node != null) {
              if (node instanceof SarosTreeRootNode) {
                setIcon(null);
              } else if (node instanceof SessionTreeRootNode) {
                setIcon(IconManager.SESSIONS_ICON);
              } else if (node instanceof ContactTreeRootNode) {
                setIcon(IconManager.CONTACTS_ICON);
              } else {
                if (node.getUserObject() instanceof LeafInfo) {
                  LeafInfo info = (LeafInfo) node.getUserObject();
                  if (info.getIcon() != null) {
                    setIcon(info.getIcon());
                  }
                }
              }
            }
          }

          return c;
        }
      };

  private final IConnectionStateListener connectionStateListener =
      (state, error) -> renderConnectionState(state);

  private final IContactsUpdate contactsUpdateListener =
      (contact, updateType) -> {
        // TODO react to other update types once the corresponding information is actually displayed
        if (updateType == UpdateType.ADDED || updateType == UpdateType.REMOVED) {
          renderConnected();
        }
      };

  public SessionAndContactsTreeView(@NotNull Project project) {
    super(new SarosTreeRootNode());

    this.project = project;

    Disposer.register(project, this);

    SarosPluginContext.initComponent(this);

    sessionTreeRootNode = new SessionTreeRootNode(this);
    ((SarosTreeRootNode) getModel().getRoot()).add(sessionTreeRootNode);
    contactTreeRootNode = new ContactTreeRootNode(this);
    ((SarosTreeRootNode) getModel().getRoot()).add(contactTreeRootNode);

    addMouseListener(new TreeClickListener(this));

    getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    setCellRenderer(renderer);

    connectionHandler.addConnectionStateListener(connectionStateListener);
    xmppContactsService.addListener(contactsUpdateListener);

    // show correct initial state
    renderConnectionState(connectionHandler.getConnectionState());
    sessionTreeRootNode.setInitialState();
  }

  @Override
  public void dispose() {
    connectionHandler.removeConnectionStateListener(connectionStateListener);
    xmppContactsService.removeListener(contactsUpdateListener);
  }

  private void renderConnectionState(ConnectionState state) {

    switch (state) {
      case CONNECTED:
        renderConnected();
        break;
      case ERROR:
      case NOT_CONNECTED:
        renderDisconnected();
        break;
      default:
        return;
    }
  }

  /**
   * Displays the 'user@domain (connected)' string and populates the contact list.
   *
   * <p>Called after a connection was made.
   */
  private void renderConnected() {
    UIUtil.invokeLaterIfNeeded(
        () -> {
          if (project.isDisposed()) {
            return;
          }

          JID userJID = connectionHandler.getLocalJID();
          if (userJID == null) return;

          getSarosTreeRootNode().setTitle(userJID.getBareJID().toString());
          updateTree();
        });
  }

  /**
   * Clears the contact list and title.
   *
   * <p>Called after a connection was disconnected.
   */
  private void renderDisconnected() {
    UIUtil.invokeLaterIfNeeded(
        new Runnable() {
          @Override
          public void run() {
            getSarosTreeRootNode().setTitleDefault();

            contactTreeRootNode.removeContacts();
            sessionTreeRootNode.removeAllChildren();

            updateTree();
          }
        });
  }

  private void updateTree() {
    DefaultTreeModel model = (DefaultTreeModel) (getModel());
    model.reload();

    expandRow(2);
  }

  protected ContactTreeRootNode getContactTreeRootNode() {
    return contactTreeRootNode;
  }

  private SarosTreeRootNode getSarosTreeRootNode() {
    return (SarosTreeRootNode) getModel().getRoot();
  }
}
