package saros.ui.command_handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import saros.SarosPluginContext;
import saros.communication.connection.ConnectionHandler;
import saros.net.xmpp.JID;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.ui.util.WizardUtils;
import saros.ui.wizards.AddResourcesToSessionWizard;

/**
 * Handles the addition of {@link JID}s that must explicitly be selected in the opening {@link
 * AddResourcesToSessionWizard} to the running {@link ISarosSession}.
 */
public class SessionAddContactsHandler {
  @Inject private ConnectionHandler connectionHandler;
  @Inject private ISarosSessionManager sessionManager;

  public SessionAddContactsHandler() {
    SarosPluginContext.initComponent(this);
  }

  @Execute
  public Object execute() {
    WizardUtils.openAddContactsToSessionWizard();
    return null;
  }

  @CanExecute
  public boolean canExecute() {
    return connectionHandler != null
        && connectionHandler.isConnected()
        && sessionManager != null
        && sessionManager.getSession() != null;
  }
}
