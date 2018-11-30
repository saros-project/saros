package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.wizard;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.DemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.wizard.composite.CompositeDemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.wizard.pages.PagesDemoSuite;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.wizard.wizards.AllWizardsDemo;
import org.eclipse.swt.widgets.Composite;

@DemoSuite({CompositeDemoSuite.class, PagesDemoSuite.class, AllWizardsDemo.class})
@Demo
public class WizardDemoSuite extends AbstractDemo {
  @Override
  public void createDemo(Composite parent) {
    // Nothing to do
  }
}
