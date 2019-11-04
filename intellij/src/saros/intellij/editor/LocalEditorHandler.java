package saros.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.activities.SPath;
import saros.filesystem.IResource;
import saros.intellij.filesystem.IntelliJReferencePointManager;
import saros.intellij.filesystem.VirtualFileConverter;
import saros.session.ISarosSession;

/**
 * Class for handling activities on local editors and transforming them to calls to {@link
 * EditorManager} for generating activities .
 */
public class LocalEditorHandler {

  private static final Logger LOG = Logger.getLogger(LocalEditorHandler.class);

  private final EditorManager manager;
  private final ISarosSession sarosSession;
  private final IntelliJReferencePointManager intelliJReferencePointManager;

  /** This is just a reference to {@link EditorManager}'s editorPool and not a separate pool. */
  private final EditorPool editorPool;

  public LocalEditorHandler(
      EditorManager editorManager,
      ISarosSession sarosSession,
      IntelliJReferencePointManager intelliJReferencePointManager) {
    this.manager = editorManager;
    this.sarosSession = sarosSession;
    this.intelliJReferencePointManager = intelliJReferencePointManager;
    this.editorPool = manager.getEditorPool();
  }

  /**
   * Opens an editor for the passed virtualFile, adds it to the pool of currently open editors and
   * calls {@link EditorManager#startEditor(Editor)} with it.
   *
   * <p><b>Note:</b> This only works for shared resources.
   *
   * @param project the project in which to open the editor
   * @param virtualFile path of the file to open
   * @param activate activate editor after opening
   * @return the opened <code>Editor</code> or <code>null</code> if the given file does not belong
   *     to a shared module
   */
  @Nullable
  public Editor openEditor(
      @NotNull Project project, @NotNull VirtualFile virtualFile, boolean activate) {

    if (!manager.hasSession()) {
      return null;
    }

    SPath path = VirtualFileConverter.convertToSPath(project, virtualFile);

    if (path == null
        || !sarosSession.isShared(
            intelliJReferencePointManager.getSarosResource(
                path.getReferencePoint(), path.getProjectRelativePath()))) {
      LOG.debug(
          "Ignored open editor request for file "
              + virtualFile
              + " as it does not belong to a shared module");

      return null;
    }

    return openEditor(virtualFile, path, activate);
  }

  /**
   * Opens an editor for the passed virtualFile, adds it to the pool of currently open editors and
   * calls {@link EditorManager#startEditor(Editor)} with it.
   *
   * <p><b>Note:</b> This only works for shared resources that belong to the given module.
   *
   * @param virtualFile path of the file to open
   * @param module module the file belongs to
   * @param activate activate editor after opening
   * @return the opened <code>Editor</code> or <code>null</code> if the given file does not belong
   *     to a shared module
   */
  @Nullable
  public Editor openEditor(
      @NotNull VirtualFile virtualFile, @NotNull Module module, boolean activate) {

    IResource resource = VirtualFileConverter.convertToResource(module.getProject(), virtualFile);

    if (resource == null || !sarosSession.isShared(resource)) {
      LOG.debug(
          "Could not open Editor for file "
              + virtualFile
              + " as it does not belong to the given module "
              + module);

      return null;
    }

    return openEditor(virtualFile, new SPath(resource), activate);
  }

  /**
   * Opens an editor for the passed virtualFile, adds it to the pool of currently open editors and
   * calls {@link EditorManager#startEditor(Editor)} with it.
   *
   * <p><b>Note:</b> This only works for shared resources.
   *
   * <p><b>Note:</b> This method expects the VirtualFile and the SPath to point to the same
   * resource.
   *
   * @param virtualFile path of the file to open
   * @param path saros resource representation of the file
   * @param activate activate editor after opening
   * @return the opened <code>Editor</code> or <code>null</code> if the given file does not exist or
   *     does not belong to a shared module
   */
  @Nullable
  private Editor openEditor(
      @NotNull VirtualFile virtualFile, @NotNull SPath path, boolean activate) {

    IResource resource =
        intelliJReferencePointManager.getSarosResource(
            path.getReferencePoint(), path.getProjectRelativePath());

    if (!virtualFile.exists()) {
      LOG.debug("Could not open Editor for file " + virtualFile + " as it does not exist");

      return null;

    } else if (!sarosSession.isShared(resource)) {
      LOG.debug("Ignored open editor request for file " + virtualFile + " as it is not shared");

      return null;
    }

    Project project =
        intelliJReferencePointManager.getModule(path.getReferencePoint()).getProject();

    Editor editor = ProjectAPI.openEditor(project, virtualFile, activate);

    editorPool.add(path, editor);
    manager.startEditor(editor);

    LOG.debug("Opened Editor " + editor + " for file " + virtualFile);

    return editor;
  }

  /**
   * Removes a file from the editorPool and calls {@link EditorManager#generateEditorClosed(SPath)}
   *
   * @param project the project in which to close the editor
   * @param virtualFile the file for which to close the editor
   */
  public void closeEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
    SPath path = VirtualFileConverter.convertToSPath(project, virtualFile);

    if (path != null
        && sarosSession.isShared(
            intelliJReferencePointManager.getSarosResource(
                path.getReferencePoint(), path.getProjectRelativePath()))) {
      editorPool.removeEditor(path);
      manager.generateEditorClosed(path);
    }
  }

  /**
   * Removes the resource belonging to the given path from the editor pool
   *
   * @param path path
   */
  public void removeEditor(@NotNull SPath path) {
    editorPool.removeEditor(path);
  }

  /**
   * Saves the document under path, thereby flushing its contents to disk.
   *
   * @param path the path for the document to save
   * @see Document
   */
  public void saveDocument(@NotNull SPath path) {

    Document document = editorPool.getDocument(path);

    if (document == null) {
      VirtualFile file = intelliJReferencePointManager.getResource(path);

      if (file == null || !file.exists()) {
        LOG.warn("Failed to save document for " + path + " - could not get a valid VirtualFile");

        return;
      }

      document = DocumentAPI.getDocument(file);

      if (document == null) {
        LOG.warn("Failed to save document for " + file + " - could not get a matching Document");

        return;
      }
    }

    DocumentAPI.saveDocument(document);
  }

  /**
   * Calls {@link EditorManager#generateEditorActivated(SPath)}.
   *
   * <p><b>NOTE:</b> This class is meant for internal use only and should generally not be used
   * outside the editor package. If you still need to access this method, please consider whether
   * your class should rather be located in the editor package.
   *
   * @param project the project in which to activate the editor
   * @param file the file whose editor was activated or <code>null</code> if there is no editor open
   */
  public void activateEditor(@NotNull Project project, @Nullable VirtualFile file) {
    if (file == null) {

      if (manager.hasSession()) {
        manager.generateEditorActivated(null);
      }

      return;
    }

    SPath path = VirtualFileConverter.convertToSPath(project, file);

    if (path != null
        && sarosSession.isShared(
            intelliJReferencePointManager.getSarosResource(
                path.getReferencePoint(), path.getProjectRelativePath()))) {
      manager.generateEditorActivated(path);
    }
  }

  /**
   * Generates an editor save activity for the given path.
   *
   * @param path the path to generate an editor saved activity for
   * @see EditorManager#generateEditorSaved(SPath)
   */
  public void generateEditorSaved(SPath path) {
    manager.generateEditorSaved(path);
  }

  /**
   * @param path
   * @return <code>true</code>, if the path is opened in an editor.
   */
  public boolean isOpenEditor(SPath path) {
    Document doc = editorPool.getDocument(path);
    if (doc == null) {
      return false;
    }

    Project project =
        intelliJReferencePointManager.getModule(path.getReferencePoint()).getProject();

    return ProjectAPI.isOpen(project, doc);
  }
}
