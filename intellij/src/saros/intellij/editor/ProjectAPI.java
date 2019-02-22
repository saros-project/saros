package saros.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import saros.intellij.filesystem.Filesystem;

/** Wrapper for interacting with the project-level Intellij editor API. */
public class ProjectAPI {
  private Project project;
  private FileEditorManager editorFileManager;

  public ProjectAPI(Project project) {
    this.project = project;

    this.editorFileManager = FileEditorManager.getInstance(project);
  }

  /**
   * Returns whether there is an open editor for the given file.
   *
   * @param virtualFile the file to check
   * @return whether there is an open editor for the given file
   */
  public boolean isOpen(VirtualFile virtualFile) {
    return editorFileManager.isFileOpen(virtualFile);
  }

  /**
   * Returns whether there is an open editor for the given document.
   *
   * @param document the document to check
   * @return Returns whether there is an open editor for the given document.
   */
  public boolean isOpen(Document document) {
    VirtualFile file = DocumentAPI.getVirtualFile(document);

    return isOpen(file);
  }

  /**
   * Opens an editor for the given file in the UI thread.
   *
   * @param virtualFile file for which to open an editor
   * @param activate activate editor after opening
   * @return Editor managing the passed file
   */
  public Editor openEditor(final VirtualFile virtualFile, final boolean activate) {
    return Filesystem.runReadAction(
        () ->
            editorFileManager.openTextEditor(
                new OpenFileDescriptor(project, virtualFile), activate));
  }

  /**
   * Closes the editor for the given file in the UI thread.
   *
   * @param virtualFile the file whose editor to close
   */
  public void closeEditor(final VirtualFile virtualFile) {

    Filesystem.runReadAction(() -> editorFileManager.closeFile(virtualFile));
  }

  /**
   * Returns an array containing the files of all currently open editors. It is sorted by the order
   * of the editor tabs.
   *
   * @return array containing the files of all currently open editors
   */
  public VirtualFile[] getOpenFiles() {
    return editorFileManager.getOpenFiles();
  }

  /**
   * Returns the currently selected editor.
   *
   * @return the currently selected editor
   */
  public Editor getActiveEditor() {
    return editorFileManager.getSelectedTextEditor();
  }

  /**
   * Returns an array containing the files of all currently active editors. It is sorted by time of
   * last activation, starting with the most recent one.
   *
   * @return an array containing the files of all currently active editors
   */
  public VirtualFile[] getSelectedFiles() {
    return editorFileManager.getSelectedFiles();
  }
}
