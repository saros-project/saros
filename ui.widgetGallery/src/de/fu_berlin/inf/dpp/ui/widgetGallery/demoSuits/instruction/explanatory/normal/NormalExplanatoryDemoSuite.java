package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanatory.normal;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.DemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import org.eclipse.swt.widgets.Composite;

@DemoSuite({ExplanatoryCompositeDemo.class, ExplanationOnlyExplanatoryCompositeDemo.class})
@Demo
public class NormalExplanatoryDemoSuite extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    // Nothing to do
  }
}
