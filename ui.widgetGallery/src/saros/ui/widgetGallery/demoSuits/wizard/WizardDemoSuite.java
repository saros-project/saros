package saros.ui.widgetGallery.demoSuits.wizard;

import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoExplorer.DemoSuite;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;
import saros.ui.widgetGallery.demoSuits.wizard.composite.CompositeDemoSuite;
import saros.ui.widgetGallery.demoSuits.wizard.pages.PagesDemoSuite;
import saros.ui.widgetGallery.demoSuits.wizard.wizards.AllWizardsDemo;

@DemoSuite({CompositeDemoSuite.class, PagesDemoSuite.class, AllWizardsDemo.class})
@Demo
public class WizardDemoSuite extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    // Nothing to do
  }
}
