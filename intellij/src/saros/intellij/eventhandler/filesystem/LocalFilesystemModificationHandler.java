package saros.intellij.eventhandler.filesystem;

import static saros.filesystem.IResource.Type.REFERENCE_POINT;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
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
import java.text.MessageFormat;
import java.util.Deque;
import java.util.Set;
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
import saros.core.ui.util.CollaborationUtils;
import saros.filesystem.IContainer;
import saros.filesystem.IFile;
import saros.filesystem.IFolder;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;
import saros.intellij.editor.DocumentAPI;
import saros.intellij.editor.EditorManager;
import saros.intellij.editor.LocalEditorHandler;
import saros.intellij.editor.ProjectAPI;
import saros.intellij.editor.annotations.AnnotationManager;
import saros.intellij.eventhandler.IApplicationEventHandler;
import saros.intellij.eventhandler.editor.document.LocalDocumentModificationActivityDispatcher;
import saros.intellij.filesystem.VirtualFileConverter;
import saros.intellij.runtime.EDTExecutor;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.NotificationPanel;
import saros.observables.FileReplacementInProgressObservable;
import saros.session.AbstractActivityProducer;
import saros.session.ISarosSession;
import saros.session.User;
import saros.util.PathUtils;

/**
 * Uses a VirtualFileListener to generate and dispatch FileActivities for shared files.
 *
 * <p>The handler is disabled and the listener is not registered by default.
 *
 * @see VirtualFileListener
 */
public class LocalFilesystemModificationHandler extends AbstractActivityProducer
    implements IApplicationEventHandler {

  private static final Logger log = Logger.getLogger(LocalFilesystemModificationHandler.class);

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
         * LocalDocumentModificationActivityDispatcher} instead.
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
    EDTExecutor.invokeAndWait(
        () -> session.addActivityProducer(LocalFilesystemModificationHandler.this),
        ModalityState.defaultModalityState());

    setEnabled(true);
  }

  @Override
  public void dispose() {
    EDTExecutor.invokeAndWait(
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
   * @see LocalDocumentModificationActivityDispatcher
   * @see VirtualFileListener#beforeContentsChange(VirtualFileEvent)
   */
  private void generateEditorSavedActivity(@NotNull VirtualFileEvent virtualFileEvent) {

    assert enabled : "the before contents change listener was triggered while it was disabled";

    VirtualFile virtualFile = virtualFileEvent.getFile();

    if (log.isTraceEnabled()) {
      log.trace("Reacting before resource contents changed: " + virtualFile);
    }

    Set<IReferencePoint> sharedReferencePoints = session.getReferencePoints();

    IFile file = (IFile) VirtualFileConverter.convertToResource(sharedReferencePoints, virtualFile);

    if (file == null || !session.isShared(file)) {
      if (log.isTraceEnabled()) {
        log.trace("Ignoring non-shared resource's contents change: " + virtualFile);
      }

      return;
    }

    if (virtualFileEvent.isFromSave()) {
      if (log.isTraceEnabled()) {
        log.trace(
            "Ignoring contents change for "
                + virtualFile
                + " as they were caused by a document save.");
      }

      localEditorHandler.generateEditorSaved(file);

      return;
    }

    if (virtualFileEvent.isFromRefresh()) {
      if (log.isTraceEnabled()) {
        log.trace(
            "Ignoring contents change for "
                + virtualFile
                + " as they were caused by a filesystem snapshot refresh. "
                + "This is already handled by the document listener.");
      }

      return;
    }

    if (!virtualFile.getFileType().isBinary()) {
      // modification already handled by document modification handler
      log.debug(
          "Ignoring content change on the virtual file level for text resource "
              + virtualFile
              + ", requested by: "
              + virtualFileEvent.getRequestor());

    } else {
      // TODO handle content changes for non-text resources; see #996
      log.warn(
          "Detected unhandled content change on the virtual file level for non-text resource "
              + virtualFile
              + ", requested by: "
              + virtualFileEvent.getRequestor());
    }
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

    if (log.isTraceEnabled()) {
      log.trace("Reacting to resource creation: " + createdVirtualFile);
    }

    Set<IReferencePoint> sharedReferencePoints = session.getReferencePoints();

    IResource resource =
        VirtualFileConverter.convertToResource(sharedReferencePoints, createdVirtualFile);

    if (resource == null || !session.isShared(resource)) {
      if (log.isTraceEnabled()) {
        log.trace("Ignoring non-shared resource creation: " + createdVirtualFile);
      }

      return;
    }

    User user = session.getLocalUser();

    IActivity activity;

    if (createdVirtualFile.isDirectory()) {
      activity = new FolderCreatedActivity(user, (IFolder) resource);

    } else {
      String charset = createdVirtualFile.getCharset().name();

      byte[] content = getContent(createdVirtualFile);

      activity =
          new FileActivity(
              user,
              Type.CREATED,
              FileActivity.Purpose.ACTIVITY,
              (IFile) resource,
              null,
              content,
              charset);
    }

    dispatchActivity(activity);

    if (!createdVirtualFile.isDirectory() && ProjectAPI.isOpen(project, createdVirtualFile)) {
      setUpCreatedFileState((IFile) resource);
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
        : "Unexpected copying event for directory. This should have been handled by the creation"
            + " listener.";

    if (log.isTraceEnabled()) {
      log.trace(
          "Reacting to resource copying - original: "
              + virtualFileCopyEvent.getOriginalFile()
              + ", copy: "
              + copy);
    }

    Set<IReferencePoint> sharedReferencePoints = session.getReferencePoints();

    IFile copyWrapper = (IFile) VirtualFileConverter.convertToResource(sharedReferencePoints, copy);

    if (copyWrapper == null || !session.isShared(copyWrapper)) {
      if (log.isTraceEnabled()) {
        log.trace("Ignoring non-shared resource copy: " + copy);
      }

      return;
    }

    User user = session.getLocalUser();
    String charset = copy.getCharset().name();

    byte[] content = getContent(copy);

    IActivity activity =
        new FileActivity(
            user, Type.CREATED, FileActivity.Purpose.ACTIVITY, copyWrapper, null, content, charset);

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

    if (log.isTraceEnabled()) {
      log.trace("Reacting before resource deletion: " + deletedVirtualFile);
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
   * <p>Also explicitly creates deletion activities for all contained (non-excluded) resources.
   * Contained files are processed first (in ascending depth order), followed by the contained
   * folders in descending depth order. This ensures that deletion activities for folders are only
   * send once the activities for all contained resources were sent, allowing the receiver to also
   * explicitly handle all resource deletions.
   *
   * @param deletedFolder the folder that was deleted
   */
  private void generateFolderDeletionActivity(@NotNull VirtualFile deletedFolder) {
    Set<IReferencePoint> sharedReferencePoints = session.getReferencePoints();

    IContainer container =
        (IContainer) VirtualFileConverter.convertToResource(sharedReferencePoints, deletedFolder);

    if (container == null || !session.isShared(container)) {
      if (log.isTraceEnabled()) {
        log.trace("Ignoring non-shared folder deletion: " + deletedFolder);
      }

      return;

    } else if (container.getType() == REFERENCE_POINT) {
      log.error(
          "Local representation of reference point "
              + container
              + " was deleted. Leaving the session.");

      NotificationPanel.showError(
          MessageFormat.format(
              Messages.LocalFilesystemModificationHandler_deleted_reference_point_message,
              deletedFolder.getPath()),
          Messages.LocalFilesystemModificationHandler_deleted_reference_point_title);

      CollaborationUtils.leaveSessionUnconditionally();

      return;
    }

    IReferencePoint baseReferencePoint = container.getReferencePoint();

    User user = session.getLocalUser();

    Deque<IActivity> queuedDeletionActivities = new ConcurrentLinkedDeque<>();

    VirtualFileFilter virtualFileFilter = getVirtualFileFilter();

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

          IFolder childFolder =
              (IFolder) VirtualFileConverter.convertToResource(fileOrDir, baseReferencePoint);

          if (childFolder == null || !session.isShared(childFolder)) {
            log.debug(
                "Ignoring non-shared child folder deletion: "
                    + fileOrDir
                    + ", parent folder: "
                    + container);

            return true;
          }

          IActivity newFolderDeletedActivity = new FolderDeletedActivity(user, childFolder);

          queuedDeletionActivities.addFirst(newFolderDeletedActivity);

          return true;
        };

    /*
     * Calls the above defined contentIterator on the base directory and every contained resource.
     * Directories are only stepped into if they match the above defined virtualFileFilter. This
     * also applies to the base directory.
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
    Set<IReferencePoint> sharedReferencePoints = session.getReferencePoints();

    IFile file = (IFile) VirtualFileConverter.convertToResource(sharedReferencePoints, deletedFile);

    if (file == null || !session.isShared(file)) {
      if (log.isTraceEnabled()) {
        log.trace("Ignoring non-shared file deletion: " + deletedFile);
      }

      return;
    }

    User user = session.getLocalUser();

    IActivity activity =
        new FileActivity(user, Type.REMOVED, FileActivity.Purpose.ACTIVITY, file, null, null, null);

    cleanUpDeletedFileState(file);
    cleanUpBackgroundEditorPool(file);

    dispatchActivity(activity);
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

    if (log.isTraceEnabled()) {
      log.trace(
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
   * <p>Also explicitly creates activities to move all contained (non-excluded) resources. When
   * creating the new resources, the creation/move activities are created in ascending depth order.
   * This ensures that creation/move activities are only send once the creation activities for all
   * parent resources were sent. When creating the deletion activities, the contained files are
   * processed first (in ascending depth order), followed by the contained folders in descending
   * depth order. This ensures that move activities for folders are only send once the activities
   * for all contained resources were sent, allowing the receiver to also explicitly handle all
   * resource deletions.
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

    Set<IReferencePoint> sharedReferencePoints = session.getReferencePoints();

    IContainer oldFolderWrapper =
        (IContainer) VirtualFileConverter.convertToResource(sharedReferencePoints, oldFile);
    IContainer newParentWrapper =
        (IContainer) VirtualFileConverter.convertToResource(sharedReferencePoints, newParent);

    User user = session.getLocalUser();

    boolean oldFolderIsShared = oldFolderWrapper != null && session.isShared(oldFolderWrapper);
    boolean newFolderIsShared = newParentWrapper != null && session.isShared(newParentWrapper);

    if (!oldFolderIsShared && !newFolderIsShared) {
      if (log.isTraceEnabled()) {
        log.trace(
            "Ignoring non-shared folder move - folder: "
                + oldFile
                + ", old parent: "
                + oldParent
                + ", new Parent: "
                + newParent);
      }

      return;

    } else if (oldFolderIsShared && oldFolderWrapper.getType() == REFERENCE_POINT) {
      if (ProjectAPI.isExcluded(project, newParent)) {
        log.error(
            "Local representation of reference point "
                + oldFolderWrapper
                + " was deleted. Leaving the session.");

        NotificationPanel.showError(
            MessageFormat.format(
                Messages
                    .LocalFilesystemModificationHandler_moved_reference_point_into_exclusion_message,
                oldFile.getPath(),
                newParent.getPath()),
            Messages.LocalFilesystemModificationHandler_moved_reference_point_into_exclusion_title);

        CollaborationUtils.leaveSessionUnconditionally();

        return;
      }

      if (log.isTraceEnabled()) {
        log.trace("Ignoring move of reference point base directory " + oldFolderWrapper);
      }

      return;
    }

    Deque<IActivity> queuedDeletionActivities = new ConcurrentLinkedDeque<>();

    VirtualFileFilter virtualFileFilter = getVirtualFileFilter();

    /*
     * Defines the actions executed on the base directory and every valid
     * child resource (defined through the virtualFileFilter).
     *
     * Returns whether the content iteration should be continued.
     */
    ContentIterator contentIterator =
        fileOrDir -> {
          Path relativePath = getRelativePath(oldFile, fileOrDir);

          if (relativePath == null) {
            return true;
          }

          if (!fileOrDir.isDirectory()) {
            generateFileMoveActivity(fileOrDir, oldParent, newParent, newFolderName, null);

            return true;
          }
          // TODO decide how to handle moved versions of ignored resources
          if (newFolderIsShared) {
            IFolder newFolder =
                newParentWrapper
                    .getReferencePoint()
                    .getFolder(
                        newParentWrapper
                            .getReferencePointRelativePath()
                            .resolve(folderName)
                            .resolve(relativePath));

            IActivity newFolderCreatedActivity = new FolderCreatedActivity(user, newFolder);

            dispatchActivity(newFolderCreatedActivity);
          }

          if (oldFolderIsShared) {
            IFolder oldFolder =
                oldFolderWrapper
                    .getReferencePoint()
                    .getFolder(
                        oldFolderWrapper.getReferencePointRelativePath().resolve(relativePath));

            if (!session.isShared(oldFolder)) {
              log.debug(
                  "Ignoring non-shared child folder move - folder: "
                      + oldFolder
                      + ", old parent: "
                      + oldFolderWrapper
                      + ", new Parent: "
                      + newParent);

            } else {
              IActivity oldFolderDeletedActivity = new FolderDeletedActivity(user, oldFolder);

              queuedDeletionActivities.addFirst(oldFolderDeletedActivity);
            }
          }

          return true;
        };

    /*
     * Calls the above defined contentIterator on the base directory and every contained resource.
     * Directories are only stepped into if they match the above defined virtualFileFilter. This
     * also applies to the base directory.
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

    Set<IReferencePoint> sharedReferencePoints = session.getReferencePoints();

    IFile oldFileWrapper =
        (IFile) VirtualFileConverter.convertToResource(sharedReferencePoints, oldFile);
    IContainer newParentWrapper =
        (IContainer) VirtualFileConverter.convertToResource(sharedReferencePoints, newBaseParent);

    User user = session.getLocalUser();

    boolean oldFileIsShared = oldFileWrapper != null && session.isShared(oldFileWrapper);
    boolean newFileIsShared = newParentWrapper != null && session.isShared(newParentWrapper);

    boolean isOpenInTextEditor = ProjectAPI.isOpenInTextEditor(project, oldFile);

    Path relativePath = getRelativePath(oldBaseParent, oldFile);

    if (relativePath == null) {
      log.warn(
          "Could not create file move activity for "
              + oldFile
              + " to "
              + newBaseParent
              + " an no relative path could be calculated for the file.");

      return;
    }

    if (newBaseName != null) {
      relativePath = Paths.get(newBaseName).resolve(PathUtils.removeFirstSegments(relativePath, 1));
    }

    if (newFileName != null) {
      relativePath = PathUtils.removeLastSegments(relativePath, 1).resolve(newFileName);
    }

    IActivity activity;

    if (oldFileIsShared && newFileIsShared) {
      // moved file inside/between shared reference point(s)
      IFile newFileWrapper =
          newParentWrapper
              .getReferencePoint()
              .getFile(newParentWrapper.getReferencePointRelativePath().resolve(relativePath));

      activity =
          new FileActivity(
              user,
              Type.MOVED,
              FileActivity.Purpose.ACTIVITY,
              newFileWrapper,
              oldFileWrapper,
              null,
              encoding);

      updateMovedFileState(oldFileWrapper, newFileWrapper);
      cleanUpBackgroundEditorPool(oldFileWrapper);

    } else if (newFileIsShared) {
      // TODO decide how to handle moved versions of ignored resources
      // moved file into shared reference point
      byte[] fileContent = getContent(oldFile);

      IFile newFileWrapper =
          newParentWrapper
              .getReferencePoint()
              .getFile(newParentWrapper.getReferencePointRelativePath().resolve(relativePath));

      activity =
          new FileActivity(
              user,
              Type.CREATED,
              FileActivity.Purpose.ACTIVITY,
              newFileWrapper,
              null,
              fileContent,
              encoding);

      if (isOpenInTextEditor) {
        setUpMovedEditorState(oldFile, newFileWrapper);
      }

    } else if (oldFileIsShared) {
      // moved file out of shared reference point
      activity =
          new FileActivity(
              user, Type.REMOVED, FileActivity.Purpose.ACTIVITY, oldFileWrapper, null, null, null);

      cleanUpDeletedFileState(oldFileWrapper);
      cleanUpBackgroundEditorPool(oldFileWrapper);

    } else {
      // neither source nor destination are shared
      if (log.isTraceEnabled()) {
        log.trace(
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

    if (oldFileIsShared && isOpenInTextEditor) {
      EditorActivity closeOldEditorActivity =
          new EditorActivity(user, EditorActivity.Type.CLOSED, oldFileWrapper);

      dispatchActivity(closeOldEditorActivity);
    }

    if (newFileIsShared && isOpenInTextEditor) {
      IFile newFileWrapper =
          newParentWrapper
              .getReferencePoint()
              .getFile(newParentWrapper.getReferencePointRelativePath().resolve(relativePath));

      EditorActivity openNewEditorActivity =
          new EditorActivity(user, EditorActivity.Type.ACTIVATED, newFileWrapper);

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

        // TODO consider using FilePropertyEvent#isRename() once it is no longer marked experimental
        if (oldName.equals(newName)) {
          log.debug(
              "Dropping file property event for "
                  + file
                  + " as it is detected to be a re-parsing of the file.");

          return;
        }

        if (log.isTraceEnabled()) {
          log.trace(
              "Reacting before resource name change - resource: "
                  + file
                  + ", old name: "
                  + oldName
                  + ", new name: "
                  + newName);
        }

        VirtualFile parent = file.getParent();

        if (parent == null) {
          Set<IReferencePoint> sharedReferencePoints = session.getReferencePoints();
          IResource resource = VirtualFileConverter.convertToResource(sharedReferencePoints, file);

          if (resource != null && session.isShared(resource)) {
            if (resource.getType() == REFERENCE_POINT) {
              if (log.isTraceEnabled()) {
                log.trace("Ignoring rename of reference point base directory " + resource);
              }

            } else {
              log.error(
                  "Renamed resource "
                      + resource
                      + " is a root directory but not a reference point. This should not be"
                      + " possible.");
            }
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
        if (log.isTraceEnabled()) {
          log.trace(
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
  private Path getRelativePath(@NotNull VirtualFile root, @NotNull VirtualFile file) {
    try {
      return Paths.get(root.getPath()).relativize(Paths.get(file.getPath()));

    } catch (IllegalArgumentException e) {
      log.warn(
          "Could not find a relative path from the base file " + root + " to the file " + file, e);

      return null;
    }
  }

  /**
   * Returns a filter determining which child resources to iterate over.
   *
   * <p>The filter only iterates over non-excluded resources.
   *
   * @return a filter determining which child resources to iterate over
   * @see ProjectAPI#isExcluded(Project, VirtualFile)
   */
  private VirtualFileFilter getVirtualFileFilter() {
    return (fileOrDir) -> ProjectAPI.isExcluded(project, fileOrDir);
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
        log.debug(
            "Could not get Document for file "
                + file
                + ", using file content on disk instead. This content might"
                + " not correctly represent the current state of the file"
                + " in Intellij.");

        return file.contentsToByteArray();
      }

    } catch (IOException e) {
      log.warn("Could not get content for file " + file, e);

      return new byte[0];
    }
  }

  /**
   * Sets the editor manager state up for the created file.
   *
   * @param createdFile the created file
   */
  private void setUpCreatedFileState(@NotNull IFile createdFile) {
    editorManager.openEditor(createdFile, false);
  }

  /**
   * Drops the held internal state for the deleted file.
   *
   * @param deletedFile the deleted file
   */
  private void cleanUpDeletedFileState(@NotNull IFile deletedFile) {
    editorManager.removeAllEditorsForFile(deletedFile);

    annotationManager.removeAnnotations(deletedFile);
  }

  /**
   * Releases and drops the held background editor for the deleted file if present.
   *
   * @param deletedFile the deleted file
   */
  private void cleanUpBackgroundEditorPool(@NotNull IFile deletedFile) {
    editorManager.removeBackgroundEditorForFile(deletedFile);
  }

  /**
   * Updates the held internal state with the new path for the moved file.
   *
   * @param oldFile the old location/version of the file
   * @param newFile the new location/version of the file
   */
  private void updateMovedFileState(@NotNull IFile oldFile, @NotNull IFile newFile) {
    editorManager.replaceAllEditorsForFile(oldFile, newFile);

    annotationManager.updateAnnotationFile(oldFile, newFile);
  }

  /**
   * Explicitly handles the new editor mapping. This is necessary when the resource with an already
   * open editor is "created" through a move into a shared reference point as this triggers before
   * the resource move is executed, meaning the new resource does not exist yet. The editor for the
   * old resource does however already exist. As the existing editor will be used for the new
   * resource once it is moved, we have to add the mapping for the new file onto the "old" editor
   * manually.
   *
   * <p>Does nothing besides opening the editor if the moved file is not represented by a text
   * editor.
   */
  private void setUpMovedEditorState(@NotNull VirtualFile oldFile, @NotNull IFile newFile) {
    Editor editor = ProjectAPI.openEditor(project, oldFile, false);

    if (editor == null) {
      log.debug("Ignoring non-text editor of moved file " + oldFile);

      return;
    }

    editorManager.addEditorMapping(newFile, editor);
  }

  /**
   * Dispatches the given activity. Drops the activity instead if there is currently a file
   * replacement in progress.
   *
   * @param activity the activity to fire
   * @see FileReplacementInProgressObservable
   */
  private void dispatchActivity(@NotNull IActivity activity) {
    // HACK for now; see issue #993
    if (fileReplacementInProgressObservable.isReplacementInProgress()) {
      if (log.isTraceEnabled()) {
        log.trace("File replacement in progress - Ignoring local activity " + activity);
      }

      return;
    }

    log.debug("Dispatching resource activity " + activity);

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
      log.trace("Disabling filesystem listener");

      this.enabled = false;

      localFileSystem.removeVirtualFileListener(virtualFileListener);

    } else if (!this.enabled && enabled) {
      log.trace("Enabling filesystem listener");

      this.enabled = true;

      localFileSystem.addVirtualFileListener(virtualFileListener);
    }
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }
}
