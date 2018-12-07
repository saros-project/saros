package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.intellij.filesystem.VirtualFileConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.picocontainer.annotations.Inject;

/** Class used to capture and re-apply which editors are currently selected by the user. */
// TODO consider duplicated open editors during screen splitting
public class SelectedEditorState {
  private static final Logger log = Logger.getLogger(SelectedEditorState.class);

  private final List<VirtualFile> selectedEditors;

  private boolean hasCapturedState;

  @Inject private static ProjectAPI projectAPI;

  @Inject private static EditorManager editorManager;

  @Inject private static Project project;

  static {
    SarosPluginContext.initComponent(new SelectedEditorState());
  }

  public SelectedEditorState() {
    this.selectedEditors = new ArrayList<>();
    this.hasCapturedState = false;
  }

  /** Captures the local selected editor state. */
  public void captureState() {
    if (hasCapturedState) {
      log.warn("Overwriting existing captured selected editor state.");

      selectedEditors.clear();
    }

    selectedEditors.addAll(Arrays.asList(projectAPI.getSelectedFiles()));

    hasCapturedState = true;
  }

  /** Applies the captured selected editor state to the local IDE. */
  public void applyCapturedState() {
    if (!hasCapturedState) {
      log.warn("Trying to applying state before capturing a local state to" + "re-apply.");

      return;
    }

    ListIterator<VirtualFile> iterator = selectedEditors.listIterator(selectedEditors.size());

    try {
      editorManager.getLocalEditorStatusChangeHandler().unsubscribe();

      while (iterator.hasPrevious()) {
        projectAPI.openEditor(iterator.previous(), true);
      }

    } finally {
      editorManager.getLocalEditorStatusChangeHandler().subscribe(project);
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

    if (!hasCapturedState) {
      log.warn(
          "Trying to replace file in state before capturing a local "
              + "state. old file: "
              + oldFile
              + ", new file: "
              + newFile);

      return;
    }

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
