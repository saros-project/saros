package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.wizard.pages;

import org.eclipse.jface.wizard.IWizardPage;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.WizardPageDemo;
import de.fu_berlin.inf.dpp.ui.wizards.pages.CreateXMPPAccountWizardPage;

public class CreateXMPPAccountDemo extends WizardPageDemo {
	public CreateXMPPAccountDemo(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public IWizardPage getWizardPage() {
		return new CreateXMPPAccountWizardPage(true);
	}
}
