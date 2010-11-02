package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanatory.simple;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;

public class SimpleExplanatoryDemoContainer extends DemoContainer {

	public SimpleExplanatoryDemoContainer(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);

		new SimpleExplanatoryCompositeDemo(this, "Full");
		new ExplanationOnlySimpleExplanatoryCompositeDemo(this, "Explanation only");
		
	}

}
