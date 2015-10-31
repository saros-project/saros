package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.wizard.pages;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.DemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;

@DemoSuite({ CreateXMPPAccountDemo.class, CreateXMPPAccountDemo2.class,
    EnterXMPPAccountDemo.class, GeneralSettingsDemo.class,
    ConfigurationDoneDemo.class, AddBuddyDemo.class,
    ProjectSelectionDemo.class, BuddySelectionDemo.class })
@Demo
public class PagesDemoSuite extends AbstractDemo {
    @Override
    public void createDemo(Composite parent) {
        // Nothing to do
    }

}
