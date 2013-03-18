package de.fu_berlin.inf.dpp;

import org.apache.log4j.Logger;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.stf.server.STFController;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * An instance of this class is instantiated when Eclipse starts, after the
 * Saros plugin has been started.
 * 
 * {@link #earlyStartup()} is called after the workbench is initialized.
 * 
 * @author Lisa Dohrmann, Sandor Szücs, Stefan Rossbach
 */
@Component(module = "integration")
public class StartupSaros implements IStartup {

    private static final Logger log = Logger.getLogger(StartupSaros.class);

    @Inject
    private ISarosContext context;

    @Inject
    private SarosUI sarosUI;

    @Inject
    private XMPPAccountStore xmppAccountStore;

    public StartupSaros() {
        SarosPluginContext.initComponent(this);
    }

    /*
     * Once the workbench is started, the method earlyStartup() will be called
     * from a separate thread
     */

    @Override
    public void earlyStartup() {

        if (xmppAccountStore.isEmpty())
            showSarosView();

        Integer port = Integer.getInteger("de.fu_berlin.inf.dpp.testmode");

        if (port != null && port > 0 && port <= 65535) {
            log.info("starting STF controller on port " + port);
            startSTFController(port);

        } else if (port != null) {
            log.error("could not start STF controller: port " + port
                + " is not a valid port number");
        } else {
            /*
             * Only show configuration wizard if no accounts are configured. If
             * Saros is already configured, do not show the tutorial because the
             * user is probably already experienced.
             */

            handleStartup(xmppAccountStore.isEmpty());
        }
    }

    private void handleStartup(boolean showConfigurationWizard) {
        if (showConfigurationWizard) {
            SWTUtils.runSafeSWTAsync(log, new Runnable() {
                @Override
                public void run() {
                    SWTUtils.openInternalBrowser(Messages.Saros_tutorial_url,
                        Messages.Saros_tutorial_title);
                }
            });
        }
    }

    private void startSTFController(final int port) {

        Utils.runSafeAsync("STF-Controller-Starter", log, new Runnable() {
            @Override
            public void run() {
                try {
                    STFController.start(port, context);
                } catch (Exception e) {
                    log.error("starting STF controller failed", e);
                }
            }
        });
    }

    private void showSarosView() {
        SWTUtils.runSafeSWTAsync(log, new Runnable() {
            @Override
            public void run() {
                IIntroManager m = PlatformUI.getWorkbench().getIntroManager();
                IIntroPart i = m.getIntro();
                /*
                 * if there is a welcome screen, don't activate the SarosView
                 * because it would be maximized and hiding the workbench window
                 */
                if (i == null)
                    sarosUI.openSarosView();
            }
        });
    }
}
