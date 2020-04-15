package saros.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.filesystem.IFile;
import saros.filesystem.IProject;
import saros.intellij.filesystem.IntelliJProjectImpl;
import saros.intellij.filesystem.VirtualFileConverter;
import saros.session.ISarosSession;

/**
 * Class for handling activities on local editors and transforming them to calls to {@link
 * EditorManager} for generating activities .
 */
public class LocalEditorHandler {

  private static final Logger log = Logger.getLogger(LocalEditorHandler.class);

  private final EditorManager manager;
  private final ISarosSession sarosSession;

  /** This is just a reference to {@link EditorManager}'s editorPool and not a separate pool. */
  private final EditorPool editorPool;

  public LocalEditorHandler(EditorManager editorManager, ISarosSession sarosSession) {
    this.manager = editorManager;
    this.sarosSession = sarosSession;

    this.editorPool = manager.getEditorPool();
  }

  /**
   * Opens an editor for the passed virtualFile, adds it to the pool of currently open editors and
   * calls {@link EditorManager#startEditor(Editor)} with it.
   *
   * <p><b>Note:</b> This only works for shared resources.
   *
   * @param project the project in which to open the editor
   * @param virtualFile the file to open
   * @param activate activate editor after opening
   * @return the opened <code>Editor</code> or <code>null</code> if the given file does not belong
   *     to a shared module
   */
  @Nullable
  public Editor openEditor(
      @NotNull Project project, @NotNull VirtualFile virtualFile, boolean activate) {

    IFile file = (IFile) VirtualFileConverter.convertToResource(project, virtualFile);

    if (file == null || !sarosSession.isShared(file)) {
      log.debug(
          "Ignored open editor request for file "
              + virtualFile
              + " as it does not belong to a shared module");

      return null;
    }

    return openEditor(virtualFile, file, activate);
  }

  /**
   * Opens an editor for the passed virtualFile.
   *
   * <p>If the editor is a text editor, it is also added to the pool of currently open editors and
   * {@link EditorManager#startEditor(Editor)} is called with it.
   *
   * <p><b>Note:</b> This only works for shared resources that belong to the given module.
   *
   * @param virtualFile the file to open
   * @param project module the file belongs to
   * @param activate activate editor after opening
   * @return the opened <code>Editor</code> or <code>null</code> if the given file does not belong
   *     to a shared module or can not be represented by a text editor
   */
  @Nullable
  public Editor openEditor(
      @NotNull VirtualFile virtualFile, @NotNull IProject project, boolean activate) {

    IFile file = (IFile) VirtualFileConverter.convertToResource(virtualFile, project);

    if (file == null || !sarosSession.isShared(file)) {
      log.debug(
          "Could not open Editor for file "
              + virtualFile
              + " as it does not belong to the given module "
              + project);

      return null;
    }

    return openEditor(virtualFile, file, activate);
  }

  /**
   * Opens an editor for the passed virtualFile.
   *
   * <p>If the editor is a text editor, it is also added to the pool of currently open editors and
   * {@link EditorManager#startEditor(Editor)} is called with it.
   *
   * <p><b>Note:</b> This only works for shared resources.
   *
   * <p><b>Note:</b> This method expects the VirtualFile and the IFile to point to the same
   * resource.
   *
   * @param virtualFile the file to open
   * @param file saros resource representation of the file
   * @param activate activate editor after opening
   * @return the opened <code>Editor</code> or <code>null</code> if the given file does not exist or
   *     does not belong to a shared module or can not be represented by a text editor
   */
  @Nullable
  private Editor openEditor(
      @NotNull VirtualFile virtualFile, @NotNull IFile file, boolean activate) {

    if (!virtualFile.exists()) {
      log.debug("Could not open Editor for file " + virtualFile + " as it does not exist");

      return null;

    } else if (!sarosSession.isShared(file)) {
      log.debug("Ignored open editor request for file " + virtualFile + " as it is not shared");

      return null;
    }

    Project project = ((IntelliJProjectImpl) file.getProject()).getModule().getProject();

    Editor editor = ProjectAPI.openEditor(project, virtualFile, activate);

    if (editor == null) {
      log.debug("Ignoring non-text editor for file " + virtualFile);

      return null;
    }

    editorPool.add(file, editor);
    manager.startEditor(editor);

    log.debug("Opened Editor " + editor + " for file " + virtualFile);

    return editor;
  }

  /**
   * Removes a file from the editorPool and calls {@link EditorManager#generateEditorClosed(IFile)}.
   *
   * <p>Does nothing if the file is not shared.
   *
   * @param project the project in which to close the editor
   * @param virtualFile the file for which to close the editor
   */
  public void closeEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
    IFile file = (IFile) VirtualFileConverter.convertToResource(project, virtualFile);

    if (file == null || !sarosSession.isShared(file)) {
      return;
    }

    editorPool.removeEditor(file);
    manager.generateEditorClosed(file);
  }

  /**
   * Saves the document for the file, thereby flushing its contents to disk.
   *
   * <p>Does nothing if there is no unsaved document content for the given file. This is the case if
   * the document has not been modified or the file can not be opened as a document.
   *
   * @param file the file for the document to save
   * @see Document
   */
  public void saveDocument(@NotNull IFile file) {

    Document document = editorPool.getDocument(file);

    if (document != null) {
      if (DocumentAPI.hasUnsavedChanges(document)) {

        DocumentAPI.saveDocument(document);
      }

      return;
    }

    VirtualFile virtualFile = VirtualFileConverter.convertToVirtualFile(file);

    if (virtualFile == null || !virtualFile.exists()) {
      log.warn("Failed to save document for " + file + " - could not get a valid VirtualFile");

      return;
    }

    if (!DocumentAPI.hasUnsavedChanges(virtualFile)) {
      return;
    }

    document = DocumentAPI.getDocument(virtualFile);

    if (document == null) {
      log.warn(
          "Failed to save document for " + virtualFile + " - could not get a matching Document");

      return;
    }

    DocumentAPI.saveDocument(document);
  }

  /**
   * Calls {@link EditorManager#generateEditorActivated(IFile)}.
   *
   * <p><b>NOTE:</b> This class is meant for internal use only and should generally not be used
   * outside the editor package. If you still need to access this method, please consider whether
   * your class should rather be located in the editor package.
   *
   * @param project the project in which to activate the editor
   * @param virtualFile the file whose editor was activated or <code>null</code> if there is no
   *     editor open
   */
  public void activateEditor(@NotNull Project project, @Nullable VirtualFile virtualFile) {
    if (virtualFile == null) {

      manager.generateEditorActivated(null);

      return;
    }

    IFile file = (IFile) VirtualFileConverter.convertToResource(project, virtualFile);

    if (file != null && sarosSession.isShared(file)) {
      manager.generateEditorActivated(file);
    }
  }

  /**
   * Generates an editor save activity for the given file.
   *
   * @param file the file to generate an editor saved activity for
   * @see EditorManager#generateEditorSaved(IFile)
   */
  public void generateEditorSaved(IFile file) {
    manager.generateEditorSaved(file);
  }

  /** @return <code>true</code>, if the file is opened in an editor. */
  public boolean isOpenEditor(IFile file) {
    Document doc = editorPool.getDocument(file);
    if (doc == null) {
      return false;
    }

    Project project = ((IntelliJProjectImpl) file.getProject()).getModule().getProject();

    return ProjectAPI.isOpen(project, doc);
  }
}
