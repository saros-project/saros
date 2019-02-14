package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import de.fu_berlin.inf.dpp.activities.SPath;
import org.jetbrains.annotations.NotNull;

/** Dispatches activities for selection changes. */
class LocalTextSelectionChangeHandler implements DisableableHandler {

  private final EditorManager editorManager;

  private final SelectionListener selectionListener =
      new SelectionListener() {
        @Override
        public void selectionChanged(@NotNull SelectionEvent e) {
          generateSelectionActivity(e);
        }
      };

  private boolean enabled;

  /**
   * Instantiates a LocalTextSelectionChangeHandler object. The handler is enabled by default. The
   * contained listener is disabled by default and has to be enabled separately for every editor
   * using {@link #register(Editor)}.
   *
   * @param editorManager the EditorManager instance
   */
  LocalTextSelectionChangeHandler(EditorManager editorManager) {
    this.editorManager = editorManager;

    this.enabled = true;
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

  /**
   * Enables or disabled the handler. This is not done by disabling the underlying listener.
   *
   * @param enabled <code>true</code> to enable the handler, <code>false</code> disable the handler
   */
  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
