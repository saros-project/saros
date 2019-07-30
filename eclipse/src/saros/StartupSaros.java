package saros;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;
import saros.account.XMPPAccountStore;
import saros.annotations.Component;
import saros.communication.connection.ConnectionHandler;
import saros.context.IContainerContext;
import saros.feedback.FeedbackPreferences;
import saros.preferences.Preferences;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.ui.commandHandlers.GettingStartedHandler;
import saros.ui.util.SWTUtils;
import saros.ui.util.ViewUtils;
import saros.ui.util.XMPPConnectionSupport;

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

    Integer testmode = Integer.getInteger("saros.testmode");

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

        SWTUtils.runSafeSWTAsync(LOG, () -> XMPPConnectionSupport.getInstance().connect(true));
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
