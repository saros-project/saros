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
import org.jetbrains.annotations.Nullable;
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
   * Calculates the line base position value for the given text base offset in the given editor.
   *
   * <p>The given offset must not be negative.
   *
   * @param editor the editor to use for the calculation
   * @param offset the text based offset
   * @return the line base position value for the given text base offsets in the given editor
   * @throws IllegalArgumentException if the the given offset is negative
   */
  public static TextPosition calculatePosition(@NotNull Editor editor, int offset) {
    if (offset < 0) {
      throw new IllegalArgumentException("The given offset must not be negative");
    }

    return calculatePositionInternal(editor, offset, null).first;
  }

  /**
   * Calculates the line base position value for the given text base offset in the given editor.
   *
   * <p>This method is used for performance optimization. It accepts a pair containing a line number
   * and the start offset of that line and also returns the calculated line number and start offset
   * of that line. This information can be used to skip calculations of line start offsets when
   * multiple text positions are located in the same line.
   *
   * <p>This method does not validate the input. The input is expected to be validated beforehand by
   * the caller.
   *
   * @param editor the editor to use for the calculation
   * @param offset the text based offset
   * @return the line base position value for the given text base offsets in the given editor
   * @throws IllegalArgumentException if the the given offset is negative
   */
  private static Pair<TextPosition, Pair<Integer, Integer>> calculatePositionInternal(
      Editor editor, int offset, @Nullable Pair<Integer, Integer> knownLineStartOffset) {

    LogicalPosition logicalPosition = editor.offsetToLogicalPosition(offset);

    int lineNumber = logicalPosition.line;

    int lineStartOffset;
    Pair<Integer, Integer> returnedKnownLineStartOffset;

    if (knownLineStartOffset != null && knownLineStartOffset.first == lineNumber) {
      lineStartOffset = knownLineStartOffset.second;

      returnedKnownLineStartOffset = knownLineStartOffset;

    } else {
      lineStartOffset = calculateLineOffset(editor, lineNumber);

      returnedKnownLineStartOffset = new Pair<>(lineNumber, lineStartOffset);
    }

    // necessary as the logical position counts a tab char as it's displayed width
    int correctedInLineOffset = offset - lineStartOffset;

    TextPosition textPosition = new TextPosition(lineNumber, correctedInLineOffset);

    return new Pair<>(textPosition, returnedKnownLineStartOffset);
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

    Pair<TextPosition, Pair<Integer, Integer>> startPositionResult =
        calculatePositionInternal(editor, selectionStartOffset, null);

    TextPosition startPosition = startPositionResult.first;

    // Skip second part of calculation if no selection is present
    if (selectionEndOffset == selectionStartOffset) {
      return new TextSelection(startPosition, startPosition);
    }

    Pair<Integer, Integer> knownLineStartOffset = startPositionResult.second;

    Pair<TextPosition, Pair<Integer, Integer>> endPositionResult =
        calculatePositionInternal(editor, selectionEndOffset, knownLineStartOffset);

    TextPosition endPosition = endPositionResult.first;

    return new TextSelection(startPosition, endPosition);
  }

  /**
   * Calculates the absolute offsets in the given editor for the given text position.
   *
   * <p><b>NOTE:</b> The given text position must not be invalid!
   *
   * @param editor the local editor for the file
   * @param textPosition the text position
   * @return the absolute offset in the given editor for the given text position
   * @throws IllegalArgumentException if the given text position is invalid
   */
  public static int calculateOffset(@NotNull Editor editor, @NotNull TextPosition textPosition) {
    if (!textPosition.isValid()) {
      throw new IllegalArgumentException("The given text position must not be invalid");
    }

    return calculateOffsetInternal(editor, textPosition);
  }

  /**
   * Calculates the absolute offsets in the given editor for the given text position.
   *
   * <p>This method does not validate the input. The input is expected to be validated beforehand by
   * the caller.
   *
   * @param editor the local editor for the file
   * @param textPosition the text position
   * @return the absolute offset in the given editor for the given text position
   */
  private static int calculateOffsetInternal(Editor editor, TextPosition textPosition) {
    int lineNumber = textPosition.getLineNumber();
    int lineStartOffset = calculateLineOffset(editor, lineNumber);

    return lineStartOffset + textPosition.getInLineOffset();
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

    int startOffset = calculateOffsetInternal(editor, startPosition);

    TextPosition endPosition = textSelection.getEndPosition();

    if (startPosition.equals(endPosition)) {
      return new Pair<>(startOffset, startOffset);
    }

    int endOffset;

    if (startPosition.getLineNumber() == endPosition.getLineNumber()) {
      int lineStartOffset = startOffset - startPosition.getInLineOffset();

      endOffset = lineStartOffset + endPosition.getInLineOffset();
    } else {
      endOffset = calculateOffset(editor, endPosition);
    }

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
