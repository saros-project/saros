package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.basic;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.DemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.basic.illustrated.IllustratedCompositeDemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.basic.rounded.RoundedCompositeDemoSuite;
import org.eclipse.swt.widgets.Composite;

@DemoSuite({RoundedCompositeDemoSuite.class, IllustratedCompositeDemoSuite.class})
public class BasicDemoSuite extends AbstractDemo {

  @Override
  public void createDemo(Composite parent) {
    // Nothing to do
  }
}
