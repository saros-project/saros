package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.wizard.pages;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;

public class PagesDemoContainer extends DemoContainer {

	public PagesDemoContainer(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);

		new CreateXMPPAccountDemo(this, "CreateXMPPAccount");
		new CreateXMPPAccountDemo2(this, "CreateXMPPAccount2");
		new EnterXMPPAccountDemo(this, "EnterXMPPAccount");
		new GeneralSettingsDemo(this, "GeneralSettings");
		new ConfigurationDoneDemo(this, "ConfigurationDone");
		new AddBuddyDemo(this, "AddBuddy");
		new ProjectSelectionDemo(this, "ProjectSelection");
		new BuddySelectionDemo(this, "BuddySelection");
	}

}
