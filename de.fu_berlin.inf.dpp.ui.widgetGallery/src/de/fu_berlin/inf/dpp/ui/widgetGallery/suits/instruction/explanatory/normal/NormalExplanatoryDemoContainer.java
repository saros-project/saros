package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanatory.normal;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;

public class NormalExplanatoryDemoContainer extends DemoContainer {

	public NormalExplanatoryDemoContainer(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);

		new ExplanatoryCompositeDemo(this, "Full");
		new ExplanationOnlyExplanatoryCompositeDemo(this, "Explanation only");
		
	}

}
