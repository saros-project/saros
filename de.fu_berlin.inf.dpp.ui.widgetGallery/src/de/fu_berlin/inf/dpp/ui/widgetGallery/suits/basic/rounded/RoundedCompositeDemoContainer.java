package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.basic.rounded;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;

public class RoundedCompositeDemoContainer extends DemoContainer {

	public RoundedCompositeDemoContainer(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);

		new SimpleRoundedCompositeDemo(this, "Simple");
		new RoundedCompositeDemo(this, "Normal");
	}

}
