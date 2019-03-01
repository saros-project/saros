package saros.ui.widgetGallery.demoSuits.instruction.explanation;

import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoExplorer.DemoSuite;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgetGallery.demoSuits.instruction.explanation.list.ListExplanationDemoSuite;
import saros.ui.widgetGallery.demoSuits.instruction.explanation.normal.NormalExplanationDemoSuite;
import saros.ui.widgetGallery.demoSuits.instruction.explanation.simple.SimpleExplanationDemoSuite;

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
