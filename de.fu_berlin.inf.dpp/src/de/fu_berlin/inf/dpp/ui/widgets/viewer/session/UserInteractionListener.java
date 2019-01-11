package de.fu_berlin.inf.dpp.ui.widgets.viewer.session;

import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.model.session.AwarenessInformationTreeElement;
import de.fu_berlin.inf.dpp.ui.model.session.UserElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class UserInteractionListener extends MouseAdapter {

  private final EditorManager editorManager;

  public UserInteractionListener(final EditorManager editorManager) {
    this.editorManager = editorManager;
  }

  /**
   * Tries to find a tree item at the given click coordinates. If it doesn't find anything, it
   * continuously tries to find a target by shifting the X coordinate.
   *
   * @param event
   * @return
   */
  private TreeItem findTreeItemNear(MouseEvent event) {

    if (!(event.getSource() instanceof Tree)) return null;

    final Tree control = (Tree) event.getSource();

    TreeItem treeItem = control.getItem(new Point(event.x, event.y));
    /*
     * Background: the items are only targetable at their text-labels and
     * icons. In the session view, the tree items get a rectangle with
     * background color that expands beyond the text label. Users think that
     * they can interact with the element by clicking anywhere on the
     * background color, but actually miss the element.
     */
    int x = event.x;
    while (treeItem == null && x > 0) {
      x -= 5; // try 5 px to the left...
      treeItem = control.getItem(new Point(x, event.y));
    }
    return treeItem;
  }

  /**
   * Toggle follow user when doubleclicked on UserElement in the session tree.
   *
   * <p>Jump to User file+position when doubleclicked on AwarenessTreeItem.
   */
  @Override
  public void mouseDoubleClick(MouseEvent event) {

    TreeItem treeItem = findTreeItemNear(event);

    if (treeItem == null) return;

    User user = (User) Platform.getAdapterManager().getAdapter(treeItem.getData(), User.class);

    if (user == null || user.isLocal()) return;

    final Object treeItemContent = treeItem.getData();

    if (treeItemContent instanceof UserElement) {
      User followedUser = editorManager.getFollowedUser();
      editorManager.setFollowing(user.equals(followedUser) ? null : user);
    } else if (treeItemContent instanceof AwarenessInformationTreeElement) {
      editorManager.jumpToUser(user);
    }
  }
}
