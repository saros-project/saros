package saros.intellij.project;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import saros.activities.FileActivity;
import saros.activities.FolderCreatedActivity;
import saros.activities.FolderDeletedActivity;
import saros.activities.IActivity;
import saros.activities.IFileSystemModificationActivity;
import saros.activities.SPath;
import saros.filesystem.IFile;
import saros.filesystem.IFolder;
import saros.intellij.context.SharedIDEContext;
import saros.intellij.editor.LocalEditorHandler;
import saros.intellij.editor.LocalEditorManipulator;
import saros.intellij.editor.SelectedEditorStateSnapshot;
import saros.intellij.editor.SelectedEditorStateSnapshotFactory;
import saros.intellij.editor.annotations.AnnotationManager;
import saros.intellij.eventhandler.IApplicationEventHandler.ApplicationEventHandlerType;
import saros.repackaged.picocontainer.Startable;
import saros.session.AbstractActivityConsumer;
import saros.session.IActivityConsumer;
import saros.session.IActivityConsumer.Priority;
import saros.session.ISarosSession;

/** The SharedResourcesManager creates and handles file and folder activities. */
public class SharedResourcesManager implements Startable {

  private static final Logger LOG = Logger.getLogger(SharedResourcesManager.class);

  private static final int DELETION_FLAGS = 0;
  private static final boolean FORCE = false;
  private static final boolean LOCAL = false;

  private final ISarosSession sarosSession;
  private final SharedIDEContext sharedIDEContext;
  private final LocalEditorHandler localEditorHandler;
  private final LocalEditorManipulator localEditorManipulator;
  private final AnnotationManager annotationManager;
  private final SelectedEditorStateSnapshotFactory selectedEditorStateSnapshotFactory;

  @Override
  public void start() {
    ApplicationManager.getApplication()
        .invokeAndWait(
            () -> sarosSession.addActivityConsumer(consumer, Priority.ACTIVE),
            ModalityState.defaultModalityState());
  }

  @Override
  public void stop() {
    ApplicationManager.getApplication()
        .invokeAndWait(
            () -> sarosSession.removeActivityConsumer(consumer),
            ModalityState.defaultModalityState());
  }

  public SharedResourcesManager(
      ISarosSession sarosSession,
      LocalEditorHandler localEditorHandler,
      LocalEditorManipulator localEditorManipulator,
      AnnotationManager annotationManager,
      SharedIDEContext sharedIDEContext,
      SelectedEditorStateSnapshotFactory selectedEditorStateSnapshotFactory) {

    this.sarosSession = sarosSession;
    this.localEditorHandler = localEditorHandler;
    this.localEditorManipulator = localEditorManipulator;
    this.annotationManager = annotationManager;
    this.sharedIDEContext = sharedIDEContext;
    this.selectedEditorStateSnapshotFactory = selectedEditorStateSnapshotFactory;
  }

  private final IActivityConsumer consumer =
      new AbstractActivityConsumer() {
        @Override
        public void exec(IActivity activity) {
          if (!(activity instanceof IFileSystemModificationActivity)) {
            return;
          }

          LOG.trace("executing " + activity + " in " + Thread.currentThread().getName());

          super.exec(activity);

          LOG.trace("done executing " + activity);
        }

        @Override
        public void receive(FileActivity activity) {
          try {
            handleFileActivity(activity);
          } catch (IOException e) {
            LOG.error("Failed to execute activity: " + activity, e);
          }
        }

        @Override
        public void receive(FolderCreatedActivity activity) {
          try {
            handleFolderCreation(activity);
          } catch (IOException e) {
            LOG.error("Failed to execute activity: " + activity, e);
          }
        }

        @Override
        public void receive(FolderDeletedActivity activity) {
          try {
            handleFolderDeletion(activity);
          } catch (IOException e) {
            LOG.error("Failed to execute activity: " + activity, e);
          }
        }
      };

  private void handleFileActivity(@NotNull FileActivity activity) throws IOException {

    if (activity.isRecovery()) {
      handleFileRecovery(activity);
      return;
    }

    switch (activity.getType()) {
      case CREATED:
        handleFileCreation(activity);
        break;
      case REMOVED:
        handleFileDeletion(activity);
        break;
      case MOVED:
        handleFileMove(activity);
        break;
      default:
        throw new UnsupportedOperationException(
            "FileActivity type "
                + activity.getType()
                + " not supported. Dropped activity: "
                + activity);
    }
  }

  private void handleFileRecovery(@NotNull FileActivity activity) throws IOException {

    SPath path = activity.getPath();

    LOG.debug("performing recovery for file: " + activity.getPath().getFullPath());

    FileActivity.Type type = activity.getType();

    try {
      if (type == FileActivity.Type.CREATED) {
        if (path.getFile().exists()) {
          localEditorManipulator.handleContentRecovery(
              path, activity.getContent(), activity.getEncoding(), activity.getSource());

        } else {
          handleFileCreation(activity);
        }

      } else if (type == FileActivity.Type.REMOVED) {
        handleFileDeletion(activity);

      } else {
        LOG.warn("performing recovery for type " + type + " is not supported");
      }
    } finally {
      /*
       * always reset Jupiter or we will get into trouble because the
       * vector time has already been reset on the host
       */
      sarosSession.getConcurrentDocumentClient().reset(path);
    }
  }

  /**
   * Applies the given move FileActivity. Subsequently cleans up the EditorPool and
   * AnnotationManager for the moved file if necessary.
   *
   * @param activity the move activity to execute
   * @throws IOException if the creation of the new file or the deletion of the old file fails
   */
  private void handleFileMove(@NotNull FileActivity activity) throws IOException {

    SPath oldPath = activity.getOldPath();
    SPath newPath = activity.getPath();

    IFile oldFile = oldPath.getFile();
    IFile newFile = newPath.getFile();

    if (!oldFile.exists()) {
      LOG.warn(
          "Could not move file "
              + oldFile
              + " as it does not exist."
              + " source: "
              + oldFile
              + " destination: "
              + newFile);

      return;
    }

    boolean fileOpen = localEditorHandler.isOpenEditor(oldPath);

    SelectedEditorStateSnapshot selectedEditorStateSnapshot = null;

    if (fileOpen) {
      selectedEditorStateSnapshot = selectedEditorStateSnapshotFactory.capturedState();
    }

    localEditorManipulator.closeEditor(oldPath);

    annotationManager.updateAnnotationPath(oldFile, newFile);

    try {
      setFilesystemModificationHandlerEnabled(false);

      localEditorHandler.saveDocument(oldPath);

      newFile.create(oldFile.getContents(), FORCE);

      if (fileOpen) {
        localEditorManipulator.openEditor(newPath, false);

        try {
          selectedEditorStateSnapshot.replaceSelectedFile(oldFile, newFile);
        } catch (IllegalStateException e) {
          LOG.warn("Failed to update the captured selected editor state", e);
        }

        selectedEditorStateSnapshot.applyHeldState();
      }

      oldFile.delete(DELETION_FLAGS);

    } finally {
      setFilesystemModificationHandlerEnabled(true);
    }

    // TODO reset the vector time for the old file
  }

  private void handleFileDeletion(@NotNull FileActivity activity) throws IOException {

    SPath path = activity.getPath();
    IFile file = path.getFile();

    if (!file.exists()) {
      LOG.warn("Could not delete file " + file + " as it does not exist.");

      return;
    }

    if (localEditorHandler.isOpenEditor(path)) {
      localEditorManipulator.closeEditor(path);
    }

    try {
      setFilesystemModificationHandlerEnabled(false);

      localEditorHandler.saveDocument(path);

      file.delete(DELETION_FLAGS);

    } finally {
      setFilesystemModificationHandlerEnabled(true);
    }

    annotationManager.removeAnnotations(file);

    // TODO reset the vector time for the deleted file
  }

  private void handleFileCreation(@NotNull FileActivity activity) throws IOException {

    SPath path = activity.getPath();
    IFile file = path.getFile();

    if (file.exists()) {
      LOG.warn("Could not create file " + file + " as it already exists.");

      return;
    }

    InputStream contents = new ByteArrayInputStream(activity.getContent());

    try {
      setFilesystemModificationHandlerEnabled(false);

      file.create(contents, FORCE);

    } finally {
      setFilesystemModificationHandlerEnabled(true);
    }
  }

  private void handleFolderCreation(@NotNull FolderCreatedActivity activity) throws IOException {

    IFolder folder = activity.getPath().getFolder();

    if (folder.exists()) {
      LOG.warn("Could not create folder " + folder + " as it already exist.");

      return;
    }

    try {
      setFilesystemModificationHandlerEnabled(false);

      folder.create(FORCE, LOCAL);

    } finally {
      setFilesystemModificationHandlerEnabled(true);
    }
  }

  /**
   * Applies the given FolderDeletedActivity.
   *
   * <p><b>NOTE:</b> This currently does not check whether the deleted folder contains resources
   * outside the session scope. As a result, submodules of the shared module that are not present
   * for a different participant can be deleted accidentally through remote activities.
   *
   * @param activity the FolderDeletedActivity to execute
   * @throws IOException if the folder deletion fails
   */
  // TODO deal with children that are not part of the current session (submodules)
  private void handleFolderDeletion(@NotNull FolderDeletedActivity activity) throws IOException {

    IFolder folder = activity.getPath().getFolder();

    if (!folder.exists()) {
      LOG.warn("Could not delete folder " + folder + " as it does not exist.");

      return;
    }

    try {
      setFilesystemModificationHandlerEnabled(false);

      folder.delete(DELETION_FLAGS);

    } finally {
      setFilesystemModificationHandlerEnabled(true);
    }
  }

  /**
   * Enables or disables all filesystem modification handlers.
   *
   * @param enabled <code>true</code> to enable the handlers, <code>false</code> to disable the
   *     handlers
   */
  private void setFilesystemModificationHandlerEnabled(boolean enabled) {
    sharedIDEContext.setApplicationEventHandlersEnabled(
        ApplicationEventHandlerType.LOCAL_FILESYSTEM_MODIFICATION_HANDLER, enabled);
  }
}
