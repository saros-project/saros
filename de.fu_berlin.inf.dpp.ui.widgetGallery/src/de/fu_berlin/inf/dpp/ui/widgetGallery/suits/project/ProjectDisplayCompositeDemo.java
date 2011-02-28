package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.project;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DescriptiveDemo;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.ProjectDisplayComposite;

public class ProjectDisplayCompositeDemo extends DescriptiveDemo {
	public ProjectDisplayCompositeDemo(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	protected ProjectDisplayComposite projectDisplayComposite;

	public String getDescription() {
		return null;
	}

	@Override
	public void createContent(Composite parent) {
		parent.setLayout(new FillLayout());

		projectDisplayComposite = new ProjectDisplayComposite(parent,
				SWT.BORDER);
	}
}
