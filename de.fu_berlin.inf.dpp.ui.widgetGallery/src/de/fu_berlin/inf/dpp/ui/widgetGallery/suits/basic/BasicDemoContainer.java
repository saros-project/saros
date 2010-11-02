package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.basic;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;

public class BasicDemoContainer extends DemoContainer {

	public BasicDemoContainer(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);
		
		DemoContainer roundedCompositeDemoContainer = new DemoContainer(this, "RoundedComposite");

		new SimpleRoundedCompositeDemo(roundedCompositeDemoContainer, "Simple");
		new RoundedCompositeDemo(roundedCompositeDemoContainer, "Normal");
	}

}
