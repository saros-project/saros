package saros.intellij.eventhandler;

import com.intellij.openapi.project.Project;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import saros.intellij.editor.EditorManager;
import saros.intellij.editor.LocalEditorHandler;
import saros.intellij.editor.LocalEditorManipulator;
import saros.intellij.editor.ProjectAPI;
import saros.intellij.editor.annotations.AnnotationManager;
import saros.intellij.eventhandler.editor.document.LocalClosedEditorModificationHandler;
import saros.intellij.eventhandler.editor.document.LocalDocumentModificationHandler;
import saros.intellij.eventhandler.editor.editorstate.AnnotationUpdater;
import saros.intellij.eventhandler.editor.editorstate.EditorStatusChangeActivityDispatcher;
import saros.intellij.eventhandler.editor.editorstate.PreexistingSelectionDispatcher;
import saros.intellij.eventhandler.editor.editorstate.ViewportAdjustmentExecutor;
import saros.intellij.eventhandler.editor.selection.LocalTextSelectionChangeHandler;
import saros.intellij.eventhandler.editor.viewport.LocalViewPortChangeHandler;
import saros.intellij.filesystem.VirtualFileConverter;
import saros.session.ISarosSession;

/** Factory to instantiate {@link ProjectEventHandlers} objects. */
public class ProjectEventHandlersFactory {

  private final EditorManager editorManager;
  private final VirtualFileConverter virtualFileConverter;
  private final ISarosSession sarosSession;
  private final ProjectAPI projectAPI;
  private final AnnotationManager annotationManager;
  private final LocalEditorHandler localEditorHandler;
  private final LocalEditorManipulator localEditorManipulator;

  public ProjectEventHandlersFactory(
      EditorManager editorManager,
      VirtualFileConverter virtualFileConverter,
      ISarosSession sarosSession,
      ProjectAPI projectAPI,
      AnnotationManager annotationManager,
      LocalEditorHandler localEditorHandler,
      LocalEditorManipulator localEditorManipulator) {

    this.editorManager = editorManager;
    this.virtualFileConverter = virtualFileConverter;
    this.sarosSession = sarosSession;
    this.projectAPI = projectAPI;
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
     * document modification handlers
     */
    projectEventHandlers.add(
        new LocalClosedEditorModificationHandler(
            editorManager, virtualFileConverter, sarosSession, projectAPI, annotationManager));

    projectEventHandlers.add(
        new LocalDocumentModificationHandler(editorManager, sarosSession, virtualFileConverter));

    /*
     * editor state change handlers
     */
    projectEventHandlers.add(
        new AnnotationUpdater(
            project, annotationManager, localEditorHandler, sarosSession, virtualFileConverter));

    projectEventHandlers.add(new EditorStatusChangeActivityDispatcher(project, localEditorHandler));

    projectEventHandlers.add(
        new PreexistingSelectionDispatcher(
            project, editorManager, localEditorHandler, sarosSession, virtualFileConverter));

    projectEventHandlers.add(
        new ViewportAdjustmentExecutor(project, projectAPI, localEditorManipulator));

    /*
     * editor selection change handlers
     */
    projectEventHandlers.add(new LocalTextSelectionChangeHandler(editorManager));

    /*
     * editor viewport change handlers
     */
    projectEventHandlers.add(new LocalViewPortChangeHandler(editorManager));

    return new ProjectEventHandlers(projectEventHandlers);
  }
}
