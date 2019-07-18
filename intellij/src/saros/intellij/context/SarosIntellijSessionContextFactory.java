package saros.intellij.context;

import saros.intellij.editor.LocalEditorHandler;
import saros.intellij.editor.LocalEditorManipulator;
import saros.intellij.editor.SelectedEditorStateSnapshotFactory;
import saros.intellij.editor.annotations.AnnotationManager;
import saros.intellij.eventhandler.ApplicationEventHandlersFactory;
import saros.intellij.eventhandler.ProjectEventHandlersFactory;
import saros.intellij.followmode.FollowModeNotificationDispatcher;
import saros.intellij.project.SharedResourcesManager;
import saros.intellij.project.filesystem.ModuleInitialization;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.session.ISarosSession;
import saros.session.ISarosSessionContextFactory;
import saros.session.SarosCoreSessionContextFactory;

/** IntelliJ implementation of the {@link ISarosSessionContextFactory} interface. */
public class SarosIntellijSessionContextFactory extends SarosCoreSessionContextFactory {

  @Override
  public void createNonCoreComponents(ISarosSession session, MutablePicoContainer container) {
    // IDE context wrapper
    container.addComponent(SharedIDEContext.class);
    container.addComponent(ApplicationEventHandlersFactory.class);
    container.addComponent(ProjectEventHandlersFactory.class);

    // Editor interaction
    container.addComponent(LocalEditorHandler.class);
    container.addComponent(LocalEditorManipulator.class);
    container.addComponent(SelectedEditorStateSnapshotFactory.class);

    // Annotation utility to create, remove, and manage annotations
    container.addComponent(AnnotationManager.class);

    // Other
    if (!session.isHost()) {
      container.addComponent(ModuleInitialization.class);
    }
    container.addComponent(SharedResourcesManager.class);

    // User notifications
    container.addComponent(FollowModeNotificationDispatcher.class);
  }
}
