package saros.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Control;

public class FontUtils {

  private FontUtils() {
    // no instantiation allowed
  }

  public static void changeFontSizeBy(Control control, int fontSizeInc) {
    FontData[] fontData = control.getFont().getFontData();
    for (int i = 0; i < fontData.length; ++i)
      fontData[i].setHeight(fontData[i].getHeight() + fontSizeInc);

    final Font newFont = new Font(control.getDisplay(), fontData);
    control.setFont(newFont);

    // Since you created the font, you must dispose it
    control.addDisposeListener(
        new DisposeListener() {
          @Override
          public void widgetDisposed(DisposeEvent e) {
            newFont.dispose();
          }
        });
  }

  public static void makeBold(Control control) {
    FontData[] boldFontData = modifyFontData(control.getFont().getFontData(), SWT.BOLD);

    final Font newFont = new Font(control.getDisplay(), boldFontData);
    control.setFont(newFont);

    // Since you created the font, you must dispose it
    control.addDisposeListener(
        new DisposeListener() {
          @Override
          public void widgetDisposed(DisposeEvent e) {
            newFont.dispose();
          }
        });
  }

  public static FontData[] modifyFontData(FontData[] fontData, int style) {
    FontData[] styledData = new FontData[fontData.length];
    for (int i = 0; i < fontData.length; i++) {
      styledData[i] =
          new FontData(
              fontData[i].getName(), fontData[i].getHeight(), fontData[i].getStyle() | style);
    }
    return styledData;
  }
}
