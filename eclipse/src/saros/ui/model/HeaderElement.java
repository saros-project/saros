package saros.ui.model;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import saros.ui.util.FontUtils;

/**
 * Abstract headline for use with {@link IContentProvider}s that use {@link ITreeElement}s.
 *
 * @author bkahlert
 */
public abstract class HeaderElement extends TreeElement {
  protected Font boldFont = null;
  protected Styler boldStyler =
      new Styler() {
        @Override
        public void applyStyles(TextStyle textStyle) {
          textStyle.font = boldFont;
        }
      };

  public HeaderElement(Font font) {
    boldFont =
        new Font(Display.getCurrent(), FontUtils.modifyFontData(font.getFontData(), SWT.BOLD));
  }

  public void dispose() {
    if (this.boldFont != null && !this.boldFont.isDisposed()) this.boldFont.dispose();
  }
}
