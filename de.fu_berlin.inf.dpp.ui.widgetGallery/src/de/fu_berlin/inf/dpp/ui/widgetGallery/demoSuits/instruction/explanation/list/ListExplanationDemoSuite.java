package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.instruction.explanation.list;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.DemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import org.eclipse.swt.widgets.Composite;

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
