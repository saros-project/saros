package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanation;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanation.list.ListExplanationDemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanation.normal.NormalExplanationDemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanation.simple.SimpleExplanationDemoContainer;

public class ExplanationDemoContainer extends DemoContainer {

	public ExplanationDemoContainer(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);

		new SimpleExplanationDemoContainer(this, "Simple");
		new ListExplanationDemoContainer(this, "List");
		new NormalExplanationDemoContainer(this, "Normal");
		
	}

}
