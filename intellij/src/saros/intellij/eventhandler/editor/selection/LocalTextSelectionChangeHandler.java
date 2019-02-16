package saros.intellij.eventhandler.editor.selection;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import org.jetbrains.annotations.NotNull;
import org.picocontainer.Startable;
import saros.activities.SPath;
import saros.intellij.editor.EditorManager;
import saros.intellij.eventhandler.DisableableHandler;

/** Dispatches activities for selection changes. */
public class LocalTextSelectionChangeHandler implements DisableableHandler, Startable {

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
  public LocalTextSelectionChangeHandler(EditorManager editorManager) {
    this.editorManager = editorManager;

    this.enabled = false;
  }

  @Override
  public void start() {
    setEnabled(true);
  }

  @Override
  public void stop() {
    setEnabled(false);
  }

  /**
   * Calls {@link EditorManager#generateSelection(SPath, SelectionEvent)}.
   *
   * @param event the event to react to
   * @see SelectionListener#selectionChanged(SelectionEvent)
   */
  private void generateSelectionActivity(@NotNull SelectionEvent event) {
    if (!enabled) {
      return;
    }

    SPath path = editorManager.getFileForOpenEditor(event.getEditor().getDocument());
    if (path != null) {
      editorManager.generateSelection(path, event);
    }
  }

  /**
   * Registers the contained SelectionListener to the given editor.
   *
   * @param editor the editor to register
   */
  public void register(@NotNull Editor editor) {
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
