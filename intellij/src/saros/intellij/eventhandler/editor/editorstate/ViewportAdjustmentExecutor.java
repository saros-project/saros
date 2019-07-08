package saros.intellij.eventhandler.editor.editorstate;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.editor.text.LineRange;
import saros.editor.text.TextSelection;
import saros.intellij.editor.LocalEditorManipulator;
import saros.intellij.editor.ProjectAPI;

/**
 * Queues viewport adjustments for editors that are not currently visible and executes the queued
 * adjustment once the corresponding editor is selected.
 */
public class ViewportAdjustmentExecutor extends AbstractLocalEditorStatusChangeHandler {
  private final ProjectAPI projectAPI;
  private final LocalEditorManipulator localEditorManipulator;

  private static final Map<String, QueuedViewPortChange> queuedViewPortChanges =
      new ConcurrentHashMap<>();

  private final FileEditorManagerListener fileEditorManagerListener =
      new FileEditorManagerListener() {
        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
          assert isEnabled() : "the selection changed listener was triggered while it was disabled";

          applyQueuedViewportChanges(event.getNewFile());
        }
      };

  public ViewportAdjustmentExecutor(
      Project project, ProjectAPI projectAPI, LocalEditorManipulator localEditorManipulator) {

    super(project);

    this.projectAPI = projectAPI;
    this.localEditorManipulator = localEditorManipulator;
  }

  @Override
  void registerListeners(@NotNull MessageBusConnection messageBusConnection) {
    messageBusConnection.subscribe(
        fileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
  }

  /**
   * Applies any queued viewport changes to the editor representing the given virtual file. Does
   * nothing if the given file is null or if there are no queued viewport changes.
   *
   * @param virtualFile the file whose editor viewport to adjust
   */
  private void applyQueuedViewportChanges(@Nullable VirtualFile virtualFile) {
    if (virtualFile == null) {
      return;
    }

    QueuedViewPortChange queuedViewPortChange = queuedViewPortChanges.remove(virtualFile.getPath());

    if (queuedViewPortChange == null) {
      return;
    }

    LineRange range = queuedViewPortChange.getRange();
    TextSelection selection = queuedViewPortChange.getSelection();
    Editor queuedEditor = queuedViewPortChange.getEditor();

    Editor editor;

    if (queuedEditor != null && !queuedEditor.isDisposed()) {
      editor = queuedEditor;

    } else {
      editor = projectAPI.openEditor(virtualFile, false);
    }

    ApplicationManager.getApplication()
        .invokeAndWait(() -> localEditorManipulator.adjustViewport(editor, range, selection));
  }

  /**
   * Queues a viewport adjustment for the given path using the given range and selection as
   * parameters for the viewport adjustment. If an editor is given, it will be used for the viewport
   * adjustment.
   *
   * @param path the path of the editor
   * @param editor the editor to queue a viewport adjustment for
   * @param range the line range used for the viewport adjustment
   * @param selection the text selection used for the viewport adjustment
   */
  public static void queueViewPortChange(
      @NotNull String path,
      @Nullable Editor editor,
      @Nullable LineRange range,
      @Nullable TextSelection selection) {

    QueuedViewPortChange requestedViewportChange =
        new QueuedViewPortChange(editor, range, selection);

    queuedViewPortChanges.put(path, requestedViewportChange);
  }

  /** Data storage class for queued viewport changes. */
  private static class QueuedViewPortChange {
    private final Editor editor;
    private final LineRange range;
    private final TextSelection selection;

    QueuedViewPortChange(
        @Nullable Editor editor, @Nullable LineRange range, @Nullable TextSelection selection) {
      this.editor = editor;
      this.range = range;
      this.selection = selection;
    }

    @Nullable
    Editor getEditor() {
      return editor;
    }

    @Nullable
    LineRange getRange() {
      return range;
    }

    @Nullable
    TextSelection getSelection() {
      return selection;
    }
  }
}
