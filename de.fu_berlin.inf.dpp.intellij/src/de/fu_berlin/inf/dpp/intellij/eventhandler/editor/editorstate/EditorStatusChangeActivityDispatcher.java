package de.fu_berlin.inf.dpp.intellij.eventhandler.editor.editorstate;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import de.fu_berlin.inf.dpp.intellij.editor.LocalEditorHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Dispatches matching editor activities when an editor for a shared file is opened/selected or
 * closed.
 */
public class EditorStatusChangeActivityDispatcher extends AbstractLocalEditorStatusChangeHandler {

  private final LocalEditorHandler localEditorHandler;

  private final FileEditorManagerListener fileEditorManagerListener =
      new FileEditorManagerListener() {
        @Override
        public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
          assert isEnabled() : "the file closed listener was triggered while it was disabled";

          generateEditorClosedActivity(file);
        }

        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
          assert isEnabled() : "the selection changed listener was triggered while it was disabled";

          generateEditorActivatedActivity(event);
        }
      };

  public EditorStatusChangeActivityDispatcher(
      Project project, LocalEditorHandler localEditorHandler) {

    super(project);

    this.localEditorHandler = localEditorHandler;

    setEnabled(true);
  }

  /**
   * Calls {@link LocalEditorHandler#closeEditor(VirtualFile)}.
   *
   * @param virtualFile the file whose editor was closed
   * @see FileEditorManagerListener#fileClosed(FileEditorManager, VirtualFile)
   */
  private void generateEditorClosedActivity(@NotNull VirtualFile virtualFile) {
    localEditorHandler.closeEditor(virtualFile);
  }

  /**
   * Calls {@link LocalEditorHandler#activateEditor(VirtualFile)}.
   *
   * @param event the event to react to
   * @see FileEditorManagerListener#selectionChanged(FileEditorManagerEvent)
   */
  private void generateEditorActivatedActivity(@NotNull FileEditorManagerEvent event) {
    localEditorHandler.activateEditor(event.getNewFile());
  }

  @Override
  void registerListeners(@NotNull MessageBusConnection messageBusConnection) {
    messageBusConnection.subscribe(
        fileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
  }
}
