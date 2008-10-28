package de.fu_berlin.inf.dpp.ui.wizards;

import org.eclipse.jface.wizard.Wizard;

public class PropertyConfigurationWizard extends Wizard {

    public PropertyConfigurationWizard() {
	setWindowTitle("Saros Configuration");
	setHelpAvailable(false);
	setNeedsProgressMonitor(true);
    }

    private NetworkSettingsPage firewallPage;

    @Override
    public void addPages() {
	this.firewallPage = new NetworkSettingsPage();
	addPage(this.firewallPage);
    }

    @Override
    public boolean performFinish() {
	return this.firewallPage.performFinish();
    }

}
