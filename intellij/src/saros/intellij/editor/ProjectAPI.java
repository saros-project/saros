package saros.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import saros.intellij.runtime.FilesystemRunner;

/** Static utility class for interacting with the project-level Intellij editor API. */
public class ProjectAPI {
  private ProjectAPI() {
    // NOP
  }

  /**
   * Returns whether there is an open editor for the given file.
   *
   * @param project the project in which to check
   * @param virtualFile the file to check
   * @return whether there is an open editor for the given file
   */
  public static boolean isOpen(@NotNull Project project, @NotNull VirtualFile virtualFile) {
    return getFileEditorManager(project).isFileOpen(virtualFile);
  }

  /**
   * Returns whether there is an open editor for the given document.
   *
   * @param project the project in which to check
   * @param document the document to check
   * @return Returns whether there is an open editor for the given document.
   */
  public static boolean isOpen(@NotNull Project project, @NotNull Document document) {
    VirtualFile file = DocumentAPI.getVirtualFile(document);

    if (file == null) {
      throw new IllegalStateException(
          "Could not retrieve the document for the given document " + document);
    }

    return isOpen(project, file);
  }

  /**
   * Opens an editor for the given file in the UI thread.
   *
   * @param project the project in which to open the editor
   * @param virtualFile file for which to open an editor
   * @param activate activate editor after opening
   * @return Editor managing the passed file
   */
  public static Editor openEditor(
      @NotNull Project project, @NotNull VirtualFile virtualFile, final boolean activate) {
    return FilesystemRunner.runReadAction(
        () ->
            getFileEditorManager(project)
                .openTextEditor(new OpenFileDescriptor(project, virtualFile), activate));
  }

  /**
   * Closes the editor for the given file in the UI thread.
   *
   * @param project the project in which to close the editor
   * @param virtualFile the file whose editor to close
   */
  public static void closeEditor(@NotNull Project project, @NotNull final VirtualFile virtualFile) {
    FilesystemRunner.runReadAction(() -> getFileEditorManager(project).closeFile(virtualFile));
  }

  /**
   * Returns an array containing the files of all currently open editors. It is sorted by the order
   * of the editor tabs.
   *
   * @param project the project whose open editors to return
   * @return array containing the files of all currently open editors
   */
  public static VirtualFile[] getOpenFiles(@NotNull Project project) {
    return getFileEditorManager(project).getOpenFiles();
  }

  /**
   * Returns the currently selected editor.
   *
   * @param project the project whose open editor to return
   * @return the currently selected editor
   */
  public static Editor getActiveEditor(@NotNull Project project) {
    return getFileEditorManager(project).getSelectedTextEditor();
  }

  /**
   * Returns an array containing the files of all currently active editors. It is sorted by time of
   * last activation, starting with the most recent one.
   *
   * @param project the project whose selected files to return
   * @return an array containing the files of all currently active editors
   */
  public static VirtualFile[] getSelectedFiles(@NotNull Project project) {
    return getFileEditorManager(project).getSelectedFiles();
  }

  @NotNull
  private static FileEditorManager getFileEditorManager(@NotNull Project project) {
    return FileEditorManager.getInstance(project);
  }

  /**
   * Returns whether the given virtual file is seen as excluded for the given project.
   *
   * @param project the project to check for
   * @param virtualFile the virtual file to check
   * @return whether the given virtual file is seen as excluded for the given project
   * @see ProjectFileIndex#isExcluded(VirtualFile)
   */
  public static boolean isExcluded(@NotNull Project project, @NotNull VirtualFile virtualFile) {
    ProjectFileIndex projectFileIndex = ProjectFileIndex.getInstance(project);
    return FilesystemRunner.runReadAction(() -> projectFileIndex.isExcluded(virtualFile));
  }
}
