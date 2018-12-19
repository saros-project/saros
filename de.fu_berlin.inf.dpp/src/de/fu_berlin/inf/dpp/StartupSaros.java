package de.fu_berlin.inf.dpp;

import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import de.fu_berlin.inf.dpp.context.IContainerContext;
import de.fu_berlin.inf.dpp.feedback.FeedbackPreferences;
import de.fu_berlin.inf.dpp.preferences.Preferences;
import de.fu_berlin.inf.dpp.ui.commandHandlers.GettingStartedHandler;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.util.ViewUtils;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;
import org.picocontainer.annotations.Inject;

/**
 * An instance of this class is instantiated when Eclipse starts, after the Saros plugin has been
 * started.
 *
 * <p>{@link #earlyStartup()} is called after the workbench is initialized.
 *
 * @author Lisa Dohrmann, Sandor Szücs, Stefan Rossbach
 */
@Component(module = "integration")
public class StartupSaros implements IStartup {

  private static final Logger LOG = Logger.getLogger(StartupSaros.class);

  @Inject private IContainerContext context;

  @Inject private IPreferenceStore preferenceStore;

  @Inject private ConnectionHandler connectionHandler;

  @Inject private Preferences preferences;

  @Inject private XMPPAccountStore xmppAccountStore;

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

    if (xmppAccountStore.isEmpty()) showSarosView();

    Integer testmode = Integer.getInteger("de.fu_berlin.inf.dpp.testmode");

    if (testmode == null) {
      /*
       * Only show configuration wizard if no accounts are configured. If
       * Saros is already configured, do not show the tutorial because the
       * user is probably already experienced.
       */

      if (xmppAccountStore.isEmpty()) showTutorial();
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

        if (!preferences.isAutoConnecting() || xmppAccountStore.isEmpty()) return;

        final XMPPAccount account = xmppAccountStore.getActiveAccount();

        ThreadUtils.runSafeAsync(
            "dpp-connect-auto",
            LOG,
            new Runnable() {
              @Override
              public void run() {
                // avoid error popups
                connectionHandler.connect(account, true);
              }
            });
      }
    }
  }

  private void showTutorial() {
    SWTUtils.runSafeSWTAsync(
        LOG,
        new Runnable() {
          @Override
          public void run() {
            try {
              new GettingStartedHandler().execute(new ExecutionEvent());
            } catch (ExecutionException e) {
              LOG.warn("failed to execute tutorial handler", e);
            }
          }
        });
  }

  private void showSarosView() {
    SWTUtils.runSafeSWTAsync(
        LOG,
        new Runnable() {
          @Override
          public void run() {
            IIntroManager m = PlatformUI.getWorkbench().getIntroManager();
            IIntroPart i = m.getIntro();
            /*
             * if there is a welcome screen, don't activate the SarosView
             * because it would be maximized and hiding the workbench window
             */
            if (i == null) ViewUtils.openSarosView();
          }
        });
  }
}
