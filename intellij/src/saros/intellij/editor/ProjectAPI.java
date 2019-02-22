package saros.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import saros.intellij.filesystem.Filesystem;

/** IntellIJ API for project-level operations on editors. */
public class ProjectAPI {
  private FileDocumentManager fileDocumentManager;

  private Project project;
  private FileEditorManager editorFileManager;

  /** Creates an ProjectAPI with the current Project and initializes Fields. */
  public ProjectAPI(Project project) {
    this.project = project;

    this.editorFileManager = FileEditorManager.getInstance(project);
    this.fileDocumentManager = FileDocumentManager.getInstance();
  }

  /**
   * Returns whether the file is opened.
   *
   * @param file
   * @return
   */
  public boolean isOpen(VirtualFile file) {
    return editorFileManager.isFileOpen(file);
  }

  /**
   * Returns whether the document is opened.
   *
   * @param doc
   * @return
   */
  public boolean isOpen(Document doc) {
    VirtualFile file = fileDocumentManager.getFile(doc);
    return isOpen(file);
  }

  /**
   * Opens an editor for the given path in the UI thread.
   *
   * @param path path of the file to open
   * @param activate activate editor after opening
   * @return Editor managing the passed file
   */
  public Editor openEditor(final VirtualFile path, final boolean activate) {
    return Filesystem.runReadAction(
        new Computable<Editor>() {

          @Override
          public Editor compute() {
            return editorFileManager.openTextEditor(
                new OpenFileDescriptor(project, path), activate);
          }
        });
  }

  /**
   * Closes the editor for the given file in the UI thread.
   *
   * @param file
   */
  public void closeEditor(final VirtualFile file) {

    Filesystem.runReadAction(
        new Runnable() {

          @Override
          public void run() {
            editorFileManager.closeFile(file);
          }
        });
  }

  public void closeEditor(Document doc) {
    VirtualFile file = fileDocumentManager.getFile(doc);
    closeEditor(file);
  }

  /**
   * Returns an array containing the files of all currently open editors. It is sorted by the order
   * of the editor tabs.
   *
   * @return
   */
  public VirtualFile[] getOpenFiles() {
    return editorFileManager.getOpenFiles();
  }

  public Editor getActiveEditor() {
    return editorFileManager.getSelectedTextEditor();
  }

  /**
   * Returns an array containing the files of all currently active editors. It is sorted by time of
   * last activation, starting with the most recent one.
   *
   * @return
   */
  public VirtualFile[] getSelectedFiles() {
    return editorFileManager.getSelectedFiles();
  }
}
