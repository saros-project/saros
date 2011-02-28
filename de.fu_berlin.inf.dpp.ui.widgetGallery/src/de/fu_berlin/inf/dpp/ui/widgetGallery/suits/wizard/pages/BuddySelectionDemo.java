package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.wizard.pages;

import org.eclipse.jface.wizard.IWizardPage;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.WizardPageDemo;
import de.fu_berlin.inf.dpp.ui.wizards.pages.BuddySelectionWizardPage;

public class BuddySelectionDemo extends WizardPageDemo {
	public BuddySelectionDemo(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public String getDescription() {
		return "This demo show a "
				+ BuddySelectionWizardPage.class.getSimpleName()
				+ " that reflects the buddies that were selected on creation.";
	}

	@Override
	public IWizardPage getWizardPage() {
		return new BuddySelectionWizardPage();
	}
}
