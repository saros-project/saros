package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanatory;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanatory.list.ListExplanatoryDemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanatory.normal.NormalExplanatoryDemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.instruction.explanatory.simple.SimpleExplanatoryDemoContainer;

public class ExplanatoryDemoContainer extends DemoContainer {

	public ExplanatoryDemoContainer(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);

		new SimpleExplanatoryDemoContainer(this, "Simple");
		new ListExplanatoryDemoContainer(this, "List");
		new NormalExplanatoryDemoContainer(this, "Normal");
		
	}

}
