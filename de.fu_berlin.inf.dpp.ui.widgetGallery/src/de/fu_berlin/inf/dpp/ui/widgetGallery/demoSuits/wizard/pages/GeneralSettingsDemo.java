package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.wizard.pages;

import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoExplorer.WizardPageDemo;
import de.fu_berlin.inf.dpp.ui.wizards.pages.ConfigurationSettingsWizardPage;
import org.eclipse.jface.wizard.IWizardPage;

@Demo
public class GeneralSettingsDemo extends WizardPageDemo {
  @Override
  public IWizardPage getWizardPage() {
    return new ConfigurationSettingsWizardPage();
  }
}
