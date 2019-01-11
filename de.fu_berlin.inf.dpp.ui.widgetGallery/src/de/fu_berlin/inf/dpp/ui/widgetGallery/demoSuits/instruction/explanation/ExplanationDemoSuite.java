package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanation;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.DemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanation.list.ListExplanationDemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanation.normal.NormalExplanationDemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanation.simple.SimpleExplanationDemoSuite;
import org.eclipse.swt.widgets.Composite;

@DemoSuite({
  SimpleExplanationDemoSuite.class,
  ListExplanationDemoSuite.class,
  NormalExplanationDemoSuite.class
})
@Demo
public class ExplanationDemoSuite extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    // Nothing to do
  }
}
