package saros.ui.widgetGallery.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import saros.ui.widgets.SimpleIllustratedComposite;

public class DemoBannerComposite extends SimpleIllustratedComposite {

  public static final int MARGIN = 5;

  public DemoBannerComposite(Composite parent, int style) {
    super(parent, style);

    this.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
    this.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_GRAY));
  }

  @Override
  public Rectangle getClientArea() {
    Rectangle clientArea = super.getClientArea();
    clientArea.x += MARGIN;
    clientArea.width -= 2 * MARGIN;
    clientArea.y += MARGIN;
    clientArea.height -= 2 * MARGIN;
    return clientArea;
  }

  @Override
  public Rectangle computeTrim(int x, int y, int width, int height) {
    Rectangle trim = super.computeTrim(x, y, width, height);
    trim.width += 2 * MARGIN;
    trim.height += 2 * MARGIN;
    return trim;
  }
}
