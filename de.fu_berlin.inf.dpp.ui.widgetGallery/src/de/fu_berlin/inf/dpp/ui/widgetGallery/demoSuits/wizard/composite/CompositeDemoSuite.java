package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.wizard.composite;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.DemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;

@DemoSuite({ EnterXMPPAccountCompositeDemo.class,
    SummaryItemCompositeDemo.class })
@Demo
public class CompositeDemoSuite extends AbstractDemo {
    @Override
    public void createDemo(Composite parent) {
        // Nothing to do
    }

}
