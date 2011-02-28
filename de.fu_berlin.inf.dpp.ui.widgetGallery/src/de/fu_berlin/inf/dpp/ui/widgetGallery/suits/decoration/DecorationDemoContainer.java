package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.decoration;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;

public class DecorationDemoContainer extends DemoContainer {

	public DecorationDemoContainer(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);

		new EmptyTextDemo(this, "EmptyText");
		new JIDComboDemo(this, "JIDCombo");
	}

}
