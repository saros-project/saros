package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.roster;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.DemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import org.eclipse.swt.widgets.Composite;

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
