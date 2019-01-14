package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.VisibleAreaEvent;
import com.intellij.openapi.editor.event.VisibleAreaListener;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.editor.text.LineRange;
import org.jetbrains.annotations.NotNull;

/** Dispatches activities for viewport changes. */
class LocalViewPortChangeHandler implements DisableableHandler {

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
  LocalViewPortChangeHandler(EditorManager editorManager, EditorAPI editorAPI) {
    this.editorManager = editorManager;
    this.editorAPI = editorAPI;

    this.enabled = true;
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
  private void generateViewportActivity(VisibleAreaEvent event) {
    if (!enabled) {
      return;
    }

    Editor editor = event.getEditor();

    SPath path = editorManager.getEditorPool().getFile(editor.getDocument());

    if (path != null) {
      editorManager.generateViewport(path, editorAPI.getLocalViewportRange(editor));
    }
  }

  /**
   * Registers the contained VisibleAreaListener to the given editor.
   *
   * @param editor the editor to register
   */
  void register(@NotNull Editor editor) {
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
