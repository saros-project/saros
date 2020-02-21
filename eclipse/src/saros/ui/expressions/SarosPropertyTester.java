package saros.ui.expressions;

import org.eclipse.core.expressions.PropertyTester;
import saros.Saros;
import saros.SarosPluginContext;
import saros.communication.connection.ConnectionHandler;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.ui.wizards.CreateXMPPAccountWizard;

/**
 * Adds tests to a running Saros plugin. Currently only supports several network connection and
 * configuration checks.
 */
public class SarosPropertyTester extends PropertyTester {

  @Inject private ConnectionHandler connectionHandler;

  public SarosPropertyTester() {
    SarosPluginContext.initComponent(this);
  }

  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    if (receiver instanceof Saros) {

      if ("isConnected".equals(property)) {
        return connectionHandler.isConnected();
      }
      if ("isCreateXMPPAccountEnabled".equals(property)) {
        return CreateXMPPAccountWizard.CREATE_DIALOG_ENABLED;
      }
    }
    return false;
  }
}
