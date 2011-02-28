package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.project;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;

public class ProjectDemoContainer extends DemoContainer {

	public ProjectDemoContainer(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	@Override
	public void createPartControls(Composite parent) {
		super.createPartControls(parent);

		new ProjectDisplayCompositeDemo(this, "ProjectDisplay");
		new BaseProjectSelectionCompositeDemo(this, "BaseProjectSelection");
		new ProjectSelectionCompositeDemo(this, "ProjectSelection");
	}

}
