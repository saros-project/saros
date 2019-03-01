package saros.ui.widgetGallery.demoSuits.basic;

import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.demoExplorer.DemoSuite;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgetGallery.demoSuits.basic.illustrated.IllustratedCompositeDemoSuite;
import saros.ui.widgetGallery.demoSuits.basic.rounded.RoundedCompositeDemoSuite;

@DemoSuite({RoundedCompositeDemoSuite.class, IllustratedCompositeDemoSuite.class})
public class BasicDemoSuite extends AbstractDemo {

  @Override
  public void createDemo(Composite parent) {
    // Nothing to do
  }
}
