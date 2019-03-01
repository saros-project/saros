package saros.ui.widgetGallery.demoSuits.wizard.pages;

import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoExplorer.DemoSuite;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;

@DemoSuite({
  CreateXMPPAccountDemo.class,
  CreateXMPPAccountDemo2.class,
  EnterXMPPAccountDemo.class,
  GeneralSettingsDemo.class,
  ConfigurationDoneDemo.class,
  AddBuddyDemo.class,
  ProjectSelectionDemo.class,
  BuddySelectionDemo.class
})
@Demo
public class PagesDemoSuite extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    // Nothing to do
  }
}
