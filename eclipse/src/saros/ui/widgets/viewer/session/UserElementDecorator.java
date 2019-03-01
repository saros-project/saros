package saros.ui.widgets.viewer.session;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;
import saros.editor.annotations.SarosAnnotation;
import saros.session.User;
import saros.ui.model.session.UserElement;
import saros.ui.util.PaintUtils;

/**
 * Draws a rounded rectangle indicating the highlighting color that is used for this user in the
 * current session
 */
public class UserElementDecorator implements Listener {

  @Override
  public void handleEvent(Event event) {

    TreeItem treeItem = (TreeItem) event.item;

    /*
     * do not adapt the object or we will draw into widget / tree items that
     * should not be *decorated*
     */

    if (!(treeItem.getData() instanceof UserElement)) return;

    User user = ((UserElement) treeItem.getData()).getUser();

    Rectangle bounds = treeItem.getBounds(event.index);

    bounds.width = 15;
    bounds.x += 15;

    /*
     * make the rectangle a little bit smaller so it does not collide with
     * the edges when the tree item is selected
     */

    bounds.y += 2;
    bounds.height -= 4;

    Color background = SarosAnnotation.getUserColor(user);
    PaintUtils.drawRoundedRectangle(event.gc, bounds, background);
    background.dispose();
  }
}
