package saros.intellij.editor;

import com.intellij.openapi.vfs.VirtualFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import saros.filesystem.IFile;
import saros.intellij.filesystem.VirtualFileConverter;

/** Class used to capture and re-apply which editors are currently selected by the user. */
// TODO consider duplicated open editors during screen splitting
public class SelectedEditorStateSnapshot {
  private static final Logger log = Logger.getLogger(SelectedEditorStateSnapshot.class);

  private final List<VirtualFile> selectedEditors;

  private final ProjectAPI projectAPI;
  private final EditorManager editorManager;

  /**
   * Initializes the object and captures the current selected editor state.
   *
   * @param projectAPI the ProjectAPI object
   * @param editorManager the EditorManager object
   */
  SelectedEditorStateSnapshot(ProjectAPI projectAPI, EditorManager editorManager) {
    this.projectAPI = projectAPI;
    this.editorManager = editorManager;

    this.selectedEditors = new ArrayList<>();

    captureState();
  }

  /** Captures the local selected editor state. */
  private void captureState() {
    selectedEditors.addAll(Arrays.asList(projectAPI.getSelectedFiles()));
  }

  /** Applies the captured selected editor state to the local IDE. */
  public void applyHeldState() {
    ListIterator<VirtualFile> iterator = selectedEditors.listIterator(selectedEditors.size());

    try {
      editorManager.setLocalEditorStatusChangeHandlersEnabled(false);
      editorManager.setLocalViewPortChangeHandlersEnabled(false);

      while (iterator.hasPrevious()) {
        projectAPI.openEditor(iterator.previous(), true);
      }

    } finally {
      editorManager.setLocalViewPortChangeHandlersEnabled(true);
      editorManager.setLocalEditorStatusChangeHandlersEnabled(true);
    }
  }

  /**
   * Replaces the given old file with the given new file in the captured selected editor state.
   *
   * @param oldFile the old file to remove from the captured selected editor state
   * @param newFile the new file to add to the captured selected editor state in the position of the
   *     old file
   */
  public void replaceSelectedFile(@NotNull VirtualFile oldFile, @NotNull VirtualFile newFile) {
    int index = selectedEditors.indexOf(oldFile);

    if (index != -1) {
      selectedEditors.set(index, newFile);

    } else {
      log.debug(
          "Could not replace "
              + oldFile
              + " with "
              + newFile
              + " as the captured state does not contain the given file to "
              + "replace.");
    }
  }

  /**
   * Replaces the given old file with the given new file in the captured selected editor state.
   *
   * @param oldFile the old file to remove from the captured selected editor state
   * @param newFile the new file to add to the captured selected editor state in the position of the
   *     old file
   * @throws IllegalStateException if either the given old or new IFile could not be converted to a
   *     VirtualFile
   */
  public void replaceSelectedFile(@NotNull IFile oldFile, @NotNull IFile newFile) {
    VirtualFile oldVirtualFile = VirtualFileConverter.convertToVirtualFile(oldFile);

    VirtualFile newVirtualFile = VirtualFileConverter.convertToVirtualFile(newFile);

    if (oldVirtualFile == null) {
      throw new IllegalStateException(
          "Could not get a VirtualFile for the old file "
              + oldFile
              + " while trying to replace it with "
              + newFile
              + " - "
              + newVirtualFile);

    } else if (newVirtualFile == null) {
      throw new IllegalStateException(
          "Could not get a VirtualFile for the new file "
              + newFile
              + " while trying to replace "
              + oldFile
              + " - "
              + oldVirtualFile);
    }

    replaceSelectedFile(oldVirtualFile, newVirtualFile);
  }
}
