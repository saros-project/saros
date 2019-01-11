package de.fu_berlin.inf.dpp.editor.annotations;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * A strategy fixing the highlighting of a remote user's selection in one's own editor.
 *
 * <p>An Eclipse local selection usually looks like this:
 *
 * <pre>
 *      ||||||||||||||||||||||||||||||||||||||||||||||||||
 * |||||||||||||||||||||||||||||||||||||||||||||||||||||||
 * |||||||||||||||||||||||||||||||||||||||||||||||||||||||
 * |||||||||||||||||||||||||||||||||||||||||||||||||||||||
 * |||||||||||||||||||||||||||||||||||||||||||||||||||||||
 * |||||||||||||||||||||||||||||||||||||||||||||||||||||||
 * ||||||||||||||||||||||
 * </pre>
 *
 * When a remote user has this selection on her screen, the local annotation may look like this:
 *
 * <pre>
 *      ||||||||||||||||||||
 * |||||||||||||||||||||||||||||
 * ||||||
 *
 * |||||||||
 * ||
 * ||||||||||||||||||||||
 * </pre>
 *
 * i. e., only the <i>text</i> is highlighted and not the ends of lines. We haven't found a way to
 * abolish the old highlighting and draw a completely new one. Instead, we have to fill up the lines
 * like this:
 *
 * <pre>
 *      ||||||||||||||||||||++++++++++++++++++++++++++++++
 * |||||||||||||||||||||||||||||++++++++++++++++++++++++++
 * ||||||+++++++++++++++++++++++++++++++++++++++++++++++++
 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++
 * |||||||||++++++++++++++++++++++++++++++++++++++++++++++
 * ||+++++++++++++++++++++++++++++++++++++++++++++++++++++
 * ||||||||||||||||||||||
 * </pre>
 *
 * That's rather nasty, of course.
 *
 * <p>Furthermore, this method is called not once for the complete selection, but for each line. So
 * we have to look whether the line's last character is included in the selection. If so, we have to
 * fill it up.
 */
public class SelectionFillUpStrategy implements IDrawingStrategy {
  @Override
  public void draw(
      Annotation annotation,
      GC gc,
      StyledText textWidget,
      int startOffset,
      int length,
      Color color) {

    SelectionFillUpAnnotation cursorAnnotation = (SelectionFillUpAnnotation) annotation;

    /**
     * We have to check if this is the end of the selected area by comparing the end of the real
     * selection and the end of the current selection. This is caused by the fact that the {@code
     * AnnotationPainter} calls draw() once per selected line instead of once per selection.
     *
     * <p>The Selection in the {@code Annotation} doesn't care for folded areas, whereas the offset
     * given by the {@code AnnotationPainter} does. To avoid this we calculate it during drawing.
     *
     * <p>An example:
     *
     * <p>(i) Assume we have selected a block and the text in the last line is selected in full. The
     * highlighting has to end there, too. draw() is called once per line with, these parameters:
     *
     * <pre>
     * startOffset  length
     * 762          0
     * 764          24
     * 790          15
     * 807          2
     * </pre>
     *
     * From the annotation we get this for the last line:
     *
     * <p>cursorAnnotation.getOffset() + cursorAnnotation.getLength() = 809
     *
     * <p>(ii) Now we press Shift+Arrowright. The text cursor jumps into the next line and the
     * highlighted background in the previous line should extend to the right margin. Again, draw()
     * is called once per line with these parameters:
     *
     * <pre>
     * startOffset  length
     * 762          0
     * 764          24
     * 790          15
     * 807          2
     * </pre>
     *
     * Surprise, surprise, they are the *same* as before. But from the annotation we now get:
     *
     * <p>cursorAnnotation.getOffset() + cursorAnnotation.getLength() = 811
     *
     * <p>The two extra characters stem from the Windows-like line break (\r\n). Thus, we can
     * distinguish between situations (i) and (ii).
     *
     * <p>(iii) Suppose now, we have some code folded above the text to be selected. We select the
     * text as in (i). The AnnotationPainter corrects offset and length, taking into account the
     * fold. Therefore now, our parameters look like this:
     *
     * <pre>
     * startOffset  length
     * 348          0
     * 350          24
     * 367          15
     * 393          2
     * </pre>
     *
     * The annotation doesn't get corrected offset and length information and therefore says this:
     *
     * <p>cursorAnnotation.getOffset() + cursorAnnotation.getLength() = 809
     *
     * <p>(iv) We leave the folding in and go one character forward as in (ii). The parameters for
     * draw() stay the same:
     *
     * <pre>
     * startOffset  length
     * 348          0
     * 350          24
     * 367          15
     * 393          2
     * </pre>
     *
     * And the annotation says this, again:
     *
     * <p>cursorAnnotation.getOffset() + cursorAnnotation.getLength() = 811
     *
     * <p>Known Issue: This doesn't work if the selection includes a folded area and thus results in
     * this case in a fully marked line if the user selects the whole last line, but not the area
     * that is not covered by text.
     *
     * <p>TODO There may be other possibilities to circumvent the whole problem with foldings:
     *
     * <p>- One might do some calculations before inserting the Annotation into the model: Find out
     * there whether the end of the selection coincides with the end of the text in a line. If so,
     * decrease its length by 1. The line will get highlighted by the SelectionAnnotation (which
     * gets the original length), but the SelectionFillUpAnnotation will think it's done. There
     * might be some corner cases.
     *
     * <p>- When creating the Annotation, one might give it a TextSelection equipped with the
     * correct IDocument. This is only a speculation.
     */

    /*
     * FIXME Don't store such information in the textWidget, let the
     * LocationAnnotationManager handle that instead. One unwanted
     * side-effect of the current implementation: The stored data won't be
     * removed (only overwritten by new data) and thus will be carried into
     * subsequent sessions.
     */

    String foldingOffsetKey = cursorAnnotation.getSource().getJID() + "offset";
    Integer foldingOffset = (Integer) textWidget.getData(foldingOffsetKey);

    if (foldingOffset == null) {
      foldingOffset = cursorAnnotation.getOffset() - startOffset;
      textWidget.setData(foldingOffsetKey, foldingOffset);
    }

    int currentEnd = startOffset + length;
    int realOffset = cursorAnnotation.getOffset();
    int realLength = cursorAnnotation.getLength();
    int realEnd = realOffset + realLength - foldingOffset;

    if (currentEnd == realEnd) {
      textWidget.setData(foldingOffsetKey, null);
      return;
    }

    // Find out more about our current position
    int lineNo = textWidget.getLineAtOffset(startOffset);
    int lineOffset = textWidget.getOffsetAtLine(lineNo);
    String line = textWidget.getLine(lineNo);
    int lastOffsInLine = lineOffset + line.length();

    // Find out how wide to draw our rectangles at most
    int xMax = textWidget.getBounds().width;

    // Find out the height of the lines
    int lineHeight = textWidget.getLineHeight();

    // textWidget.getTextBounds gives an error on the last sign
    if (lastOffsInLine >= textWidget.getCharCount()) {
      return;
    }

    // Find out where in the horizontal we have to start drawing
    Rectangle r = textWidget.getTextBounds(lastOffsInLine, lastOffsInLine);
    int x = r.x + r.width;

    // Find out where we are in the vertical
    int y = textWidget.getLocationAtOffset(startOffset).y;

    // Clear the space for drawing (see IDrawingStrategy's docu)
    if (gc == null) {
      textWidget.redraw(x, y, xMax - x, lineHeight, true);
      textWidget.setData(foldingOffsetKey, null);
      return;
    }

    // Stop after deleting the selection if the remote user just clicked.
    if (cursorAnnotation.isDeselection()) {
      textWidget.setData(foldingOffsetKey, null);
      return;
    }

    // Do nothing if the selection ends before the end of the line
    if (startOffset + length < lastOffsInLine) {
      textWidget.setData(foldingOffsetKey, null);
      return;
    }

    // Do nothing if drawing would hide the local selection
    Point localSelection = textWidget.getSelection();
    int firstLineOfLocalSelection = textWidget.getLineAtOffset(localSelection.x);
    int lastLineOfLocalSelection = textWidget.getLineAtOffset(localSelection.y);

    /*
     * The lastLineOfLocalSelection is assured to be only partly selected,
     * since we have a complete selection. This allows us to just ignore the
     * lastLine.
     */
    if (lineNo >= firstLineOfLocalSelection && lineNo < lastLineOfLocalSelection) {
      return;
    }

    // Set the right user color
    Color c =
        SarosAnnotation.getColor(
            SelectionAnnotation.TYPE, cursorAnnotation.getSource().getColorID());
    gc.setBackground(c);

    // Draw the rectangle
    gc.fillRectangle(x, y, xMax - x, lineHeight);
    c.dispose();

    return;
  }
}
