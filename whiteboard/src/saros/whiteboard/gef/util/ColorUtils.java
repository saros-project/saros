package saros.whiteboard.gef.util;

import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.RGB;
import saros.whiteboard.gef.actions.ColorListener;

public class ColorUtils {
  private static final Logger log = Logger.getLogger(ColorUtils.class);

  private static RGB rgbFore = new RGB(0, 0, 0);

  private static RGB rgbBack = new RGB(255, 255, 255);

  private static List<ColorListener> listeners = new LinkedList<ColorListener>();

  public static void setForegroundColor(RGB rgb) {
    log.debug("set foregroundcolor " + rgb);
    rgbFore = rgb;

    // inform about change
    fireListeners();
  }

  /** inform listeners about color change */
  private static void fireListeners() {
    for (ColorListener c : listeners) {
      c.updateColor(rgbFore, rgbBack);
    }
  }

  public static void setBackgroundColor(RGB rgb) {
    rgbBack = rgb;

    // inform about change
    fireListeners();
  }

  public static String getBackgroundColor() {
    return rgbBack.red + "," + rgbBack.green + "," + rgbBack.blue;
  }

  public static RGB getRGBBackgroundColor() {
    return rgbBack;
  }

  public static String getForegroundColor() {
    return rgbFore.red + "," + rgbFore.green + "," + rgbFore.blue;
  }

  public static RGB getRGBForegroundColor() {
    return rgbFore;
  }

  /**
   * Add listener
   *
   * @param c
   */
  public static void addListener(ColorListener c) {
    if (!listeners.contains(c)) listeners.add(c);
  }

  /**
   * remove listener
   *
   * @param c
   */
  public static void removeListener(ColorListener c) {
    listeners.remove(c);
  }
}
