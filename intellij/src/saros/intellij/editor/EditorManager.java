package saros.intellij.editor;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.activities.EditorActivity;
import saros.activities.EditorActivity.Type;
import saros.activities.TextEditActivity;
import saros.activities.TextSelectionActivity;
import saros.activities.ViewportActivity;
import saros.editor.IEditorManager;
import saros.editor.ISharedEditorListener;
import saros.editor.SharedEditorListenerDispatch;
import saros.editor.remote.EditorState;
import saros.editor.remote.UserEditorStateManager;
import saros.editor.text.LineRange;
import saros.editor.text.TextPosition;
import saros.editor.text.TextSelection;
import saros.filesystem.IFile;
import saros.filesystem.IReferencePoint;
import saros.intellij.context.SharedIDEContext;
import saros.intellij.editor.annotations.AnnotationManager;
import saros.intellij.eventhandler.IProjectEventHandler.ProjectEventHandlerType;
import saros.intellij.eventhandler.editor.editorstate.ViewportAdjustmentExecutor;
import saros.intellij.filesystem.IntellijReferencePoint;
import saros.intellij.filesystem.VirtualFileConverter;
import saros.intellij.runtime.EDTExecutor;
import saros.intellij.runtime.FilesystemRunner;
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

/** Intellij implementation of the {@link IEditorManager} interface. */
public class EditorManager extends AbstractActivityProducer implements IEditorManager {

  private static final Logger log = Logger.getLogger(EditorManager.class);

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

          IFile file = editorActivity.getResource();

          if (file == null) {
            return;
          }

          log.debug(file + " editor activity received " + editorActivity);

          final User user = editorActivity.getSource();

          switch (editorActivity.getType()) {
            case ACTIVATED:
              editorListenerDispatch.editorActivated(user, file);
              break;

            case CLOSED:
              editorListenerDispatch.editorClosed(user, file);
              break;
            case SAVED:
              localEditorHandler.saveDocument(file);
              break;
            default:
              log.warn("Unexpected type: " + editorActivity.getType());
          }
        }

        private void execTextEdit(TextEditActivity textEditActivity) {
          IFile file = textEditActivity.getResource();

          log.debug(file + " text edit activity received " + textEditActivity);

          Editor calculationEditor = getCalculationEditor(file);

          if (calculationEditor == null) {
            log.warn(
                "Could not apply text edit "
                    + textEditActivity
                    + " as no editor could be obtained for resource "
                    + file);

            return;
          }

          /*
           * Intellij internally always uses UNIX line separators for editor content
           * -> no line ending denormalization necessary as normalized format matches editor format
           */
          String replacedText = textEditActivity.getReplacedText();
          String newText = textEditActivity.getNewText();

          int start =
              EditorAPI.calculateOffset(calculationEditor, textEditActivity.getStartPosition());

          int oldEnd = start + replacedText.length();
          int newEnd = start + newText.length();

          Document document = calculationEditor.getDocument();

          applyTextEdit(file, document, start, oldEnd, replacedText, newText);

          User user = textEditActivity.getSource();

          adjustAnnotationsAfterEdit(user, file, editorPool.getEditor(file), start, oldEnd, newEnd);

          editorListenerDispatch.textEdited(textEditActivity);
        }

        /**
         * Obtains an editor for the given file.
         *
         * <p>This editor might be a background editor and must therefore only be used for
         * calculation purposes.
         *
         * @param file the file to obtain an editor for
         * @return returns a valid editor for the given file or <code>null</code> if no such editor
         *     could be obtained
         * @see BackgroundEditorPool
         */
        private Editor getCalculationEditor(@NotNull IFile file) {
          Editor calculationEditor = editorPool.getEditor(file);

          if (calculationEditor != null) {
            return calculationEditor;
          }

          VirtualFile virtualFile = VirtualFileConverter.convertToVirtualFile(file);

          if (virtualFile == null) {
            log.warn(
                "Could not create an editor instance for "
                    + file
                    + " as no virtual file could be obtained for the resource.");

            return null;
          }

          Document document = DocumentAPI.getDocument(virtualFile);

          if (document == null) {
            log.warn(
                "Could not create an editor instance for "
                    + virtualFile
                    + " as no document could be obtained for the file.");

            return null;
          }

          return backgroundEditorPool.getBackgroundEditor(file, document);
        }

        /**
         * Applies the text edit represented by the given values to the given document.
         *
         * @param file the file whose document to modify
         * @param document the document to modify
         * @param start the start offset for the modification
         * @param oldEnd the end offset of the replaced text
         * @param replacedText the text replaced in the document
         * @param newText the new text inserted into the document
         */
        private void applyTextEdit(
            @NotNull IFile file,
            @NotNull Document document,
            int start,
            int oldEnd,
            @NotNull String replacedText,
            @NotNull String newText) {

          Project project = ((IntellijReferencePoint) file.getReferencePoint()).getProject();

          if (!replacedText.isEmpty()) {
            String documentReplacedText = document.getText(new TextRange(start, oldEnd));

            if (!replacedText.equals(documentReplacedText)) {
              log.error(
                  "Text to be replaced for "
                      + file
                      + " from offset "
                      + start
                      + " to "
                      + oldEnd
                      + " does not match the given replaced text. Should be '"
                      + StringEscapeUtils.escapeJava(replacedText)
                      + "', but is '"
                      + StringEscapeUtils.escapeJava(documentReplacedText)
                      + "'.");
            }
          }

          try {
            /*
             * Disable documentListener temporarily to avoid being notified of
             * the change
             */
            setLocalDocumentModificationHandlersEnabled(false);

            boolean writePermission = document.isWritable();

            if (!writePermission) {
              document.setReadOnly(false);
            }

            DocumentAPI.replaceText(project, document, start, oldEnd, newText);

            if (!writePermission) {
              document.setReadOnly(true);
            }

          } finally {
            setLocalDocumentModificationHandlersEnabled(true);
          }
        }

        /**
         * Adjusts all current annotations for the given file according to the given text edit
         * offsets. Adds a contribution annotation if the text edit added new text.
         *
         * @param user the user that caused the text edit
         * @param file the file that was modified
         * @param editor the editor for the file
         * @param start the start offset of the text edit
         * @param oldEnd the end offset of the replaced text
         * @param newEnd the end offset of the new text
         * @see AnnotationManager#moveAnnotationsAfterDeletion(IFile, int, int)
         * @see AnnotationManager#moveAnnotationsAfterAddition(IFile, int, int)
         */
        private void adjustAnnotationsAfterEdit(
            @NotNull User user,
            @NotNull IFile file,
            @Nullable Editor editor,
            int start,
            int oldEnd,
            int newEnd) {

          if (oldEnd != start) {
            if (editor == null) {
              annotationManager.moveAnnotationsAfterDeletion(file, start, oldEnd);

            } else {
              // ensure that annotations invalidated by deletion are pruned
              annotationManager.updateAnnotationStore(file);
            }
          }

          if (newEnd != start) {
            if (editor == null) {
              annotationManager.moveAnnotationsAfterAddition(file, start, newEnd);
            }

            annotationManager.addContributionAnnotation(user, file, start, newEnd, editor);
          }
        }

        private void execTextSelection(TextSelectionActivity selection) {

          IFile file = selection.getResource();

          if (file == null) {
            return;
          }

          log.debug("Text selection activity received: " + file + ", " + selection);

          User user = selection.getSource();

          Editor editor = editorPool.getEditor(file);

          // Editor used for position calculation
          Editor calcEditor = editor;

          if (calcEditor == null) {
            VirtualFile virtualFile = VirtualFileConverter.convertToVirtualFile(file);

            if (virtualFile == null) {
              log.warn(
                  "Could not apply selection "
                      + selection
                      + " as no virtual file could be obtained for resource "
                      + file);

              return;
            }

            Document document = DocumentAPI.getDocument(virtualFile);

            if (document == null) {
              log.warn(
                  "Could not apply selection "
                      + selection
                      + " as no document could be obtained for resource "
                      + virtualFile);

              return;
            }

            calcEditor = backgroundEditorPool.getBackgroundEditor(file, document);
          }

          TextSelection textSelection = selection.getSelection();

          if (textSelection.isEmpty()) {
            annotationManager.removeSelectionAnnotation(user, file);

          } else {
            Pair<Integer, Integer> offsets = EditorAPI.calculateOffsets(calcEditor, textSelection);

            int start = offsets.first;
            int end = offsets.second;

            boolean isBackwardsSelection = textSelection.isBackwardsSelection();

            annotationManager.addSelectionAnnotation(
                user, file, start, end, editor, isBackwardsSelection);
          }

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
        public void userFinishedResourceNegotiation(User user) {
          sendAwarenessInformation(user);
        }

        @Override
        public void userLeft(final User user) {
          annotationManager.removeAnnotations(user);
        }

        @Override
        public void resourcesAdded(final IReferencePoint referencePoint) {
          executeInUIThreadSynchronous(() -> addReferencePointResources(referencePoint));
        }

        /**
         * Sends the awareness information for all open shared editors. This is done to populate the
         * UserEditorState of the participant that finished the resource negotiation.
         *
         * <p>This is done by first sending the needed state for all locally open editors. After the
         * awareness information for all locally open editors (including the active editor) has been
         * transmitted, a second editor activated activity is send for the locally active editor to
         * correctly set the active editor in the remote user editor state for the local user.
         *
         * <p>This will not be executed for the user that finished the resource negotiation as their
         * user editor state will be propagated through {@link #resourcesAdded(IReferencePoint)}
         * when the shared resources are initially added.
         */
        private void sendAwarenessInformation(@NotNull User user) {
          User localUser = session.getLocalUser();

          if (localUser.equals(user)) {
            return;
          }

          Set<String> visibleFilePaths = new HashSet<>();

          Project project = sharedIDEContext.getProject();

          for (VirtualFile virtualFile : ProjectAPI.getSelectedFiles(project)) {
            visibleFilePaths.add(virtualFile.getPath());
          }

          editorPool
              .getMapping()
              .forEach(
                  (file, editor) -> {
                    sendEditorOpenInformation(localUser, file);

                    sendViewPortInformation(localUser, file, editor, visibleFilePaths);

                    sendSelectionInformation(localUser, file, editor);
                  });

          sendActiveEditorInformation(localUser, project);
        }
      };

  /**
   * Generates and dispatches a TextSelectionActivity for the current selection in the given editor.
   * The local user will be used as the source of the activity and the given file will be used as
   * the file for the editor.
   *
   * <p><b>NOTE:</b> This should only be used to transfer pre-existing selection. To notify other
   * participants about new selections, {@link #generateSelection(IFile, Editor, int, int)} should
   * be used instead.
   *
   * <p><b>NOTE:</b> This class is meant for internal use only and should generally not be used
   * outside the editor package. If you still need to access this method, please consider whether
   * your class should rather be located in the editor package.
   *
   * @param file the file for the given editor
   * @param editor the editor to send the selection for
   */
  public void sendExistingSelection(@NotNull IFile file, @NotNull Editor editor) {
    User localUser = session.getLocalUser();

    sendSelectionInformation(localUser, file, editor);
  }

  private void sendEditorOpenInformation(@NotNull User user, @Nullable IFile file) {
    EditorActivity activateEditor = new EditorActivity(user, EditorActivity.Type.ACTIVATED, file);

    fireActivity(activateEditor);
  }

  /**
   * Sends the viewport information for the given editor if it is currently visible.
   *
   * @param user the local user
   * @param file the file for the editor
   * @param editor the editor to send the viewport for
   * @param visibleFilePaths the paths of all currently visible editors
   */
  private void sendViewPortInformation(
      @NotNull User user,
      @NotNull IFile file,
      @NotNull Editor editor,
      @NotNull Set<String> visibleFilePaths) {

    VirtualFile fileForEditor = DocumentAPI.getVirtualFile(editor.getDocument());

    if (fileForEditor == null) {
      log.warn(
          "Encountered editor without valid virtual file representation - file held in editor pool: "
              + file);

      return;
    }

    if (!visibleFilePaths.contains(fileForEditor.getPath())) {
      log.debug(
          "Ignoring "
              + file
              + " while sending viewport awareness information as the editor is not currently visible.");

      return;
    }

    LineRange localViewPort = EditorAPI.getLocalViewPortRange(editor);
    int viewPortStartLine = localViewPort.getStartLine();
    int viewPortLength = localViewPort.getNumberOfLines();

    ViewportActivity setViewPort =
        new ViewportActivity(user, viewPortStartLine, viewPortLength, file);

    fireActivity(setViewPort);
  }

  private void sendSelectionInformation(
      @NotNull User user, @NotNull IFile file, @NotNull Editor editor) {

    TextSelection selection = EditorAPI.getSelection(editor);

    TextSelectionActivity setSelection = new TextSelectionActivity(user, selection, file);

    fireActivity(setSelection);
  }

  /**
   * Sends an editor activation activity for the active editor. This should be done after sending
   * user editor state information about other editors to ensure that the correct editor is still
   * set as the active editor in the user editor state held by the other participants.
   *
   * @param localUser the local user
   * @param project the shared project
   * @see ProjectAPI#getActiveEditor(Project)
   */
  private void sendActiveEditorInformation(@NotNull User localUser, @NotNull Project project) {

    Editor activeEditor = ProjectAPI.getActiveEditor(project);

    IFile activeEditorFile;

    if (activeEditor != null) {
      activeEditorFile = editorPool.getFile(activeEditor.getDocument());

    } else {
      activeEditorFile = null;
    }

    sendEditorOpenInformation(localUser, activeEditorFile);
  }

  /**
   * Adds all currently open text editors belonging to the passed reference point to the pool of
   * open editors.
   *
   * @param referencePoint the added reference point
   */
  private void addReferencePointResources(IReferencePoint referencePoint) {
    IntellijReferencePoint intellijReferencePoint = (IntellijReferencePoint) referencePoint;
    Project project = intellijReferencePoint.getProject();

    Map<IFile, Editor> openFileMapping = new HashMap<>();

    SelectedEditorStateSnapshot selectedEditorStateSnapshot =
        selectedEditorStateSnapshotFactory.capturedState();

    try {
      setLocalEditorStatusChangeHandlersEnabled(false);
      setLocalViewPortChangeHandlersEnabled(false);

      for (VirtualFile openFile : ProjectAPI.getOpenFiles(project)) {
        IFile file = (IFile) VirtualFileConverter.convertToResource(openFile, referencePoint);

        if (file == null) {
          continue;

        } else if (file.isIgnored()) {
          log.debug("Skipping editor for ignored open file " + file);

          continue;
        }

        Editor editor = localEditorHandler.openEditor(openFile, referencePoint, false);

        if (editor != null) {
          openFileMapping.put(file, editor);
        }
      }

    } finally {
      setLocalViewPortChangeHandlersEnabled(true);
      setLocalEditorStatusChangeHandlersEnabled(true);
    }

    selectedEditorStateSnapshot.applyHeldState();

    User localUser = session.getLocalUser();

    Set<String> selectedFiles = new HashSet<>();

    for (VirtualFile selectedFile : ProjectAPI.getSelectedFiles(project)) {
      if (VirtualFileConverter.convertToResource(selectedFile, referencePoint) != null) {
        selectedFiles.add(selectedFile.getPath());
      }
    }

    openFileMapping.forEach(
        (file, editor) -> {
          sendEditorOpenInformation(localUser, file);

          sendViewPortInformation(localUser, file, editor, selectedFiles);

          sendSelectionInformation(localUser, file, editor);
        });

    sendActiveEditorInformation(localUser, project);
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

          localEditorHandler = sarosSession.getComponent(LocalEditorHandler.class);
          localEditorManipulator = sarosSession.getComponent(LocalEditorManipulator.class);

          annotationManager = sarosSession.getComponent(AnnotationManager.class);

          selectedEditorStateSnapshotFactory =
              sarosSession.getComponent(SelectedEditorStateSnapshotFactory.class);

          sharedIDEContext = sarosSession.getComponent(SharedIDEContext.class);
        }

        /** Drops all held components that were read from the session context. */
        private void dropHeldSessionContextComponents() {

          session = null;

          userEditorStateManager = null;

          localEditorHandler = null;
          localEditorManipulator = null;

          annotationManager = null;

          selectedEditorStateSnapshotFactory = null;

          sharedIDEContext = null;
        }

        /** Initializes all local components for the new session. */
        private void startSession() {
          assert editorPool.getEditors().isEmpty() : "EditorPool was not correctly reset!";

          if (!backgroundEditorPool.isEmpty()) {
            log.warn(
                "BackgroundEditorPool already contains entries at session start! Possible memory leak.");
          }

          session.getStopManager().addBlockable(stopManagerListener);

          hasWriteAccess = session.hasWriteAccess();
          session.addListener(sessionListener);

          session.addActivityProducer(EditorManager.this);
          session.addActivityConsumer(consumer, Priority.ACTIVE);

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
          backgroundEditorPool.clear();

          session.removeListener(sessionListener);
          session.removeActivityProducer(EditorManager.this);
          session.removeActivityConsumer(consumer);
        }
      };

  private final FileReplacementInProgressObservable fileReplacementInProgressObservable;

  /* Session Components */
  private UserEditorStateManager userEditorStateManager;
  private ISarosSession session;
  private LocalEditorHandler localEditorHandler;
  private LocalEditorManipulator localEditorManipulator;
  private AnnotationManager annotationManager;
  private SelectedEditorStateSnapshotFactory selectedEditorStateSnapshotFactory;
  private SharedIDEContext sharedIDEContext;

  /* Session state */
  private final BackgroundEditorPool backgroundEditorPool = new BackgroundEditorPool();
  private final EditorPool editorPool = new EditorPool(backgroundEditorPool);

  private final SharedEditorListenerDispatch editorListenerDispatch =
      new SharedEditorListenerDispatch();

  private boolean hasWriteAccess;
  // FIXME why is this never assigned? Either assign or remove flag
  private boolean isLocked;

  public EditorManager(
      ISarosSessionManager sessionManager,
      FileReplacementInProgressObservable fileReplacementInProgressObservable) {

    sessionManager.addSessionLifecycleListener(sessionLifecycleListener);
    this.fileReplacementInProgressObservable = fileReplacementInProgressObservable;
  }

  @Override
  public Set<IFile> getOpenEditors() {
    return editorPool.getFiles();
  }

  @Override
  public String getContent(final IFile file) {
    return FilesystemRunner.runReadAction(
        () -> {
          VirtualFile virtualFile = VirtualFileConverter.convertToVirtualFile(file);

          if (virtualFile == null || !virtualFile.exists() || virtualFile.isDirectory()) {

            log.warn(
                "Could not retrieve content of "
                    + file
                    + " as a matching VirtualFile could not be found,"
                    + " does not exist, or is a directory");

            return null;
          }

          Document doc = DocumentAPI.getDocument(virtualFile);

          return (doc != null) ? doc.getText() : null;
        });
  }

  @Override
  public String getNormalizedContent(IFile file) {
    // Intellij editor content is already normalized
    return getContent(file);
  }

  @Override
  public void addSharedEditorListener(ISharedEditorListener listener) {
    editorListenerDispatch.add(listener);
  }

  @Override
  public void removeSharedEditorListener(ISharedEditorListener listener) {
    editorListenerDispatch.remove(listener);
  }

  public void removeAllEditorsForFile(IFile file) {
    editorPool.removeEditor(file);
  }

  public void replaceAllEditorsForFile(IFile oldFile, IFile newFile) {
    editorPool.replaceFile(oldFile, newFile);
  }

  /**
   * Removes the given resource from the background editor pool if present.
   *
   * <p>This should be used to drop background editors for resources that are no longer available,
   * i.e. were moved or removed.
   *
   * @param file the file to remove from the background editor pool if present
   */
  public void removeBackgroundEditorForFile(@NotNull IFile file) {
    backgroundEditorPool.dropBackgroundEditor(file);
  }

  EditorPool getEditorPool() {
    return editorPool;
  }

  /**
   * Returns the file corresponding to the given document if the editor for the document is known to
   * the editor pool.
   *
   * @param document the document to get a file for
   * @return the file corresponding to the given document or <code>null</code> if the given document
   *     is <code>null</code> or is not known to the editor pool
   */
  @Nullable
  public IFile getFileForOpenEditor(@Nullable Document document) {
    return editorPool.getFile(document);
  }

  /**
   * Sets the local editor 'opened' and fires an {@link EditorActivity} of type {@link
   * Type#ACTIVATED}.
   *
   * @param file the file that the editor is currently editing or <code>null</code> if the local
   *     user has no editor open.
   */
  void generateEditorActivated(IFile file) {
    if (file == null || session.isShared(file)) {
      editorListenerDispatch.editorActivated(session.getLocalUser(), file);

      fireActivity(new EditorActivity(session.getLocalUser(), EditorActivity.Type.ACTIVATED, file));
    }
  }

  /**
   * Fires an EditorActivity.Type.CLOSED event for the given file and notifies the local
   * EditorListenerDispatcher.
   *
   * @param file the closed file
   */
  void generateEditorClosed(@NotNull IFile file) {
    if (session.isShared(file)) {
      editorListenerDispatch.editorClosed(session.getLocalUser(), file);

      fireActivity(new EditorActivity(session.getLocalUser(), EditorActivity.Type.CLOSED, file));
    }
  }

  /**
   * Generates an editor save activity for the given file.
   *
   * @param file the file to generate an editor saved activity for
   */
  void generateEditorSaved(IFile file) {
    fireActivity(new EditorActivity(session.getLocalUser(), Type.SAVED, file));
  }

  /**
   * Generates a {@link TextSelectionActivity} and fires it.
   *
   * <p><b>NOTE:</b> This class is meant for internal use only and should generally not be used
   * outside the editor package. If you still need to access this method, please consider whether
   * your class should rather be located in the editor package.
   */
  public void generateSelection(IFile file, Editor editor, int startOffset, int endOffset) {

    if (editor != null) {
      TextSelection selection =
          EditorAPI.calculateSelectionPosition(editor, startOffset, endOffset);

      fireActivity(new TextSelectionActivity(session.getLocalUser(), selection, file));
    }
  }

  /**
   * Generates a {@link ViewportActivity} and fires it.
   *
   * <p><b>NOTE:</b> This class is meant for internal use only and should generally not be used
   * outside the editor package. If you still need to access this method, please consider whether
   * your class should rather be located in the editor package.
   */
  public void generateViewport(IFile file, LineRange viewport) {
    if (session == null) {
      log.warn("SharedEditorListener not correctly unregistered!");
      return;
    }

    fireActivity(
        new ViewportActivity(
            session.getLocalUser(), viewport.getStartLine(), viewport.getNumberOfLines(), file));
  }

  /**
   * Generates a TextEditActivity and fires it.
   *
   * <p>Also generates a text selection activity for the new selection after the edit as document
   * modification induced changes are not covered by the caret listener.
   *
   * <p><b>NOTE:</b> This class is meant for internal use only and should generally not be used
   * outside the editor package. If you still need to access this method, please consider whether
   * your class should rather be located in the editor package.
   *
   * @see saros.intellij.eventhandler.editor.caret.LocalCaretPositionChangeHandler
   */
  public void generateTextEdit(
      int offset,
      @NotNull String newText,
      @NotNull String replacedText,
      @NotNull IFile file,
      @NotNull Document document) {

    if (session == null) {
      return;
    }

    Editor editor = editorPool.getEditor(file);

    Editor calculationEditor;
    if (editor != null) {
      calculationEditor = editor;
    } else {
      calculationEditor = backgroundEditorPool.getBackgroundEditor(file, document);
    }

    TextPosition startPosition = EditorAPI.calculatePosition(calculationEditor, offset);

    /*
     * Intellij internally always uses UNIX line separators for editor content
     * -> no line ending normalization necessary as content already normalized
     */
    TextEditActivity textEdit =
        TextEditActivity.buildTextEditActivity(
            session.getLocalUser(), startPosition, newText, replacedText, file);

    if (!hasWriteAccess || isLocked) {
      /*
       * TODO If we don't have {@link User.Permission#WRITE_ACCESS}, then
       * receiving this event might indicate that the user somehow
       * achieved to change his document. We should run a consistency
       * check.
       *
       * But watch out for changes because of a consistency check!
       */

      log.warn(
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
     * activities during the resource negotiation
     */
    if (fileReplacementInProgressObservable.isReplacementInProgress()) {
      if (log.isTraceEnabled()) {
        log.trace("File replacement in progress - Ignoring local activity " + textEdit);
      }

      return;
    }

    fireActivity(textEdit);

    editorListenerDispatch.textEdited(textEdit);

    // send selection after text edit
    if (editor != null) {
      Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
      int caretPosition = primaryCaret.getOffset();

      int selectionStart;
      int selectionEnd;

      if (primaryCaret.hasSelection()) {
        selectionStart = primaryCaret.getSelectionStart();
        selectionEnd = primaryCaret.getSelectionEnd();

      } else {
        selectionStart = caretPosition;
        selectionEnd = caretPosition;
      }

      log.debug(
          "Sending selection ("
              + selectionStart
              + ","
              + selectionEnd
              + ") after text edit "
              + textEdit);

      generateSelection(file, editor, selectionStart, selectionEnd);
    }
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
      log.info("Remote user " + jumpTo + " does not have an open editor.");
      return;
    }

    executeInUIThreadSynchronous(
        () -> {
          Editor newEditor = localEditorManipulator.openEditor(remoteActiveEditor.getFile(), true);

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
   * Enables or disables all editor state change handlers.
   *
   * @param enabled <code>true</code> to enable the handler, <code>false</code> disable the handlers
   * @see SharedIDEContext#setProjectEventHandlersEnabled(ProjectEventHandlerType, boolean)
   */
  void setLocalEditorStatusChangeHandlersEnabled(boolean enabled) {
    sharedIDEContext.setProjectEventHandlersEnabled(
        ProjectEventHandlerType.EDITOR_STATUS_CHANGE_HANDLER, enabled);
  }

  /**
   * Enables or disables all viewport change handlers.
   *
   * @param enabled <code>true</code> to enable the handler, <code>false</code> disable the handlers
   * @see SharedIDEContext#setProjectEventHandlersEnabled(ProjectEventHandlerType, boolean)
   */
  void setLocalViewPortChangeHandlersEnabled(boolean enabled) {
    sharedIDEContext.setProjectEventHandlersEnabled(
        ProjectEventHandlerType.VIEWPORT_CHANGE_HANDLER, enabled);
  }

  /**
   * Returns whether the document modification handlers are currently enabled.
   *
   * @return whether the document modification handlers are currently enabled
   * @see SharedIDEContext#areProjectEventHandlersEnabled(ProjectEventHandlerType)
   */
  boolean isDocumentModificationHandlerEnabled() {
    return sharedIDEContext.areProjectEventHandlersEnabled(
        ProjectEventHandlerType.DOCUMENT_MODIFICATION_HANDLER);
  }

  /**
   * Enables or disabled all document modification handlers.
   *
   * @param enabled <code>true</code> to enable the handlers, <code>false</code> disable the
   *     handlers
   * @see SharedIDEContext#setProjectEventHandlersEnabled(ProjectEventHandlerType, boolean)
   */
  void setLocalDocumentModificationHandlersEnabled(boolean enabled) {
    sharedIDEContext.setProjectEventHandlersEnabled(
        ProjectEventHandlerType.DOCUMENT_MODIFICATION_HANDLER, enabled);
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
    EDTExecutor.invokeAndWait(runnable, ModalityState.defaultModalityState());
  }

  @Override
  public void saveEditors(final IReferencePoint referencePoint) {
    DocumentAPI.saveAllDocuments();
  }

  @Override
  public void openEditor(final IFile file, final boolean activate) {
    executeInUIThreadSynchronous(() -> localEditorManipulator.openEditor(file, activate));
  }

  @Override
  public void closeEditor(final IFile file) {
    executeInUIThreadSynchronous(() -> localEditorManipulator.closeEditor(file));
  }

  /**
   * {@inheritDoc}
   *
   * <p>Only adjusts the viewport directly if the editor for the file is currently open. Otherwise,
   * the viewport adjustment will be queued until the editor is selected/opened the next time, at
   * which point the viewport will be adjusted. If multiple adjustment requests are done while the
   * editor is not currently visible, only the last one will be applied to the editor once it is
   * selected/opened.
   *
   * @param file {@inheritDoc}
   * @param range {@inheritDoc}
   * @param selection {@inheritDoc}
   * @see LocalEditorManipulator#adjustViewport(Editor, LineRange, TextSelection)
   */
  @Override
  public void adjustViewport(
      @NotNull final IFile file,
      @Nullable final LineRange range,
      @Nullable final TextSelection selection) {

    Set<String> visibleFilePaths = new HashSet<>();

    Project project = ((IntellijReferencePoint) file.getReferencePoint()).getProject();

    for (VirtualFile virtualFile : ProjectAPI.getSelectedFiles(project)) {
      visibleFilePaths.add(virtualFile.getPath());
    }

    VirtualFile passedFile = VirtualFileConverter.convertToVirtualFile(file);

    if (passedFile == null) {
      log.warn(
          "Ignoring request to adjust viewport as no valid VirtualFile could be found for "
              + file
              + " - given range: "
              + range
              + ", given selection: "
              + selection);

      return;
    }

    Editor editor = editorPool.getEditor(file);

    if (!visibleFilePaths.contains(passedFile.getPath())) {
      ViewportAdjustmentExecutor.queueViewPortChange(
          passedFile.getPath(), editor, range, selection);

      return;
    }

    if (editor == null) {
      log.warn(
          "Failed to adjust viewport for "
              + file
              + " as it is not known to the editor pool even though it is currently open");

      return;
    }

    executeInUIThreadSynchronous(
        () -> localEditorManipulator.adjustViewport(editor, range, selection));
  }

  /**
   * Adds the given editor to the editor pool with the given file.
   *
   * <p><b>NOTE:</b> This method should only be used when adding editors for files that are not yet
   * part of the session scope. This can be the case when an open file is moved into the session
   * scope. If the file is already part of the session scope, {@link #openEditor(IFile, boolean)}}
   * should be used instead as it ensures that the right editor for the file is used.
   *
   * @param file the file to add to the editor pool
   * @param editor the editor representing the given file
   * @see #openEditor(IFile, boolean)
   * @see #startEditor(Editor)
   */
  public void addEditorMapping(@NotNull IFile file, @NotNull Editor editor) {
    startEditor(editor);
    editorPool.add(file, editor);
  }
}
