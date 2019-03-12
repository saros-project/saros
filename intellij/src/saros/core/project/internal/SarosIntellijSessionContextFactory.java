package saros.core.project.internal;

import org.picocontainer.MutablePicoContainer;
import saros.intellij.editor.LocalEditorHandler;
import saros.intellij.editor.LocalEditorManipulator;
import saros.intellij.editor.annotations.AnnotationManager;
import saros.intellij.eventhandler.editor.document.LocalClosedEditorModificationHandler;
import saros.intellij.eventhandler.editor.document.LocalDocumentModificationHandler;
import saros.intellij.eventhandler.editor.editorstate.AnnotationUpdater;
import saros.intellij.eventhandler.editor.editorstate.EditorStatusChangeActivityDispatcher;
import saros.intellij.eventhandler.editor.editorstate.PreexistingSelectionDispatcher;
import saros.intellij.eventhandler.editor.editorstate.ViewportAdjustmentExecutor;
import saros.intellij.eventhandler.editor.selection.LocalTextSelectionChangeHandler;
import saros.intellij.eventhandler.editor.viewport.LocalViewPortChangeHandler;
import saros.intellij.eventhandler.filesystem.LocalFilesystemModificationHandler;
import saros.intellij.followmode.FollowModeNotificationDispatcher;
import saros.intellij.project.SharedResourcesManager;
import saros.intellij.project.filesystem.ModuleInitialization;
import saros.session.ISarosSession;
import saros.session.ISarosSessionContextFactory;
import saros.session.SarosCoreSessionContextFactory;

/** IntelliJ implementation of the {@link ISarosSessionContextFactory} interface. */
public class SarosIntellijSessionContextFactory extends SarosCoreSessionContextFactory {

  @Override
  public void createNonCoreComponents(ISarosSession session, MutablePicoContainer container) {
    // Editor interaction
    container.addComponent(LocalEditorHandler.class);
    container.addComponent(LocalEditorManipulator.class);

    // Annotation utility to create, remove, and manage annotations
    container.addComponent(AnnotationManager.class);

    // Other
    if (!session.isHost()) {
      container.addComponent(ModuleInitialization.class);
    }
    container.addComponent(SharedResourcesManager.class);

    /* Intellij resource event handlers */
    // Filesystem modification handlers
    container.addComponent(LocalFilesystemModificationHandler.class);
    // Document modification handlers
    container.addComponent(LocalDocumentModificationHandler.class);
    container.addComponent(LocalClosedEditorModificationHandler.class);
    // Editor status change handlers
    container.addComponent(AnnotationUpdater.class);
    container.addComponent(EditorStatusChangeActivityDispatcher.class);
    container.addComponent(PreexistingSelectionDispatcher.class);
    container.addComponent(ViewportAdjustmentExecutor.class);
    // Text selection change handlers
    container.addComponent(LocalTextSelectionChangeHandler.class);
    // Viewport change handlers
    container.addComponent(LocalViewPortChangeHandler.class);

    // User notifications
    container.addComponent(FollowModeNotificationDispatcher.class);
  }
}
