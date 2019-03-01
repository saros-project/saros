package de.fu_berlin.inf.dpp.editor.remote;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.editor.text.LineRange;
import de.fu_berlin.inf.dpp.editor.text.TextSelection;

/** Represents the current status of an editor. */
public class EditorState {

  private final SPath path;

  private TextSelection selection;

  private LineRange viewport;

  EditorState(SPath path) {
    this.path = path;
  }

  EditorState(SPath path, LineRange viewport, TextSelection selection) {
    this.path = path;
    this.viewport = viewport;
    this.selection = selection;
  }

  /**
   * Returns the path of the file of which is the content is displayed in the editor described by
   * this state object.
   */
  public SPath getPath() {
    return path;
  }

  /**
   * Returns the currently displayed viewport, i.e. the currently visible lines in the editor this
   * state object describes. Might be <code>null</code>, which means there is no known viewport
   * (yet).
   */
  public LineRange getViewport() {
    return viewport;
  }

  void setViewport(LineRange viewport) {
    this.viewport = viewport;
  }

  /**
   * Returns the selection in the editor described by this state object or <code>null</code> if
   * there is no selection in the editor.
   */
  public TextSelection getSelection() {
    return selection;
  }

  void setSelection(TextSelection selection) {
    this.selection = selection;
  }
}
