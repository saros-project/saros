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
import de.fu_berlin.inf.dpp.editor.text.LineRange;
import de.fu_berlin.inf.dpp.intellij.filesystem.Filesystem;
import java.awt.Point;
import java.awt.Rectangle;

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
   * @param editor the editor to get the viewport line range for
   * @return the logical line range of the local viewport for the given editor
   * @see LogicalPosition
   */
  LineRange getLocalViewportRange(Editor editor) {
    Rectangle visibleAreaRectangle = editor.getScrollingModel().getVisibleAreaOnScrollingFinished();

    int basePos = visibleAreaRectangle.y;
    int endPos = visibleAreaRectangle.y + visibleAreaRectangle.height;

    int currentViewportStartLine = editor.xyToLogicalPosition(new Point(0, basePos)).line;
    int currentViewportEndLine = editor.xyToLogicalPosition(new Point(0, endPos)).line;

    return new LineRange(
        currentViewportStartLine, currentViewportEndLine - currentViewportStartLine);
  }

  /**
   * Inserts text at the given position inside the UI thread.
   *
   * @param doc
   * @param position
   * @param text
   */
  public void insertText(final Document doc, final int position, final String text) {

    Runnable action =
        new Runnable() {
          @Override
          public void run() {
            commandProcessor.executeCommand(
                project,
                new Runnable() {

                  @Override
                  public void run() {
                    doc.insertString(position, text);
                  }
                },
                "Saros text insertion at index " + position + " of \"" + text + "\"",
                commandProcessor.getCurrentCommandGroupId(),
                UndoConfirmationPolicy.REQUEST_CONFIRMATION,
                doc);
          }
        };

    Filesystem.runWriteAction(action, ModalityState.defaultModalityState());
  }

  /**
   * Deletes text in document in the specified range in the UI thread.
   *
   * @param doc
   * @param start
   * @param end
   */
  public void deleteText(final Document doc, final int start, final int end) {
    Runnable action =
        new Runnable() {
          @Override
          public void run() {
            commandProcessor.executeCommand(
                project,
                new Runnable() {

                  @Override
                  public void run() {
                    doc.deleteString(start, end);
                  }
                },
                "Saros text deletion from index " + start + " to " + end,
                commandProcessor.getCurrentCommandGroupId(),
                UndoConfirmationPolicy.REQUEST_CONFIRMATION,
                doc);
          }
        };

    Filesystem.runWriteAction(action, ModalityState.defaultModalityState());
  }
}
