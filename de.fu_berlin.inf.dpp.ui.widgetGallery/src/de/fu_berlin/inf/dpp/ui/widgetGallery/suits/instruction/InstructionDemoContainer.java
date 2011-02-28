package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanation.ExplanationDemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanatory.ExplanatoryDemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.note.NoteCompositeDemoContainer;

public class InstructionDemoContainer extends DemoContainer {

	public InstructionDemoContainer(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);

		new ExplanationDemoContainer(this, "ExplanationComposite");
		new NoteCompositeDemoContainer(this, "NoteComposite");
		new ExplanatoryDemoContainer(this, "ExplanatoryComposite");
	}
}
