package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanation.normal;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;

public class NormalExplanationDemoContainer extends DemoContainer {

	public NormalExplanationDemoContainer(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);

		new ExplanationCompositeDemo(this, "Normal");
		new ExplanationOnlyExplanationCompositeDemo(this, "Explication only");
		new IconOnlyExplanationCompositeDemo(this, "Icon only");
		
	}

}
