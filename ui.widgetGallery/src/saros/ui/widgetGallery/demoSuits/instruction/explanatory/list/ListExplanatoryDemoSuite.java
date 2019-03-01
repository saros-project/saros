package saros.ui.widgetGallery.demoSuits.instruction.explanatory.list;

import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoExplorer.DemoSuite;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;

@DemoSuite({
  ListExplanatoryCompositeDemo.class,
  IntroductoryTextOnlyListExplanatoryCompositeDemo.class,
  ItemsOnlyListExplanatoryCompositeDemo.class
})
@Demo
public class ListExplanatoryDemoSuite extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    // Nothing to do
  }
}
