package saros.intellij.eventhandler;

import com.intellij.openapi.project.Project;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import saros.intellij.editor.EditorManager;
import saros.intellij.editor.LocalEditorHandler;
import saros.intellij.editor.LocalEditorManipulator;
import saros.intellij.editor.annotations.AnnotationManager;
import saros.intellij.eventhandler.colorscheme.AnnotationReloader;
import saros.intellij.eventhandler.editor.caret.LocalCaretPositionChangeHandler;
import saros.intellij.eventhandler.editor.document.LocalDocumentModificationActivityDispatcher;
import saros.intellij.eventhandler.editor.document.LocalDocumentModificationAnnotationUpdater;
import saros.intellij.eventhandler.editor.editorstate.AnnotationUpdater;
import saros.intellij.eventhandler.editor.editorstate.EditorStatusChangeActivityDispatcher;
import saros.intellij.eventhandler.editor.editorstate.PreexistingSelectionDispatcher;
import saros.intellij.eventhandler.editor.editorstate.ViewportAdjustmentExecutor;
import saros.intellij.eventhandler.editor.selection.LocalTextSelectionChangeHandler;
import saros.intellij.eventhandler.editor.viewport.LocalViewPortChangeHandler;
import saros.session.ISarosSession;

/** Factory to instantiate {@link ProjectEventHandlers} objects. */
public class ProjectEventHandlersFactory {

  private final EditorManager editorManager;
  private final ISarosSession sarosSession;
  private final AnnotationManager annotationManager;
  private final LocalEditorHandler localEditorHandler;
  private final LocalEditorManipulator localEditorManipulator;

  public ProjectEventHandlersFactory(
      EditorManager editorManager,
      ISarosSession sarosSession,
      AnnotationManager annotationManager,
      LocalEditorHandler localEditorHandler,
      LocalEditorManipulator localEditorManipulator) {

    this.editorManager = editorManager;
    this.sarosSession = sarosSession;
    this.annotationManager = annotationManager;
    this.localEditorHandler = localEditorHandler;
    this.localEditorManipulator = localEditorManipulator;
  }

  /**
   * Returns a new <code>ProjectEventHandlers</code> instance with handlers for the given project.
   *
   * <p><b>NOTE:</b> Consider carefully whether you want to instantiate and start a new set of
   * project listeners as this should generally only be done once in in the beginning of the
   * session.
   *
   * @param project the project to instantiate handlers for
   * @return a new <code>ProjectEventHandlers</code> instance with handlers for the given project
   */
  @NotNull
  public ProjectEventHandlers createProjectEventHandlers(@NotNull Project project) {

    Set<IProjectEventHandler> projectEventHandlers = new HashSet<>();

    /*
     * caret position change handlers
     */
    projectEventHandlers.add(new LocalCaretPositionChangeHandler(project, editorManager));

    /*
     * color scheme change handlers
     */
    projectEventHandlers.add(new AnnotationReloader(project, annotationManager));

    /*
     * document modification handlers
     */
    projectEventHandlers.add(
        new LocalDocumentModificationAnnotationUpdater(
            project, editorManager, sarosSession, annotationManager));

    projectEventHandlers.add(
        new LocalDocumentModificationActivityDispatcher(project, editorManager, sarosSession));

    /*
     * editor state change handlers
     */
    projectEventHandlers.add(
        new AnnotationUpdater(project, annotationManager, localEditorHandler, sarosSession));

    projectEventHandlers.add(new EditorStatusChangeActivityDispatcher(project, localEditorHandler));

    projectEventHandlers.add(
        new PreexistingSelectionDispatcher(
            project, editorManager, localEditorHandler, sarosSession));

    projectEventHandlers.add(new ViewportAdjustmentExecutor(project, localEditorManipulator));

    /*
     * editor selection change handlers
     */
    projectEventHandlers.add(new LocalTextSelectionChangeHandler(project, editorManager));

    /*
     * editor viewport change handlers
     */
    projectEventHandlers.add(new LocalViewPortChangeHandler(project, editorManager));

    return new ProjectEventHandlers(projectEventHandlers);
  }
}
