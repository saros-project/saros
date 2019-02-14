/** */
package de.fu_berlin.inf.dpp.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/** Displays a colored rectangle with a line of text to the right. */
public class ColorLabel extends Canvas {
  /** The radius of the arc of the color label */
  protected static final int RECTANGLE_ARC = 4;
  /** The horizontal gap between the label the right end of the widget */
  protected static final int HORIZONTAL_GAP = 5;
  /** The gap between the color rectangle and the edges of the widget */
  protected static final int BORDER_GAP = 2;
  /** The vertical offset of the color rectangle within the widget */
  protected static final int VERTICAL_OFFSET = 4;

  /** The width of the frame surrounding a color rectangle when it is selected */
  protected static final int FRAME_WIDTH_SELECTED = 3;

  /** The width of the frame surrounding a color rectangle when it is not selected */
  protected static final int FRAME_WIDTH_UNSELECTED = 1;

  /** The colors look differently when they are disabled */
  protected static final float DISABLED_COLOR_SATURATION = 0.02f;

  /** The colors look differently when they are disabled */
  protected static final float DISABLED_COLOR_BRIGHTNESS = 0.9f;

  /** The color of the frame around the color rectangle when it is enabled */
  protected final Color frameEnabledColor;
  /** The color of the frame around the color rectangle when it is disabled */
  protected final Color frameDisabledColor;
  /** The enabled color */
  protected Color enabledColor;
  /** The disabled color */
  protected Color disabledColor;

  /** The width of the color rectangle */
  protected int rectangleWidth = 20;

  /** Is the current frame selected? */
  protected boolean selected = false;

  /** The height of the color rectangle */
  protected int rectangleHeight = 10;

  public ColorLabel(Composite parent, int style) {
    super(parent, style);

    frameEnabledColor = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);

    frameDisabledColor = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);

    addPaintListener(
        new PaintListener() {
          @Override
          public void paintControl(PaintEvent e) {
            onPaint(e);
          }
        });

    addDisposeListener(
        new DisposeListener() {
          @Override
          public void widgetDisposed(DisposeEvent e) {
            onDispose(e);
          }
        });
  }

  /**
   * Set size of the color rectangle.
   *
   * @param width width of the rectangle
   * @param height height of the rectangle
   */
  public void setPreferredSize(int width, int height) {
    if (width <= 0 || height <= 0) {
      return;
    }

    this.rectangleWidth = width;
    this.rectangleHeight = height;

    this.layout();
  }

  @Override
  public Point computeSize(int width, int height, boolean changed) {
    int localWidth = 0, localHeight = 0;

    // consider colored rectangle
    if (enabledColor != null) {
      localWidth = rectangleWidth + HORIZONTAL_GAP;
      localHeight = rectangleHeight;
    }

    // calculate width according to given hints
    if (width != SWT.DEFAULT) localWidth = width;
    if (height != SWT.DEFAULT) localHeight = height;

    return new Point(localWidth + BORDER_GAP, localHeight + BORDER_GAP + VERTICAL_OFFSET);
  }

  /**
   * Draws the color label
   *
   * @param e
   */
  protected void onPaint(PaintEvent e) {
    GC gc = e.gc;

    int frameWidth = selected ? FRAME_WIDTH_SELECTED : FRAME_WIDTH_UNSELECTED;

    int x = frameWidth;
    int y = VERTICAL_OFFSET + frameWidth;
    int width = rectangleWidth - 2 * frameWidth;
    int height = rectangleHeight - 2 * frameWidth;

    if (enabledColor != null) {
      /*
       * Save the old background color and set it back to this after
       * drawing the rectangle. Otherwise the text will have the same
       * background color as the rectangle.
       */
      final Color oldBackground = gc.getBackground();
      final Color oldForeground = gc.getForeground();

      // set frame color
      Color colorToPaint = isEnabled() ? frameEnabledColor : frameDisabledColor;

      gc.setForeground(colorToPaint);
      gc.setBackground(colorToPaint);

      // Draw the frame
      gc.fillRoundRectangle(
          0, VERTICAL_OFFSET, rectangleWidth, rectangleHeight, RECTANGLE_ARC, RECTANGLE_ARC);

      // set label color
      colorToPaint = isEnabled() ? enabledColor : disabledColor;

      gc.setForeground(colorToPaint);
      gc.setBackground(colorToPaint);

      // Draw the label
      gc.fillRoundRectangle(x, y, width, height, RECTANGLE_ARC, RECTANGLE_ARC);
      // setting back the colors like they were before
      gc.setForeground(oldForeground);
      gc.setBackground(oldBackground);
    }
  }

  /**
   * Creates a greyish color value on the basis of the color the label represents.
   *
   * @return
   */
  private Color getDisabledColor(Color color) {

    // Use HSB-value because it's easier to transform the values
    float[] hsb = color.getRGB().getHSB();

    // Converts to a greyish color
    hsb[1] = DISABLED_COLOR_SATURATION;
    hsb[2] = DISABLED_COLOR_BRIGHTNESS;

    return new Color(Display.getCurrent(), new RGB(hsb[0], hsb[1], hsb[2]));
  }

  protected void onDispose(DisposeEvent e) {
    if (disabledColor != null && !disabledColor.isDisposed()) disabledColor.dispose();
  }

  /** @return the color of this color label */
  public Color getColor() {
    return enabledColor;
  }

  /**
   * Changes the color of this color label.
   *
   * @param color the new color for this label
   */
  public void setColor(Color color) {

    enabledColor = color;

    if (disabledColor != null && !disabledColor.isDisposed()) disabledColor.dispose();

    disabledColor = getDisabledColor(color);

    redraw();
  }

  /**
   * Set label as selected, causing a frame to be drawn around the color rectangle.
   *
   * @param selected
   */
  public void setSelected(boolean selected) {
    if (this.selected == selected) return;

    this.selected = selected;
    redraw();
  }
}
