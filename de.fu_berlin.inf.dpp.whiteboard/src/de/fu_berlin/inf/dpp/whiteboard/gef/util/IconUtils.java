package de.fu_berlin.inf.dpp.whiteboard.gef.util;

import java.util.HashMap;
import org.apache.log4j.Logger;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Static class that creates provides the icon figures and transforms them to an image
 *
 * @author jurke
 */
public class IconUtils {
  private static final Logger log = Logger.getLogger(IconUtils.class);

  private static Image imageBuffer = null;
  private static GC imageGC = null;
  private static SWTGraphics swtGraphics = null;

  private static int LOW_WIDTH = 20;
  private static int LOW_HEIGHT = 16;
  private static int inset = 2;

  private static Color blue = new Color(null, 0, 0, 255);
  private static Color red = new Color(null, 255, 0, 0);
  private static Color black = new Color(null, 0, 0, 0);

  private static HashMap<String, Image> imageCache = new HashMap<String, Image>(10);

  private static Image resize(Image image, int width, int height) {
    Image scaled = new Image(Display.getDefault(), width, height);
    GC gc = new GC(scaled);
    gc.setAntialias(SWT.ON);
    gc.setInterpolation(SWT.HIGH);
    gc.drawImage(
        image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, width, height);
    gc.dispose();
    // image.dispose(); // don't forget about me!
    return scaled;
  }

  public static Image getAnnotationImage() {
    Image i = imageCache.get("Annotation");
    if (i == null) {
      i = resize(Display.getDefault().getSystemImage(SWT.ICON_INFORMATION), LOW_HEIGHT, LOW_HEIGHT);
      imageCache.put("Annotation", i);
    }
    return i;
  }

  public static Image getRectImage() {
    Image i = imageCache.get("Rectangle");
    if (i == null) {
      i = getIconImage(getRectangleIconFigure());
      imageCache.put("Rectangle", i);
    }
    return i;
  }

  public static Image getPencilImage() {
    Image i = imageCache.get("Pencil");
    if (i == null) {
      i = getIconImage(getPencilIconFigure());
      imageCache.put("Pencil", i);
    }
    return i;
  }

  public static Image getLineImage() {
    Image i = imageCache.get("Line");
    if (i == null) {
      i = getIconImage(getLineIconFigure());
      imageCache.put("Line", i);
    }
    return i;
  }

  public static Image getArrowImage() {
    Image i = imageCache.get("Arrow");
    if (i == null) {
      i = getIconImage(getArrowIconFigure());
      imageCache.put("Arrow", i);
    }
    return i;
  }

  public static Image getEllipseImage() {
    Image i = imageCache.get("Ellipse");
    if (i == null) {
      i = getIconImage(getEllipseIconFigure());
      imageCache.put("Ellipse", i);
    }
    return i;
  }

  public static Image getTextBoxImage() {
    Image i = imageCache.get("TextBox");
    if (i == null) {
      i = getIconImage(getTextBoxIconFigure());
      imageCache.put("TextBox", i);
    }
    return i;
  }

  public static Image getBackgroundColorImage() {
    // no caching or colorIcon is not refreshed
    return getIconImage(getBackgroundColorIconFigure());
  }

  public static Image getForegroundColorImage() {
    // no caching or colorIcon is not refreshed
    return getIconImage(getForegroundColorIconFigure());
  }

  private static Image getIconImage(IFigure figure) {
    imageBuffer = new Image(null, LOW_WIDTH, LOW_HEIGHT);
    imageGC = new GC(imageBuffer, (SWT.RIGHT_TO_LEFT | SWT.LEFT_TO_RIGHT));
    swtGraphics = new SWTGraphics(imageGC);
    figure.paint(swtGraphics);
    return imageBuffer;
  }

  private static RectangleFigure getRectangleIconFigure() {
    RectangleFigure rect = new RectangleFigure();
    rect.setBackgroundColor(red);
    rect.setForegroundColor(blue);
    rect.setBounds(new Rectangle(inset, inset, LOW_WIDTH - 2 * inset, LOW_HEIGHT - 2 * inset));
    return rect;
  }

  private static Ellipse getEllipseIconFigure() {
    Ellipse ellipse = new Ellipse();
    ellipse.setBackgroundColor(red);
    ellipse.setForegroundColor(blue);
    ellipse.setBounds(new Rectangle(inset, inset, LOW_WIDTH - 2 * inset, LOW_HEIGHT - 2 * inset));
    return ellipse;
  }

  private static Polyline getTextBoxIconFigure() {
    Polyline line = new Polyline();
    int[] rawPoints = {3, 1, 9, 1, 6, 1, 6, 10};
    PointList points = new PointList(rawPoints);
    line.setPoints(points);
    line.setForegroundColor(black);
    return line;
  }

  private static Polyline getLineIconFigure() {
    Polyline line = new Polyline();
    int[] rawPoints = {1, 10, 10, 1};
    PointList points = new PointList(rawPoints);
    line.setPoints(points);
    line.setForegroundColor(black);
    return line;
  }

  private static Polyline getArrowIconFigure() {
    // TODO CREATE ARROW POLYLINE !!
    Polyline line = new Polyline();
    int[] rawPoints = {1, 10, 10, 1, 5, 1, 10, 1, 10, 5};
    PointList points = new PointList(rawPoints);
    line.setPoints(points);
    line.setForegroundColor(black);
    return line;
  }

  private static Polyline getPencilIconFigure() {
    Polyline line = new Polyline();
    int[] rawPoints = {
      11, 3, 6, 3, 6, 4, 3, 4, 3, 5, 2, 5, 1, 6, 1, 8, 2, 8, 2, 9, 3, 9, 4, 9, 5, 8, 7, 8, 7, 7, 8,
      7, 8, 6, 13, 6, 14, 7, 14, 10, 13, 10, 13, 11, 9, 11, 9, 12, 7, 12
    };
    PointList points = new PointList(rawPoints);
    line.setPoints(points);
    line.setForegroundColor(black);
    return line;
  }

  private static IFigure getBackgroundColorIconFigure() {

    log.debug("color: " + ColorUtils.getRGBBackgroundColor());
    RectangleFigure rect = new RectangleFigure();
    rect.setForegroundColor(black);
    rect.setBackgroundColor(new Color(null, ColorUtils.getRGBBackgroundColor()));
    rect.setBounds(new Rectangle(inset, inset, LOW_WIDTH - 2 * inset, LOW_HEIGHT - 2 * inset));
    return rect;
  }

  private static IFigure getForegroundColorIconFigure() {
    log.debug("create foreground icon with color " + ColorUtils.getRGBForegroundColor());
    RectangleFigure rect = new RectangleFigure();
    rect.setForegroundColor(black);
    rect.setBackgroundColor(new Color(null, ColorUtils.getRGBForegroundColor()));
    rect.setBounds(new Rectangle(inset, inset, LOW_WIDTH - 2 * inset, LOW_HEIGHT - 2 * inset));
    return rect;
  }
}
