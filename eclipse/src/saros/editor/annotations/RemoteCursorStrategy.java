package saros.editor.annotations;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

/**
 * Used to draw a cursor-like line at the position of the text cursor of a remote user. This enables
 * the local user to see exactly where the other users' cursors are.
 */
public class RemoteCursorStrategy implements IDrawingStrategy {
  private static final int CURSOR_WIDTH = 2;

  /**
   * {@inheritDoc}
   *
   * @param annotation An RemoteCursorAnnotation passed by the {@link AnnotationPainter}
   * @param offset offset of the end of the Selection
   * @param length always 0, will be ignored
   */
  @Override
  public void draw(
      Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
    Point currentCursorPosition = textWidget.getLocationAtOffset(offset);

    // clearing mode
    if (gc == null) {
      /*
       * Redraw the surrounding area of the cursor. Because we draw a line
       * with a width larger than 1, we have to clear the area around the
       * actual coordinates (start a bit more left, and extend a bit to
       * the right).
       */
      textWidget.redraw(
          currentCursorPosition.x - CURSOR_WIDTH / 2,
          currentCursorPosition.y,
          CURSOR_WIDTH + 1,
          textWidget.getLineHeight(),
          false);

      return;
    }

    final Color oldBackground = gc.getBackground();
    final Color oldForeground = gc.getForeground();

    /*
     * Draw the cursor line
     */
    gc.setBackground(color);
    gc.setForeground(color);

    gc.setLineWidth(CURSOR_WIDTH);
    gc.drawLine(
        currentCursorPosition.x,
        currentCursorPosition.y,
        currentCursorPosition.x,
        currentCursorPosition.y + textWidget.getLineHeight());

    // set back the colors like they were before
    gc.setBackground(oldBackground);
    gc.setForeground(oldForeground);
  }
}
