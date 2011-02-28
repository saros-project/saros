package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.basic;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.basic.illustrated.IllustratedCompositeDemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.basic.rounded.RoundedCompositeDemoContainer;

public class BasicDemoContainer extends DemoContainer {

	public BasicDemoContainer(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);

		new RoundedCompositeDemoContainer(this, "Rounded");
		new IllustratedCompositeDemoContainer(this, "Illustrated");
	}
}
