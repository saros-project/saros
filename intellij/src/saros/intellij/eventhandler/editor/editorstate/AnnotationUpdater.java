package saros.intellij.eventhandler.editor.editorstate;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import saros.activities.SPath;
import saros.filesystem.IFile;
import saros.intellij.editor.LocalEditorHandler;
import saros.intellij.editor.annotations.AnnotationManager;
import saros.intellij.filesystem.VirtualFileConverter;
import saros.session.ISarosSession;

/**
 * Loads all stored annotations when an editor is opened for a shared file and updates the stored
 * annotations with the current state before an editor for a shared file is closed.
 */
public class AnnotationUpdater extends AbstractLocalEditorStatusChangeHandler {

  private final AnnotationManager annotationManager;
  private final LocalEditorHandler localEditorHandler;
  private final ISarosSession sarosSession;
  private final VirtualFileConverter virtualFileConverter;

  private final FileEditorManagerListener fileEditorManagerListener =
      new FileEditorManagerListener() {
        @Override
        public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
          assert isEnabled() : "the file opened listener was triggered while it was disabled";

          setUpOpenedEditor(file);
        }
      };

  private final FileEditorManagerListener.Before beforeFileEditorManagerListener =
      new FileEditorManagerListener.Before() {
        @Override
        public void beforeFileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
          assert isEnabled()
              : "the before file closed listener was triggered while it was disabled";

          cleanUpAnnotations(file);
        }
      };

  public AnnotationUpdater(
      Project project,
      AnnotationManager annotationManager,
      LocalEditorHandler localEditorHandler,
      ISarosSession sarosSession,
      VirtualFileConverter virtualFileConverter) {

    super(project);

    this.annotationManager = annotationManager;
    this.localEditorHandler = localEditorHandler;
    this.sarosSession = sarosSession;
    this.virtualFileConverter = virtualFileConverter;

    setEnabled(true);
  }

  /**
   * Adds the opened editor to the EditorPool and adds the local representation to annotations for
   * the opened file.
   *
   * @param virtualFile the file whose editor was opened
   * @see FileEditorManagerListener#fileOpened(FileEditorManager, VirtualFile)
   */
  private void setUpOpenedEditor(@NotNull VirtualFile virtualFile) {
    Editor editor = localEditorHandler.openEditor(virtualFile, false);

    SPath sPath = virtualFileConverter.convertToSPath(virtualFile);

    if (sPath == null || editor == null) {
      return;
    }

    IFile file = sPath.getFile();

    if (sarosSession.isShared(file)) {
      annotationManager.applyStoredAnnotations(file, editor);
    }
  }

  /**
   * Cleans up the held annotations for the closed file and removes their local representation.
   *
   * @param virtualFile the file whose editor was closed
   * @see FileEditorManagerListener.Before#beforeFileClosed(FileEditorManager, VirtualFile)
   */
  private void cleanUpAnnotations(@NotNull VirtualFile virtualFile) {
    SPath sPath = virtualFileConverter.convertToSPath(virtualFile);

    if (sPath == null) {
      return;
    }

    IFile file = sPath.getFile();

    if (sarosSession.isShared(file)) {
      annotationManager.updateAnnotationStore(file);
      annotationManager.removeLocalRepresentation(file);
    }
  }

  @Override
  void registerListeners(@NotNull MessageBusConnection messageBusConnection) {
    messageBusConnection.subscribe(
        fileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
    messageBusConnection.subscribe(
        beforeFileEditorManagerListener.FILE_EDITOR_MANAGER, beforeFileEditorManagerListener);
  }
}
