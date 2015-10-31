package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanation.normal;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.DemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;

@DemoSuite({ ExplanationCompositeDemo.class,
    ExplanationOnlyExplanationCompositeDemo.class,
    IconOnlyExplanationCompositeDemo.class, HugeExplanationCompositeDemo.class })
@Demo
public class NormalExplanationDemoSuite extends AbstractDemo {

    @Override
    public void createDemo(Composite parent) {
        // Nothing to do
    }

}
