package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.VisibleAreaEvent;
import com.intellij.openapi.editor.event.VisibleAreaListener;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.editor.text.LineRange;
import java.awt.Rectangle;
import org.jetbrains.annotations.NotNull;

/** Dispatches activities for viewport changes. */
class StoppableViewPortListener extends AbstractStoppableListener {

  private final VisibleAreaListener visibleAreaListener = this::generateViewportActivity;

  StoppableViewPortListener(EditorManager manager) {
    super(manager);
  }

  /**
   * Calls {@link EditorManager#generateViewport(SPath, LineRange)}.
   *
   * @param event the event to react to
   * @see VisibleAreaListener#visibleAreaChanged(VisibleAreaEvent)
   */
  private void generateViewportActivity(VisibleAreaEvent event) {
    if (!enabled) {
      return;
    }
    SPath path = editorManager.getEditorPool().getFile(event.getEditor().getDocument());

    if (path != null) {
      editorManager.generateViewport(path, getLineRange(event));
    }
  }

  private LineRange getLineRange(VisibleAreaEvent event) {
    Rectangle rec = event.getEditor().getScrollingModel().getVisibleAreaOnScrollingFinished();
    int lineHeight = event.getEditor().getLineHeight();

    return new LineRange((int) (rec.getMinY() / lineHeight), (int) (rec.getMaxY() / lineHeight));
  }

  /**
   * Registers the contained VisibleAreaListener to the given editor.
   *
   * @param editor the editor to register
   */
  void register(@NotNull Editor editor) {
    editor.getScrollingModel().addVisibleAreaListener(visibleAreaListener);
  }
}
