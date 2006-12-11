package de.fu_berlin.inf.dpp.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class PrivacyPage extends WizardPage implements IWizardPage2 {

	protected PrivacyPage() {
		super("PrivacyPage");
	}

	public void createControl(Composite parent) {
		
		Composite root = new Composite(parent, SWT.NONE);

		root.setLayout(new GridLayout(2, false));

		setTitle("Configure Privacy Setting");
		setDescription("Saros is a research project and we are very interested in understanding how people use distributed pair programming.");

		
		 // Of course we respect your privacy and give you full control over which information you would like to share.
		Label portDescription = new Label(root, SWT.NONE);
		GridData twoColumn = new GridData();
		twoColumn.horizontalSpan = 3;
		portDescription.setLayoutData(twoColumn);
		portDescription.setText("Share aggregated anonymous data");
		
		setControl(root);
	}

	public boolean performFinish() {
		return true;
	}
}
