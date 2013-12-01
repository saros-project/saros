/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.ui.wizards;

import org.bitlet.weupnp.GatewayDevice;
import org.eclipse.jface.preference.IPreferenceStore;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.editor.colorstorage.UserColorID;
import de.fu_berlin.inf.dpp.feedback.ErrorLogManager;
import de.fu_berlin.inf.dpp.feedback.StatisticManagerConfiguration;
import de.fu_berlin.inf.dpp.net.upnp.IUPnPService;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.wizards.pages.ColorChooserWizardPage;
import de.fu_berlin.inf.dpp.ui.wizards.pages.ConfigurationSettingsWizardPage;
import de.fu_berlin.inf.dpp.ui.wizards.pages.ConfigurationSummaryWizardPage;

/**
 * A wizard to configure Saros (XMPP account, network settings, statistic
 * submission).
 * 
 * @author bkahlert
 */
public class ConfigurationWizard extends AddXMPPAccountWizard {

    @Inject
    private IUPnPService upnpService;

    private ConfigurationSettingsWizardPage configurationSettingsWizardPage = new ConfigurationSettingsWizardPage();
    private ConfigurationSummaryWizardPage configurationSummaryWizardPage = new ConfigurationSummaryWizardPage();
    private ColorChooserWizardPage colorChooserWizardPage = new ColorChooserWizardPage(
        false);

    public ConfigurationWizard() {
        SarosPluginContext.initComponent(this);

        setWindowTitle("Saros Configuration");
        setDefaultPageImageDescriptor(ImageManager.WIZBAN_CONFIGURATION);
        colorChooserWizardPage
            .setTitle(Messages.ChangeColorWizardPage_configuration_mode_title);

        colorChooserWizardPage
            .setDescription(Messages.ChangeColorWizardPage_configuration_mode_description);
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
     * Stores the Saros configuration on the base of the
     * {@link ConfigurationSettingsWizardPage} and
     * {@link ColorChooserWizardPage}into the PreferenceStore.
     */
    protected void setConfiguration() {
        IPreferenceStore preferences = saros.getPreferenceStore();

        String skypeUsername = (configurationSettingsWizardPage.isSkypeUsage()) ? configurationSettingsWizardPage
            .getSkypeUsername() : "";

        preferences.setValue(PreferenceConstants.AUTO_CONNECT,
            configurationSettingsWizardPage.isAutoConnect());

        preferences.setValue(PreferenceConstants.SKYPE_USERNAME, skypeUsername);

        int colorID = colorChooserWizardPage.getSelectedColor();

        if (UserColorID.isValid(colorID))
            preferences.setValue(PreferenceConstants.FAVORITE_SESSION_COLOR_ID,
                colorID);

        boolean statisticSubmissionAllowed = configurationSettingsWizardPage
            .isStatisticSubmissionAllowed();

        boolean errorLogSubmissionAllowed = configurationSettingsWizardPage
            .isErrorLogSubmissionAllowed();

        StatisticManagerConfiguration
            .setStatisticSubmissionAllowed(statisticSubmissionAllowed);

        ErrorLogManager.setErrorLogSubmissionAllowed(errorLogSubmissionAllowed);

        GatewayDevice gatewayDevice = configurationSettingsWizardPage
            .getPortmappingDevice();

        if (gatewayDevice != null)
            preferences.setValue(PreferenceConstants.AUTO_PORTMAPPING_DEVICEID,
                gatewayDevice.getUSN());
    }
}
