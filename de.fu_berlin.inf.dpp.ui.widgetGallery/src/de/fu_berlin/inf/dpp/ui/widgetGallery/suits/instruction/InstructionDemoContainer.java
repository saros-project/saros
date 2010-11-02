package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanation.ExplanationDemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanatory.ExplanatoryDemoContainer;

public class InstructionDemoContainer extends DemoContainer {

	public InstructionDemoContainer(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);

		new ExplanationDemoContainer(this, "ExplanationComposite");
		
		new ExplanatoryDemoContainer(this, "ExplanatoryComposite");

	}

}
