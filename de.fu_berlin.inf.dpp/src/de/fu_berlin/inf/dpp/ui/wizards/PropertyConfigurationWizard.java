package de.fu_berlin.inf.dpp.ui.wizards;

import org.eclipse.jface.wizard.Wizard;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.wizards.pages.GeneralSettingsPage;

public class PropertyConfigurationWizard extends Wizard {

    protected final Saros saros;
    protected final PreferenceUtils preferenceUtils;

    public PropertyConfigurationWizard(Saros saros,
        PreferenceUtils preferenceUtils) {
        setWindowTitle("Saros Configuration");
        setHelpAvailable(false);
        setNeedsProgressMonitor(true);

        this.saros = saros;
        this.preferenceUtils = preferenceUtils;
    }

    private GeneralSettingsPage firewallPage;

    @Override
    public void addPages() {
        this.firewallPage = new GeneralSettingsPage(saros, preferenceUtils);
        addPage(this.firewallPage);
    }

    @Override
    public boolean performFinish() {
        return this.firewallPage.performFinish();
    }

}
