package de.fu_berlin.inf.dpp.core.project.internal;

import de.fu_berlin.inf.dpp.intellij.project.SharedResourcesManager;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.ModuleInitialization;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionContextFactory;
import de.fu_berlin.inf.dpp.session.SarosCoreSessionContextFactory;
import org.picocontainer.MutablePicoContainer;

/** IntelliJ implementation of the {@link ISarosSessionContextFactory} interface. */
public class SarosIntellijSessionContextFactory extends SarosCoreSessionContextFactory {

  @Override
  public void createNonCoreComponents(ISarosSession session, MutablePicoContainer container) {

    // Other
    if (!session.isHost()) {
      container.addComponent(ModuleInitialization.class);
    }
    container.addComponent(SharedResourcesManager.class);
  }
}
