package saros.intellij.eventhandler.editor.editorstate;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import saros.intellij.editor.LocalEditorHandler;
import saros.intellij.editor.ProjectAPI;

/**
 * Dispatches matching editor activities when an editor for a shared file is opened/selected or
 * closed.
 *
 * <p>The listener for closed editors is called before the editor is closed. This gives us the
 * change to check the type of the editor before it is actually closed, which is necessary as we
 * only care about text editors.
 */
public class EditorStatusChangeActivityDispatcher extends AbstractLocalEditorStatusChangeHandler {

  private final LocalEditorHandler localEditorHandler;

  private final FileEditorManagerListener fileEditorManagerListener =
      new FileEditorManagerListener() {
        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
          assert isEnabled() : "the selection changed listener was triggered while it was disabled";

          generateEditorActivatedActivity(event);
        }
      };

  private final FileEditorManagerListener.Before beforeFileEditorManagerListener =
      new FileEditorManagerListener.Before() {
        @Override
        public void beforeFileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
          assert isEnabled() : "the file closed listener was triggered while it was disabled";

          generateEditorClosedActivity(file);
        }
      };

  public EditorStatusChangeActivityDispatcher(
      Project project, LocalEditorHandler localEditorHandler) {

    super(project);

    this.localEditorHandler = localEditorHandler;
  }

  /**
   * Calls {@link LocalEditorHandler#closeEditor(Project, VirtualFile)}.
   *
   * <p>Does nothing if the closed editor is not a text editor.
   *
   * @param virtualFile the file whose editor was closed
   * @see FileEditorManagerListener#fileClosed(FileEditorManager, VirtualFile)
   */
  private void generateEditorClosedActivity(@NotNull VirtualFile virtualFile) {
    if (ProjectAPI.isOpenInTextEditor(project, virtualFile)) {
      localEditorHandler.closeEditor(project, virtualFile);
    }
  }

  /**
   * Calls {@link LocalEditorHandler#activateEditor(Project, VirtualFile)}.
   *
   * <p>Does nothing if the opened editor is not a text editor.
   *
   * @param event the event to react to
   * @see FileEditorManagerListener#selectionChanged(FileEditorManagerEvent)
   */
  private void generateEditorActivatedActivity(@NotNull FileEditorManagerEvent event) {
    FileEditor newEditor = event.getNewEditor();

    if (newEditor == null || newEditor instanceof TextEditor) {
      localEditorHandler.activateEditor(project, event.getNewFile());
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
