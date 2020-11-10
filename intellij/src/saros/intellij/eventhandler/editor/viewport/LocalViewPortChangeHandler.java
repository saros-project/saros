package saros.intellij.eventhandler.editor.viewport;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.VisibleAreaEvent;
import com.intellij.openapi.editor.event.VisibleAreaListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.awt.Rectangle;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import saros.editor.text.LineRange;
import saros.filesystem.IFile;
import saros.intellij.editor.DocumentAPI;
import saros.intellij.editor.EditorAPI;
import saros.intellij.editor.EditorManager;
import saros.intellij.eventhandler.IProjectEventHandler;

/** Dispatches activities for viewport changes. */
public class LocalViewPortChangeHandler implements IProjectEventHandler {
  private static final Logger log = Logger.getLogger(LocalViewPortChangeHandler.class);

  private final Project project;
  private final EditorManager editorManager;

  private final VisibleAreaListener visibleAreaListener = this::generateViewportActivity;

  private boolean enabled;
  private boolean disposed;

  /**
   * Instantiates a LocalViewPortChangeHandler object.
   *
   * <p>The handler is disabled and the contained listener is not registered by default.
   *
   * @param editorManager the EditorManager instance
   */
  public LocalViewPortChangeHandler(Project project, EditorManager editorManager) {
    this.project = project;
    this.editorManager = editorManager;

    this.enabled = false;
    this.disposed = false;
  }

  @Override
  @NotNull
  public ProjectEventHandlerType getHandlerType() {
    return ProjectEventHandlerType.VIEWPORT_CHANGE_HANDLER;
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
   * Generates a ViewPortActivity for the viewport change represented by the given activities. The
   * used line range is calculated using logical positions.
   *
   * <p>Calls {@link EditorManager#generateViewport(IFile, LineRange)} to create and dispatch the
   * activity.
   *
   * <p>This method relies on the EditorPool to filter editor events.
   *
   * @param event the event to react to
   * @see VisibleAreaListener#visibleAreaChanged(VisibleAreaEvent)
   * @see LogicalPosition
   */
  private void generateViewportActivity(@NotNull VisibleAreaEvent event) {
    assert enabled : "the visible area listener was triggered while it was disabled";

    Editor editor = event.getEditor();
    Rectangle newVisibleRectangle = event.getNewRectangle();

    IFile file = editorManager.getFileForOpenEditor(editor.getDocument());

    if (file == null) {
      if (log.isTraceEnabled()) {
        VirtualFile virtualFile = DocumentAPI.getVirtualFile(editor.getDocument());

        log.trace(
            "Ignoring local view port change for "
                + virtualFile
                + " as the editor is not known to the editor pool.");
      }

      return;
    }

    if (newVisibleRectangle.x < 0
        || newVisibleRectangle.y < 0
        || newVisibleRectangle.width < 0
        || newVisibleRectangle.height < 0) {

      if (log.isTraceEnabled()) {
        log.trace(
            "Ignoring local viewport change for "
                + file
                + " as the new visible rectangle does not have valid dimensions: "
                + newVisibleRectangle);
      }

      return;
    }

    LineRange newVisibleLineRange = EditorAPI.getLocalViewPortRange(editor, newVisibleRectangle);

    editorManager.generateViewport(file, newVisibleLineRange);
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
      EditorFactory.getInstance()
          .getEventMulticaster()
          .removeVisibleAreaListener(visibleAreaListener);

      this.enabled = false;

    } else if (!this.enabled && enabled) {
      EditorFactory.getInstance()
          .getEventMulticaster()
          .addVisibleAreaListener(visibleAreaListener, project);

      this.enabled = true;
    }
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }
}
