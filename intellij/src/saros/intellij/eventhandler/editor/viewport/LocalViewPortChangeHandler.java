package saros.intellij.eventhandler.editor.viewport;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.VisibleAreaEvent;
import com.intellij.openapi.editor.event.VisibleAreaListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import java.awt.Rectangle;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.picocontainer.Startable;
import saros.activities.SPath;
import saros.editor.text.LineRange;
import saros.intellij.editor.EditorAPI;
import saros.intellij.editor.EditorManager;
import saros.intellij.eventhandler.DisableableHandler;

/** Dispatches activities for viewport changes. */
public class LocalViewPortChangeHandler implements DisableableHandler, Startable {
  private static final Logger log = Logger.getLogger(LocalViewPortChangeHandler.class);

  private final EditorManager editorManager;
  private final EditorAPI editorAPI;

  private final VisibleAreaListener visibleAreaListener = this::generateViewportActivity;

  private boolean enabled;

  /**
   * Instantiates a LocalViewPortChangeHandler object. The handler is enabled by default. The
   * contained listener is disabled by default and has to be enabled separately for every editor
   * using {@link #register(Editor)}.
   *
   * @param editorManager the EditorManager instance
   */
  public LocalViewPortChangeHandler(EditorManager editorManager, EditorAPI editorAPI) {
    this.editorManager = editorManager;
    this.editorAPI = editorAPI;

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
   * Generates a ViewPortActivity for the viewport change represented by the given activities. The
   * used line range is calculated using logical positions.
   *
   * <p>Calls {@link EditorManager#generateViewport(SPath, LineRange)} to create and dispatch the
   * activity.
   *
   * @param event the event to react to
   * @see VisibleAreaListener#visibleAreaChanged(VisibleAreaEvent)
   * @see LogicalPosition
   */
  private void generateViewportActivity(@NotNull VisibleAreaEvent event) {
    if (!enabled) {
      return;
    }

    Editor editor = event.getEditor();
    Rectangle newVisibleRectangle = event.getNewRectangle();

    SPath path = editorManager.getFileForOpenEditor(editor.getDocument());

    if (path == null) {
      if (log.isTraceEnabled()) {
        VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());

        log.trace(
            "Ignoring local view port change for "
                + file
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
                + path
                + " as the new visible rectangle does not have valid dimensions: "
                + newVisibleRectangle);
      }

      return;
    }

    LineRange newVisibleLineRange = editorAPI.getLocalViewPortRange(editor, newVisibleRectangle);

    editorManager.generateViewport(path, newVisibleLineRange);
  }

  /**
   * Registers the contained VisibleAreaListener to the given editor.
   *
   * @param editor the editor to register
   */
  public void register(@NotNull Editor editor) {
    editor.getScrollingModel().addVisibleAreaListener(visibleAreaListener);
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
