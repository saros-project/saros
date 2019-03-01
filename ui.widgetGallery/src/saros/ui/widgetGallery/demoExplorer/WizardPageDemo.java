package saros.ui.widgetGallery.demoExplorer;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import saros.ui.widgetGallery.demoSuits.AbstractDemo;

public abstract class WizardPageDemo extends AbstractDemo {

  protected IWizardPage wizardPage;

  @Override
  public void createDemo(Composite parent) {
    parent.setLayout(new GridLayout(1, false));

    Composite content = new Composite(parent, SWT.NONE);
    content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    content.setLayout(new FillLayout());

    this.wizardPage = this.getWizardPage();
    this.wizardPage.createControl(content);
  }

  public abstract IWizardPage getWizardPage();
}
