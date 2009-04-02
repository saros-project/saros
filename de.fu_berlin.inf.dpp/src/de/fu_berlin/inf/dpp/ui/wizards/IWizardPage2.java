package de.fu_berlin.inf.dpp.ui.wizards;

import org.eclipse.jface.wizard.IWizardPage;

/**
 * This interface is used to expose the performFinished method of a WizardPage
 */
public interface IWizardPage2 extends IWizardPage {

    public boolean performFinish();

}
