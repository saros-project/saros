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
import de.fu_berlin.inf.dpp.intellij.editor.annotations.AnnotationManager;
import de.fu_berlin.inf.dpp.intellij.filesystem.VirtualFileConverter;
import de.fu_berlin.inf.dpp.intellij.session.SessionUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/** Dispatches activities for editor changes. */
class StoppableEditorFileListener extends AbstractStoppableListener {

  private static final Logger LOG = Logger.getLogger(StoppableEditorFileListener.class);

  private final AnnotationManager annotationManager;

  private MessageBusConnection messageBusConnection;

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

  StoppableEditorFileListener(EditorManager manager, AnnotationManager annotationManager) {

    super(manager);

    this.annotationManager = annotationManager;
  }

  /**
   * Adds the opened editor to the EditorPool and adds the local representation to annotations for
   * the opened file.
   *
   * @param virtualFile the file whose editor was opened
   * @see FileEditorManagerListener#fileOpened(FileEditorManager, VirtualFile)
   */
  private void setUpOpenedEditor(@NotNull VirtualFile virtualFile) {
    if (!enabled) {
      return;
    }

    Editor editor = editorManager.getLocalEditorHandler().openEditor(virtualFile, false);

    SPath sPath = VirtualFileConverter.convertToSPath(virtualFile);

    if (sPath != null && SessionUtils.isShared(sPath) && editor != null) {
      annotationManager.applyStoredAnnotations(sPath.getFile(), editor);
    }
  }

  /**
   * Calls {@link LocalEditorHandler#closeEditor(VirtualFile)}.
   *
   * @param virtualFile the file whose editor was closed
   * @see FileEditorManagerListener#fileClosed(FileEditorManager, VirtualFile)
   */
  private void generateEditorClosedActivity(@NotNull VirtualFile virtualFile) {
    if (!enabled) {
      return;
    }

    editorManager.getLocalEditorHandler().closeEditor(virtualFile);
  }

  /**
   * Calls {@link LocalEditorHandler#activateEditor(VirtualFile)}.
   *
   * @param event the event to react to
   * @see FileEditorManagerListener#selectionChanged(FileEditorManagerEvent)
   */
  private void generateEditorActivatedActivity(@NotNull FileEditorManagerEvent event) {

    VirtualFile virtualFile = event.getNewFile();

    if (!enabled || virtualFile == null) {
      return;
    }

    editorManager.getLocalEditorHandler().activateEditor(virtualFile);
  }

  /**
   * Cleans up the held annotations for the closed file and removes their local representation.
   *
   * @param virtualFile the file whose editor was closed
   * @see FileEditorManagerListener.Before#beforeFileClosed(FileEditorManager, VirtualFile)
   */
  private void cleanUpAnnotations(@NotNull VirtualFile virtualFile) {
    SPath sPath = VirtualFileConverter.convertToSPath(virtualFile);

    if (sPath != null && SessionUtils.isShared(sPath)) {
      IFile file = sPath.getFile();

      annotationManager.updateAnnotationStore(file);
      annotationManager.removeLocalRepresentation(file);
    }
  }

  /**
   * Subscribes the editor listeners to the given project.
   *
   * @param project the project whose file operations to listen to
   */
  void subscribe(@NotNull Project project) {

    if (messageBusConnection != null) {
      LOG.warn("Tried to register StoppableEditorListener that was " + "already registered");

      return;
    }

    messageBusConnection = project.getMessageBus().connect();

    messageBusConnection.subscribe(
        fileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
    messageBusConnection.subscribe(
        beforeFileEditorManagerListener.FILE_EDITOR_MANAGER, beforeFileEditorManagerListener);
  }

  /** Unsubscribes the editor listeners. */
  void unsubscribe() {
    messageBusConnection.disconnect();

    messageBusConnection = null;
  }
}
