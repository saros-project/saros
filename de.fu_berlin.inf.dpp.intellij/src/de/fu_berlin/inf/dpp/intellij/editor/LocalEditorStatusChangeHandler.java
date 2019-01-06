package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.intellij.editor.annotations.AnnotationManager;
import de.fu_berlin.inf.dpp.intellij.filesystem.IntelliJReferencePointManager;
import de.fu_berlin.inf.dpp.intellij.filesystem.VirtualFileConverter;
import de.fu_berlin.inf.dpp.intellij.session.SessionUtils;
import org.jetbrains.annotations.NotNull;

/** Dispatches activities for editor changes. */
class LocalEditorStatusChangeHandler implements DisableableHandler {

  private final Project project;
  private final LocalEditorHandler localEditorHandler;
  private final AnnotationManager annotationManager;
  private final IntelliJReferencePointManager intelliJReferencePointManager;

  private MessageBusConnection messageBusConnection;
  private boolean enabled;

  private final FileEditorManagerListener fileEditorManagerListener =
      new FileEditorManagerListener() {
        @Override
        public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
          setUpOpenedEditor(file);
        }

        @Override
        public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
          generateEditorClosedActivity(file);
        }

        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
          generateEditorActivatedActivity(event);
        }
      };

  private final FileEditorManagerListener.Before beforeFileEditorManagerListener =
      new FileEditorManagerListener.Before() {
        @Override
        public void beforeFileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
          cleanUpAnnotations(file);
        }

        /**
         * NOP. Only needed to preserve backwards compatibility to Intellij versions older than
         * 2018.2.6.
         */
        // TODO remove once requiring the users to use Intellij 2018.2.6 or newer is acceptable
        @Override
        public void beforeFileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
          // NOP
        }
      };

  /**
   * Instantiates a LocalEditorStatusChangeHandler object. The handler is enabled by default.
   *
   * @param localEditorHandler the LocalEditorHandler instance
   * @param annotationManager the AnnotationManager instance
   */
  LocalEditorStatusChangeHandler(
      Project project,
      LocalEditorHandler localEditorHandler,
      AnnotationManager annotationManager,
      IntelliJReferencePointManager intelliJReferencePointManager) {

    this.project = project;
    this.localEditorHandler = localEditorHandler;
    this.annotationManager = annotationManager;
    this.intelliJReferencePointManager = intelliJReferencePointManager;

    subscribe();
    this.enabled = true;
  }

  /**
   * Adds the opened editor to the EditorPool and adds the local representation to annotations for
   * the opened file.
   *
   * @param virtualFile the file whose editor was opened
   * @see FileEditorManagerListener#fileOpened(FileEditorManager, VirtualFile)
   */
  private void setUpOpenedEditor(@NotNull VirtualFile virtualFile) {
    assert enabled : "the file opened listener was triggered while it was disabled";

    Editor editor = localEditorHandler.openEditor(virtualFile, false);

    SPath sPath = VirtualFileConverter.convertToSPath(virtualFile);

    if (sPath != null && SessionUtils.isShared(sPath) && editor != null) {
      annotationManager.applyStoredAnnotations(getFile(sPath), editor);
    }
  }

  /**
   * Calls {@link LocalEditorHandler#closeEditor(VirtualFile)}.
   *
   * @param virtualFile the file whose editor was closed
   * @see FileEditorManagerListener#fileClosed(FileEditorManager, VirtualFile)
   */
  private void generateEditorClosedActivity(@NotNull VirtualFile virtualFile) {
    assert enabled : "the file closed listener was triggered while it was disabled";

    localEditorHandler.closeEditor(virtualFile);
  }

  /**
   * Calls {@link LocalEditorHandler#activateEditor(VirtualFile)}.
   *
   * @param event the event to react to
   * @see FileEditorManagerListener#selectionChanged(FileEditorManagerEvent)
   */
  private void generateEditorActivatedActivity(@NotNull FileEditorManagerEvent event) {
    assert enabled : "the selection changed listener was triggered while it was disabled";

    localEditorHandler.activateEditor(event.getNewFile());
  }

  /**
   * Cleans up the held annotations for the closed file and removes their local representation.
   *
   * @param virtualFile the file whose editor was closed
   * @see FileEditorManagerListener.Before#beforeFileClosed(FileEditorManager, VirtualFile)
   */
  // TODO move to separate class in annotation package
  private void cleanUpAnnotations(@NotNull VirtualFile virtualFile) {
    SPath sPath = VirtualFileConverter.convertToSPath(virtualFile);

    if (sPath != null && SessionUtils.isShared(sPath)) {
      IFile file = getFile(sPath);

      annotationManager.updateAnnotationStore(file);
      annotationManager.removeLocalRepresentation(file);
    }
  }

  /** Subscribes the editor listeners to the given project. */
  private void subscribe() {
    messageBusConnection = project.getMessageBus().connect();

    messageBusConnection.subscribe(
        fileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
    messageBusConnection.subscribe(
        beforeFileEditorManagerListener.FILE_EDITOR_MANAGER, beforeFileEditorManagerListener);
  }

  /** Unsubscribes the editor listeners. */
  private void unsubscribe() {
    messageBusConnection.disconnect();

    messageBusConnection = null;
  }

  /**
   * Enables or disables the handler. This is done by registering or unregistering the held
   * listener.
   *
   * <p>This method does nothing if the given state already matches the current state.
   *
   * @param enabled <code>true</code> to enable the handler, <code>false</code> disable the handler
   */
  @Override
  public void setEnabled(boolean enabled) {
    if (this.enabled && !enabled) {
      unsubscribe();

      this.enabled = false;

    } else if (!this.enabled && enabled) {
      subscribe();

      this.enabled = true;
    }
  }

  private IFile getFile(SPath path) {
    IReferencePoint referencePoint = path.getReferencePoint();
    IPath referencePointRelativePath = path.getReferencePointRelativePath();

    VirtualFile virtualFile =
        intelliJReferencePointManager.getResource(referencePoint, referencePointRelativePath);

    return (IFile) VirtualFileConverter.convertToResource(virtualFile);
  }
}
