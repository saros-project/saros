package saros.stf;

import org.apache.log4j.Logger;
import org.eclipse.ui.IStartup;
import org.picocontainer.annotations.Inject;
import saros.SarosPluginContext;
import saros.annotations.Component;
import saros.context.IContainerContext;
import saros.stf.server.STFController;
import saros.util.ThreadUtils;

/**
 * An instance of this class is instantiated when Eclipse starts, after the Saros plugin has been
 * started.
 *
 * <p>{@link #earlyStartup()} is called after the workbench is initialized.
 */
@Component(module = "integration")
public class StartupSarosSTF implements IStartup {

  private static final Logger LOG = Logger.getLogger(StartupSarosSTF.class);

  @Inject private IContainerContext context;

  public StartupSarosSTF() {
    SarosPluginContext.initComponent(this);
  }

  /*
   * Once the workbench is started, the method earlyStartup() will be called
   * from a separate thread
   */

  @Override
  public void earlyStartup() {

    Integer port = Integer.getInteger("saros.testmode");

    if (port != null && port > 0 && port <= 65535) {
      LOG.info("starting STF controller on port " + port);
      startSTFController(port);

    } else {
      LOG.error("could not start STF controller: port " + port + " is not a valid port number");
    }
  }

  private void startSTFController(final int port) {

    ThreadUtils.runSafeAsync(
        "dpp-stf-startup",
        LOG,
        new Runnable() {
          @Override
          public void run() {
            try {
              STFController.start(port, context);
            } catch (Exception e) {
              LOG.error("starting STF controller failed", e);
            }
          }
        });
  }
}
