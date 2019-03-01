package saros.ui.util;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/** Utility class for paint actions */
public class PaintUtils {
  public static final int ARC = 5;
  public static final int LINE_WEIGHT = 1;

  public static void drawRoundedRectangle(
      GC gc, Rectangle bounds, Color backgroundColor, Color borderColor) {
    drawRoundedRectangle(gc, bounds, backgroundColor);
    drawRoundedBorder(gc, bounds, borderColor);
  }

  public static void drawRoundedRectangle(GC gc, Rectangle bounds, Color backgroundColor) {
    Color backupBackground = gc.getBackground();
    gc.setBackground(backgroundColor);
    gc.fillRoundRectangle(bounds.x, bounds.y, bounds.width, bounds.height, ARC, ARC);
    gc.setBackground(backupBackground);
  }

  public static void drawRoundedBorder(GC gc, Rectangle bounds, Color borderColor) {
    Color backupBackground = gc.getBackground();
    int backupLineWidth = gc.getLineWidth();

    gc.setLineWidth(LINE_WEIGHT);
    gc.setForeground(borderColor);
    gc.drawRoundRectangle(
        bounds.x, bounds.y, bounds.width - LINE_WEIGHT, bounds.height - LINE_WEIGHT, ARC, ARC);

    gc.setBackground(backupBackground);
    gc.setLineWidth(backupLineWidth);
  }
}
