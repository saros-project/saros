package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.project;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.DemoSuite;

@DemoSuite({ ProjectDisplayCompositeDemo.class,
	BaseProjectSelectionCompositeDemo.class,
	ProjectSelectionCompositeDemo.class })
@Demo
public class ProjectDemoSuite extends AbstractDemo {
    @Override
    public void createDemo(Composite parent) {

    }

}
