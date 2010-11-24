package de.fu_berlin.inf.dpp.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Label;

public class FontUtil {
    public static void changeFontSizeBy(Label label, int fontSizeInc) {
        FontData[] fontData = label.getFont().getFontData();
        for (int i = 0; i < fontData.length; ++i)
            fontData[i].setHeight(fontData[i].getHeight() + fontSizeInc);

        final Font newFont = new Font(label.getDisplay(), fontData);
        label.setFont(newFont);

        // Since you created the font, you must dispose it
        label.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                newFont.dispose();
            }
        });
    }

    public static void makeBold(Label label) {
        FontData[] fontData = label.getFont().getFontData();
        for (FontData fontData_ : fontData) {
            fontData_.setStyle(SWT.BOLD);
        }

        final Font newFont = new Font(label.getDisplay(), fontData);
        label.setFont(newFont);

        // Since you created the font, you must dispose it
        label.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                newFont.dispose();
            }
        });
    }
}
