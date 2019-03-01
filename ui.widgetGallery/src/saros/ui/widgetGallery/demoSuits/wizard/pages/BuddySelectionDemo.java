package saros.ui.widgetGallery.demoSuits.wizard.pages;

import org.eclipse.jface.wizard.IWizardPage;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoExplorer.WizardPageDemo;
import saros.ui.wizards.pages.ContactSelectionWizardPage;

@Demo(
    "This demo show a BuddySelectionWizardPage that reflects the buddies that were selected on creation.")
public class BuddySelectionDemo extends WizardPageDemo {
  @Override
  public IWizardPage getWizardPage() {
    return new ContactSelectionWizardPage();
  }
}
