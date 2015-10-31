package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanation.simple;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.DemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;

@DemoSuite({ SimpleExplanationCompositeDemo.class,
    ExplanationOnlySimpleExplanationCompositeDemo.class,
    IconOnlySimpleExplanationCompositeDemo.class,
    HugeSimpleExplanationCompositeDemo.class })
@Demo
public class SimpleExplanationDemoSuite extends AbstractDemo {

    @Override
    public void createDemo(Composite parent) {
        // Nothing to do
    }

}
