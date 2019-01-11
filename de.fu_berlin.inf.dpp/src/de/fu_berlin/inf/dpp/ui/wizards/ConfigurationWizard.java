package de.fu_berlin.inf.dpp.ui.wizards;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.editor.colorstorage.UserColorID;
import de.fu_berlin.inf.dpp.feedback.ErrorLogManager;
import de.fu_berlin.inf.dpp.feedback.StatisticManagerConfiguration;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.wizards.pages.ColorChooserWizardPage;
import de.fu_berlin.inf.dpp.ui.wizards.pages.ConfigurationSettingsWizardPage;
import de.fu_berlin.inf.dpp.ui.wizards.pages.ConfigurationSummaryWizardPage;
import org.bitlet.weupnp.GatewayDevice;
import org.eclipse.jface.preference.IPreferenceStore;
import org.picocontainer.annotations.Inject;

/**
 * A wizard to configure Saros (XMPP account, network settings, statistic submission).
 *
 * @author bkahlert
 */
public class ConfigurationWizard extends AddXMPPAccountWizard {

  private final ConfigurationSettingsWizardPage configurationSettingsWizardPage =
      new ConfigurationSettingsWizardPage();

  private final ConfigurationSummaryWizardPage configurationSummaryWizardPage =
      new ConfigurationSummaryWizardPage(
          enterXMPPAccountWizardPage, configurationSettingsWizardPage);

  private final ColorChooserWizardPage colorChooserWizardPage = new ColorChooserWizardPage(false);

  @Inject private IPreferenceStore preferences;

  public ConfigurationWizard() {
    SarosPluginContext.initComponent(this);

    setWindowTitle("Saros Configuration");
    setDefaultPageImageDescriptor(ImageManager.WIZBAN_CONFIGURATION);
    colorChooserWizardPage.setTitle(Messages.ChangeColorWizardPage_configuration_mode_title);

    colorChooserWizardPage.setDescription(
        Messages.ChangeColorWizardPage_configuration_mode_description);
  }

  @Override
  public void addPages() {
    super.addPages();
    addPage(configurationSettingsWizardPage);
    addPage(colorChooserWizardPage);
    addPage(configurationSummaryWizardPage);
  }

  @Override
  public boolean performFinish() {
    setConfiguration();
    return super.performFinish();
  }

  @Override
  public boolean canFinish() {
    return getContainer().getCurrentPage() == configurationSummaryWizardPage;
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

    boolean statisticSubmissionAllowed =
        configurationSettingsWizardPage.isStatisticSubmissionAllowed();

    boolean errorLogSubmissionAllowed =
        configurationSettingsWizardPage.isErrorLogSubmissionAllowed();

    StatisticManagerConfiguration.setStatisticSubmissionAllowed(statisticSubmissionAllowed);

    ErrorLogManager.setErrorLogSubmissionAllowed(errorLogSubmissionAllowed);

    GatewayDevice gatewayDevice = configurationSettingsWizardPage.getPortmappingDevice();

    if (gatewayDevice != null)
      preferences.setValue(PreferenceConstants.AUTO_PORTMAPPING_DEVICEID, gatewayDevice.getUSN());
  }
}
