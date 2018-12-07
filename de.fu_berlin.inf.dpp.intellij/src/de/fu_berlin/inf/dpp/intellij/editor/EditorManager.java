package de.fu_berlin.inf.dpp.intellij.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.util.Computable;
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
import de.fu_berlin.inf.dpp.editor.FollowModeManager;
import de.fu_berlin.inf.dpp.editor.IEditorManager;
import de.fu_berlin.inf.dpp.editor.ISharedEditorListener;
import de.fu_berlin.inf.dpp.editor.SharedEditorListenerDispatch;
import de.fu_berlin.inf.dpp.editor.remote.EditorState;
import de.fu_berlin.inf.dpp.editor.remote.UserEditorStateManager;
import de.fu_berlin.inf.dpp.editor.text.LineRange;
import de.fu_berlin.inf.dpp.editor.text.TextSelection;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.intellij.editor.annotations.AnnotationManager;
import de.fu_berlin.inf.dpp.intellij.editor.annotations.LocalClosedEditorModificationHandler;
import de.fu_berlin.inf.dpp.intellij.filesystem.Filesystem;
import de.fu_berlin.inf.dpp.intellij.filesystem.VirtualFileConverter;
import de.fu_berlin.inf.dpp.intellij.ui.util.NotificationPanel;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer.Priority;
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

/**
 * IntelliJ implementation of the {@link IEditorManager} interface.
 *
 * <p>FIXME: There are several usages of the "followedUser" field that should have nothing to do
 * with the follow mode. Search for "FIXME followMode" to find all instances.
 */
public class EditorManager extends AbstractActivityProducer implements IEditorManager {

  private static final Logger LOG = Logger.getLogger(EditorManager.class);

  private final Blockable stopManagerListener =
      new Blockable() {

        @Override
        public void unblock() {
          executeInUIThreadSynchronous(
              new Runnable() {
                @Override
                public void run() {
                  unlockAllEditors();
                }
              });
        }

        @Override
        public void block() {
          executeInUIThreadSynchronous(
              new Runnable() {
                @Override
                public void run() {
                  lockAllEditors();
                }
              });
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

        @Override
        public void receive(ViewportActivity viewportActivity) {
          execViewport(viewportActivity);
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
              // TODO: Let the FollowModeManager handle this
              if (isFollowing(user)) {
                localEditorManipulator.openEditor(path, false);
              }
              editorListenerDispatch.editorActivated(user, path);
              break;

            case CLOSED:
              // TODO: Let the FollowModeManager handle this
              if (isFollowing(user)) {
                localEditorManipulator.closeEditor(path);
              }
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

        private void execViewport(ViewportActivity viewport) {

          SPath path = viewport.getPath();
          LOG.debug(path + " viewport activity received " + viewport);
          if (path == null) {
            return;
          }

          User user = viewport.getSource();
          if (isFollowing(user)) {
            localEditorManipulator.setViewPort(
                path,
                viewport.getStartLine(),
                viewport.getStartLine() + viewport.getNumberOfLines());
          }
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

          refreshAnnotations();
        }

        @Override
        public void userFinishedProjectNegotiation(User user) {

          // Send awareness-information
          User localUser = session.getLocalUser();
          for (SPath path : getOpenEditors()) {
            fireActivity(new EditorActivity(localUser, EditorActivity.Type.ACTIVATED, path));
          }

          fireActivity(new EditorActivity(localUser, EditorActivity.Type.ACTIVATED, activeEditor));

          if (activeEditor == null) {
            return;
          }
          if (localViewport != null) {
            fireActivity(
                new ViewportActivity(
                    localUser,
                    localViewport.getStartLine(),
                    localViewport.getNumberOfLines(),
                    activeEditor));
          } else {
            LOG.warn("No viewport for locallyActivateEditor: " + activeEditor);
          }

          if (localSelection != null) {
            int offset = localSelection.getNewRange().getStartOffset();
            int length =
                localSelection.getNewRange().getEndOffset()
                    - localSelection.getNewRange().getStartOffset();

            fireActivity(new TextSelectionActivity(localUser, offset, length, activeEditor));
          } else {
            LOG.warn("No selection for locallyActivateEditor: " + activeEditor);
          }
        }

        @Override
        public void userLeft(final User user) {
          annotationManager.removeAnnotations(user);

          // TODO: Let the FollowModeManager handle this
          if (user.equals(followedUser)) {
            setFollowing(null);
          }
        }

        @Override
        public void resourcesAdded(final IProject project) {
          ApplicationManager.getApplication()
              .invokeAndWait(
                  new Runnable() {
                    @Override
                    public void run() {
                      addProjectResources(project);
                    }
                  },
                  ModalityState.defaultModalityState());
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

    for (VirtualFile openFile : openFiles) {
      localEditorHandler.openEditor(openFile, project, false);
      // TODO create selection activity if there is a current selection
    }

    selectedEditorState.applyCapturedState();
  }

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

          executeInUIThreadSynchronous(
              new Runnable() {
                @Override
                public void run() {
                  endSession();
                }
              });
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

          setFollowing(null);

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
          activeEditor = null;
        }
      };

  private final LocalEditorHandler localEditorHandler;
  private final LocalEditorManipulator localEditorManipulator;
  private final AnnotationManager annotationManager;
  private final FileReplacementInProgressObservable fileReplacementInProgressObservable;

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

  /** The user that is followed or <code>null</code> if no user is followed. */
  private User followedUser = null;

  private boolean hasWriteAccess;
  private boolean isLocked;
  private SelectionEvent localSelection;
  private LineRange localViewport;
  private SPath activeEditor;
  private ProjectAPI projectAPI;

  public EditorManager(
      ISarosSessionManager sessionManager,
      LocalEditorHandler localEditorHandler,
      LocalEditorManipulator localEditorManipulator,
      ProjectAPI projectAPI,
      AnnotationManager annotationManager,
      FileReplacementInProgressObservable fileReplacementInProgressObservable) {

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    this.localEditorHandler = localEditorHandler;
    this.localEditorManipulator = localEditorManipulator;
    this.annotationManager = annotationManager;
    this.fileReplacementInProgressObservable = fileReplacementInProgressObservable;

    localDocumentModificationHandler = new LocalDocumentModificationHandler(this);
    localClosedEditorModificationHandler =
        new LocalClosedEditorModificationHandler(this, projectAPI, annotationManager);
    localEditorStatusChangeHandler =
        new LocalEditorStatusChangeHandler(localEditorHandler, annotationManager);

    localTextSelectionChangeHandler = new LocalTextSelectionChangeHandler(this);
    localViewPortChangeHandler = new LocalViewPortChangeHandler(this);

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
        new Computable<String>() {

          @Override
          public String compute() {
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
          }
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
  public void saveDocument(SPath path) {
    localEditorHandler.saveDocument(path);
  }

  public void removeAllEditorsForPath(SPath path) {
    editorPool.removeEditor(path);
  }

  public void replaceAllEditorsForPath(SPath oldPath, SPath newPath) {
    editorPool.replacePath(oldPath, newPath);
  }

  /**
   * Returns the followed {@link User} or <code>null</code> if currently no user is followed.
   *
   * @deprecated Use the {@link FollowModeManager} instead.
   */
  @Deprecated
  public User getFollowedUser() {
    return followedUser;
  }

  /** @deprecated Use the {@link UserEditorStateManager} component directly. */
  @Deprecated
  public UserEditorStateManager getUserEditorStateManager() {
    return userEditorStateManager;
  }

  public boolean isActiveEditorShared() {
    return activeEditor != null && isSharedEditor(activeEditor);
  }

  /**
   * Sets the {@link User} to follow or <code>null</code> if no user should be followed. Calls
   * {@link SharedEditorListenerDispatch#followModeChanged(User, boolean)} to inform the other
   * participants of the change.
   *
   * <p>Jumps to the newly followed user.
   *
   * @deprecated Use the {@link FollowModeManager} instead.
   */
  @Deprecated
  public void setFollowing(User newFollowedUser) {
    assert newFollowedUser == null || !newFollowedUser.equals(session.getLocalUser())
        : "local user cannot follow himself!";

    User oldFollowedUser = followedUser;
    followedUser = newFollowedUser;

    if (oldFollowedUser != null && !oldFollowedUser.equals(newFollowedUser)) {
      editorListenerDispatch.followModeChanged(oldFollowedUser, false);
    }

    if (newFollowedUser != null) {
      editorListenerDispatch.followModeChanged(newFollowedUser, true);
      jumpToUser(newFollowedUser);
    }
  }

  LocalEditorHandler getLocalEditorHandler() {
    return localEditorHandler;
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

    activeEditor = path;

    if (path == null || session.isShared(path.getResource())) {
      editorListenerDispatch.editorActivated(session.getLocalUser(), path);

      fireActivity(new EditorActivity(session.getLocalUser(), EditorActivity.Type.ACTIVATED, path));

      //  generateSelection(path, selection);  //FIXME: add this feature
      //  generateViewport(path, viewport);    //FIXME:s add this feature
    }
  }

  /**
   * Fires an EditorActivity.Type.CLOSED event for the given path and leaves following, if closing
   * the followed editor.
   */
  void generateEditorClosed(SPath path) {
    // if closing the followed editor, leave follow mode
    if (followedUser != null) {
      // TODO Let the FollowModeManager handle this
      EditorState activeEditor =
          userEditorStateManager.getState(followedUser).getActiveEditorState();

      if (activeEditor != null && activeEditor.getPath().equals(path)) {
        // follower closed the followed editor (no other editor gets
        // activated)
        setFollowing(null);
        NotificationPanel.showInformation(
            "You closed the followed editor.", "Follow Mode stopped!");
      }
    }

    // TODO What about cleaning up (editorPool, currentlyOpenEditors, ...)?
    // TODO What about a editorListenerDispatch.editorClosed() call?

    fireActivity(new EditorActivity(session.getLocalUser(), EditorActivity.Type.CLOSED, path));
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

    if (path.equals(activeEditor)) {
      localSelection = newSelection;
    }

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

    if (path.equals(activeEditor)) {
      localViewport = viewport;
    }

    fireActivity(
        new ViewportActivity(
            session.getLocalUser(), viewport.getStartLine(), viewport.getNumberOfLines(), path));

    //  editorListenerDispatch.viewportGenerated(part, viewport, path);  //FIXME: add this feature
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

  /**
   * Returns <code>true</code> if it is currently following user, otherwise <code>false</code>.
   *
   * @deprecated Use the {@link FollowModeManager} instead.
   */
  @Deprecated
  boolean isFollowing(User user) {
    return followedUser != null && followedUser.equals(user);
  }

  /**
   * Locally opens the editor that the User jumpTo has currently open, adjusts the viewport and
   * calls {@link SharedEditorListenerDispatch#jumpedToUser(User)} to inform the session
   * participants of the jump.
   */
  @Override
  public void jumpToUser(final User jumpTo) {

    // you can't jump to yourself
    if (session.getLocalUser().equals(jumpTo)) {
      return;
    }

    final EditorState remoteActiveEditor =
        userEditorStateManager.getState(jumpTo).getActiveEditorState();

    if (remoteActiveEditor == null) {
      LOG.info(jumpTo.getJID() + " has no editor open");
      return;
    }

    executeInUIThreadSynchronous(
        new Runnable() {
          @Override
          public void run() {
            Editor newEditor =
                localEditorManipulator.openEditor(remoteActiveEditor.getPath(), true);

            if (newEditor == null) {
              return;
            }

            LineRange viewport = remoteActiveEditor.getViewport();

            if (viewport == null) {
              LOG.warn(
                  jumpTo.getJID() + " has no viewport in editor: " + remoteActiveEditor.getPath());
              return;
            }
            // FIXME Why are we suddenly interested in the followedUser?
            EditorState state =
                userEditorStateManager.getState(followedUser).getActiveEditorState();

            TextSelection selection = (state == null) ? null : state.getSelection();

            // state.getSelection() can return null
            if (selection != null) {
              // FIXME Why are we only jumping if we know the selection,
              // but not if there is no selection but a perfectly usable
              // viewport?
              localEditorManipulator.adjustViewport(newEditor, viewport, selection);
            }
          }
        });

    editorListenerDispatch.jumpedToUser(jumpTo);
  }

  void refreshAnnotations() {
    // FIXME: needs implementation
  }

  boolean isSharedEditor(SPath editorFilePath) {
    if (session == null) {
      return false;
    }

    if (!localEditorHandler.isOpenEditor(editorFilePath)) {
      return false;
    }

    return session.isShared(editorFilePath.getResource());
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
  void setHandlersEnabled(boolean enable) {
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
  void unlockAllEditors() {
    setHandlersEnabled(true);
    editorPool.unlockAllDocuments();
  }

  /** Locks all open editors, by setting them to read-only. */
  void lockAllEditors() {
    setHandlersEnabled(false);
    editorPool.lockAllDocuments();
  }

  /** Unlocks all locally open editors by starting them. */
  public void unlockAllLocalOpenedEditors() {
    for (Editor editor : editorPool.getEditors()) {
      startEditor(editor);
    }
  }

  private void executeInUIThreadSynchronous(Runnable runnable) {
    ApplicationManager.getApplication()
        .invokeAndWait(runnable, ModalityState.defaultModalityState());
  }

  private void executeInUIThreadAsynchronous(Runnable runnable) {
    ApplicationManager.getApplication().invokeLater(runnable);
  }

  @Override
  public void saveEditors(final IProject project) {
    executeInUIThreadSynchronous(
        new Runnable() {
          @Override
          public void run() {

            Set<SPath> editorPaths = new HashSet<>(editorPool.getFiles());

            if (userEditorStateManager != null) {
              editorPaths.addAll(userEditorStateManager.getOpenEditors());
            }

            for (SPath editorPath : editorPaths) {
              if (project == null || project.equals(editorPath.getProject())) {

                saveDocument(editorPath);
              }
            }
          }
        });
  }

  @Override
  public void openEditor(final SPath path, final boolean activate) {
    executeInUIThreadSynchronous(
        new Runnable() {
          @Override
          public void run() {
            localEditorManipulator.openEditor(path, activate);
          }
        });
  }

  @Override
  public void closeEditor(final SPath path) {
    executeInUIThreadSynchronous(
        new Runnable() {
          @Override
          public void run() {
            localEditorManipulator.closeEditor(path);
          }
        });
  }

  @Override
  public void adjustViewport(
      final SPath path, final LineRange range, final TextSelection selection) {

    executeInUIThreadSynchronous(
        new Runnable() {
          @Override
          public void run() {
            Editor editor = localEditorManipulator.openEditor(path, false);
            localEditorManipulator.adjustViewport(editor, range, selection);
          }
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
}
