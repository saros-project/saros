package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanatory.list;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.DemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import org.eclipse.swt.widgets.Composite;

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
