package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanation.list;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;

public class ListExplanationDemoContainer extends DemoContainer {

	public ListExplanationDemoContainer(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);


		new ListExplanationCompositeDemo(this, "Normal");
		new IntroductoryTextOnlyListExplanationCompositeDemo(this, "Introductory text only");
		new ItemsOnlyListExplanationCompositeDemo(this, "List items only");
		
	}

}
