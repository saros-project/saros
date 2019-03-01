package saros.ui.widgetGallery.demoSuits.instruction.explanation.simple;

import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoExplorer.DemoSuite;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;

@DemoSuite({
  SimpleExplanationCompositeDemo.class,
  ExplanationOnlySimpleExplanationCompositeDemo.class,
  IconOnlySimpleExplanationCompositeDemo.class,
  HugeSimpleExplanationCompositeDemo.class
})
@Demo
public class SimpleExplanationDemoSuite extends AbstractDemo {

  @Override
  public void createDemo(Composite parent) {
    // Nothing to do
  }
}
