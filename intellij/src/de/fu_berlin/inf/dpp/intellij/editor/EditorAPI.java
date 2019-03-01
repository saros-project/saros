package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import de.fu_berlin.inf.dpp.editor.text.LineRange;
import de.fu_berlin.inf.dpp.intellij.filesystem.Filesystem;
import java.awt.Point;
import java.awt.Rectangle;
import org.jetbrains.annotations.NotNull;

/**
 * IntellJ editor API. An Editor is a window for editing source files.
 *
 * <p>Performs IntelliJ editor related actions in the UI thread.
 */
public class EditorAPI {

  private Application application;
  private CommandProcessor commandProcessor;

  private Project project;

  /** Creates an EditorAPI with the current Project and initializes Fields. */
  public EditorAPI(Project project) {
    this.project = project;
    this.application = ApplicationManager.getApplication();
    this.commandProcessor = CommandProcessor.getInstance();
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
  void scrollToViewPortCenter(final Editor editor, final int line) {
    application.invokeAndWait(
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
  LineRange getLocalViewPortRange(@NotNull Editor editor) {
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
  public LineRange getLocalViewPortRange(
      @NotNull Editor editor, @NotNull Rectangle visibleAreaRectangle) {
    int basePos = visibleAreaRectangle.y;
    int endPos = visibleAreaRectangle.y + visibleAreaRectangle.height;

    int currentViewportStartLine = editor.xyToLogicalPosition(new Point(0, basePos)).line;
    int currentViewportEndLine = editor.xyToLogicalPosition(new Point(0, endPos)).line;

    return new LineRange(
        currentViewportStartLine, currentViewportEndLine - currentViewportStartLine);
  }

  /**
   * Returns the offset and length of the local selection for the given editor.
   *
   * <p>The values are returned as a {@link Pair}. The first value is the starting offset of the
   * selection and the second value is the length of the selection.
   *
   * @param editor the editor to get the local selection offsets for.
   * @return a Pair containing the local selection offset and length for the given editor.
   */
  @NotNull
  Pair<Integer, Integer> getLocalSelectionOffsets(@NotNull Editor editor) {
    int selectionStartOffset = editor.getSelectionModel().getSelectionStart();
    int selectionEndOffset = editor.getSelectionModel().getSelectionEnd();

    int selectionLength = selectionEndOffset - selectionStartOffset;

    return new Pair<>(selectionStartOffset, selectionLength);
  }

  /**
   * Returns the logical line range of the section represented by the given offsets for the given
   * editor.
   *
   * @param editor the editor to get the line range for
   * @param startOffset the start offset of the section
   * @param endOffset the end offset of the section
   * @return the logical line range of the section represented by the given offsets for the given
   *     editor
   * @see LogicalPosition
   */
  @NotNull
  LineRange getLineRange(@NotNull Editor editor, int startOffset, int endOffset) {
    assert startOffset <= endOffset;

    int startLine = editor.offsetToLogicalPosition(startOffset).line;
    int endLine = editor.offsetToLogicalPosition(endOffset).line;

    int rangeLength = endLine - startLine;

    return new LineRange(startLine, rangeLength);
  }

  /**
   * Inserts the specified text at the specified offset in the document. Line breaks in the inserted
   * text must be normalized as \n.
   *
   * @param document the document to insert the text into
   * @param offset the offset to insert the text at
   * @param text the text to insert
   * @see Document#insertString(int, CharSequence)
   */
  void insertText(@NotNull final Document document, final int offset, final String text) {

    Runnable insertCommand =
        () -> {
          Runnable insertString = () -> document.insertString(offset, text);

          String commandName = "Saros text insertion at index " + offset + " of \"" + text + "\"";

          commandProcessor.executeCommand(
              project,
              insertString,
              commandName,
              commandProcessor.getCurrentCommandGroupId(),
              UndoConfirmationPolicy.REQUEST_CONFIRMATION,
              document);
        };

    Filesystem.runWriteAction(insertCommand, ModalityState.defaultModalityState());
  }

  /**
   * Deletes the specified range of text from the given document.
   *
   * @param doc the document to delete text from
   * @param start the start offset of the range to delete
   * @param end the end offset of the range to delete
   * @see Document#deleteString(int, int)
   */
  void deleteText(@NotNull final Document doc, final int start, final int end) {
    Runnable deletionCommand =
        () -> {
          Runnable deleteRange = () -> doc.deleteString(start, end);

          String commandName = "Saros text deletion from index " + start + " to " + end;

          commandProcessor.executeCommand(
              project,
              deleteRange,
              commandName,
              commandProcessor.getCurrentCommandGroupId(),
              UndoConfirmationPolicy.REQUEST_CONFIRMATION,
              doc);
        };

    Filesystem.runWriteAction(deletionCommand, ModalityState.defaultModalityState());
  }
}
