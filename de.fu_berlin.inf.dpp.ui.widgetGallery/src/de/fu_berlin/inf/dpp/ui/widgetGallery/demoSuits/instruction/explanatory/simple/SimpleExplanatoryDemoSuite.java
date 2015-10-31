package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanatory.simple;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.DemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;

@DemoSuite({ SimpleExplanatoryCompositeDemo.class,
    ExplanationOnlySimpleExplanatoryCompositeDemo.class })
@Demo
public class SimpleExplanatoryDemoSuite extends AbstractDemo {
    @Override
    public void createDemo(Composite parent) {
        // Nothing to do
    }

}
