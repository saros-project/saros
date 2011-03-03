/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
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

import org.eclipse.jface.preference.IPreferenceStore;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.feedback.ErrorLogManager;
import de.fu_berlin.inf.dpp.feedback.StatisticManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.ImageManager;
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
    protected PreferenceUtils preferenceUtils;

    @Inject
    protected StatisticManager statisticManager;

    @Inject
    protected ErrorLogManager errorLogManager;

    ConfigurationSettingsWizardPage configurationSettingsWizardPage = new ConfigurationSettingsWizardPage();

    public ConfigurationWizard() {
        SarosPluginContext.initComponent(this);

        setWindowTitle("Saros Configuration");
        setDefaultPageImageDescriptor(ImageManager.WIZBAN_CONFIGURATION);
    }

    @Override
    public void addPages() {
        super.addPages();
        addPage(configurationSettingsWizardPage);
        addPage(new ConfigurationSummaryWizardPage());
    }

    @Override
    public boolean performFinish() {
        if (!super.performFinish())
            return false;
        setConfiguration();
        return true;
    }

    /**
     * Sets the Saros configuration on the base of the
     * {@link ConfigurationSettingsWizardPage}.
     */
    protected void setConfiguration() {
        IPreferenceStore preferences = saros.getPreferenceStore();

        /*
         * network
         */
        boolean autoConnect = this.configurationSettingsWizardPage
            .isAutoConnect();
        String skypeUsername = (this.configurationSettingsWizardPage
            .isSkypeUsage()) ? this.configurationSettingsWizardPage
            .getSkypeUsername() : "";
        preferences.setValue(PreferenceConstants.AUTO_CONNECT, autoConnect);
        preferences.setValue(PreferenceConstants.SKYPE_USERNAME, skypeUsername);

        /*
         * statistic
         */
        boolean statisticSubmissionAllowed = this.configurationSettingsWizardPage
            .isStatisticSubmissionAllowed();
        boolean errorLogSubmissionAllowed = this.configurationSettingsWizardPage
            .isErrorLogSubmissionAllowed();
        statisticManager
            .setStatisticSubmissionAllowed(statisticSubmissionAllowed);
        errorLogManager.setErrorLogSubmissionAllowed(errorLogSubmissionAllowed);
    }

}