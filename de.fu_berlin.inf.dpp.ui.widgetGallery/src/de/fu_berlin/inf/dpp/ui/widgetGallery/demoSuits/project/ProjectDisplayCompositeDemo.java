package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.project;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.project.ProjectDisplayComposite;

@Demo
public class ProjectDisplayCompositeDemo extends AbstractDemo {
    protected ProjectDisplayComposite projectDisplayComposite;

    @Override
    public void createDemo(Composite parent) {
	parent.setLayout(new FillLayout());

	projectDisplayComposite = new ProjectDisplayComposite(parent,
		SWT.BORDER);
    }
}
