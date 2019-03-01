package saros.ui.widgetGallery.demoSuits.wizard.pages;

import org.eclipse.jface.wizard.IWizardPage;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoExplorer.WizardPageDemo;
import saros.ui.wizards.pages.ResourceSelectionWizardPage;

@Demo(
    "This demo shows a ProjectSelectionWizardPage that reflects the projects that were selected on creation.")
public class ProjectSelectionDemo extends WizardPageDemo {
  @Override
  public IWizardPage getWizardPage() {
    return new ResourceSelectionWizardPage(null);
  }
}
