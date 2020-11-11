package saros.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.intellij.runtime.EDTExecutor;
import saros.intellij.runtime.FilesystemRunner;

/** Static utility class for interacting with the project-level Intellij editor API. */
public class ProjectAPI {
  private static final EditorFactory editorFactory = EditorFactory.getInstance();

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
   * Returns whether there is an open text editor for the given file.
   *
   * @param project the project in which to check
   * @param file the file to check
   * @return whether there is an open text editor for the given file
   */
  public static boolean isOpenInTextEditor(@NotNull Project project, @NotNull VirtualFile file) {
    FileEditorManager fileEditorManager = getFileEditorManager(project);

    FileEditor[] fileEditors = EDTExecutor.invokeAndWait(() -> fileEditorManager.getEditors(file));

    for (FileEditor fileEditor : fileEditors) {
      if (fileEditor instanceof TextEditor) {
        return true;
      }
    }

    return false;
  }

  /**
   * Opens an editor for the given file in the UI thread.
   *
   * @param project the project in which to open the editor
   * @param virtualFile file for which to open an editor
   * @param activate activate editor after opening
   * @return text editor managing the passed file or <code>null</code> if the opened editor is not a
   *     text editor
   */
  @Nullable
  public static Editor openEditor(
      @NotNull Project project, @NotNull VirtualFile virtualFile, final boolean activate) {

    FileEditorManager fileEditorManager = getFileEditorManager(project);

    return EDTExecutor.invokeAndWait(
        () ->
            fileEditorManager.openTextEditor(
                new OpenFileDescriptor(project, virtualFile), activate));
  }

  /**
   * Closes the editor for the given file in the UI thread.
   *
   * @param project the project in which to close the editor
   * @param virtualFile the file whose editor to close
   */
  public static void closeEditor(@NotNull Project project, @NotNull final VirtualFile virtualFile) {
    FileEditorManager fileEditorManager = getFileEditorManager(project);

    EDTExecutor.invokeAndWait(() -> fileEditorManager.closeFile(virtualFile));
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
    FileEditorManager fileEditorManager = getFileEditorManager(project);

    return EDTExecutor.invokeAndWait(fileEditorManager::getSelectedTextEditor);
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
   * Returns whether the given virtual file is part of the content of the given project.
   *
   * <p>A resource is seen as part of the content of a project if it is located under the content
   * root of any of the modules of the project.
   *
   * @param project the project for which to check
   * @param virtualFile the virtual file to check
   * @return whether the given virtual file is part of the content of the given project
   * @see ProjectFileIndex#isInContent(VirtualFile)
   */
  public static boolean isInProjectContent(
      @NotNull Project project, @NotNull VirtualFile virtualFile) {
    ProjectFileIndex projectFileIndex = ProjectFileIndex.getInstance(project);
    return FilesystemRunner.runReadAction(() -> projectFileIndex.isInContent(virtualFile));
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

  /**
   * Instantiates and returns a background editor for the given document. When no longer needed, the
   * editor <b>must</b> be released using {@link #releaseBackgroundEditor(Editor)}.
   *
   * <p><b>NOTE:</b> This method is only meant to allow optimized access to the editor API for
   * resources that are currently not open in an editor. Editors obtained through this method are
   * not actually visible to the user.
   *
   * <p>To open user-visible editors, use {@link ProjectAPI#openEditor(Project, VirtualFile,
   * boolean)} instead.
   *
   * @param document the document to open a background editor for
   * @param project the project to associate the editor with
   * @return a background editor for the given document
   */
  @NotNull
  public static Editor createBackgroundEditor(
      @NotNull Document document, @NotNull Project project) {

    return EDTExecutor.invokeAndWait(() -> editorFactory.createEditor(document, project));
  }

  /**
   * Releases the given editor. This should be called on any background editor that is no longer
   * needed/will no longer be used to dispose it correctly.
   *
   * <p><b>NOTE:</b> This method must only be called for background editors that were obtained using
   * {@link #createBackgroundEditor(Document, Project)}.
   *
   * @param editor the background editor to release
   * @see EditorFactory#releaseEditor(Editor)
   */
  public static void releaseBackgroundEditor(@NotNull Editor editor) {
    EDTExecutor.invokeAndWait(() -> editorFactory.releaseEditor(editor));
  }
}
