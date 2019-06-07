package saros.intellij.eventhandler.editor.selection;

import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import org.jetbrains.annotations.NotNull;
import saros.activities.SPath;
import saros.intellij.editor.EditorManager;
import saros.intellij.eventhandler.IProjectEventHandler;

/** Dispatches activities for selection changes. */
public class LocalTextSelectionChangeHandler implements IProjectEventHandler {

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
   * Instantiates a LocalTextSelectionChangeHandler object.
   *
   * <p>The handler is disabled and the listener is not registered by default.
   *
   * @param editorManager the EditorManager instance
   */
  public LocalTextSelectionChangeHandler(EditorManager editorManager) {
    this.editorManager = editorManager;

    this.enabled = false;
    this.disposed = false;
  }

  @Override
  @NotNull
  public ProjectEventHandlerType getHandlerType() {
    return ProjectEventHandlerType.TEXT_SELECTION_CHANGE_HANDLER;
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

  @Override
  public boolean isEnabled() {
    return enabled;
  }
}
