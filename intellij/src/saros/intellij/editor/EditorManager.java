package saros.intellij.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.activities.EditorActivity;
import saros.activities.EditorActivity.Type;
import saros.activities.SPath;
import saros.activities.TextEditActivity;
import saros.activities.TextSelectionActivity;
import saros.activities.ViewportActivity;
import saros.concurrent.jupiter.Operation;
import saros.concurrent.jupiter.internal.text.DeleteOperation;
import saros.concurrent.jupiter.internal.text.InsertOperation;
import saros.editor.IEditorManager;
import saros.editor.ISharedEditorListener;
import saros.editor.SharedEditorListenerDispatch;
import saros.editor.remote.EditorState;
import saros.editor.remote.UserEditorStateManager;
import saros.editor.text.LineRange;
import saros.editor.text.TextSelection;
import saros.filesystem.IFile;
import saros.filesystem.IProject;
import saros.intellij.editor.annotations.AnnotationManager;
import saros.intellij.eventhandler.editor.document.AbstractLocalDocumentModificationHandler;
import saros.intellij.eventhandler.editor.document.LocalClosedEditorModificationHandler;
import saros.intellij.eventhandler.editor.document.LocalDocumentModificationHandler;
import saros.intellij.eventhandler.editor.editorstate.AnnotationUpdater;
import saros.intellij.eventhandler.editor.editorstate.EditorStatusChangeActivityDispatcher;
import saros.intellij.eventhandler.editor.editorstate.PreexistingSelectionDispatcher;
import saros.intellij.eventhandler.editor.editorstate.ViewportAdjustmentExecutor;
import saros.intellij.eventhandler.editor.viewport.LocalViewPortChangeHandler;
import saros.intellij.filesystem.Filesystem;
import saros.intellij.filesystem.VirtualFileConverter;
import saros.observables.FileReplacementInProgressObservable;
import saros.session.AbstractActivityConsumer;
import saros.session.AbstractActivityProducer;
import saros.session.IActivityConsumer;
import saros.session.IActivityConsumer.Priority;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.ISessionLifecycleListener;
import saros.session.ISessionListener;
import saros.session.SessionEndReason;
import saros.session.User;
import saros.synchronize.Blockable;

/** IntelliJ implementation of the {@link IEditorManager} interface. */
public class EditorManager extends AbstractActivityProducer implements IEditorManager {

  private static final Logger LOG = Logger.getLogger(EditorManager.class);

  private final Blockable stopManagerListener =
      new Blockable() {

        @Override
        public void unblock() {
          executeInUIThreadSynchronous(EditorManager.this::unlockAllEditors);
        }

        @Override
        public void block() {
          executeInUIThreadSynchronous(EditorManager.this::lockAllEditors);
        }
      };

  private final IActivityConsumer consumer =
      new AbstractActivityConsumer() {

        @Override
        public void receive(EditorActivity editorActivity) {
          execEditorActivity(editorActivity);
        }

        @Override
        public void receive(TextEditActivity textEditActivity) {
          execTextEdit(textEditActivity);
        }

        @Override
        public void receive(TextSelectionActivity textSelectionActivity) {
          execTextSelection(textSelectionActivity);
        }

        private void execEditorActivity(EditorActivity editorActivity) {

          SPath path = editorActivity.getPath();
          if (path == null) {
            return;
          }

          LOG.debug(path + " editor activity received " + editorActivity);

          final User user = editorActivity.getSource();

          switch (editorActivity.getType()) {
            case ACTIVATED:
              editorListenerDispatch.editorActivated(user, path);
              break;

            case CLOSED:
              editorListenerDispatch.editorClosed(user, path);
              break;
            case SAVED:
              localEditorHandler.saveDocument(path);
              break;
            default:
              LOG.warn("Unexpected type: " + editorActivity.getType());
          }
        }

        private void execTextEdit(TextEditActivity editorActivity) {

          SPath path = editorActivity.getPath();

          if (path == null) {
            return;
          }

          LOG.debug(path + " text edit activity received " + editorActivity);

          User user = editorActivity.getSource();

          Operation operation = editorActivity.toOperation();

          localEditorManipulator.applyTextOperations(path, operation);

          adjustAnnotationsAfterEdit(user, path.getFile(), editorPool.getEditor(path), operation);

          editorListenerDispatch.textEdited(editorActivity);
        }

        /**
         * Adjusts the currently present notifications.
         *
         * <p>
         *
         * <p>If the given operation is an <code>InsertOperation</code>, a <code>
         * ContributionAnnotation</code> is added for the inserted text and all existing annotations
         * for the file are adjusted through {@link
         * AnnotationManager#moveAnnotationsAfterAddition(IFile, int, int)}.
         *
         * <p>If the given operation is a <code>DeleteOperation</code>, all existing annotations for
         * the file are adjusted through {@link
         * AnnotationManager#moveAnnotationsAfterDeletion(IFile, int, int)}.
         *
         * @param user the user for the given operation
         * @param file the file for the given operation
         * @param editor the editor for the given file
         * @param operations the received operation
         */
        private void adjustAnnotationsAfterEdit(
            @NotNull User user,
            @NotNull IFile file,
            @Nullable Editor editor,
            @NotNull Operation operations) {

          operations
              .getTextOperations()
              .forEach(
                  textOperation -> {
                    int start = textOperation.getPosition();
                    int end = textOperation.getPosition() + textOperation.getTextLength();

                    if (textOperation instanceof InsertOperation) {
                      if (editor == null) {
                        annotationManager.moveAnnotationsAfterAddition(file, start, end);
                      }

                      annotationManager.addContributionAnnotation(user, file, start, end, editor);

                    } else if (textOperation instanceof DeleteOperation && editor == null) {

                      annotationManager.moveAnnotationsAfterDeletion(file, start, end);
                    }
                  });
        }

        private void execTextSelection(TextSelectionActivity selection) {

          SPath path = selection.getPath();

          if (path == null) {
            return;
          }

          IFile file = path.getFile();

          LOG.debug("Text selection activity received: " + path + ", " + selection);

          User user = selection.getSource();
          int start = selection.getOffset();
          int end = start + selection.getLength();

          Editor editor = editorPool.getEditor(path);

          annotationManager.addSelectionAnnotation(user, file, start, end, editor);

          editorListenerDispatch.textSelectionChanged(selection);
        }
      };

  private final ISessionListener sessionListener =
      new ISessionListener() {

        @Override
        public void permissionChanged(final User user) {

          hasWriteAccess = session.hasWriteAccess();

          if (user.isLocal()) {
            if (hasWriteAccess) {
              lockAllEditors();
            } else {
              unlockAllEditors();
            }
          }
        }

        @Override
        public void userFinishedProjectNegotiation(User user) {
          sendAwarenessInformation();
        }

        @Override
        public void userLeft(final User user) {
          annotationManager.removeAnnotations(user);
        }

        @Override
        public void resourcesAdded(final IProject project) {
          executeInUIThreadSynchronous(() -> addProjectResources(project));
        }

        /**
         * Sends awareness information to populate the UserEditorState of the participant that
         * joined the session.
         *
         * <p>This is done by first sending the needed state for all locally open editors. After the
         * awareness information for all locally open editors (including the active editor) has been
         * transmitted, a second editor activated activity is send for the locally active editor to
         * correctly set the active editor in the remote user editor state for the local user.
         */
        private void sendAwarenessInformation() {
          User localUser = session.getLocalUser();

          Set<String> visibleFilePaths = new HashSet<>();

          for (VirtualFile virtualFile : projectAPI.getSelectedFiles()) {
            visibleFilePaths.add(virtualFile.getPath());
          }

          editorPool
              .getMapping()
              .forEach(
                  (path, editor) -> {
                    sendEditorOpenInformation(localUser, path);

                    sendViewPortInformation(localUser, path, editor, visibleFilePaths);

                    sendSelectionInformation(localUser, path, editor);
                  });

          Editor activeEditor = projectAPI.getActiveEditor();

          SPath activeEditorPath;

          if (activeEditor != null) {
            activeEditorPath = editorPool.getFile(activeEditor.getDocument());
          } else {
            activeEditorPath = null;
          }

          sendEditorOpenInformation(localUser, activeEditorPath);
        }

        private void sendEditorOpenInformation(@NotNull User user, @Nullable SPath path) {
          EditorActivity activateEditor =
              new EditorActivity(user, EditorActivity.Type.ACTIVATED, path);

          fireActivity(activateEditor);
        }

        /**
         * Sends the viewport information for the given editor if it is currently visible.
         *
         * @param user the local user
         * @param path the path of the editor
         * @param editor the editor to send the viewport for
         * @param visibleFilePaths the paths of all currently visible editors
         */
        private void sendViewPortInformation(
            @NotNull User user,
            @NotNull SPath path,
            @NotNull Editor editor,
            @NotNull Set<String> visibleFilePaths) {

          VirtualFile fileForEditor =
              FileDocumentManager.getInstance().getFile(editor.getDocument());

          if (fileForEditor == null) {
            LOG.warn(
                "Encountered editor without valid virtual file representation - path held in editor pool: "
                    + path);

            return;
          }

          if (!visibleFilePaths.contains(fileForEditor.getPath())) {
            LOG.debug(
                "Ignoring "
                    + path
                    + " while sending viewport awareness information as the editor is not currently visible.");

            return;
          }

          LineRange localViewPort = editorAPI.getLocalViewPortRange(editor);
          int viewPortStartLine = localViewPort.getStartLine();
          int viewPortLength = localViewPort.getNumberOfLines();

          ViewportActivity setViewPort =
              new ViewportActivity(user, viewPortStartLine, viewPortLength, path);

          fireActivity(setViewPort);
        }
      };

  /**
   * Generates and dispatches a TextSelectionActivity for the current selection in the given editor.
   * The local user will be used as the source of the activity and the given path will be used as
   * the path for the editor.
   *
   * <p><b>NOTE:</b> This should only be used to transfer pre-existing selection. To notify other
   * participants about new selections, {@link #generateSelection(SPath, SelectionEvent)} should be
   * used instead.
   *
   * <p><b>NOTE:</b> This class is meant for internal use only and should generally not be used
   * outside the editor package. If you still need to access this method, please consider whether
   * your class should rather be located in the editor package.
   *
   * @param path the path representing the given editor
   * @param editor the editor to send the selection for
   */
  public void sendExistingSelection(@NotNull SPath path, @NotNull Editor editor) {
    User localUser = session.getLocalUser();

    sendSelectionInformation(localUser, path, editor);
  }

  private void sendSelectionInformation(
      @NotNull User user, @NotNull SPath path, @NotNull Editor editor) {

    Pair<Integer, Integer> localSelectionOffsets = editorAPI.getLocalSelectionOffsets(editor);
    int selectionStartOffset = localSelectionOffsets.first;
    int selectionLength = localSelectionOffsets.second;

    TextSelectionActivity setSelection =
        new TextSelectionActivity(user, selectionStartOffset, selectionLength, path);

    fireActivity(setSelection);
  }

  /**
   * Adds all currently open editors belonging to the passed project to the pool of open editors.
   *
   * @param project the added project
   */
  private void addProjectResources(IProject project) {
    VirtualFile[] openFiles = projectAPI.getOpenFiles();

    SelectedEditorStateSnapshot selectedEditorStateSnapshot =
        selectedEditorStateSnapshotFactory.capturedState();

    try {
      setLocalEditorStatusChangeHandlersEnabled(false);
      setLocalViewPortChangeHandlersEnabled(false);

      for (VirtualFile openFile : openFiles) {
        localEditorHandler.openEditor(openFile, project, false);
      }

    } finally {
      setLocalViewPortChangeHandlersEnabled(true);
      setLocalEditorStatusChangeHandlersEnabled(true);
    }

    selectedEditorStateSnapshot.applyHeldState();
  }

  @SuppressWarnings("FieldCanBeLocal")
  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
          getSessionContextComponents(newSarosSession);

          startSession();
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {

          assert session == oldSarosSession;
          session.getStopManager().removeBlockable(stopManagerListener); // todo

          executeInUIThreadSynchronous(this::endSession);

          dropHeldSessionContextComponents();
        }

        /**
         * Reads the needed components from the session context.
         *
         * @param sarosSession the session to read from
         */
        private void getSessionContextComponents(ISarosSession sarosSession) {

          session = sarosSession;

          userEditorStateManager = session.getComponent(UserEditorStateManager.class);

          editorAPI = sarosSession.getComponent(EditorAPI.class);
          localEditorHandler = sarosSession.getComponent(LocalEditorHandler.class);
          localEditorManipulator = sarosSession.getComponent(LocalEditorManipulator.class);

          annotationManager = sarosSession.getComponent(AnnotationManager.class);

          selectedEditorStateSnapshotFactory =
              sarosSession.getComponent(SelectedEditorStateSnapshotFactory.class);

          localDocumentModificationHandler =
              sarosSession.getComponent(LocalDocumentModificationHandler.class);
          localClosedEditorModificationHandler =
              sarosSession.getComponent(LocalClosedEditorModificationHandler.class);

          annotationUpdater = sarosSession.getComponent(AnnotationUpdater.class);
          editorStatusChangeActivityDispatcher =
              sarosSession.getComponent(EditorStatusChangeActivityDispatcher.class);
          preexistingSelectionDispatcher =
              sarosSession.getComponent(PreexistingSelectionDispatcher.class);
          viewportAdjustmentExecutor = sarosSession.getComponent(ViewportAdjustmentExecutor.class);

          localViewPortChangeHandler = sarosSession.getComponent(LocalViewPortChangeHandler.class);
        }

        /** Drops all held components that were read from the session context. */
        private void dropHeldSessionContextComponents() {

          session = null;

          userEditorStateManager = null;

          editorAPI = null;
          localEditorHandler = null;
          localEditorManipulator = null;

          annotationManager = null;

          selectedEditorStateSnapshotFactory = null;

          localDocumentModificationHandler = null;
          localClosedEditorModificationHandler = null;

          annotationUpdater = null;
          editorStatusChangeActivityDispatcher = null;
          preexistingSelectionDispatcher = null;
          viewportAdjustmentExecutor = null;

          localViewPortChangeHandler = null;
        }

        /** Initializes all local components for the new session. */
        private void startSession() {
          assert editorPool.getEditors().isEmpty() : "EditorPool was not correctly reset!";

          session.getStopManager().addBlockable(stopManagerListener);

          hasWriteAccess = session.hasWriteAccess();
          session.addListener(sessionListener);

          session.addActivityProducer(EditorManager.this);
          session.addActivityConsumer(consumer, Priority.ACTIVE);

          setLocalDocumentModificationHandlersEnabled(true);

          // TODO: Test, whether this leads to problems because it is not called
          // from the UI thread.
          LocalFileSystem.getInstance().refresh(true);
        }

        /** Resets all local components for the session. */
        private void endSession() {
          // This sets all editors, that were set to read only, writeable
          // again
          unlockAllEditors();
          editorPool.clear();

          session.removeListener(sessionListener);
          session.removeActivityProducer(EditorManager.this);
          session.removeActivityConsumer(consumer);

          setLocalDocumentModificationHandlersEnabled(false);
        }
      };

  private final FileReplacementInProgressObservable fileReplacementInProgressObservable;
  private final ProjectAPI projectAPI;

  /* Session Components */
  private UserEditorStateManager userEditorStateManager;
  private ISarosSession session;
  private EditorAPI editorAPI;
  private LocalEditorHandler localEditorHandler;
  private LocalEditorManipulator localEditorManipulator;
  private AnnotationManager annotationManager;
  private SelectedEditorStateSnapshotFactory selectedEditorStateSnapshotFactory;

  /* Event handlers */
  // document changes
  private LocalDocumentModificationHandler localDocumentModificationHandler;
  private LocalClosedEditorModificationHandler localClosedEditorModificationHandler;
  // editor state changes
  private AnnotationUpdater annotationUpdater;
  private EditorStatusChangeActivityDispatcher editorStatusChangeActivityDispatcher;
  private PreexistingSelectionDispatcher preexistingSelectionDispatcher;
  private ViewportAdjustmentExecutor viewportAdjustmentExecutor;
  // viewport changes
  private LocalViewPortChangeHandler localViewPortChangeHandler;

  /* Session state */
  private final EditorPool editorPool = new EditorPool();

  private final SharedEditorListenerDispatch editorListenerDispatch =
      new SharedEditorListenerDispatch();

  private boolean hasWriteAccess;
  // FIXME why is this never assigned? Either assign or remove flag
  private boolean isLocked;

  public EditorManager(
      ISarosSessionManager sessionManager,
      ProjectAPI projectAPI,
      FileReplacementInProgressObservable fileReplacementInProgressObservable) {

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    this.fileReplacementInProgressObservable = fileReplacementInProgressObservable;
    this.projectAPI = projectAPI;
  }

  @Override
  public Set<SPath> getOpenEditors() {
    return editorPool.getFiles();
  }

  @Override
  public String getContent(final SPath path) {
    return Filesystem.runReadAction(
        () -> {
          VirtualFile virtualFile = VirtualFileConverter.convertToVirtualFile(path);

          if (virtualFile == null || !virtualFile.exists() || virtualFile.isDirectory()) {

            LOG.warn(
                "Could not retrieve content of "
                    + path
                    + " as a matching VirtualFile could not be found,"
                    + " does not exist, or is a directory");

            return null;
          }

          Document doc = projectAPI.getDocument(virtualFile);

          return (doc != null) ? doc.getText() : null;
        });
  }

  @Override
  public void addSharedEditorListener(ISharedEditorListener listener) {
    editorListenerDispatch.add(listener);
  }

  @Override
  public void removeSharedEditorListener(ISharedEditorListener listener) {
    editorListenerDispatch.remove(listener);
  }

  /**
   * Saves the document under path, thereby flushing its contents to disk.
   *
   * @param path the path for the document to save
   * @see Document
   * @see LocalEditorHandler#saveDocument(SPath)
   */
  private void saveDocument(SPath path) {
    localEditorHandler.saveDocument(path);
  }

  public void removeAllEditorsForPath(SPath path) {
    editorPool.removeEditor(path);
  }

  public void replaceAllEditorsForPath(SPath oldPath, SPath newPath) {
    editorPool.replacePath(oldPath, newPath);
  }

  EditorPool getEditorPool() {
    return editorPool;
  }

  /**
   * Returns an SPath representing the file corresponding to the given document if the editor for
   * the document is known to the editor pool.
   *
   * @param document the document to get an SPath for
   * @return an SPath representing the file corresponding to the given document or <code>null</code>
   *     if the given document is <code>null</code> or is not known to the editor pool.
   */
  @Nullable
  public SPath getFileForOpenEditor(@Nullable Document document) {
    return editorPool.getFile(document);
  }

  ISarosSession getSession() {
    return session;
  }

  boolean hasSession() {
    return session != null;
  }

  /**
   * Sets the local editor 'opened' and fires an {@link EditorActivity} of type {@link
   * Type#ACTIVATED}.
   *
   * @param path the project-relative path to the resource that the editor is currently editing or
   *     <code>null</code> if the local user has no editor open.
   */
  void generateEditorActivated(SPath path) {
    if (path == null || session.isShared(path.getResource())) {
      editorListenerDispatch.editorActivated(session.getLocalUser(), path);

      fireActivity(new EditorActivity(session.getLocalUser(), EditorActivity.Type.ACTIVATED, path));

      //  generateSelection(path, selection);  //FIXME: add this feature
      //  generateViewport(path, viewport);    //FIXME:s add this feature
    }
  }

  /**
   * Fires an EditorActivity.Type.CLOSED event for the given path and notifies the local
   * EditorListenerDispatcher.
   */
  void generateEditorClosed(@NotNull SPath path) {
    if (session.isShared(path.getResource())) {
      editorListenerDispatch.editorClosed(session.getLocalUser(), path);

      fireActivity(new EditorActivity(session.getLocalUser(), EditorActivity.Type.CLOSED, path));
    }
  }

  /**
   * Generates an editor save activity for the given path.
   *
   * @param path the path to generate an editor saved activity for
   */
  void generateEditorSaved(SPath path) {
    fireActivity(new EditorActivity(session.getLocalUser(), Type.SAVED, path));
  }

  /**
   * Generates a {@link TextSelectionActivity} and fires it.
   *
   * <p><b>NOTE:</b> This class is meant for internal use only and should generally not be used
   * outside the editor package. If you still need to access this method, please consider whether
   * your class should rather be located in the editor package.
   */
  public void generateSelection(SPath path, SelectionEvent newSelection) {
    int offset = newSelection.getNewRange().getStartOffset();
    int length = newSelection.getNewRange().getLength();

    fireActivity(new TextSelectionActivity(session.getLocalUser(), offset, length, path));
  }

  /**
   * Generates a {@link ViewportActivity} and fires it.
   *
   * <p><b>NOTE:</b> This class is meant for internal use only and should generally not be used
   * outside the editor package. If you still need to access this method, please consider whether
   * your class should rather be located in the editor package.
   */
  public void generateViewport(SPath path, LineRange viewport) {
    if (session == null) {
      LOG.warn("SharedEditorListener not correctly unregistered!");
      return;
    }

    fireActivity(
        new ViewportActivity(
            session.getLocalUser(), viewport.getStartLine(), viewport.getNumberOfLines(), path));
  }

  /**
   * Generates a TextEditActivity and fires it.
   *
   * <p><b>NOTE:</b> This class is meant for internal use only and should generally not be used
   * outside the editor package. If you still need to access this method, please consider whether
   * your class should rather be located in the editor package.
   */
  public void generateTextEdit(int offset, String newText, String replacedText, SPath path) {

    if (session == null) {
      return;
    }

    TextEditActivity textEdit =
        new TextEditActivity(session.getLocalUser(), offset, newText, replacedText, path);

    if (!hasWriteAccess || isLocked) {
      /*
       * TODO If we don't have {@link User.Permission#WRITE_ACCESS}, then
       * receiving this event might indicate that the user somehow
       * achieved to change his document. We should run a consistency
       * check.
       *
       * But watch out for changes because of a consistency check!
       */

      LOG.warn(
          "local user caused text changes: "
              + textEdit
              + " | write access : "
              + hasWriteAccess
              + ", session locked : "
              + isLocked);
      return;
    }

    /*
     * hack to avoid sending activities for changes caused by received
     * activities during the project negotiation
     */
    if (fileReplacementInProgressObservable.isReplacementInProgress()) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("File replacement in progress - Ignoring local activity " + textEdit);
      }

      return;
    }

    fireActivity(textEdit);

    editorListenerDispatch.textEdited(textEdit);
  }

  @Override
  public void jumpToUser(@NotNull final User jumpTo) {

    // you can't jump to yourself
    if (session.getLocalUser().equals(jumpTo)) {
      return;
    }

    final EditorState remoteActiveEditor =
        userEditorStateManager.getState(jumpTo).getActiveEditorState();

    if (remoteActiveEditor == null) {
      LOG.info("Remote user " + jumpTo + " does not have an open editor.");
      return;
    }

    executeInUIThreadSynchronous(
        () -> {
          Editor newEditor = localEditorManipulator.openEditor(remoteActiveEditor.getPath(), true);

          if (newEditor == null) {
            return;
          }

          LineRange viewport = remoteActiveEditor.getViewport();
          TextSelection textSelection = remoteActiveEditor.getSelection();

          localEditorManipulator.adjustViewport(newEditor, viewport, textSelection);
        });

    editorListenerDispatch.jumpedToUser(jumpTo);
  }

  /**
   * Enables or disables all editor state change listeners. This is done by registering or
   * unregistering the held listeners.
   *
   * <p>This method does nothing if the given state already matches the current state.
   *
   * @param enabled <code>true</code> to enable the handler, <code>false</code> disable the handler
   * @see AnnotationUpdater#setEnabled(boolean)
   * @see EditorStatusChangeActivityDispatcher#setEnabled(boolean)
   * @see PreexistingSelectionDispatcher#setEnabled(boolean)
   * @see ViewportAdjustmentExecutor#setEnabled(boolean)
   */
  void setLocalEditorStatusChangeHandlersEnabled(boolean enabled) {
    annotationUpdater.setEnabled(enabled);
    editorStatusChangeActivityDispatcher.setEnabled(enabled);
    preexistingSelectionDispatcher.setEnabled(enabled);
    viewportAdjustmentExecutor.setEnabled(enabled);
  }

  /**
   * Enables or disables all viewport change handler. This is not done by disabling the underlying
   * listener.
   *
   * @param enabled <code>true</code> to enable the handler, <code>false</code> disable the handler
   * @see LocalViewPortChangeHandler#setEnabled(boolean)
   */
  void setLocalViewPortChangeHandlersEnabled(boolean enabled) {
    localViewPortChangeHandler.setEnabled(enabled);
  }

  boolean isDocumentModificationHandlerEnabled() {
    return localDocumentModificationHandler.isEnabled();
  }

  /**
   * Enables or disabled all document modification handlers. Enables or disables the handler. This
   * is done by registering or unregistering the held listener.
   *
   * <p>This method does nothing if the given state already matches the current state.
   *
   * @param enabled <code>true</code> to enable the handlers, <code>false</code> disable the
   *     handlers
   * @see AbstractLocalDocumentModificationHandler#setEnabled(boolean)
   */
  void setLocalDocumentModificationHandlersEnabled(boolean enabled) {
    localDocumentModificationHandler.setEnabled(enabled);
    localClosedEditorModificationHandler.setEnabled(enabled);
  }

  /**
   * Sets the editor's document writable and adds LocalTextSelectionChangeHandler,
   * LocalViewPortChangeHandler and the localDocumentModificationHandler.
   */
  void startEditor(Editor editor) {
    editor.getDocument().setReadOnly(isLocked || !hasWriteAccess);
  }

  /** Unlocks all editors in the editorPool. */
  private void unlockAllEditors() {
    editorPool.unlockAllDocuments();
  }

  /** Locks all open editors, by setting them to read-only. */
  private void lockAllEditors() {
    editorPool.lockAllDocuments();
  }

  private void executeInUIThreadSynchronous(Runnable runnable) {
    ApplicationManager.getApplication()
        .invokeAndWait(runnable, ModalityState.defaultModalityState());
  }

  @Override
  public void saveEditors(final IProject project) {
    executeInUIThreadSynchronous(
        () -> {
          Set<SPath> editorPaths = new HashSet<>(editorPool.getFiles());

          if (userEditorStateManager != null) {
            editorPaths.addAll(userEditorStateManager.getOpenEditors());
          }

          for (SPath editorPath : editorPaths) {
            if (project == null || project.equals(editorPath.getProject())) {

              saveDocument(editorPath);
            }
          }
        });
  }

  @Override
  public void openEditor(final SPath path, final boolean activate) {
    executeInUIThreadSynchronous(() -> localEditorManipulator.openEditor(path, activate));
  }

  @Override
  public void closeEditor(final SPath path) {
    executeInUIThreadSynchronous(() -> localEditorManipulator.closeEditor(path));
  }

  /**
   * {@inheritDoc}
   *
   * <p>Only adjusts the viewport directly if the editor for the path is currently open. Otherwise,
   * the viewport adjustment will be queued until the editor is selected/opened the next time, at
   * which point the viewport will be adjusted. If multiple adjustment requests are done while the
   * editor is not currently visible, only the last one will be applied to the editor once it is
   * selected/opened.
   *
   * @param path {@inheritDoc}
   * @param range {@inheritDoc}
   * @param selection {@inheritDoc}
   * @see LocalEditorManipulator#adjustViewport(Editor, LineRange, TextSelection)
   */
  @Override
  public void adjustViewport(
      @NotNull final SPath path, final LineRange range, final TextSelection selection) {

    Set<String> visibleFilePaths = new HashSet<>();

    for (VirtualFile virtualFile : projectAPI.getSelectedFiles()) {
      visibleFilePaths.add(virtualFile.getPath());
    }

    VirtualFile passedFile = VirtualFileConverter.convertToVirtualFile(path);

    if (passedFile == null) {
      LOG.warn(
          "Ignoring request to adjust viewport as no valid VirtualFile could be found for "
              + path
              + " - given range: "
              + range
              + ", given selection: "
              + selection);

      return;
    }

    Editor editor = editorPool.getEditor(path);

    if (!visibleFilePaths.contains(passedFile.getPath())) {
      viewportAdjustmentExecutor.queueViewPortChange(
          passedFile.getPath(), editor, range, selection);

      return;
    }

    if (editor == null) {
      LOG.warn(
          "Failed to adjust viewport for "
              + path
              + " as it is not known to the editor pool even though it is currently open");

      return;
    }

    executeInUIThreadSynchronous(
        () -> localEditorManipulator.adjustViewport(editor, range, selection));
  }

  /**
   * Starts the listeners for the given editor and adds it to the editor pool with the given path.
   *
   * <p><b>NOTE:</b> This method should only be used when adding editors for files that are not yet
   * part of the session scope. This can be the case when an open file is moved into the session
   * scope. If the file is already part of the session scope, {@link #openEditor(SPath, boolean)}}
   * should be used instead as it ensures that the right editor for the path is used.
   *
   * @param file the file to add to the editor pool
   * @param editor the editor representing the given file
   * @see #openEditor(SPath, boolean)
   * @see #startEditor(Editor)
   */
  public void addEditorMapping(SPath file, Editor editor) {
    startEditor(editor);
    editorPool.add(file, editor);
  }
}
