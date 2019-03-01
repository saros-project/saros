package saros.ui.widgetGallery.util;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CompositeUtils {
  public static void emptyComposite(Composite composite) {
    Control[] children = composite.getChildren();
    for (Control child : children) {
      if (!child.isDisposed()) {
        child.dispose();
      }
    }
  }
}
