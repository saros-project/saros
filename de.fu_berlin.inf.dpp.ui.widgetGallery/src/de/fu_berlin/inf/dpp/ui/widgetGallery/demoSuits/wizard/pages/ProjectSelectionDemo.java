package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.wizard.pages;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.WizardPageDemo;
import de.fu_berlin.inf.dpp.ui.wizards.pages.ResourceSelectionWizardPage;
import org.eclipse.jface.wizard.IWizardPage;

@Demo(
    "This demo shows a ProjectSelectionWizardPage that reflects the projects that were selected on creation.")
public class ProjectSelectionDemo extends WizardPageDemo {
  @Override
  public IWizardPage getWizardPage() {
    return new ResourceSelectionWizardPage(null);
  }
}
