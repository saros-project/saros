package saros.editor.annotations;

import org.eclipse.jface.text.source.IAnnotationPresentation;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import saros.session.User;
import saros.session.User.Permission;
import saros.ui.Messages;
import saros.ui.util.ColorUtils;
import saros.ui.util.ModelFormatUtils;

/**
 * The annotation that shows the viewports of users with {@link Permission#WRITE_ACCESS}.
 *
 * <p>Configuration of this annotation is done in the plugin-xml.
 *
 * @author rdjemili
 */
public class ViewportAnnotation extends SarosAnnotation implements IAnnotationPresentation {

  protected static final String TYPE = "saros.annotations.viewport";

  private static final int INSET = 2;

  private static final float STROKE_SCALE = 1.05f;

  private static final float FILL_SCALE = 1.22f;

  private Color strokeColor;

  private Color fillColor;

  private boolean multipleLines = false;

  public ViewportAnnotation(User source) {
    super(
        ViewportAnnotation.TYPE,
        true,
        ModelFormatUtils.format(Messages.ViewportAnnotation_visible_scope_of, source),
        source);

    Display display = Display.getDefault();

    Color currentColor = getColor(TYPE, source.getColorID());

    RGB rgb = currentColor.getRGB();

    currentColor.dispose();

    strokeColor = new Color(display, ColorUtils.scaleColorBy(rgb, STROKE_SCALE));
    // FIXME: dispose strokeColor somewhere
    fillColor = new Color(display, ColorUtils.scaleColorBy(rgb, FILL_SCALE));
    // FIXME: dispose fillColor somewhere
  }

  @Override
  public void paint(GC gc, Canvas canvas, Rectangle bounds) {
    Point canvasSize = canvas.getSize();

    gc.setBackground(fillColor);
    gc.setForeground(strokeColor);
    gc.setLineWidth(1);

    int x = ViewportAnnotation.INSET;
    int y = bounds.y;
    int w = canvasSize.x - 2 * ViewportAnnotation.INSET;
    int h = bounds.height;

    if (multipleLines) {
      h += gc.getFontMetrics().getHeight();
    }

    if (y < 0) {
      h = h + y;
      y = 0;
    }

    if (h <= 0) {
      return;
    }

    gc.fillRectangle(x, y, w, h);
    gc.drawRectangle(x, y, w, h);
  }

  /**
   * Enables the advanced computation of the Viewport, because the calculation of the viewport
   * annotation differs between files with one line and files with more than one line.
   *
   * @param multipleLines boolean flag that signs, if the editor has more than one line
   */
  public void setMoreThanOneLine(boolean multipleLines) {
    this.multipleLines = multipleLines;
  }

  @Override
  public int getLayer() {
    return IAnnotationPresentation.DEFAULT_LAYER;
  }
}
