package saros.context;

import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.resource_change_handlers.FileActivityConsumer;
import saros.resource_change_handlers.FolderActivityConsumer;
import saros.resource_change_handlers.SharedResourcesManager;
import saros.session.ISarosSession;
import saros.session.ISarosSessionContextFactory;
import saros.session.SarosCoreSessionContextFactory;
import saros.session.resources.validation.ResourceChangeValidatorSupport;

/** Eclipse implementation of the {@link ISarosSessionContextFactory} interface. */
public class SarosEclipseSessionContextFactory extends SarosCoreSessionContextFactory {

  @Override
  public void createNonCoreComponents(ISarosSession session, MutablePicoContainer container) {

    // file activity related
    container.addComponent(SharedResourcesManager.class);
    container.addComponent(FileActivityConsumer.class);
    container.addComponent(FolderActivityConsumer.class);

    // permission validation on resource changes
    container.addComponent(ResourceChangeValidatorSupport.class);
  }
}
