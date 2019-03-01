package saros.ui.widgetGallery.demoSuits.wizard.pages;

import org.eclipse.jface.wizard.IWizardPage;
import saros.ui.widgetGallery.annotations.Demo;
import saros.ui.widgetGallery.demoExplorer.WizardPageDemo;
import saros.ui.wizards.pages.ConfigurationSummaryWizardPage;

@Demo
public class ConfigurationDoneDemo extends WizardPageDemo {
  @Override
  public IWizardPage getWizardPage() {
    return new ConfigurationSummaryWizardPage(null, null);
  }
}
