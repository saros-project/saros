package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.intellij.filesystem.Filesystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** IntellIJ API for project-level operations on editors and documents. */
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
   * Returns a document for the given file.
   *
   * @param file the <code>VirtualFile</code> for which the document is requested
   * @return a <code>Document</code> for the given file or <code>null</code> if the file does not
   *     exist, could not be read, is a directory, or is to large
   */
  @Nullable
  public Document getDocument(@NotNull final VirtualFile file) {
    if (!file.exists()) {
      return null;
    }

    return Filesystem.runReadAction(
        new Computable<Document>() {

          @Override
          public Document compute() {
            return fileDocumentManager.getDocument(file);
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

  /**
   * Saves the given document in the UI thread.
   *
   * @param doc
   */
  public void saveDocument(final Document doc) {
    Filesystem.runWriteAction(
        new Runnable() {

          @Override
          public void run() {
            fileDocumentManager.saveDocument(doc);
          }
        },
        ModalityState.NON_MODAL);
  }

  /**
   * Reloads the current document in the UI thread.
   *
   * @param doc
   */
  public void reloadFromDisk(final Document doc) {
    Filesystem.runReadAction(
        new Runnable() {

          @Override
          public void run() {
            fileDocumentManager.reloadFromDisk(doc);
          }
        });
  }

  /** Saves all documents in the UI thread. */
  public void saveAllDocuments() {
    Filesystem.runWriteAction(
        new Runnable() {

          @Override
          public void run() {
            fileDocumentManager.saveAllDocuments();
          }
        },
        ModalityState.NON_MODAL);
  }

  /**
   * Subscribes the given <code>LocalEditorStatusChangeHandler</code> to the current project.
   *
   * @param listener the listener that should subscribe to the current project
   */
  void addFileEditorManagerListener(@NotNull LocalEditorStatusChangeHandler listener) {
    listener.subscribe(project);
  }
}
