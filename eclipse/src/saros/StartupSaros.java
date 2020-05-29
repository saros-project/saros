package saros;

import org.apache.log4j.Logger;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;
import saros.account.XMPPAccountStore;
import saros.preferences.Preferences;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.ui.util.SWTUtils;
import saros.ui.util.ViewUtils;
import saros.ui.util.WizardUtils;
import saros.ui.util.XMPPConnectionSupport;

/**
 * An instance of this class is instantiated when Eclipse starts, after the Saros plugin has been
 * started.
 *
 * <p>{@link #earlyStartup()} is called after the workbench is initialized.
 */
public class StartupSaros implements IStartup {

  private static final Logger log = Logger.getLogger(StartupSaros.class);

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

    if (xmppAccountStore.isEmpty()) SWTUtils.runSafeSWTAsync(log, StartupSaros::openSarosView);

    Integer testmode = Integer.getInteger("saros.testmode");

    if (testmode != null) return;

    /*
     * Only show the Configuration Wizard if no accounts are configured. If
     * Saros is already configured, do not show the tutorial because the
     * user is probably already experienced.
     */

    /*
     *  TODO first display a dialog if the user wants to get some help. Afterwards open this wizard
     *  and maybe also open a web site with getting started?
     */

    if (xmppAccountStore.isEmpty()) {
      SWTUtils.runSafeSWTAsync(log, WizardUtils::openSarosConfigurationWizard);
      return;
    }

    /*
     * HACK workaround for http://sourceforge.net/p/dpp/bugs/782/
     * Perform connecting after the view is created so that the
     * necessary GUI elements for the chat have already installed
     * their listeners.
     *
     * FIXME This will not work if the view is not created on
     * startup !
     */

    if (preferences.isAutoConnecting() && xmppAccountStore.getDefaultAccount() != null)
      SWTUtils.runSafeSWTAsync(log, () -> XMPPConnectionSupport.getInstance().connect(true));
  }

  private static void openSarosView() {

    IIntroManager m = PlatformUI.getWorkbench().getIntroManager();
    IIntroPart i = m.getIntro();
    /*
     * if there is a welcome screen, do not open the SarosView
     * because it would be maximized and hiding the workbench window.
     */
    if (i == null) ViewUtils.openSarosView();
  }
}
