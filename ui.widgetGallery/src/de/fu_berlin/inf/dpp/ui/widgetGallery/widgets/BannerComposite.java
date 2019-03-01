package de.fu_berlin.inf.dpp.ui.widgetGallery.widgets;

import de.fu_berlin.inf.dpp.ui.util.FontUtils;
import de.fu_berlin.inf.dpp.ui.widgets.SimpleIllustratedComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

public class BannerComposite extends SimpleIllustratedComposite {

  public static final int MARGIN = 5;

  public BannerComposite(Composite parent, int style) {
    super(parent, style);

    this.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    this.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
  }

  @Override
  public void setContent(IllustratedText illustratedText) {
    super.setContent(illustratedText);
    if (this.contentLabel != null && !this.contentLabel.isDisposed()) {
      FontUtils.changeFontSizeBy(this.contentLabel, 3);
    }
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
