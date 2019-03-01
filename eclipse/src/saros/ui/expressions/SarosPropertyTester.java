package saros.ui.expressions;

import org.eclipse.core.expressions.PropertyTester;
import org.picocontainer.annotations.Inject;
import saros.Saros;
import saros.SarosPluginContext;
import saros.communication.connection.ConnectionHandler;

/**
 * Adds tests to a running Saros plugin. Currently only supports several network connection and
 * configuration checks.
 */
public class SarosPropertyTester extends PropertyTester {

  private static final boolean MDNS_MODE = Boolean.getBoolean("saros.net.ENABLE_MDNS");

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

      if ("isXMPPEnabled".equals(property)) {
        return !MDNS_MODE;
      }
    }
    return false;
  }
}
