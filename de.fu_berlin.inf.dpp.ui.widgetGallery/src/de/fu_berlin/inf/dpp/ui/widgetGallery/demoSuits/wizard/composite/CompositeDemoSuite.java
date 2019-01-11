package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.wizard.composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.DemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import org.eclipse.swt.widgets.Composite;

@DemoSuite({EnterXMPPAccountCompositeDemo.class, SummaryItemCompositeDemo.class})
@Demo
public class CompositeDemoSuite extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    // Nothing to do
  }
}
