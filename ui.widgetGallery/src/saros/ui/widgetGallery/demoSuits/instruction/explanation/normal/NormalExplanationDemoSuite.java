package saros.ui.widgetGallery.demoSuits.instruction.explanation.normal;

import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoExplorer.DemoSuite;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;

@DemoSuite({
  ExplanationCompositeDemo.class,
  ExplanationOnlyExplanationCompositeDemo.class,
  IconOnlyExplanationCompositeDemo.class,
  HugeExplanationCompositeDemo.class
})
@Demo
public class NormalExplanationDemoSuite extends AbstractDemo {

  @Override
  public void createDemo(Composite parent) {
    // Nothing to do
  }
}
