package saros.ui.command_handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import saros.SarosPluginContext;
import saros.communication.connection.ConnectionHandler;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.ui.util.WizardUtils;

public class AddContactHandler {
  @Inject private ConnectionHandler connectionHandler;

  public AddContactHandler() {
    SarosPluginContext.initComponent(this);
  }

  @Execute
  public Object execute() {
    WizardUtils.openAddContactWizard();
    return null;
  }

  @CanExecute
  public boolean canExecute() {
    return connectionHandler.isConnected();
  }
}
