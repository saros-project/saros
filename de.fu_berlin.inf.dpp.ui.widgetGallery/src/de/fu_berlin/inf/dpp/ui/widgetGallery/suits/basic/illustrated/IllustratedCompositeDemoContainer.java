package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.basic.illustrated;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;

public class IllustratedCompositeDemoContainer extends DemoContainer {

	public IllustratedCompositeDemoContainer(DemoContainer demoContainer,
			String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);

		new SimpleIllustratedCompositeDemo(this, "Simple");
		new IllustratedCompositeDemo(this, "Normal");
	}

}
