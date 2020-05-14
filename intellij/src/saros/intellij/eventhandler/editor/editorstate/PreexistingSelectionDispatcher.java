package saros.intellij.eventhandler.editor.editorstate;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import saros.filesystem.IFile;
import saros.filesystem.IProject;
import saros.intellij.editor.EditorManager;
import saros.intellij.editor.LocalEditorHandler;
import saros.intellij.filesystem.VirtualFileConverter;
import saros.session.ISarosSession;

/**
 * Dispatches a TextSelectionActivity containing the current selection when an editor for a shared
 * file is opened. This is needed to correctly update the user editor state for the opened editor
 * held by the other participants.
 *
 * <p>The pre-existing selection is always transmitted when an editor is opened for a shared file as
 * there currently is not way of differentiation whether an editors has previously been opened
 * during the current session or not.
 */
public class PreexistingSelectionDispatcher extends AbstractLocalEditorStatusChangeHandler {

  private final EditorManager editorManager;
  private final LocalEditorHandler localEditorHandler;
  private final ISarosSession sarosSession;

  private final FileEditorManagerListener fileEditorManagerListener =
      new FileEditorManagerListener() {
        @Override
        public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
          assert isEnabled() : "the file opened listener was triggered while it was disabled";

          sendExistingSelection(file);
        }
      };

  public PreexistingSelectionDispatcher(
      Project project,
      EditorManager editorManager,
      LocalEditorHandler localEditorHandler,
      ISarosSession sarosSession) {

    super(project);

    this.editorManager = editorManager;
    this.localEditorHandler = localEditorHandler;
    this.sarosSession = sarosSession;
  }

  /**
   * Generates and dispatches a TextSelectionActivity for the opened editor. This is done to inform
   * other participants of pre-existing selections in case the editor has not been opened before
   * during the current session.
   *
   * @param virtualFile the file to send the current selection information for
   */
  private void sendExistingSelection(@NotNull VirtualFile virtualFile) {
    Set<IProject> sharedReferencePoints = sarosSession.getProjects();

    IFile file = (IFile) VirtualFileConverter.convertToResource(sharedReferencePoints, virtualFile);

    if (file == null || !sarosSession.isShared(file)) {
      return;
    }

    Editor editor = localEditorHandler.openEditor(file, virtualFile, false);

    if (editor != null) {
      editorManager.sendExistingSelection(file, editor);
    }
  }

  @Override
  void registerListeners(@NotNull MessageBusConnection messageBusConnection) {
    messageBusConnection.subscribe(
        fileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
  }
}
