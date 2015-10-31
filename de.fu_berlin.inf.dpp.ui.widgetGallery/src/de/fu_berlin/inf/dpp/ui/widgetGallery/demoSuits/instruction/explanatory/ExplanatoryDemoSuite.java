package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanatory;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.DemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanatory.list.ListExplanatoryDemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanatory.normal.NormalExplanatoryDemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanatory.simple.SimpleExplanatoryDemoSuite;

@DemoSuite({ SimpleExplanatoryDemoSuite.class, ListExplanatoryDemoSuite.class,
    NormalExplanatoryDemoSuite.class })
@Demo
public class ExplanatoryDemoSuite extends AbstractDemo {
    @Override
    public void createDemo(Composite parent) {
        // Nothing to do
    }

}
