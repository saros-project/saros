package de.fu_berlin.inf.dpp.ui.widgetGallery.demos;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public abstract class WizardPageDemo extends DescriptiveDemo {

	protected IWizardPage wizardPage;

	public WizardPageDemo(Composite parent) {
		super(parent);
	}

	public WizardPageDemo(DemoContainer parent, String title) {
		super(parent, title);
	}

	public void createContent(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		Composite content = new Composite(parent, SWT.NONE);
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		content.setLayout(new FillLayout());

		this.wizardPage = this.getWizardPage();
		this.wizardPage.createControl(content);
	}

	public abstract IWizardPage getWizardPage();

	@Override
	public void setVisible() {
		if (this.wizardPage != null) {
			this.wizardPage.setVisible(true);
		}
	}

}
