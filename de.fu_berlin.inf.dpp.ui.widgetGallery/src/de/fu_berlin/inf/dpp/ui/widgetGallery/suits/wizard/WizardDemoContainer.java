package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.wizard;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.wizard.composite.CompositeDemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.wizard.pages.PagesDemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.suits.wizard.wizards.AllWizardsDemo;

public class WizardDemoContainer extends DemoContainer {

    public WizardDemoContainer(DemoContainer demoContainer, String title) {
	super(demoContainer, title);
    }

    @Override
    public void createPartControls(Composite parent) {
	super.createPartControls(parent);

	new CompositeDemoContainer(this, "Composites");
	new PagesDemoContainer(this, "Pages");
	new AllWizardsDemo(this, "Wizards");
    }

}
