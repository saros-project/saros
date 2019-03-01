package saros.core.project.internal;

import org.picocontainer.MutablePicoContainer;
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

    // Other
    if (!session.isHost()) {
      container.addComponent(ModuleInitialization.class);
    }
    container.addComponent(SharedResourcesManager.class);

    // User notifications
    container.addComponent(FollowModeNotificationDispatcher.class);
  }
}
