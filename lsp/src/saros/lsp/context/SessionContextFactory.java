package saros.lsp.context;

import saros.lsp.activity.FileActivityHandler;
import saros.lsp.activity.InconsistencyHandler;
import saros.repackaged.picocontainer.MutablePicoContainer;
import saros.session.ISarosSession;
import saros.session.SarosCoreSessionContextFactory;

/** ContextFactory for components that are being used throughout an active session. */
public class SessionContextFactory extends SarosCoreSessionContextFactory {

  @Override
  public void createNonCoreComponents(ISarosSession session, MutablePicoContainer container) {
    container.addComponent(InconsistencyHandler.class);
    container.addComponent(FileActivityHandler.class);
  }
}
