package saros.intellij.editor;

/**
 * Factory class that can be used to create and obtain snapshots of the current selected editor
 * state.
 *
 * @see SelectedEditorStateSnapshot
 */
public class SelectedEditorStateSnapshotFactory {

  private final ProjectAPI projectAPI;
  private final EditorManager editorManager;

  public SelectedEditorStateSnapshotFactory(ProjectAPI projectAPI, EditorManager editorManager) {
    this.projectAPI = projectAPI;
    this.editorManager = editorManager;
  }

  /**
   * Returns a snapshot of the current selected editor state.
   *
   * @return a snapshot of the current selected editor state
   * @see SelectedEditorStateSnapshot
   */
  public SelectedEditorStateSnapshot capturedState() {
    return new SelectedEditorStateSnapshot(projectAPI, editorManager);
  }
}
