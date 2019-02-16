package saros.intellij.eventhandler.editor.selection;

import com.intellij.openapi.editor.EditorFactory;
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
  private boolean disposed;

  /**
   * Instantiates a LocalTextSelectionChangeHandler object. The handler is enabled by default and
   * the contained listener is registered by default.
   *
   * @param editorManager the EditorManager instance
   */
  public LocalTextSelectionChangeHandler(EditorManager editorManager) {
    this.editorManager = editorManager;

    this.enabled = false;
    this.disposed = false;
  }

  @Override
  public void start() {
    setEnabled(true);
  }

  @Override
  public void stop() {
    disposed = true;
    setEnabled(false);
  }

  /**
   * Calls {@link EditorManager#generateSelection(SPath, SelectionEvent)}.
   *
   * <p>This method relies on the EditorPool to filter editor events.
   *
   * @param event the event to react to
   * @see SelectionListener#selectionChanged(SelectionEvent)
   */
  private void generateSelectionActivity(@NotNull SelectionEvent event) {
    assert enabled : "the selection changed listener was triggered while it was disabled";

    SPath path = editorManager.getFileForOpenEditor(event.getEditor().getDocument());

    if (path != null) {
      editorManager.generateSelection(path, event);
    }
  }

  /**
   * Enables or disables the handler. This is not done by disabling the underlying listener.
   *
   * @param enabled <code>true</code> to enable the handler, <code>false</code> disable the handler
   */
  @Override
  public void setEnabled(boolean enabled) {
    assert !disposed || !enabled : "disposed listeners must not be enabled";

    if (this.enabled && !enabled) {
      EditorFactory.getInstance().getEventMulticaster().removeSelectionListener(selectionListener);

      this.enabled = false;

    } else if (!this.enabled && enabled) {
      EditorFactory.getInstance().getEventMulticaster().addSelectionListener(selectionListener);

      this.enabled = true;
    }
  }
}
