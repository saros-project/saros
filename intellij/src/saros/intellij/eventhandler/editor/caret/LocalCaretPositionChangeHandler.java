package saros.intellij.eventhandler.editor.caret;

import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.project.Project;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import saros.filesystem.IFile;
import saros.intellij.editor.EditorManager;
import saros.intellij.eventhandler.IProjectEventHandler;

/**
 * Dispatches text selection activities for caret moves.
 *
 * <p>This handler is only triggered for explicit caret moves caused by the user and not implicit
 * caret moves caused by text edits (even if these edits were caused by the user).
 *
 * <p>Caret moves caused by document modifications are handled in {@link
 * EditorManager#generateTextEdit(int, String, String, IFile, Document)}.
 *
 * @see CaretListener#caretPositionChanged(CaretEvent)
 */
public class LocalCaretPositionChangeHandler implements IProjectEventHandler {
  private static final Logger log = Logger.getLogger(LocalCaretPositionChangeHandler.class);

  private final Project project;
  private final EditorManager editorManager;

  private final CaretListener caretListener =
      new CaretListener() {
        @Override
        public void caretPositionChanged(@NotNull CaretEvent event) {
          handleCaretPositionChange(event);
        }
      };

  private final SelectionListener selectionListener =
      new SelectionListener() {
        @Override
        public void selectionChanged(@NotNull SelectionEvent selectionEvent) {
          disableForEditorsWithSelection(selectionEvent);
        }
      };

  /**
   * Set of shared editors that currently have an active selection and are therefore ignored.
   *
   * @see #disableForEditorsWithSelection(SelectionEvent)
   */
  private Set<Editor> editorsWithSelection;

  private boolean enabled;
  private boolean disposed;

  public LocalCaretPositionChangeHandler(Project project, EditorManager editorManager) {
    this.project = project;
    this.editorManager = editorManager;

    this.editorsWithSelection = Collections.synchronizedSet(new HashSet<>());
    this.enabled = false;
    this.disposed = false;
  }

  @NotNull
  @Override
  public ProjectEventHandlerType getHandlerType() {
    return ProjectEventHandlerType.CARET_LOCATION_CHANGE_HANDLER;
  }

  @Override
  public void initialize() {
    setEnabled(true);
  }

  @Override
  public void dispose() {
    disposed = true;
    setEnabled(false);
  }

  /**
   * Calls {@link EditorManager#generateSelection(IFile, Editor, int, int)}.
   *
   * <p>This method relies on the EditorPool to filter editor events.
   *
   * <p>Does nothing if the editor of the event is contained in {@link #editorsWithSelection}.
   *
   * @param event the event to react to
   * @see CaretListener#caretPositionChanged(CaretEvent)
   * @see #disableForEditorsWithSelection(SelectionEvent)
   */
  private void handleCaretPositionChange(@NotNull CaretEvent event) {
    assert enabled : "the selection changed listener was triggered while it was disabled";

    Editor editor = event.getEditor();

    if (editorsWithSelection.contains(editor)) {
      return;
    }

    Caret caret = event.getCaret();
    IFile file = editorManager.getFileForOpenEditor(editor.getDocument());

    if (file == null || caret == null) {
      return;
    }

    if (!caret.equals(caret.getCaretModel().getPrimaryCaret())) {
      log.debug("filtered out caret event for non-primary caret " + caret);
      return;
    }

    int caretPosition = caret.getOffset();

    editorManager.generateSelection(file, editor, caretPosition, caretPosition);
  }

  /**
   * Disables the caret listener internally while there is a selection for the editor.
   *
   * <p>This is a hack to avoid issue described here:
   * https://intellij-support.jetbrains.com/hc/en-us/community/posts/360006880480-CaretListener-caretPositionChanged-gets-called-before-selection-data-is-updated
   *
   * <p>As the caret listener can not be used to obtain current selection values, we are not
   * interested in the caret listener while there is an active selection. To avoid creating too many
   * unnecessary text selection activities, this method disables the caret listener for the editor
   * if it has an active selection. The selection listener is called one last time when there no
   * longer is a local selected, which allows us to re-enable the caret listener for the editor.
   *
   * @param selectionEvent the event to react to
   */
  private void disableForEditorsWithSelection(@NotNull SelectionEvent selectionEvent) {
    Editor editor = selectionEvent.getEditor();
    IFile file = editorManager.getFileForOpenEditor(editor.getDocument());

    if (file == null) {
      return;
    }

    int selectionStartingPoint = selectionEvent.getNewRange().getStartOffset();
    int selectionEndingPoint = selectionEvent.getNewRange().getEndOffset();

    if (selectionStartingPoint != selectionEndingPoint) {
      editorsWithSelection.add(editor);
    } else {
      editorsWithSelection.remove(editor);
    }
  }

  /**
   * Enables or disables the handler.
   *
   * @param enabled <code>true</code> to enable the handler, <code>false</code> disable the handler
   */
  @Override
  public void setEnabled(boolean enabled) {
    assert !disposed || !enabled : "disposed listeners must not be enabled";

    EditorEventMulticaster editorEventMulticaster =
        EditorFactory.getInstance().getEventMulticaster();

    if (this.enabled && !enabled) {
      editorEventMulticaster.removeCaretListener(caretListener);
      editorEventMulticaster.removeSelectionListener(selectionListener);

      this.enabled = false;

    } else if (!this.enabled && enabled) {
      editorEventMulticaster.addCaretListener(caretListener, project);
      editorEventMulticaster.addSelectionListener(selectionListener, project);

      this.enabled = true;
    }
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }
}
