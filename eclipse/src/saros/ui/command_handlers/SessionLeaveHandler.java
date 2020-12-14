package saros.ui.command_handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import saros.SarosPluginContext;
import saros.communication.connection.ConnectionHandler;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSessionManager;
import saros.ui.util.CollaborationUtils;

public class SessionLeaveHandler {
  @Inject private ConnectionHandler connectionHandler;
  @Inject private ISarosSessionManager sessionManager;

  public SessionLeaveHandler() {
    SarosPluginContext.initComponent(this);
  }

  @Execute
  public Object execute() {
    CollaborationUtils.leaveSession();
    return null;
  }

  @CanExecute
  public boolean canExecute() {
    return connectionHandler.isConnected() && sessionManager.getSession() != null;
  }
}
