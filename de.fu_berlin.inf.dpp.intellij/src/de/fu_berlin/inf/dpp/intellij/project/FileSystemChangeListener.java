package de.fu_berlin.inf.dpp.intellij.project;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileCopyEvent;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity.Type;
import de.fu_berlin.inf.dpp.activities.FolderCreatedActivity;
import de.fu_berlin.inf.dpp.activities.FolderDeletedActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.intellij.editor.AbstractStoppableListener;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.editor.LocalEditorHandler;
import de.fu_berlin.inf.dpp.intellij.editor.ProjectAPI;
import de.fu_berlin.inf.dpp.intellij.editor.StoppableDocumentListener;
import de.fu_berlin.inf.dpp.intellij.editor.annotations.AnnotationManager;
import de.fu_berlin.inf.dpp.intellij.filesystem.VirtualFileConverter;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.IntelliJPathImpl;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.IntelliJWorkspaceImpl;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.picocontainer.annotations.Inject;

/**
 * Virtual file system listener. It receives events for all files in all projects opened by the
 * user. It filters for files that are shared and creates the corresponding activities dispatches
 * these activities using {@link SharedResourcesManager#fireActivity(IActivity)}.
 *
 * <p>The listener is enabled by default when the session context is created.
 *
 * @see VirtualFileListener
 */
// TODO decouple from SharedResourceManager and add to session context instead
public class FileSystemChangeListener extends AbstractStoppableListener
    implements VirtualFileListener {

  private static final Logger LOG = Logger.getLogger(FileSystemChangeListener.class);

  private final SharedResourcesManager resourceManager;
  private final IntelliJWorkspaceImpl intellijWorkspace;
  private final ISarosSession session;

  @Inject private ProjectAPI projectAPI;

  @Inject private AnnotationManager annotationManager;

  @Inject private Project project;

  @Inject private LocalEditorHandler localEditorHandler;

  public FileSystemChangeListener(
      SharedResourcesManager resourceManager,
      EditorManager editorManager,
      IntelliJWorkspaceImpl intellijWorkspace,
      ISarosSession session) {

    super(editorManager);

    this.resourceManager = resourceManager;
    this.intellijWorkspace = intellijWorkspace;
    this.session = session;

    SarosPluginContext.initComponent(this);

    intellijWorkspace.addResourceListener(this);
  }

  /**
   * {@inheritDoc} Works for all files in the application scope, including meta-files like Intellij
   * configuration files.
   *
   * <p>File changes done though an Intellij editor are processed in the {@link
   * StoppableDocumentListener} instead.
   *
   * @param virtualFileEvent {@inheritDoc}
   * @see StoppableDocumentListener
   */
  @Override
  public void beforeContentsChange(@NotNull VirtualFileEvent virtualFileEvent) {

    assert enabled : "the before contents change listener was triggered while it was disabled";

    VirtualFile file = virtualFileEvent.getFile();

    if (LOG.isTraceEnabled()) {
      LOG.trace("Reacting before resource contents changed: " + file);
    }

    SPath path = VirtualFileConverter.convertToSPath(file);

    if (path == null || !session.isShared(path.getResource())) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Ignoring non-shared resource's contents change: " + file);
      }

      return;
    }

    if (virtualFileEvent.isFromSave()) {
      if (LOG.isTraceEnabled()) {
        LOG.trace(
            "Ignoring contents change for " + file + " as they were caused by a document save.");
      }

      localEditorHandler.generateEditorSaved(path);

      return;
    }

    if (virtualFileEvent.isFromRefresh()) {
      if (LOG.isTraceEnabled()) {
        LOG.trace(
            "Ignoring contents change for "
                + file
                + " as they were caused by a filesystem snapshot refresh. "
                + "This is already handled by the document listener.");
      }

      return;
    }

    // TODO figure out if this can happen
    LOG.warn(
        "Detected unhandled content change on the virtual file level "
            + "for "
            + file
            + ", requested by: "
            + virtualFileEvent.getRequestor());
  }

  /**
   * {@inheritDoc}
   *
   * <p>Generates and dispatches a creation activity for the new resource.
   *
   * @param virtualFileEvent {@inheritDoc}
   */
  @Override
  public void fileCreated(@NotNull VirtualFileEvent virtualFileEvent) {

    assert enabled : "the file created listener was triggered while it was disabled";

    VirtualFile createdVirtualFile = virtualFileEvent.getFile();

    if (LOG.isTraceEnabled()) {
      LOG.trace("Reacting to resource creation: " + createdVirtualFile);
    }

    SPath path = VirtualFileConverter.convertToSPath(createdVirtualFile);

    if (path == null || !session.isShared(path.getResource())) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Ignoring non-shared resource creation: " + createdVirtualFile);
      }

      return;
    }

    User user = session.getLocalUser();

    IActivity activity;

    if (createdVirtualFile.isDirectory()) {
      activity = new FolderCreatedActivity(user, path);

    } else {
      String charset = createdVirtualFile.getCharset().name();

      byte[] content = getContent(createdVirtualFile);

      activity =
          new FileActivity(
              user, Type.CREATED, FileActivity.Purpose.ACTIVITY, path, null, content, charset);

      editorManager.openEditor(path, false);
    }

    fireActivity(activity);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Generates and dispatches creation activities for copied files. Copied directories are
   * handled by {@link #fileCreated(VirtualFileEvent)} and contained files are subsequently handled
   * by this listener.
   *
   * @param virtualFileCopyEvent {@inheritDoc}
   */
  @Override
  public void fileCopied(@NotNull VirtualFileCopyEvent virtualFileCopyEvent) {

    assert enabled : "the file copied listener was triggered while it was disabled";

    VirtualFile copy = virtualFileCopyEvent.getFile();

    assert !copy.isDirectory()
        : "Unexpected copying event for directory. This should have been handled by the creation listener.";

    if (LOG.isTraceEnabled()) {
      LOG.trace(
          "Reacting to resource copying - original: "
              + virtualFileCopyEvent.getOriginalFile()
              + ", copy: "
              + copy);
    }

    SPath copyPath = VirtualFileConverter.convertToSPath(copy);

    if (copyPath == null || !session.isShared(copyPath.getResource())) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Ignoring non-shared resource copy: " + copy);
      }

      return;
    }

    User user = session.getLocalUser();
    String charset = copy.getCharset().name();

    byte[] content = getContent(copy);

    IActivity activity =
        new FileActivity(
            user, Type.CREATED, FileActivity.Purpose.ACTIVITY, copyPath, null, content, charset);

    fireActivity(activity);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Generates and dispatches a deletion activity for the deleted resource. If the resource was a
   * file, subsequently removes any editors for the file from the editor pool and drops any held
   * annotations for the file.
   *
   * @param virtualFileEvent {@inheritDoc}
   */
  @Override
  public void beforeFileDeletion(@NotNull VirtualFileEvent virtualFileEvent) {

    assert enabled : "the before file deletion listener was triggered while it was disabled";

    VirtualFile deletedVirtualFile = virtualFileEvent.getFile();

    if (LOG.isTraceEnabled()) {
      LOG.trace("Reacting before resource deletion: " + deletedVirtualFile);
    }

    SPath path = VirtualFileConverter.convertToSPath(deletedVirtualFile);

    if (path == null || !session.isShared(path.getResource())) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Ignoring non-shared resource deletion: " + deletedVirtualFile);
      }

      return;
    }

    User user = session.getLocalUser();

    IActivity activity;

    if (deletedVirtualFile.isDirectory()) {
      // TODO create deletion activities for child resources
      // TODO clean up editor pool and annotations for child resources
      activity = new FolderDeletedActivity(user, path);

    } else {
      activity =
          new FileActivity(
              user, Type.REMOVED, FileActivity.Purpose.ACTIVITY, path, null, null, null);

      editorManager.removeAllEditorsForPath(path);

      annotationManager.removeAnnotations(path.getFile());
    }

    fireActivity(activity);

    // TODO reset the vector time for the deleted file or contained files if folder
  }

  /**
   * {@inheritDoc}
   *
   * <p>Generates and dispatches activities handling resources moves.
   *
   * <p>Intellij offers multiple ways of moving resources through the UI that are handled in
   * different ways internally:
   * <li><i>Move file to other package:</i>
   *
   *     <p>Triggers the move listener for the file.
   * <li><i>Move package to other package</i> and <i>Move package to other source root:</i>
   *
   *     <p>Just triggers the move listener for the package directory, does not trigger a listener
   *     event for the contained files or folders. This means the listener has to iterate the
   *     contained resources and create matching actions in the right order.
   * <li><i>Move package to other directory:</i>
   *
   *     <p>Triggers the create listener for the new path of the contained directories in right
   *     order. Then triggers the move listener for the contained files. Then triggers the delete
   *     listener for the old path of the contained directories.
   *
   * @param virtualFileMoveEvent {@inheritDoc}
   * @see #generateFileMove(VirtualFile, VirtualFile, VirtualFile, String, String)
   * @see #generateFolderMove(VirtualFile, VirtualFile, VirtualFile, String)
   * @see #fileCreated(VirtualFileEvent)
   * @see #fileDeleted(VirtualFileEvent)
   */
  @Override
  public void beforeFileMovement(@NotNull VirtualFileMoveEvent virtualFileMoveEvent) {

    assert enabled : "the before file move listener was triggered while it was disabled";

    VirtualFile oldFile = virtualFileMoveEvent.getFile();
    VirtualFile oldParent = virtualFileMoveEvent.getOldParent();
    VirtualFile newParent = virtualFileMoveEvent.getNewParent();

    if (LOG.isTraceEnabled()) {
      LOG.trace(
          "Reacting before resource move - resource: "
              + oldFile
              + ", old parent: "
              + oldParent
              + ", new Parent: "
              + newParent);
    }

    if (oldFile.isDirectory()) {
      generateFolderMove(oldFile, oldParent, newParent, null);

    } else {
      generateFileMove(oldFile, oldParent, newParent, null, null);
    }
  }

  /**
   * Generates and dispatches matching creation and deletion activities replicating moving the given
   * directory. Also dispatches activities for all contained resources belonging to the same module.
   *
   * <p>How the resources (including the given directory) are handled depends on whether the source
   * and target directory is shared.
   *
   * <ul>
   *   <li>If only the source directory is shared, only deletion activities for the old resources
   *       are dispatched.
   *   <li>If only the target directory is shared, only creation activities for the new resources
   *       are dispatched.
   *   <li>If both directories are shared, the resources are moved as follows:
   *       <p>Creation activities for the new directories (located in the target directory) and move
   *       activities for the contained files are dispatched in increasing depth order. Then,
   *       deletion activities for the old directories (in the source directory) are dispatched in
   *       decreasing depth order. This ensures that any resource move is already handled correctly
   *       before one of its parent resources is deleted.
   * </ul>
   *
   * <p>Renaming directories is also handled as a move activity. This can be done with the optional
   * parameter <code>newFolderName</code>.
   *
   * @param oldFile the directory that is about to be moved
   * @param oldParent the old parent of the moved file
   * @param newParent the new parent of the moved file
   * @param newFolderName the new name for the folder or null if the folder was not renamed
   * @see #generateFileMove(VirtualFile, VirtualFile, VirtualFile, String, String)
   */
  private void generateFolderMove(
      @NotNull VirtualFile oldFile,
      @NotNull VirtualFile oldParent,
      @NotNull VirtualFile newParent,
      @Nullable String newFolderName) {

    String folderName = newFolderName != null ? newFolderName : oldFile.getName();

    SPath oldPath = VirtualFileConverter.convertToSPath(oldFile);
    SPath newParentPath = VirtualFileConverter.convertToSPath(newParent);

    User user = session.getLocalUser();

    boolean oldPathIsShared = oldPath != null && session.isShared(oldPath.getResource());
    boolean newPathIsShared =
        newParentPath != null && session.isShared(newParentPath.getResource());

    if (!oldPathIsShared && !newPathIsShared) {
      if (LOG.isTraceEnabled()) {
        LOG.trace(
            "Ignoring non-shared resource move - resource: "
                + oldFile
                + ", old parent: "
                + oldParent
                + ", new Parent: "
                + newParent);
      }

      return;
    }

    Deque<IActivity> queuedDeletionActivities = new ConcurrentLinkedDeque<>();

    /*
     * Filter determining which child resources to iterate over.
     * It only iterated over resources belonging to the same module as the
     * base directory. This is needed to exclude submodules.
     *
     * TODO once sharing multiple modules is possible, introduce logic to handle deleted submodules if they are shared
     */
    VirtualFileFilter virtualFileFilter =
        file -> {
          Module baseModule = ModuleUtil.findModuleForFile(oldFile, project);
          Module module = ModuleUtil.findModuleForFile(file, project);

          return baseModule != null && baseModule.equals(module);
        };

    /*
     * Defines the actions executed on the base directory and every valid
     * child resource (defined through the virtualFileFilter).
     *
     * Returns whether the content iteration should be continued.
     */
    ContentIterator contentIterator =
        fileOrDir -> {
          IPath relativePath = getRelativePath(oldFile, fileOrDir);

          if (relativePath == null) {
            return true;
          }

          if (!fileOrDir.isDirectory()) {
            generateFileMove(fileOrDir, oldParent, newParent, newFolderName, null);

            return true;
          }

          if (newPathIsShared) {
            SPath newFolderPath =
                new SPath(
                    newParentPath.getProject(),
                    newParentPath.getProjectRelativePath().append(folderName).append(relativePath));

            IActivity newFolderCreatedActivity = new FolderCreatedActivity(user, newFolderPath);

            fireActivity(newFolderCreatedActivity);
          }

          if (oldPathIsShared) {
            SPath oldFolderPath =
                new SPath(
                    oldPath.getProject(), oldPath.getProjectRelativePath().append(relativePath));

            IActivity newFolderDeletedActivity = new FolderDeletedActivity(user, oldFolderPath);

            queuedDeletionActivities.addFirst(newFolderDeletedActivity);
          }

          return true;
        };

    /*
     * Calls the above defined contentIterator on the base directory and
     * every contained resource.
     * Directories are only stepped into if they match the above defined
     * virtualFileFilter. This also applies to the base directory.
     */
    VfsUtilCore.iterateChildrenRecursively(oldFile, virtualFileFilter, contentIterator);

    while (!queuedDeletionActivities.isEmpty()) {
      fireActivity(queuedDeletionActivities.pop());
    }
  }

  /**
   * How the moved file is handled depends on whether the source and target directory are shared:
   *
   * <ul>
   *   <li>If only the source directory is shared, a deletion activity for the old file is created
   *       and dispatched. Subsequently removes any editor for the file from the editor pool and
   *       drops any annotation held for the file.
   *   <li>If only the target directory is shared, a creation activity for the new file is created
   *       and dispatched. Subsequently adds an entry for the new file to the editor pool if it is
   *       currently open in an editor.
   *   <li>If both the source and target directory are shared, a move activity for the file is
   *       created and dispatched. Subsequently updates the path of any editor for the file in the
   *       editor pool and updates the path for any held annotations for the file.
   * </ul>
   *
   * If the source directory is shared, an editor closed activity is also dispatched. If the target
   * directory is shared, an editor opened activity is also dispatched. This is necessary as
   * Intellij does not close and re-open the editors of moved files but rather changes the path of
   * the editor internally, meaning the <code>UserEditorState</code> held by the other participants
   * would not be updated correctly.
   *
   * <p>Renaming files is also handled as a move activity. This can be done with the optional
   * parameter <code>newFileName</code>.
   *
   * <p>The optional parameter <code>newBaseName</code> can be used to handle cases where a parent
   * file is renamed. The renamed directory has to be the directory located under <code>
   * oldBaseParent</code> in the old path.
   *
   * @param oldFile the file that is about to be moved
   * @param oldBaseParent the old base parent of the file
   * @param newBaseParent the new base parent of the file
   * @param newBaseName the new name for the base parent or null if it was not renamed
   * @param newFileName the new name for the file or null if it was not renamed
   * @see #generateFolderMove(VirtualFile, VirtualFile, VirtualFile, String)
   */
  private void generateFileMove(
      @NotNull VirtualFile oldFile,
      @NotNull VirtualFile oldBaseParent,
      @NotNull VirtualFile newBaseParent,
      @Nullable String newBaseName,
      @Nullable String newFileName) {

    String encoding = oldFile.getCharset().name();

    SPath oldFilePath = VirtualFileConverter.convertToSPath(oldFile);
    SPath newParentPath = VirtualFileConverter.convertToSPath(newBaseParent);

    User user = session.getLocalUser();

    boolean oldPathIsShared = oldFilePath != null && session.isShared(oldFilePath.getResource());
    boolean newPathIsShared =
        newParentPath != null && session.isShared(newParentPath.getResource());

    boolean fileIsOpen = projectAPI.isOpen(oldFile);

    IPath relativePath = getRelativePath(oldBaseParent, oldFile);

    if (relativePath == null) {
      return;
    }

    if (newBaseName != null) {
      relativePath =
          IntelliJPathImpl.fromString(newBaseName).append(relativePath.removeFirstSegments(1));
    }

    if (newFileName != null) {
      relativePath =
          relativePath.removeLastSegments(1).append(IntelliJPathImpl.fromString(newFileName));
    }

    IActivity activity;

    if (oldPathIsShared && newPathIsShared) {
      // moved file inside shared modules
      SPath newFilePath =
          new SPath(
              newParentPath.getProject(),
              newParentPath.getProjectRelativePath().append(relativePath));

      activity =
          new FileActivity(
              user,
              Type.MOVED,
              FileActivity.Purpose.ACTIVITY,
              newFilePath,
              oldFilePath,
              null,
              encoding);

      editorManager.replaceAllEditorsForPath(oldFilePath, newFilePath);

      annotationManager.updateAnnotationPath(oldFilePath.getFile(), newFilePath.getFile());

    } else if (newPathIsShared) {
      // moved file into shared module
      byte[] fileContent = getContent(oldFile);

      SPath newFilePath =
          new SPath(
              newParentPath.getProject(),
              newParentPath.getProjectRelativePath().append(relativePath));

      activity =
          new FileActivity(
              user,
              Type.CREATED,
              FileActivity.Purpose.ACTIVITY,
              newFilePath,
              null,
              fileContent,
              encoding);

      if (fileIsOpen) {
        Editor editor = projectAPI.openEditor(oldFile, false);

        editorManager.addEditorMapping(newFilePath, editor);
      }

    } else if (oldPathIsShared) {
      // moved file out of shared module
      activity =
          new FileActivity(
              user, Type.REMOVED, FileActivity.Purpose.ACTIVITY, oldFilePath, null, null, null);

      editorManager.removeAllEditorsForPath(oldFilePath);

      annotationManager.removeAnnotations(oldFilePath.getFile());

    } else {
      // neither source nor destination are shared
      if (LOG.isTraceEnabled()) {
        LOG.trace(
            "Ignoring non-shared resource move - resource: "
                + oldFile
                + ", old base parent: "
                + oldBaseParent
                + ", new base parent: "
                + newBaseParent);
      }

      return;
    }

    fireActivity(activity);

    if (oldPathIsShared) {
      if (fileIsOpen) {
        EditorActivity closeOldEditorActivity =
            new EditorActivity(user, EditorActivity.Type.CLOSED, oldFilePath);

        fireActivity(closeOldEditorActivity);
      }

      // TODO reset the vector time for the old file
    }

    if (newPathIsShared && fileIsOpen) {
      SPath newFilePath =
          new SPath(
              newParentPath.getProject(),
              newParentPath.getProjectRelativePath().append(relativePath));

      EditorActivity openNewEditorActivity =
          new EditorActivity(user, EditorActivity.Type.ACTIVATED, newFilePath);

      fireActivity(openNewEditorActivity);
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Handles name changes for resource as resource moves. Generates and dispatches the needed
   * activities. For directories, the listener is not called for all contained resources, meaning
   * these resources are also handled by the call for the directory.
   *
   * <p>Other property changes are ignored.
   *
   * @param filePropertyEvent {@inheritDoc}
   * @see #generateFolderMove(VirtualFile, VirtualFile, VirtualFile, String)
   * @see #generateFileMove(VirtualFile, VirtualFile, VirtualFile, String, String)
   */
  @Override
  public void beforePropertyChange(@NotNull VirtualFilePropertyEvent filePropertyEvent) {

    assert enabled : "the before property change listener was triggered while it was disabled";

    VirtualFile file = filePropertyEvent.getFile();
    String propertyName = filePropertyEvent.getPropertyName();
    Object oldValue = filePropertyEvent.getOldValue();
    Object newValue = filePropertyEvent.getNewValue();

    switch (propertyName) {
      case (VirtualFile.PROP_NAME):
        String oldName = (String) oldValue;
        String newName = (String) newValue;

        if (LOG.isTraceEnabled()) {
          LOG.trace(
              "Reacting before resource name change - resource: "
                  + file
                  + ", old name: "
                  + oldName
                  + ", new name: "
                  + newName);
        }

        VirtualFile parent = file.getParent();

        if (parent == null) {

          SPath path = VirtualFileConverter.convertToSPath(file);

          if (path != null && session.isShared(path.getResource())) {
            LOG.error(
                "Renamed resource is a root directory. "
                    + "Such an activity can not be shared through Saros.");
          }

          return;
        }

        if (file.isDirectory()) {
          generateFolderMove(file, parent, parent, newName);
        } else {
          generateFileMove(file, parent, parent, null, newName);
        }

        break;

      default:
        if (LOG.isTraceEnabled()) {
          LOG.trace(
              "Ignoring change of property + "
                  + propertyName
                  + " for file "
                  + file
                  + " - old value: "
                  + oldValue
                  + ", new value: "
                  + newValue);
        }
    }
  }

  /**
   * Returns the relative path between the given root and the given file.
   *
   * @param root the root file
   * @param file the file to get a relative path for
   * @return the relative path between the given root and the given file or <code>null</code> if
   *     such a path could not be found
   */
  @Nullable
  private IPath getRelativePath(@NotNull VirtualFile root, @NotNull VirtualFile file) {

    try {
      Path relativePath = Paths.get(root.getPath()).relativize(Paths.get(file.getPath()));

      return IntelliJPathImpl.fromString(relativePath.toString());

    } catch (IllegalArgumentException e) {
      LOG.warn(
          "Could not find a relative path from the content root " + root + " to the file " + file,
          e);

      return null;
    }
  }

  /**
   * Returns the content of the given file. If available, the cached document content representing
   * the file held by Intellij will be used. Otherwise, the file content on disk (obtained using the
   * <code>VirtualFile</code>) will be used.
   *
   * @param file the file to get the content for
   * @return the content for the given file (cached by Intellij or read from disk if no cache is
   *     available) or an empty byte array if the content of the file could not be obtained.
   * @see Document
   * @see VirtualFile
   */
  private byte[] getContent(VirtualFile file) {
    Document document = projectAPI.getDocument(file);

    try {
      if (document != null) {
        return document.getText().getBytes(file.getCharset().name());

      } else {
        LOG.debug(
            "Could not get Document for file "
                + file
                + ", using file content on disk instead. This content might"
                + " not correctly represent the current state of the file"
                + " in Intellij.");

        return file.contentsToByteArray();
      }

    } catch (IOException e) {
      LOG.warn("Could not get content for file " + file, e);

      return new byte[0];
    }
  }

  /**
   * Dispatches the given activity.
   *
   * @param activity the activity to fire
   * @see SharedResourcesManager#internalFireActivity(IActivity)
   */
  private void fireActivity(@NotNull IActivity activity) {

    LOG.debug("Dispatching resource activity " + activity);

    resourceManager.internalFireActivity(activity);
  }

  /**
   * Enables or disables the filesystem listener. This is done by registering or unregistering the
   * listener.
   *
   * <p>This method does nothing if the given state already matches the current state.
   *
   * @param enabled <code>true</code> to enable the listener, <code>false</code> to disable the
   *     listener
   */
  @Override
  public void setEnabled(boolean enabled) {
    if (this.enabled && !enabled) {
      LOG.trace("Disabling filesystem listener");

      this.enabled = false;

      intellijWorkspace.removeResourceListener(this);

    } else if (!this.enabled && enabled) {
      LOG.trace("Enabling filesystem listener");

      this.enabled = true;

      intellijWorkspace.addResourceListener(this);
    }
  }
}
