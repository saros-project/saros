package de.fu_berlin.inf.dpp.intellij.ui.tree;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.User;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.picocontainer.annotations.Inject;

/** Tree click listener for showing {@link ContactPopMenu} or {@link SessionPopMenu}. */
public class TreeClickListener extends MouseAdapter {
  private static final boolean ENABLE_FOLLOW_MODE =
      Boolean.getBoolean("saros.intellij.ENABLE_FOLLOW_MODE");

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
          ContactPopMenu menu = new ContactPopMenu(contactInfo);
          menu.show(e.getComponent(), e.getX(), e.getY());
        }
      } else if (node.getUserObject() instanceof SessionTreeRootNode.UserInfo) {

        if (!ENABLE_FOLLOW_MODE) {
          return;
        }

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
