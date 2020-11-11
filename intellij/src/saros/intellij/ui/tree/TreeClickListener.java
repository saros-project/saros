package saros.intellij.ui.tree;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import saros.SarosPluginContext;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.User;

/** Tree click listener for showing {@link StartSessionContactPopMenu} or {@link SessionPopMenu}. */
public class TreeClickListener extends MouseAdapter {
  private JTree tree;

  @Inject private ISarosSessionManager sessionManager;

  public TreeClickListener(SessionAndContactsTreeView treeView) {
    SarosPluginContext.initComponent(this);
    tree = treeView;
  }

  @Override
  public void mousePressed(MouseEvent e) {
    if (e.isPopupTrigger()) {
      doPop(e);
    }
  }

  @Override
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
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
      if (node.getUserObject() instanceof ContactTreeRootNode.ContactInfo) {
        ContactTreeRootNode.ContactInfo contactInfo =
            (ContactTreeRootNode.ContactInfo) node.getUserObject();
        if (contactInfo.isOnline()) {

          ISarosSession sarosSession = sessionManager.getSession();
          boolean isSessionRunning = sarosSession != null;
          boolean isHost = isSessionRunning && sarosSession.isHost();

          JPopupMenu menu = null;

          if (!isSessionRunning) {
            menu = new StartSessionContactPopMenu(contactInfo);

          } else if (isHost) {
            menu = new InviteToSessionContactPopMenu(contactInfo);
          }

          if (menu != null) {
            menu.show(e.getComponent(), e.getX(), e.getY());
          }
        }

      } else if (node.getUserObject() instanceof SessionTreeRootNode.UserInfo) {
        SessionTreeRootNode.UserInfo userInfo = (SessionTreeRootNode.UserInfo) node.getUserObject();
        User user = userInfo.getUser();
        if (!user.equals(sessionManager.getSession().getLocalUser())) {
          SessionPopMenu menu = new SessionPopMenu(user);
          menu.show(e.getComponent(), e.getX(), e.getY());
        }

      } else if (node.getUserObject() instanceof SessionTreeRootNode.SessionInfo) {

        // TODO implement behavior when clicking on session entry
      }
    }
  }
}
