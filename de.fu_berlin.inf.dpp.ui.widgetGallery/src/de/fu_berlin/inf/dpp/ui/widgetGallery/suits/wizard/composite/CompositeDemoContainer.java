package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.wizard.composite;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;

public class CompositeDemoContainer extends DemoContainer {

	public CompositeDemoContainer(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);

		new EnterXMPPAccountCompositeDemo(this, "EnterXMPPAccount");
		new SummaryItemCompositeDemo(this, "SummaryItem");
	}

}
