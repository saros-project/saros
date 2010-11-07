package de.fu_berlin.inf.dpp.ui.widgetGallery.suits;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.basic.BasicDemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.chat.ChatDemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.InstructionDemoContainer;

public class MainDemo extends DemoContainer {

	public MainDemo(Composite parent) {
		super(parent);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);

		new BasicDemoContainer(this, "RoundedComposite");
		new InstructionDemoContainer(this, "Explanation");
		open(new ChatDemoContainer(this, "Chat"));
	}
}
