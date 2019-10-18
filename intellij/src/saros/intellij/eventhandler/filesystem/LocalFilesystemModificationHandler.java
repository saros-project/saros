package saros.intellij.eventhandler.filesystem;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileCopyEvent;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.activities.EditorActivity;
import saros.activities.FileActivity;
import saros.activities.FileActivity.Type;
import saros.activities.FolderCreatedActivity;
import saros.activities.FolderDeletedActivity;
import saros.activities.IActivity;
import saros.activities.SPath;
import saros.filesystem.IPath;
import saros.filesystem.IResource;
import saros.intellij.editor.DocumentAPI;
import saros.intellij.editor.EditorManager;
import saros.intellij.editor.LocalEditorHandler;
import saros.intellij.editor.ProjectAPI;
import saros.intellij.editor.annotations.AnnotationManager;
import saros.intellij.eventhandler.IApplicationEventHandler;
import saros.intellij.eventhandler.editor.document.LocalDocumentModificationHandler;
import saros.intellij.filesystem.VirtualFileConverter;
import saros.intellij.project.filesystem.IntelliJPathImpl;
import saros.observables.FileReplacementInProgressObservable;
import saros.session.AbstractActivityProducer;
import saros.session.ISarosSession;
import saros.session.User;

/**
 * Uses a VirtualFileListener to generate and dispatch FileActivities for shared files.
 *
 * <p>The handler is disabled and the listener is not registered by default.
 *
 * @see VirtualFileListener
 */
public class LocalFilesystemModificationHandler extends AbstractActivityProducer
    implements IApplicationEventHandler {

  private static final Logger LOG = Logger.getLogger(LocalFilesystemModificationHandler.class);

  private final Project project;

  private final EditorManager editorManager;
  private final ISarosSession session;
  private final LocalFileSystem localFileSystem;
  private final FileReplacementInProgressObservable fileReplacementInProgressObservable;
  private final AnnotationManager annotationManager;
  private final LocalEditorHandler localEditorHandler;

  private boolean enabled;
  private boolean disposed;

  private final VirtualFileListener virtualFileListener =
      new VirtualFileListener() {
        @Override
        public void fileCreated(@NotNull VirtualFileEvent event) {
          generateResourceCreationActivity(event);
        }

        @Override
        public void fileCopied(@NotNull VirtualFileCopyEvent event) {
          generateResourceCopyCreationActivity(event);
        }

        @Override
        public void beforePropertyChange(@NotNull VirtualFilePropertyEvent event) {
          generateRenamingResourceMoveActivity(event);
        }

        /**
         * {@inheritDoc}
         *
         * <p>Works for all files in the application scope, including meta-files like Intellij
         * configuration files.
         *
         * <p>File changes done though an Intellij editor are processed in the {@link
         * LocalDocumentModificationHandler} instead.
         *
         * @param event
         */
        @Override
        public void beforeContentsChange(@NotNull VirtualFileEvent event) {
          generateEditorSavedActivity(event);
        }

        @Override
        public void beforeFileDeletion(@NotNull VirtualFileEvent event) {
          generateResourceDeletionActivity(event);
        }

        /**
         * {@inheritDoc}
         *
         * <p>Intellij offers multiple ways of moving resources through the UI that are handled in
         * different ways internally:
         * <li><i>Move file to other package:</i>
         *
         *     <p>Triggers the move listener for the file.
         * <li><i>Move package to other package</i> and <i>Move package to other source root:</i>
         *
         *     <p>Just triggers the move listener for the package directory, does not trigger a
         *     listener event for the contained files or folders. This means the listener has to
         *     iterate the contained resources and create matching actions in the right order.
         * <li><i>Move package to other directory:</i>
         *
         *     <p>Triggers the create listener for the new path of the contained directories in
         *     right order. Then triggers the move listener for the contained files. Then triggers
         *     the delete listener for the old path of the contained directories.
         *
         * @param event
         */
        @Override
        public void beforeFileMovement(@NotNull VirtualFileMoveEvent event) {
          generateResourceMoveActivity(event);
        }
      };

  @Override
  @NotNull
  public ApplicationEventHandlerType getHandlerType() {
    return ApplicationEventHandlerType.LOCAL_FILESYSTEM_MODIFICATION_HANDLER;
  }

  @Override
  public void initialize() {
    ApplicationManager.getApplication()
        .invokeAndWait(
            () -> session.addActivityProducer(LocalFilesystemModificationHandler.this),
            ModalityState.defaultModalityState());

    setEnabled(true);
  }

  @Override
  public void dispose() {
    ApplicationManager.getApplication()
        .invokeAndWait(
            () -> session.removeActivityProducer(LocalFilesystemModificationHandler.this),
            ModalityState.defaultModalityState());

    disposed = true;
    setEnabled(false);
  }

  /**
   * Instantiates a LocalFilesystemModificationHandler object. The handler, including the held
   * filesystem listener, is enabled by default.
   *
   * @see #initialize()
   * @see #dispose()
   */
  public LocalFilesystemModificationHandler(
      Project project,
      EditorManager editorManager,
      ISarosSession session,
      FileReplacementInProgressObservable fileReplacementInProgressObservable,
      AnnotationManager annotationManager,
      LocalEditorHandler localEditorHandler) {

    this.project = project;

    this.editorManager = editorManager;
    this.session = session;
    this.fileReplacementInProgressObservable = fileReplacementInProgressObservable;
    this.annotationManager = annotationManager;
    this.localEditorHandler = localEditorHandler;

    this.enabled = false;
    this.disposed = false;

    this.localFileSystem = LocalFileSystem.getInstance();
  }

  /**
   * Notifies the other session participants of the local save of the given file.
   *
   * @param virtualFileEvent the event to react to
   * @see LocalDocumentModificationHandler
   * @see VirtualFileListener#beforeContentsChange(VirtualFileEvent)
   */
  private void generateEditorSavedActivity(@NotNull VirtualFileEvent virtualFileEvent) {

    assert enabled : "the before contents change listener was triggered while it was disabled";

    VirtualFile file = virtualFileEvent.getFile();

    if (LOG.isTraceEnabled()) {
      LOG.trace("Reacting before resource contents changed: " + file);
    }

    SPath path = VirtualFileConverter.convertToSPath(project, file);

    if (!isShared(path, session)) {
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
   * Generates and dispatches a creation activity for the new resource.
   *
   * @param virtualFileEvent the event to react to
   * @see VirtualFileListener#fileCreated(VirtualFileEvent)
   */
  private void generateResourceCreationActivity(@NotNull VirtualFileEvent virtualFileEvent) {

    assert enabled : "the file created listener was triggered while it was disabled";

    VirtualFile createdVirtualFile = virtualFileEvent.getFile();

    if (LOG.isTraceEnabled()) {
      LOG.trace("Reacting to resource creation: " + createdVirtualFile);
    }

    SPath path = VirtualFileConverter.convertToSPath(project, createdVirtualFile);

    if (!isShared(path, session)) {
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
    }

    dispatchActivity(activity);

    // TODO check whether the editor is actually open locally
    if (!createdVirtualFile.isDirectory()) {
      setUpCreatedFileState(path);
    }
  }

  /**
   * Generates and dispatches creation activities for copied files. Copied directories are handled
   * by {@link #generateResourceCreationActivity(VirtualFileEvent)} (VirtualFileEvent)} and
   * contained files are subsequently handled by this listener.
   *
   * @param virtualFileCopyEvent event to react to
   * @see VirtualFileListener#fileCopied(VirtualFileCopyEvent)
   */
  private void generateResourceCopyCreationActivity(
      @NotNull VirtualFileCopyEvent virtualFileCopyEvent) {

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

    SPath copyPath = VirtualFileConverter.convertToSPath(project, copy);

    if (!isShared(copyPath, session)) {
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

    dispatchActivity(activity);
  }

  /**
   * Generates and dispatches a deletion activity for the deleted resource.
   *
   * @param virtualFileEvent the event to react to
   * @see VirtualFileListener#beforeFileDeletion(VirtualFileEvent)
   * @see #generateFileDeletionActivity(VirtualFile)
   * @see #generateFolderDeletionActivity(VirtualFile)
   */
  private void generateResourceDeletionActivity(@NotNull VirtualFileEvent virtualFileEvent) {

    assert enabled : "the before file deletion listener was triggered while it was disabled";

    VirtualFile deletedVirtualFile = virtualFileEvent.getFile();

    if (LOG.isTraceEnabled()) {
      LOG.trace("Reacting before resource deletion: " + deletedVirtualFile);
    }

    if (deletedVirtualFile.isDirectory()) {
      generateFolderDeletionActivity(deletedVirtualFile);
    } else {
      generateFileDeletionActivity(deletedVirtualFile);
    }
  }

  /**
   * Generates and dispatches a folder deletion activity for the deleted folder.
   *
   * <p>Also explicitly creates deletion activities for all contained resources belonging to the
   * same module. Contained files are processed first (in ascending depth order), followed by the
   * contained folders in descending depth order. This ensures that deletion activities for folders
   * are only send once the activities for all contained resources were sent, allowing the receiver
   * to also explicitly handle all resource deletions.
   *
   * @param deletedFolder the folder that was deleted
   */
  private void generateFolderDeletionActivity(@NotNull VirtualFile deletedFolder) {
    SPath path = VirtualFileConverter.convertToSPath(project, deletedFolder);

    if (!isShared(path, session)) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Ignoring non-shared folder deletion: " + deletedFolder);
      }

      return;
    }

    User user = session.getLocalUser();

    Deque<IActivity> queuedDeletionActivities = new ConcurrentLinkedDeque<>();

    VirtualFileFilter virtualFileFilter = getVirtualFileFilter(deletedFolder);

    /*
     * Defines the actions executed on the base directory and every valid
     * child resource (defined through the virtualFileFilter).
     *
     * Returns whether the content iteration should be continued.
     */
    ContentIterator contentIterator =
        fileOrDir -> {
          if (!fileOrDir.isDirectory()) {
            generateFileDeletionActivity(fileOrDir);

            return true;
          }

          SPath folderPath = VirtualFileConverter.convertToSPath(project, fileOrDir);

          if (folderPath == null) {
            LOG.debug("Skipping deleted folder as no SPath could be obtained " + fileOrDir);

            return true;
          }

          IActivity newFolderDeletedActivity = new FolderDeletedActivity(user, folderPath);

          queuedDeletionActivities.addFirst(newFolderDeletedActivity);

          return true;
        };

    /*
     * Calls the above defined contentIterator on the base directory and
     * every contained resource.
     * Directories are only stepped into if they match the above defined
     * virtualFileFilter. This also applies to the base directory.
     */
    VfsUtilCore.iterateChildrenRecursively(deletedFolder, virtualFileFilter, contentIterator);

    while (!queuedDeletionActivities.isEmpty()) {
      dispatchActivity(queuedDeletionActivities.pop());
    }
  }

  /**
   * Generated and dispatches a file deletion activity for the deleted file. Subsequently removes
   * any editors for the file from the editor pool and drops any held annotations for the file.
   *
   * @param deletedFile the file that was deleted
   */
  private void generateFileDeletionActivity(VirtualFile deletedFile) {
    SPath path = VirtualFileConverter.convertToSPath(project, deletedFile);

    if (path == null || !session.isShared(path.getResource())) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Ignoring non-shared file deletion: " + deletedFile);
      }

      return;
    }

    User user = session.getLocalUser();

    IActivity activity =
        new FileActivity(user, Type.REMOVED, FileActivity.Purpose.ACTIVITY, path, null, null, null);

    cleanUpDeletedFileState(path);

    dispatchActivity(activity);

    // TODO reset the vector time for the deleted file or contained files if folder
  }

  /**
   * Generates and dispatches activities handling resources moves.
   *
   * @param virtualFileMoveEvent the event to react to
   * @see #generateFileMoveActivity(VirtualFile, VirtualFile, VirtualFile, String, String)
   * @see #generateFolderMoveActivity(VirtualFile, VirtualFile, VirtualFile, String)
   * @see #generateResourceCreationActivity(VirtualFileEvent) (VirtualFileEvent)
   * @see #generateResourceDeletionActivity(VirtualFileEvent) (VirtualFileEvent)
   */
  private void generateResourceMoveActivity(@NotNull VirtualFileMoveEvent virtualFileMoveEvent) {

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
      generateFolderMoveActivity(oldFile, oldParent, newParent, null);

    } else {
      generateFileMoveActivity(oldFile, oldParent, newParent, null, null);
    }
  }

  /**
   * Generates and dispatches matching creation and deletion activities replicating moving the given
   * directory.
   *
   * <p>Also explicitly creates activities to move all contained resources belonging to the same
   * module. When creating the new resources, the creation/move activities are created in ascending
   * depth order. This ensures that creation/move activities are only send once the creation
   * activities for all parent resources were sent. When creating the deletion activities, the
   * contained files are processed first (in ascending depth order), followed by the contained
   * folders in descending depth order. This ensures that move activities for folders are only send
   * once the activities for all contained resources were sent, allowing the receiver to also
   * explicitly handle all resource deletions.
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
   * @see #generateFileMoveActivity(VirtualFile, VirtualFile, VirtualFile, String, String)
   */
  private void generateFolderMoveActivity(
      @NotNull VirtualFile oldFile,
      @NotNull VirtualFile oldParent,
      @NotNull VirtualFile newParent,
      @Nullable String newFolderName) {

    String folderName = newFolderName != null ? newFolderName : oldFile.getName();

    SPath oldPath = VirtualFileConverter.convertToSPath(project, oldFile);
    SPath newParentPath = VirtualFileConverter.convertToSPath(project, newParent);

    User user = session.getLocalUser();

    boolean oldPathIsShared = isShared(oldPath, session);
    boolean newPathIsShared = isShared(newParentPath, session);

    if (!oldPathIsShared && !newPathIsShared) {
      if (LOG.isTraceEnabled()) {
        LOG.trace(
            "Ignoring non-shared folder move - folder: "
                + oldFile
                + ", old parent: "
                + oldParent
                + ", new Parent: "
                + newParent);
      }

      return;
    }

    Deque<IActivity> queuedDeletionActivities = new ConcurrentLinkedDeque<>();

    VirtualFileFilter virtualFileFilter = getVirtualFileFilter(oldFile);

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
            generateFileMoveActivity(fileOrDir, oldParent, newParent, newFolderName, null);

            return true;
          }

          if (newPathIsShared) {
            SPath newFolderPath =
                new SPath(
                    newParentPath.getProject(),
                    newParentPath.getProjectRelativePath().append(folderName).append(relativePath));

            IActivity newFolderCreatedActivity = new FolderCreatedActivity(user, newFolderPath);

            dispatchActivity(newFolderCreatedActivity);
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
      dispatchActivity(queuedDeletionActivities.pop());
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
   * @see #generateFolderMoveActivity(VirtualFile, VirtualFile, VirtualFile, String)
   */
  private void generateFileMoveActivity(
      @NotNull VirtualFile oldFile,
      @NotNull VirtualFile oldBaseParent,
      @NotNull VirtualFile newBaseParent,
      @Nullable String newBaseName,
      @Nullable String newFileName) {

    String encoding = oldFile.getCharset().name();

    SPath oldFilePath = VirtualFileConverter.convertToSPath(project, oldFile);
    SPath newParentPath = VirtualFileConverter.convertToSPath(project, newBaseParent);

    User user = session.getLocalUser();

    boolean oldPathIsShared = isShared(oldFilePath, session);
    boolean newPathIsShared = isShared(newParentPath, session);

    boolean fileIsOpen = ProjectAPI.isOpen(project, oldFile);

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

      updateMovedFileState(oldFilePath, newFilePath);

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
        setUpMovedEditorState(oldFile, newFilePath);
      }

    } else if (oldPathIsShared) {
      // moved file out of shared module
      activity =
          new FileActivity(
              user, Type.REMOVED, FileActivity.Purpose.ACTIVITY, oldFilePath, null, null, null);

      cleanUpDeletedFileState(oldFilePath);

    } else {
      // neither source nor destination are shared
      if (LOG.isTraceEnabled()) {
        LOG.trace(
            "Ignoring non-shared file move - file: "
                + oldFile
                + ", old base parent: "
                + oldBaseParent
                + ", new base parent: "
                + newBaseParent);
      }

      return;
    }

    dispatchActivity(activity);

    if (oldPathIsShared) {
      if (fileIsOpen) {
        EditorActivity closeOldEditorActivity =
            new EditorActivity(user, EditorActivity.Type.CLOSED, oldFilePath);

        dispatchActivity(closeOldEditorActivity);
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

      dispatchActivity(openNewEditorActivity);
    }
  }

  /**
   * Handles name changes for resource as resource moves. Generates and dispatches the needed
   * activities. For directories, the listener is not called for all contained resources, meaning
   * these resources are also handled by the call for the directory.
   *
   * <p>Other property changes are ignored.
   *
   * @param filePropertyEvent the event to react to
   * @see #generateFolderMoveActivity(VirtualFile, VirtualFile, VirtualFile, String)
   * @see #generateFileMoveActivity(VirtualFile, VirtualFile, VirtualFile, String, String)
   */
  private void generateRenamingResourceMoveActivity(
      @NotNull VirtualFilePropertyEvent filePropertyEvent) {

    assert enabled : "the before property change listener was triggered while it was disabled";

    VirtualFile file = filePropertyEvent.getFile();
    String propertyName = filePropertyEvent.getPropertyName();
    Object oldValue = filePropertyEvent.getOldValue();
    Object newValue = filePropertyEvent.getNewValue();

    //noinspection SwitchStatementWithTooFewBranches
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

          SPath path = VirtualFileConverter.convertToSPath(project, file);

          if (isShared(path, session)) {
            LOG.error(
                "Renamed resource is a root directory. "
                    + "Such an activity can not be shared through Saros.");
          }

          return;
        }

        if (file.isDirectory()) {
          generateFolderMoveActivity(file, parent, parent, newName);
        } else {
          generateFileMoveActivity(file, parent, parent, null, newName);
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

  private boolean isShared(@Nullable SPath path, @NotNull ISarosSession sarosSession) {
    if (path == null) {
      return false;
    }

    IResource resource = path.getResource();

    if (resource == null) {
      return false;
    }

    return sarosSession.isShared(resource);
  }

  /**
   * Returns a filter determining which child resources to iterate over. It only iterated over
   * resources belonging to the same module as the base directory. This is needed to exclude
   * submodules. Furthermore, this implicitly excludes resources marked as 'excluded' as the logic
   * won't find a module for them.
   *
   * @param baseFile the base file the iteration starts at
   * @return a filter determining which child resources to iterate over
   */
  // TODO once sharing multiple modules is possible, introduce logic to handle deleted submodules if
  //  they are shared
  private VirtualFileFilter getVirtualFileFilter(@NotNull VirtualFile baseFile) {
    return (file) -> {
      Module baseModule = ModuleUtil.findModuleForFile(baseFile, project);
      Module module = ModuleUtil.findModuleForFile(file, project);

      return Objects.equals(baseModule, module);
    };
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
    Document document = DocumentAPI.getDocument(file);

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
   * Sets the editor manager state up for the created file.
   *
   * @param createdFilePath the created file
   */
  private void setUpCreatedFileState(@NotNull SPath createdFilePath) {
    editorManager.openEditor(createdFilePath, false);
  }

  /**
   * Drops the held internal state for the deleted file.
   *
   * @param deletedFilePath the deleted file
   */
  private void cleanUpDeletedFileState(@NotNull SPath deletedFilePath) {
    editorManager.removeAllEditorsForPath(deletedFilePath);

    annotationManager.removeAnnotations(deletedFilePath.getFile());
  }

  /**
   * Updates the held internal state with the new path for the moved file.
   *
   * @param oldFilePath the old location/version of the file
   * @param newFilePath the new location/version of the file
   */
  private void updateMovedFileState(@NotNull SPath oldFilePath, @NotNull SPath newFilePath) {
    editorManager.replaceAllEditorsForPath(oldFilePath, newFilePath);

    annotationManager.updateAnnotationPath(oldFilePath.getFile(), newFilePath.getFile());
  }

  /**
   * Explicitly handles the new editor mapping. This is necessary when the resource with an already
   * open editor is "created" through a move into a shared module as this triggers before the
   * resource move is executed, meaning the new resource does not exist yet. The editor for the old
   * resource does however already exist. As the existing editor will be used for the new resource
   * once it is moved, we have to add the mapping for the new file onto the "old" editor manually.
   */
  private void setUpMovedEditorState(@NotNull VirtualFile oldFile, @NotNull SPath newFilePath) {
    Editor editor = ProjectAPI.openEditor(project, oldFile, false);

    editorManager.addEditorMapping(newFilePath, editor);
  }

  /**
   * Dispatches the given activity. Drops the activity instead if there is currently a file
   * replacement in progress.
   *
   * @param activity the activity to fire
   * @see FileReplacementInProgressObservable
   */
  private void dispatchActivity(@NotNull IActivity activity) {
    // HACK for now
    if (fileReplacementInProgressObservable.isReplacementInProgress()) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("File replacement in progress - Ignoring local activity " + activity);
      }

      return;
    }

    LOG.debug("Dispatching resource activity " + activity);

    fireActivity(activity);
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
    assert !disposed || !enabled : "disposed handlers must not be enabled";

    if (this.enabled && !enabled) {
      LOG.trace("Disabling filesystem listener");

      this.enabled = false;

      localFileSystem.removeVirtualFileListener(virtualFileListener);

    } else if (!this.enabled && enabled) {
      LOG.trace("Enabling filesystem listener");

      this.enabled = true;

      localFileSystem.addVirtualFileListener(virtualFileListener);
    }
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }
}
