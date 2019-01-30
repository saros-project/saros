package de.fu_berlin.inf.dpp.intellij.eventhandler.editor.editorstate;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBusConnection;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.editor.text.LineRange;
import de.fu_berlin.inf.dpp.editor.text.TextSelection;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.intellij.editor.EditorManager;
import de.fu_berlin.inf.dpp.intellij.editor.LocalEditorHandler;
import de.fu_berlin.inf.dpp.intellij.editor.LocalEditorManipulator;
import de.fu_berlin.inf.dpp.intellij.editor.ProjectAPI;
import de.fu_berlin.inf.dpp.intellij.editor.annotations.AnnotationManager;
import de.fu_berlin.inf.dpp.intellij.eventhandler.DisableableHandler;
import de.fu_berlin.inf.dpp.intellij.filesystem.VirtualFileConverter;
import de.fu_berlin.inf.dpp.intellij.session.SessionUtils;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Dispatches activities for editor changes. */
public class LocalEditorStatusChangeHandler implements DisableableHandler {

  private static final Logger log = Logger.getLogger(LocalEditorStatusChangeHandler.class);

  private final EditorManager editorManager;
  private final Project project;
  private final ProjectAPI projectAPI;
  private final LocalEditorHandler localEditorHandler;
  private final LocalEditorManipulator localEditorManipulator;
  private final AnnotationManager annotationManager;

  private MessageBusConnection messageBusConnection;
  private boolean enabled;

  private final Map<String, QueuedViewPortChange> queuedViewPortChanges;

  private final FileEditorManagerListener fileEditorManagerListener =
      new FileEditorManagerListener() {
        @Override
        public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
          sendExistingSelection(file);

          setUpOpenedEditor(file);
        }

        @Override
        public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
          generateEditorClosedActivity(file);
        }

        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
          generateEditorActivatedActivity(event);

          applyQueuedViewportChanges(event.getNewFile());
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
   * @param editorManager the EditorManager instance
   * @param project the current IntelliJ Project instance
   * @param projectAPI the ProjectAPI instance
   * @param localEditorHandler the LocalEditorHandler instance
   * @param localEditorManipulator the LocalEditorManipulator instance
   * @param annotationManager the AnnotationManager instance
   */
  public LocalEditorStatusChangeHandler(
      EditorManager editorManager,
      Project project,
      ProjectAPI projectAPI,
      LocalEditorHandler localEditorHandler,
      LocalEditorManipulator localEditorManipulator,
      AnnotationManager annotationManager) {

    this.editorManager = editorManager;
    this.project = project;
    this.projectAPI = projectAPI;
    this.localEditorHandler = localEditorHandler;
    this.localEditorManipulator = localEditorManipulator;
    this.annotationManager = annotationManager;

    this.queuedViewPortChanges = new HashMap<>();

    subscribe();
    this.enabled = true;
  }

  /**
   * Generates and dispatches a TextSelectionActivity for the opened editor. This is done to inform
   * other participants of pre-existing selections in case the editor has not been opened before
   * during the current session.
   *
   * @param virtualFile the file to send the current selection information for
   */
  private void sendExistingSelection(@NotNull VirtualFile virtualFile) {

    if (!enabled) {
      return;
    }

    Editor editor = localEditorHandler.openEditor(virtualFile, false);

    SPath sPath = VirtualFileConverter.convertToSPath(virtualFile);

    if (sPath != null && SessionUtils.isShared(sPath) && editor != null) {
      editorManager.sendExistingSelection(sPath, editor);
    }
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
    assert enabled : "the file closed listener was triggered while it was disabled";

    localEditorHandler.closeEditor(virtualFile);
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
      IFile file = sPath.getFile();

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
  public void queueViewPortChange(
      @NotNull String path,
      @Nullable Editor editor,
      @Nullable LineRange range,
      @Nullable TextSelection selection) {

    QueuedViewPortChange requestedViewportChange =
        new QueuedViewPortChange(editor, range, selection);

    queuedViewPortChanges.put(path, requestedViewportChange);
  }

  /** Data storage class for queued viewport changes. */
  private class QueuedViewPortChange {
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
