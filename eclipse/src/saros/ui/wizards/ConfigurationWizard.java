package saros.ui.wizards;

import saros.net.upnp.IGateway;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.Wizard;
import saros.SarosPluginContext;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.editor.colorstorage.UserColorID;
import saros.net.xmpp.JID;
import saros.preferences.PreferenceConstants;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.XMPPConnectionSupport;
import saros.ui.wizards.pages.ColorChooserWizardPage;
import saros.ui.wizards.pages.ConfigurationSettingsWizardPage;
import saros.ui.wizards.pages.ConfigurationSummaryWizardPage;
import saros.ui.wizards.pages.EnterXMPPAccountWizardPage;

/** A wizard to configure Saros (XMPP account, network settings, statistic submission). */
public class ConfigurationWizard extends Wizard {

  private final EnterXMPPAccountWizardPage enterXMPPAccountWizardPage =
      new EnterXMPPAccountWizardPage();

  private final ConfigurationSettingsWizardPage configurationSettingsWizardPage =
      new ConfigurationSettingsWizardPage();

  private final ConfigurationSummaryWizardPage configurationSummaryWizardPage =
      new ConfigurationSummaryWizardPage(
          enterXMPPAccountWizardPage, configurationSettingsWizardPage);

  private final ColorChooserWizardPage colorChooserWizardPage = new ColorChooserWizardPage(false);

  @Inject private IPreferenceStore preferences;

  @Inject private XMPPAccountStore accountStore;

  public ConfigurationWizard() {
    SarosPluginContext.initComponent(this);

    setWindowTitle("Saros Configuration");
    setHelpAvailable(false);
    setNeedsProgressMonitor(false);
    setDefaultPageImageDescriptor(
        ImageManager.getImageDescriptor(ImageManager.WIZBAN_CONFIGURATION));

    colorChooserWizardPage.setTitle(Messages.ChangeColorWizardPage_configuration_mode_title);

    colorChooserWizardPage.setDescription(
        Messages.ChangeColorWizardPage_configuration_mode_description);
  }

  @Override
  public void addPages() {
    addPage(enterXMPPAccountWizardPage);
    addPage(configurationSettingsWizardPage);
    addPage(colorChooserWizardPage);
    addPage(configurationSummaryWizardPage);
  }

  @Override
  public boolean performFinish() {
    setConfiguration();

    final XMPPAccount accountToConnect = addOrGetXMPPAccount();

    assert (accountToConnect != null);

    /*
     * it is possible to finish the wizard multiple times (also it makes no
     * sense) so ensure the behavior is always the same.
     */

    accountStore.setDefaultAccount(accountToConnect);

    if (preferences.getBoolean(PreferenceConstants.AUTO_CONNECT)) {
      getShell().getDisplay().asyncExec(() -> XMPPConnectionSupport.getInstance().connect(false));
    }

    return true;
  }

  @Override
  public boolean canFinish() {
    return getContainer().getCurrentPage() == configurationSummaryWizardPage;
  }

  @Override
  public boolean performCancel() {

    if (!enterXMPPAccountWizardPage.isExistingAccount()) return true;

    return MessageDialog.openQuestion(
        getShell(),
        Messages.ConfigurationWizard_account_created,
        Messages.ConfigurationWizard_account_created_text);
  }

  /**
   * Stores the Saros configuration on the base of the {@link ConfigurationSettingsWizardPage} and
   * {@link ColorChooserWizardPage}into the PreferenceStore.
   */
  protected void setConfiguration() {

    String skypeUsername =
        (configurationSettingsWizardPage.isSkypeUsage())
            ? configurationSettingsWizardPage.getSkypeUsername()
            : "";

    preferences.setValue(
        PreferenceConstants.AUTO_CONNECT, configurationSettingsWizardPage.isAutoConnect());

    preferences.setValue(PreferenceConstants.SKYPE_USERNAME, skypeUsername);

    int colorID = colorChooserWizardPage.getSelectedColor();

    if (UserColorID.isValid(colorID))
      preferences.setValue(PreferenceConstants.FAVORITE_SESSION_COLOR_ID, colorID);

    IGateway gatewayDevice = configurationSettingsWizardPage.getPortmappingDevice();

    if (gatewayDevice != null)
      preferences.setValue(PreferenceConstants.AUTO_PORTMAPPING_DEVICEID, gatewayDevice.getUSN());
  }

  /** Adds the {@link EnterXMPPAccountWizardPage}'s account data to the {@link XMPPAccountStore}. */
  private XMPPAccount addOrGetXMPPAccount() {

    boolean isExistingAccount = enterXMPPAccountWizardPage.isExistingAccount();
    JID jid = enterXMPPAccountWizardPage.getJID();

    String username = jid.getName();
    String password = enterXMPPAccountWizardPage.getPassword();
    String domain = jid.getDomain().toLowerCase();
    String server = enterXMPPAccountWizardPage.getServer();

    int port;

    if (enterXMPPAccountWizardPage.getPort().length() != 0)
      port = Integer.valueOf(enterXMPPAccountWizardPage.getPort());
    else port = 0;

    boolean useTLS = enterXMPPAccountWizardPage.isUsingTLS();
    boolean useSASL = enterXMPPAccountWizardPage.isUsingSASL();

    if (isExistingAccount) return accountStore.getAccount(username, domain, server, port);

    return accountStore.createAccount(username, password, domain, server, port, useTLS, useSASL);
  }
}
