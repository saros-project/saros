package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanation.simple;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;

public class SimpleExplanationDemoContainer extends DemoContainer {

	public SimpleExplanationDemoContainer(DemoContainer demoContainer,
			String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);

		new SimpleExplanationCompositeDemo(this, "Normal");
		new ExplanationOnlySimpleExplanationCompositeDemo(this,
				"Explication only");
		new IconOnlySimpleExplanationCompositeDemo(this, "Icon only");
		new HugeSimpleExplanationCompositeDemo(this, "Much content");

	}

}
