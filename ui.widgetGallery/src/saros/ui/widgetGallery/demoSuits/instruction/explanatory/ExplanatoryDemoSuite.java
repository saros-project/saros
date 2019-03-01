package saros.ui.widgetGallery.demoSuits.instruction.explanatory;

import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoExplorer.DemoSuite;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgetGallery.demoSuits.instruction.explanatory.list.ListExplanatoryDemoSuite;
import saros.ui.widgetGallery.demoSuits.instruction.explanatory.normal.NormalExplanatoryDemoSuite;
import saros.ui.widgetGallery.demoSuits.instruction.explanatory.simple.SimpleExplanatoryDemoSuite;

@DemoSuite({
  SimpleExplanatoryDemoSuite.class,
  ListExplanatoryDemoSuite.class,
  NormalExplanatoryDemoSuite.class
})
@Demo
public class ExplanatoryDemoSuite extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    // Nothing to do
  }
}
