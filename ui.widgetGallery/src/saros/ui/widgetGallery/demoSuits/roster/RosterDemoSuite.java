package saros.ui.widgetGallery.demoSuits.roster;

import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoExplorer.DemoSuite;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;

@DemoSuite({
  BuddyDisplayCompositeDemo.class,
  BaseBuddySelectionCompositeDemo.class,
  BuddySelectionCompositeDemo.class
})
@Demo
public class RosterDemoSuite extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    // Nothing to do
  }
}
