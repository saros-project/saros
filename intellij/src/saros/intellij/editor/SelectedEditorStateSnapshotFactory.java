package saros.intellij.editor;

import saros.intellij.context.SharedIDEContext;

/**
 * Factory class that can be used to create and obtain snapshots of the current selected editor
 * state.
 *
 * @see SelectedEditorStateSnapshot
 */
public class SelectedEditorStateSnapshotFactory {

  private final SharedIDEContext sharedIDEContext;
  private final EditorManager editorManager;

  public SelectedEditorStateSnapshotFactory(
      SharedIDEContext sharedIDEContext, EditorManager editorManager) {
    this.sharedIDEContext = sharedIDEContext;
    this.editorManager = editorManager;
  }

  /**
   * Returns a snapshot of the current selected editor state.
   *
   * @return a snapshot of the current selected editor state
   * @see SelectedEditorStateSnapshot
   */
  public SelectedEditorStateSnapshot capturedState() {
    return new SelectedEditorStateSnapshot(sharedIDEContext, editorManager);
  }
}
