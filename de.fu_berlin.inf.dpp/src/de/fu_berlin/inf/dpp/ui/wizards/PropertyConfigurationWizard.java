package de.fu_berlin.inf.dpp.ui.wizards;

import org.eclipse.jface.wizard.Wizard;

import de.fu_berlin.inf.dpp.Saros;

public class PropertyConfigurationWizard extends Wizard {

    protected final Saros saros;

    public PropertyConfigurationWizard(Saros saros) {
        setWindowTitle("Saros Configuration");
        setHelpAvailable(false);
        setNeedsProgressMonitor(true);
        this.saros = saros;
    }

    private GeneralSettingsPage firewallPage;

    @Override
    public void addPages() {
        this.firewallPage = new GeneralSettingsPage(saros);
        addPage(this.firewallPage);
    }

    @Override
    public boolean performFinish() {
        return this.firewallPage.performFinish();
    }

}
