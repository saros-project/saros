package saros.intellij.context;

import saros.filesystem.IWorkspace;
import saros.filesystem.checksum.IChecksumCache;
import saros.filesystem.checksum.NullChecksumCache;
import saros.intellij.editor.LocalEditorHandler;
import saros.intellij.editor.LocalEditorManipulator;
import saros.intellij.editor.SelectedEditorStateSnapshotFactory;
import saros.intellij.editor.annotations.AnnotationManager;
import saros.intellij.eventhandler.ApplicationEventHandlersFactory;
import saros.intellij.eventhandler.ProjectEventHandlersFactory;
import saros.intellij.eventhandler.project.ProjectClosedHandler;
import saros.intellij.filesystem.IntellijWorkspace;
import saros.intellij.filesystem.SharedResourcesManager;
import saros.intellij.followmode.FollowModeNotificationDispatcher;
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

    // Project interaction
    container.addComponent(ProjectClosedHandler.class);

    // Editor interaction
    container.addComponent(LocalEditorHandler.class);
    container.addComponent(LocalEditorManipulator.class);
    container.addComponent(SelectedEditorStateSnapshotFactory.class);

    // Annotation utility to create, remove, and manage annotations
    container.addComponent(AnnotationManager.class);

    // Checksum cache support
    container.addComponent(IChecksumCache.class, NullChecksumCache.class);
    container.addComponent(IWorkspace.class, IntellijWorkspace.class);

    // Other
    container.addComponent(SharedResourcesManager.class);

    // User notifications
    container.addComponent(FollowModeNotificationDispatcher.class);
  }
}
