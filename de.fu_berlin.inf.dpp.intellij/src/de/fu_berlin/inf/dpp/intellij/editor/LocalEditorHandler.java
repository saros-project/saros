package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.filesystem.VirtualFileConverter;
import de.fu_berlin.inf.dpp.intellij.session.SessionUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class for handling activities on local editors and transforming them to calls to {@link
 * EditorManager} for generating activities .
 */
public class LocalEditorHandler {

  private static final Logger LOG = Logger.getLogger(LocalEditorHandler.class);

  private final ProjectAPI projectAPI;

  /** This is just a reference to {@link EditorManager}'s editorPool and not a separate pool. */
  private EditorPool editorPool;

  private EditorManager manager;

  public LocalEditorHandler(ProjectAPI projectAPI) {

    this.projectAPI = projectAPI;
  }

  /**
   * Initializes all fields that require an EditorManager. It has to be called after the constructor
   * and before the object is used, otherwise it will not work.
   *
   * <p>The reason for this late initialization is that this way the LocalEditorHandler can be
   * instantiated by the PicoContainer, otherwise there would be a cyclic dependency.
   *
   * @param editorManager - an EditorManager
   */
  public void initialize(EditorManager editorManager) {
    this.editorPool = editorManager.getEditorPool();
    this.manager = editorManager;
  }

  /**
   * Opens an editor for the passed virtualFile, adds it to the pool of currently open editors and
   * calls {@link EditorManager#startEditor(Editor)} with it.
   *
   * <p><b>Note:</b> This only works for shared resources.
   *
   * @param virtualFile path of the file to open
   * @param activate activate editor after opening
   * @return the opened <code>Editor</code> or <code>null</code> if the given file does not belong
   *     to a shared module
   */
  @Nullable
  public Editor openEditor(@NotNull VirtualFile virtualFile, boolean activate) {

    if (!manager.hasSession()) {
      return null;
    }

    SPath path = VirtualFileConverter.convertToSPath(virtualFile);

    if (path == null || !SessionUtils.isShared(path)) {
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
   * @param referencePoint reference point the file belongs to
   * @param activate activate editor after opening
   * @return the opened <code>Editor</code> or <code>null</code> if the given file does not belong
   *     to a shared module
   */
  @Nullable
  public Editor openEditor(
      @NotNull VirtualFile virtualFile, @NotNull IReferencePoint referencePoint, boolean activate) {

    IResource resource = VirtualFileConverter.convertToResource(virtualFile, referencePoint);

    if (resource == null || !SessionUtils.isShared(resource)) {
      LOG.debug(
          "Could not open Editor for file "
              + virtualFile
              + " as it does not belong to the given module "
              + referencePoint);

      return null;
    }

    SPath sPath = VirtualFileConverter.convertToSPath(virtualFile);

    return openEditor(virtualFile, sPath, activate);
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

    if (!virtualFile.exists()) {
      LOG.debug("Could not open Editor for file " + virtualFile + " as it does not exist");

      return null;

    } else if (!SessionUtils.isShared(path)) {
      LOG.debug("Ignored open editor request for file " + virtualFile + " as it is not shared");

      return null;
    }

    Editor editor = projectAPI.openEditor(virtualFile, activate);

    editorPool.add(path, editor);
    manager.startEditor(editor);

    LOG.debug("Opened Editor " + editor + " for file " + virtualFile);

    return editor;
  }

  /**
   * Removes a file from the editorPool and calls {@link EditorManager#generateEditorClosed(SPath)}
   *
   * @param virtualFile
   */
  public void closeEditor(@NotNull VirtualFile virtualFile) {
    SPath path = VirtualFileConverter.convertToSPath(virtualFile);

    if (path != null && SessionUtils.isShared(path)) {
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
      VirtualFile file = VirtualFileConverter.convertToVirtualFile(path);

      if (file == null || !file.exists()) {
        LOG.warn("Failed to save document for " + path + " - could not get a valid VirtualFile");

        return;
      }

      document = projectAPI.getDocument(file);

      if (document == null) {
        LOG.warn("Failed to save document for " + file + " - could not get a matching Document");

        return;
      }
    }

    projectAPI.saveDocument(document);
  }

  /**
   * Calls {@link EditorManager#generateEditorActivated(SPath)}.
   *
   * @param file the file whose editor was activated or <code>null</code> if there is no editor open
   */
  void activateEditor(@Nullable VirtualFile file) {
    if (file == null) {

      if (manager.hasSession()) {
        manager.generateEditorActivated(null);
      }

      return;
    }

    SPath path = VirtualFileConverter.convertToSPath(file);

    if (path != null && SessionUtils.isShared(path)) {
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

    return projectAPI.isOpen(doc);
  }
}
