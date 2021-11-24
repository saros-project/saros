package saros.ui.handlers.menu;

import org.eclipse.core.resources.IResource;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import saros.SarosPluginContext;
import saros.communication.connection.ConnectionHandler;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.ui.util.WizardUtils;
import saros.ui.wizards.AddResourcesToSessionWizard;

/**
 * Handles the addition of {@link IResource}s that must explicitly be selected in the opening {@link
 * AddResourcesToSessionWizard} to the running {@link ISarosSession}.
 *
 * <p>This class is used to define the behavior of the saros menu entry to add reference points to a
 * running session.
 */
public class SessionAddResourcesHandler {
  @Inject private ConnectionHandler connectionHandler;
  @Inject private ISarosSessionManager sessionManager;

  public SessionAddResourcesHandler() {
    SarosPluginContext.initComponent(this);
  }

  @Execute
  public Object execute() {
    WizardUtils.openAddResourcesToSessionWizard(null);
    return null;
  }

  @CanExecute
  public boolean canExecute() {
    final ISarosSession session = sessionManager.getSession();
    return connectionHandler.isConnected() && session != null && session.hasWriteAccess();
  }
}
