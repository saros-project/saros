package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.EditorActivity.Type;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.activities.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.ViewportActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.DeleteOperation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.InsertOperation;
import de.fu_berlin.inf.dpp.core.editor.RemoteWriteAccessManager;
import de.fu_berlin.inf.dpp.editor.IEditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.editor.SharedEditorListenerDispatch;
import de.fu_berlin.inf.dpp.editor.remote.EditorState;
import de.fu_berlin.inf.dpp.editor.remote.UserEditorStateManager;
import de.fu_berlin.inf.dpp.editor.text.LineRange;
import de.fu_berlin.inf.dpp.editor.text.TextSelection;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.editor.annotations.AnnotationManager;
import de.fu_berlin.inf.dpp.intellij.editor.annotations.LocalClosedEditorModificationHandler;
import de.fu_berlin.inf.dpp.intellij.filesystem.Filesystem;
import de.fu_berlin.inf.dpp.intellij.filesystem.IntelliJReferencePointManager;
import de.fu_berlin.inf.dpp.intellij.filesystem.VirtualFileConverter;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer.Priority;
import de.fu_berlin.inf.dpp.session.IReferencePointManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISessionLifecycleListener;
import de.fu_berlin.inf.dpp.session.ISessionListener;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.synchronize.Blockable;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        public void exec(IActivity activity) {
          // First let the remote manager update itself based on the
          // Activity
          remoteWriteAccessManager.exec(activity);

          super.exec(activity);
        }

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

          adjustAnnotationsAfterEdit(user, getFile(path), editorPool.getEditor(path), operation);

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

          IFile file = getFile(path);

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
        public void resourcesAdded(final IReferencePoint referencePoint) {
          IReferencePointManager referencePointManager =
              session.getComponent(IReferencePointManager.class);
          IProject project = referencePointManager.get(referencePoint);

          ApplicationManager.getApplication()
              .invokeAndWait(
                  () -> addProjectResources(project), ModalityState.defaultModalityState());
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

          editorPool
              .getMapping()
              .forEach(
                  (path, editor) -> {
                    sendEditorOpenInformation(localUser, path);

                    sendViewPortInformation(localUser, path, editor);

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

        private void sendViewPortInformation(
            @NotNull User user, @NotNull SPath path, @NotNull Editor editor) {

          LineRange localViewPort = editorAPI.getLocalViewportRange(editor);
          int viewPortStartLine = localViewPort.getStartLine();
          int viewPortLength = localViewPort.getNumberOfLines();

          ViewportActivity setViewPort =
              new ViewportActivity(user, viewPortStartLine, viewPortLength, path);

          fireActivity(setViewPort);
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
      };

  /**
   * Adds all currently open editors belonging to the passed project to the pool of open editors.
   *
   * @param project the added project
   */
  private void addProjectResources(IProject project) {
    VirtualFile[] openFiles = projectAPI.getOpenFiles();

    SelectedEditorState selectedEditorState = new SelectedEditorState();
    selectedEditorState.captureState();

    try {
      localEditorStatusChangeHandler.setEnabled(false);

      for (VirtualFile openFile : openFiles) {
        localEditorHandler.openEditor(openFile, project, false);
      }

    } finally {
      localEditorStatusChangeHandler.setEnabled(true);
    }

    selectedEditorState.applyCapturedState();
  }

  @SuppressWarnings("FieldCanBeLocal")
  private final ISessionLifecycleListener sessionLifecycleListener =
      new ISessionLifecycleListener() {

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
          startSession(newSarosSession);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession, SessionEndReason reason) {

          assert session == oldSarosSession;
          session.getStopManager().removeBlockable(stopManagerListener); // todo

          executeInUIThreadSynchronous(this::endSession);
        }

        private void startSession(ISarosSession newSarosSession) {
          assert editorPool.getEditors().isEmpty() : "EditorPool was not correctly reset!";

          session = newSarosSession;
          session.getStopManager().addBlockable(stopManagerListener);

          hasWriteAccess = session.hasWriteAccess();
          session.addListener(sessionListener);

          session.addActivityProducer(EditorManager.this);
          session.addActivityConsumer(consumer, Priority.ACTIVE);

          localDocumentModificationHandler.setEnabled(true);
          localClosedEditorModificationHandler.setEnabled(true);

          userEditorStateManager = session.getComponent(UserEditorStateManager.class);
          remoteWriteAccessManager = new RemoteWriteAccessManager(session);

          // TODO: Test, whether this leads to problems because it is not called
          // from the UI thread.
          LocalFileSystem.getInstance().refresh(true);
        }

        private void endSession() {
          annotationManager.removeAllAnnotations();

          // This sets all editors, that were set to read only, writeable
          // again
          unlockAllEditors();
          editorPool.clear();

          session.removeListener(sessionListener);
          session.removeActivityProducer(EditorManager.this);
          session.removeActivityConsumer(consumer);

          localDocumentModificationHandler.setEnabled(false);
          localClosedEditorModificationHandler.setEnabled(false);

          session = null;

          userEditorStateManager = null;
          remoteWriteAccessManager.dispose();
          remoteWriteAccessManager = null;
        }
      };

  private final LocalEditorHandler localEditorHandler;
  private final LocalEditorManipulator localEditorManipulator;
  private final AnnotationManager annotationManager;
  private final FileReplacementInProgressObservable fileReplacementInProgressObservable;
  private final ProjectAPI projectAPI;
  private final EditorAPI editorAPI;

  private final EditorPool editorPool = new EditorPool();

  private final SharedEditorListenerDispatch editorListenerDispatch =
      new SharedEditorListenerDispatch();
  private UserEditorStateManager userEditorStateManager;
  private RemoteWriteAccessManager remoteWriteAccessManager;
  private ISarosSession session;

  private final LocalDocumentModificationHandler localDocumentModificationHandler;
  private final LocalClosedEditorModificationHandler localClosedEditorModificationHandler;
  private final LocalEditorStatusChangeHandler localEditorStatusChangeHandler;
  private final LocalTextSelectionChangeHandler localTextSelectionChangeHandler;
  private final LocalViewPortChangeHandler localViewPortChangeHandler;
  private final IntelliJReferencePointManager intelliJReferencePointManager;

  private boolean hasWriteAccess;
  // FIXME why is this never assigned? Either assign or remove flag
  private boolean isLocked;

  public EditorManager(
      ISarosSessionManager sessionManager,
      LocalEditorHandler localEditorHandler,
      LocalEditorManipulator localEditorManipulator,
      ProjectAPI projectAPI,
      AnnotationManager annotationManager,
      FileReplacementInProgressObservable fileReplacementInProgressObservable,
      Project project,
      EditorAPI editorAPI,
      IntelliJReferencePointManager intelliJReferencePointManager) {

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    this.localEditorHandler = localEditorHandler;
    this.localEditorManipulator = localEditorManipulator;
    this.annotationManager = annotationManager;
    this.fileReplacementInProgressObservable = fileReplacementInProgressObservable;
    this.editorAPI = editorAPI;
    this.intelliJReferencePointManager = intelliJReferencePointManager;

    localDocumentModificationHandler = new LocalDocumentModificationHandler(this);
    localClosedEditorModificationHandler =
        new LocalClosedEditorModificationHandler(
            this, projectAPI, annotationManager, intelliJReferencePointManager);
    localEditorStatusChangeHandler =
        new LocalEditorStatusChangeHandler(
            project, localEditorHandler, annotationManager, intelliJReferencePointManager);

    localTextSelectionChangeHandler = new LocalTextSelectionChangeHandler(this);
    localViewPortChangeHandler = new LocalViewPortChangeHandler(this, editorAPI);

    localEditorHandler.initialize(this);
    localEditorManipulator.initialize(this);

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

  ISarosSession getSession() {
    return session;
  }

  boolean hasSession() {
    return session != null;
  }

  LocalEditorStatusChangeHandler getLocalEditorStatusChangeHandler() {
    return localEditorStatusChangeHandler;
  }

  /**
   * Sets the local editor 'opened' and fires an {@link EditorActivity} of type {@link
   * Type#ACTIVATED}.
   *
   * @param path the project-relative path to the resource that the editor is currently editing or
   *     <code>null</code> if the local user has no editor open.
   */
  void generateEditorActivated(SPath path) {
    if (path == null || session.isShared(getResource(path))) {
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
    if (session.isShared(getResource(path))) {
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

  /** Generates a {@link TextSelectionActivity} and fires it. */
  void generateSelection(SPath path, SelectionEvent newSelection) {
    int offset = newSelection.getNewRange().getStartOffset();
    int length = newSelection.getNewRange().getLength();

    fireActivity(new TextSelectionActivity(session.getLocalUser(), offset, length, path));
  }

  /** Generates a {@link ViewportActivity} and fires it. */
  void generateViewport(SPath path, LineRange viewport) {
    if (session == null) {
      LOG.warn("SharedEditorListener not correctly unregistered!");
      return;
    }

    fireActivity(
        new ViewportActivity(
            session.getLocalUser(), viewport.getStartLine(), viewport.getNumberOfLines(), path));
  }

  /** Generates a TextEditActivity and fires it. */
  void generateTextEdit(int offset, String newText, String replacedText, SPath path) {

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

  boolean isDocumentModificationHandlerEnabled() {
    return localDocumentModificationHandler.isEnabled();
  }

  void enableDocumentHandlers() {
    localDocumentModificationHandler.setEnabled(true);
    localClosedEditorModificationHandler.setEnabled(true);
  }

  void disableDocumentHandlers() {
    localDocumentModificationHandler.setEnabled(false);
    localClosedEditorModificationHandler.setEnabled(false);
  }

  /**
   * Enables the localDocumentModificationHandler, the localEditorStatusChangeHandler, the
   * localTextSelectionChangeHandler and the localViewPortChangeHandler if the parameter is <code>
   * true</code>, else disables them.
   */
  private void setHandlersEnabled(boolean enable) {
    localDocumentModificationHandler.setEnabled(enable);
    localClosedEditorModificationHandler.setEnabled(enable);
    localEditorStatusChangeHandler.setEnabled(enable);
    localTextSelectionChangeHandler.setEnabled(enable);
    localViewPortChangeHandler.setEnabled(enable);
  }

  /**
   * Sets the editor's document writable and adds LocalTextSelectionChangeHandler,
   * LocalViewPortChangeHandler and the localDocumentModificationHandler.
   */
  void startEditor(Editor editor) {
    editor.getDocument().setReadOnly(isLocked || !hasWriteAccess);
    localTextSelectionChangeHandler.register(editor);
    localViewPortChangeHandler.register(editor);
  }

  /** Unlocks all editors in the editorPool. */
  private void unlockAllEditors() {
    setHandlersEnabled(true);
    editorPool.unlockAllDocuments();
  }

  /** Locks all open editors, by setting them to read-only. */
  private void lockAllEditors() {
    setHandlersEnabled(false);
    editorPool.lockAllDocuments();
  }

  private void executeInUIThreadSynchronous(Runnable runnable) {
    ApplicationManager.getApplication()
        .invokeAndWait(runnable, ModalityState.defaultModalityState());
  }

  @Override
  public void saveEditors(final IReferencePoint referencePoint) {
    executeInUIThreadSynchronous(
        () -> {
          Set<SPath> editorPaths = new HashSet<>(editorPool.getFiles());

          if (userEditorStateManager != null) {
            editorPaths.addAll(userEditorStateManager.getOpenEditors());
          }

          for (SPath editorPath : editorPaths) {
            if (referencePoint == null || referencePoint.equals(editorPath.getReferencePoint())) {
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
   * <p>Only adjusts the viewport if there currently is an open editor for the given path (meaning
   * such an editor is contained in the editor pool).
   *
   * @param path {@inheritDoc}
   * @param range {@inheritDoc}
   * @param selection {@inheritDoc}
   * @see LocalEditorManipulator#adjustViewport(Editor, LineRange, TextSelection)
   */
  @Override
  public void adjustViewport(
      @NotNull final SPath path, final LineRange range, final TextSelection selection) {

    executeInUIThreadSynchronous(
        () -> {
          Editor editor = editorPool.getEditor(path);

          if (editor == null) {
            LOG.warn(
                "Failed to adjust viewport for "
                    + path
                    + " as it is not known to the editor pool.");

            return;
          }

          localEditorManipulator.adjustViewport(editor, range, selection);
        });
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

  private IFile getFile(SPath path) {
    IReferencePoint referencePoint = path.getReferencePoint();
    IPath referencePointRelativePath = path.getReferencePointRelativePath();

    VirtualFile virtualFile =
        intelliJReferencePointManager.getResource(referencePoint, referencePointRelativePath);

    return (IFile) VirtualFileConverter.convertToResource(virtualFile);
  }

  private IResource getResource(SPath path) {
    IReferencePoint referencePoint = path.getReferencePoint();
    IPath referencePointRelativePath = path.getReferencePointRelativePath();

    VirtualFile virtualFile =
        intelliJReferencePointManager.getResource(referencePoint, referencePointRelativePath);

    return VirtualFileConverter.convertToResource(virtualFile);
  }
}
