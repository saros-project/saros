package saros.ui.widgetGallery.demoSuits.instruction.explanatory.simple;

import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoExplorer.DemoSuite;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;

@DemoSuite({
  SimpleExplanatoryCompositeDemo.class,
  ExplanationOnlySimpleExplanatoryCompositeDemo.class
})
@Demo
public class SimpleExplanatoryDemoSuite extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    // Nothing to do
  }
}
