package de.fu_berlin.inf.dpp.ui.widgets.viewer.session;

import de.fu_berlin.inf.dpp.editor.annotations.SarosAnnotation;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.model.session.UserElement;
import de.fu_berlin.inf.dpp.ui.util.PaintUtils;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;

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
