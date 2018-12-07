package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import de.fu_berlin.inf.dpp.activities.SPath;
import org.jetbrains.annotations.NotNull;

/** Dispatches activities for selection changes. */
class LocalTextSelectionChangeHandler extends AbstractStoppableListener {

  private final SelectionListener selectionListener = this::generateSelectionActivity;

  LocalTextSelectionChangeHandler(EditorManager manager) {
    super(manager);
  }

  /**
   * Calls {@link EditorManager#generateSelection(SPath, SelectionEvent)}.
   *
   * @param event the event to react to
   * @see SelectionListener#selectionChanged(SelectionEvent)
   */
  private void generateSelectionActivity(SelectionEvent event) {
    if (!enabled) {
      return;
    }

    SPath path = editorManager.getEditorPool().getFile(event.getEditor().getDocument());
    if (path != null) {
      editorManager.generateSelection(path, event);
    }
  }

  /**
   * Registers the contained SelectionListener to the given editor.
   *
   * @param editor the editor to register
   */
  void register(@NotNull Editor editor) {
    editor.getSelectionModel().addSelectionListener(selectionListener);
  }
}
