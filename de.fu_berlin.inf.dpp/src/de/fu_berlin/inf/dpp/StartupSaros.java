package de.fu_berlin.inf.dpp;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import de.fu_berlin.inf.dpp.feedback.FeedbackPreferences;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.stf.server.STFController;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.ViewUtils;
import de.fu_berlin.inf.dpp.util.ThreadUtils;

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
    private IPreferenceStore preferenceStore;

    @Inject
    private ConnectionHandler connectionHandler;

    @Inject
    private PreferenceUtils preferenceUtils;

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

        /*
         * HACK as the preferences are initialized after the context is created
         * and the default preferences does not affect the global preferences
         * needed by the Feedback component we have to initialize them here
         */

        FeedbackPreferences.applyDefaults(preferenceStore);

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

            if (xmppAccountStore.isEmpty())
                showTutorialWebpage();
            else {
                /*
                 * HACK workaround for http://sourceforge.net/p/dpp/bugs/782/
                 * Perform connecting after the view is created so that the
                 * necessary GUI elements for the chat have already installed
                 * their listeners.
                 * 
                 * FIXME This will not work if the view is not created on
                 * startup !
                 */

                if (!preferenceUtils.isAutoConnecting()
                    || xmppAccountStore.isEmpty())
                    return;

                ThreadUtils.runSafeAsync("AutoConnect", log, new Runnable() {
                    @Override
                    public void run() {
                        connectionHandler.connect(/* avoid error popups */true);
                    }
                });

            }
        }
    }

    private void showTutorialWebpage() {
        SWTUtils.runSafeSWTAsync(log, new Runnable() {
            @Override
            public void run() {
                SWTUtils.openInternalBrowser(Messages.Saros_tutorial_url,
                    Messages.Saros_tutorial_title);
            }
        });
    }

    private void startSTFController(final int port) {

        ThreadUtils.runSafeAsync("STF-Controller-Starter", log, new Runnable() {
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
                    ViewUtils.openSarosView();
            }
        });
    }
}
