package saros.whiteboard.gef.util;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Translatable;
import saros.whiteboard.gef.model.LayoutElementRecord;
import saros.whiteboard.gef.model.SVGRootRecord;

public class LayoutUtils {

  /**
   * Translates the translatable from the provided parent up the hierarchy until reaching a record
   * that is a composite.
   */
  public static LayoutElementRecord translateAndGetParent(
      Translatable translatable, LayoutElementRecord parent) {
    while (!parent.isComposite()) {
      Point loc = parent.getLayout().getLocation();
      translatable.performTranslate(loc.x, loc.y);
      parent = (LayoutElementRecord) parent.getParent();
    }
    return parent;
  }

  /**
   * Translates the translatable from the provided parent up the hierarchy until reaching the root
   */
  public static LayoutElementRecord translateToAndGetRoot(
      Translatable translatable, LayoutElementRecord parent) {
    while (!(parent instanceof SVGRootRecord)) {
      Point loc = parent.getLayout().getLocation();
      translatable.performTranslate(loc.x, loc.y);
      parent = (LayoutElementRecord) parent.getParent();
    }
    return parent;
  }
}
