package saros.ui.widgetGallery.demoSuits.instruction.explanation.list;

import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoExplorer.DemoSuite;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;

@DemoSuite({
  ListExplanationCompositeDemo.class,
  IntroductoryTextOnlyListExplanationCompositeDemo.class,
  ItemsOnlyListExplanationCompositeDemo.class
})
@Demo
public class ListExplanationDemoSuite extends AbstractDemo {

  @Override
  public void createDemo(Composite parent) {
    // Nothing to do
  }
}
