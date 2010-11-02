package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanatory.list;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;

public class ListExplanatoryDemoContainer extends DemoContainer {

	public ListExplanatoryDemoContainer(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);

		new ListExplanatoryCompositeDemo(this, "Full");
		new IntroductoryTextOnlyListExplanatoryCompositeDemo(this, "Introductory text only");
		new ItemsOnlyListExplanatoryCompositeDemo(this, "Items only");
		
	}

}
