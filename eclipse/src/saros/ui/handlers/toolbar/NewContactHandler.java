package saros.ui.handlers.toolbar;

import javax.annotation.PreDestroy;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import saros.SarosPluginContext;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectionStateListener;
import saros.net.ConnectionState;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.ui.util.WizardUtils;

public class NewContactHandler {

  public static final String ID = NewContactHandler.class.getName();

  private final IConnectionStateListener connectionListener =
      (state, error) -> connectionState = state == ConnectionState.CONNECTED;

  private boolean connectionState = false;

  @Inject private ConnectionHandler connectionHandler;

  public NewContactHandler() {
    SarosPluginContext.initComponent(this);

    connectionHandler.addConnectionStateListener(connectionListener);
    connectionState = connectionHandler.isConnected();
  }

  @PreDestroy
  public void dispose() {
    connectionHandler.removeConnectionStateListener(connectionListener);
  }

  @CanExecute
  public boolean canExecute() {
    return connectionState;
  }

  @Execute
  public void run() {
    WizardUtils.openAddContactWizard();
  }
}
