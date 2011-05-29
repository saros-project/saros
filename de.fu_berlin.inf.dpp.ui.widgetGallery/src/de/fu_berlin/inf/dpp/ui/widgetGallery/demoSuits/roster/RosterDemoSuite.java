package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.roster;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.DemoSuite;

@DemoSuite({ BuddyDisplayCompositeDemo.class,
	BaseBuddySelectionCompositeDemo.class,
	BuddySelectionCompositeDemo.class })
@Demo
public class RosterDemoSuite extends AbstractDemo {
    @Override
    public void createDemo(Composite parent) {

    }

}
