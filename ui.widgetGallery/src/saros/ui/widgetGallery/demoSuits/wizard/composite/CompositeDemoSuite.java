package saros.ui.widgetGallery.demoSuits.wizard.composite;

import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoExplorer.DemoSuite;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;

@DemoSuite({EnterXMPPAccountCompositeDemo.class, SummaryItemCompositeDemo.class})
@Demo
public class CompositeDemoSuite extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    // Nothing to do
  }
}
