package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.rosterSession;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;

public class RosterSessionDemoContainer extends DemoContainer {

	public RosterSessionDemoContainer(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);

		new BuddySessionDisplayCompositeDemo(this, "BuddySessionDisplay");
	}

}
