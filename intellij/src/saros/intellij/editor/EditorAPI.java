package saros.intellij.editor;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.util.Pair;
import java.awt.Point;
import java.awt.Rectangle;
import org.jetbrains.annotations.NotNull;
import saros.editor.text.LineRange;
import saros.editor.text.TextPosition;
import saros.editor.text.TextSelection;
import saros.intellij.runtime.EDTExecutor;

/**
 * IntellJ editor API. An Editor is a window for editing source files.
 *
 * <p>Performs IntelliJ editor related actions in the UI thread.
 */
public class EditorAPI {

  private EditorAPI() {
    // NOP
  }

  /**
   * Scrolls the given editor so that the given line is in the center of the local viewport. The
   * given line represents the logical position in the editor.
   *
   * <p><b>NOTE:</b> This function only works correctly when the given editor is currently visible.
   * If the given editor is not currently visible, it is not guaranteed that the editor will scroll
   * to the correct position.
   *
   * <p><b>NOTE:</b> The center of the local viewport is at 1/3 for IntelliJ.
   *
   * @param editor the editor to scroll
   * @param line the line to scroll to
   * @see LogicalPosition
   */
  static void scrollToViewPortCenter(final Editor editor, final int line) {
    EDTExecutor.invokeAndWait(
        () -> {
          LogicalPosition logicalPosition = new LogicalPosition(line, 0);

          editor.getScrollingModel().scrollTo(logicalPosition, ScrollType.CENTER);
        },
        ModalityState.defaultModalityState());
  }

  /**
   * Returns the logical line range of the local viewport for the given editor.
   *
   * <p><b>NOTE:</b> This function only works correctly when the given editor is currently visible.
   * If the given editor is not currently visible, it is not guaranteed that the returned line range
   * actually corresponds to the visible part of the editor.
   *
   * @param editor the editor to get the viewport line range for
   * @return the logical line range of the local viewport for the given editor
   * @see LogicalPosition
   */
  @NotNull
  static LineRange getLocalViewPortRange(@NotNull Editor editor) {
    Rectangle visibleAreaRectangle = editor.getScrollingModel().getVisibleAreaOnScrollingFinished();

    return getLocalViewPortRange(editor, visibleAreaRectangle);
  }

  /**
   * Returns the logical line range of the given visible rectangle for the given editor.
   *
   * @param editor the editor to get the viewport line range for
   * @param visibleAreaRectangle the visible rectangle to get the line range for
   * @return the logical line range of the local viewport for the given editor
   * @see LogicalPosition
   */
  public static LineRange getLocalViewPortRange(
      @NotNull Editor editor, @NotNull Rectangle visibleAreaRectangle) {
    int basePos = visibleAreaRectangle.y;
    int endPos = visibleAreaRectangle.y + visibleAreaRectangle.height;

    int currentViewportStartLine = editor.xyToLogicalPosition(new Point(0, basePos)).line;
    int currentViewportEndLine = editor.xyToLogicalPosition(new Point(0, endPos)).line;

    return new LineRange(
        currentViewportStartLine, currentViewportEndLine - currentViewportStartLine);
  }

  /**
   * Returns the current selection in the given editor.
   *
   * @param editor the editor to get the selection for
   * @return the current selection in the given editor
   */
  @NotNull
  static TextSelection getSelection(@NotNull Editor editor) {
    SelectionModel selectionModel = editor.getSelectionModel();

    int selectionStartOffset = selectionModel.getSelectionStart();
    int selectionEndOffset = selectionModel.getSelectionEnd();

    return calculateSelectionPosition(editor, selectionStartOffset, selectionEndOffset);
  }

  /**
   * Calculates the line base selection values for the given text base selection offsets in the
   * given editor.
   *
   * <p>The given offsets must not be negative.
   *
   * @param editor the editor to use for the calculation
   * @param selectionStartOffset the text based offset of the selection start
   * @param selectionEndOffset the text based offset of the selection end
   * @return the line base selection values for the given text base selection offsets in the given
   *     editor
   * @throws IllegalArgumentException if one of the given offsets is negative
   */
  @NotNull
  static TextSelection calculateSelectionPosition(
      @NotNull Editor editor, int selectionStartOffset, int selectionEndOffset) {

    if (selectionStartOffset < 0 || selectionEndOffset < 0) {
      throw new IllegalArgumentException(
          "The given offsets must be larger than or equal to zero. s: "
              + selectionStartOffset
              + ", o: "
              + selectionEndOffset);
    }

    LogicalPosition selectionStart = editor.offsetToLogicalPosition(selectionStartOffset);

    int startLine = selectionStart.line;
    int startLineOffset = calculateLineOffset(editor, startLine);

    // necessary as the logical position counts a tab char as it's displayed width
    int correctedStartInLineOffset = selectionStartOffset - startLineOffset;

    TextPosition startPosition = new TextPosition(startLine, correctedStartInLineOffset);

    // Skip second part of calculation if no selection is present
    if (selectionEndOffset == selectionStartOffset) {
      return new TextSelection(startPosition, startPosition);
    }

    LogicalPosition selectionEnd = editor.offsetToLogicalPosition(selectionEndOffset);

    int endLine = selectionEnd.line;
    int endLineOffset;

    // Skip line offset calculation if end is in same line as start
    if (endLine == startLine) {
      endLineOffset = startLineOffset;
    } else {
      endLineOffset = calculateLineOffset(editor, endLine);
    }

    // necessary as the logical position counts a tab char as it's displayed width
    int correctedEndInLineOffset = selectionEndOffset - endLineOffset;

    TextPosition endPosition = new TextPosition(endLine, correctedEndInLineOffset);

    return new TextSelection(startPosition, endPosition);
  }

  /**
   * Calculates the absolute offsets in the given editor for the given text selection.
   *
   * <p><b>NOTE:</b> The given text selection must not be empty!
   *
   * @param editor the local editor for the file
   * @param textSelection the text selection
   * @return the absolute offsets in the given editor for the given text selection
   * @throws IllegalArgumentException if the given text selection is empty
   */
  static Pair<Integer, Integer> calculateOffsets(
      @NotNull Editor editor, @NotNull TextSelection textSelection) {

    if (textSelection.isEmpty()) {
      throw new IllegalArgumentException("The given text selection must not be empty");
    }

    TextPosition startPosition = textSelection.getStartPosition();

    int startLine = startPosition.getLineNumber();
    int startLineOffset = calculateLineOffset(editor, startLine);

    int startOffset = startLineOffset + startPosition.getInLineOffset();

    TextPosition endPosition = textSelection.getEndPosition();

    int endLine = endPosition.getLineNumber();

    int endLineOffset;

    if (endLine == startLine) {
      endLineOffset = startLineOffset;
    } else {
      endLineOffset = calculateLineOffset(editor, endLine);
    }

    int endOffset = endLineOffset + endPosition.getInLineOffset();

    return new Pair<>(startOffset, endOffset);
  }

  /**
   * Calculates the text based offset for the start of the given line in the given editor.
   *
   * @param editor the editor to use for the calculations
   * @param lineNumber the line number whose offset to calculate
   * @return the text based offset for the start of the given line in the given editor
   */
  private static int calculateLineOffset(@NotNull Editor editor, int lineNumber) {
    return editor.logicalPositionToOffset(new LogicalPosition(lineNumber, 0, true));
  }
}
