package saros.ui.wizards;

import org.eclipse.jface.wizard.Wizard;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.wizards.pages.ColorChooserWizardPage;

public class ColorChooserWizard extends Wizard {

  private ColorChooserWizardPage colorChooserWizardPage = new ColorChooserWizardPage(true);

  public ColorChooserWizard() {
    setWindowTitle(Messages.ChangeColorWizard_title);
    setHelpAvailable(false);
    setNeedsProgressMonitor(false);
    setDefaultPageImageDescriptor(ImageManager.WIZBAN_CONFIGURATION);

    colorChooserWizardPage.setTitle(Messages.ChangeColorWizardPage_title);
    colorChooserWizardPage.setDescription(Messages.ChangeColorWizardPage_description);
  }

  @Override
  public void addPages() {
    addPage(colorChooserWizardPage);
  }

  @Override
  public boolean performFinish() {
    return true;
  }

  public int getChosenColor() {
    return colorChooserWizardPage.getSelectedColor();
  }

  @Override
  public boolean needsPreviousAndNextButtons() {
    return false;
  }
}
