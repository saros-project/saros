package saros.editor.remote;

import saros.editor.text.LineRange;
import saros.editor.text.TextSelection;
import saros.filesystem.IFile;

/** Represents the current status of an editor. */
public class EditorState {

  private final IFile file;

  private TextSelection selection;

  private LineRange viewport;

  EditorState(IFile file) {
    this.file = file;
  }

  EditorState(IFile file, LineRange viewport, TextSelection selection) {
    this.file = file;
    this.viewport = viewport;
    this.selection = selection;
  }

  /**
   * Returns the file of which is the content is displayed in the editor described by this state
   * object.
   */
  public IFile getFile() {
    return file;
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
