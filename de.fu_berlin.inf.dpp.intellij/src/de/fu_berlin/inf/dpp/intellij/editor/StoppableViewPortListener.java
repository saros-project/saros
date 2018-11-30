package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.event.VisibleAreaEvent;
import com.intellij.openapi.editor.event.VisibleAreaListener;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.editor.text.LineRange;
import java.awt.Rectangle;

/** Intellij editor ViewPort listener */
public class StoppableViewPortListener extends AbstractStoppableListener
    implements VisibleAreaListener {

  public StoppableViewPortListener(EditorManager manager) {
    super(manager);
  }

  /**
   * Calls {@link EditorManager#generateViewport(SPath, LineRange)}.
   *
   * @param event
   */
  @Override
  public void visibleAreaChanged(VisibleAreaEvent event) {
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
}
