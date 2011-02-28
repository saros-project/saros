package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.note;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;

public class NoteCompositeDemoContainer extends DemoContainer {

	public NoteCompositeDemoContainer(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);

		new SimpleNoteCompositeDemo(this, "Simple");
		new NoteCompositeDemo(this, "Normal");
	}
}
