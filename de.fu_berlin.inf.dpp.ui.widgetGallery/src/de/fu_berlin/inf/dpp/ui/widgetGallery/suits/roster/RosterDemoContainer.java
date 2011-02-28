package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.roster;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;

public class RosterDemoContainer extends DemoContainer {

	public RosterDemoContainer(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);

		new BuddyDisplayCompositeDemo(this, "BuddyDisplay");
		new BaseBuddySelectionCompositeDemo(this, "BaseBuddySelection");
		new BuddySelectionCompositeDemo(this, "BuddySelection");
	}

}
