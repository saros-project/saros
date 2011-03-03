package de.fu_berlin.inf.dpp.ui.widgetGallery.suits.wizard.wizards;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.dpp.ui.util.WizardUtils;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DemoContainer;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demos.DescriptiveDemo;

public class AllWizardsDemo extends DescriptiveDemo {
	public AllWizardsDemo(DemoContainer demoContainer, String title) {
		super(demoContainer, title);
	}

	public String getDescription() {
		return "All available wizards.";
	}

	@Override
	public void createContent(final Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		final Map<String, Runnable> wizards = new LinkedHashMap<String, Runnable>();
		wizards.put("NewWizard (internal)", new Runnable() {
			public void run() {
				WizardUtils.openNewProjectWizard();
			}
		});
		wizards.put("ConfigurationWizard", new Runnable() {
			public void run() {
				WizardUtils.openSarosConfigurationWizard();
			}
		});
		wizards.put("AddXMPPAcountWizard", new Runnable() {
			public void run() {
				WizardUtils.openAddXMPPAccountWizard();
			}
		});
		wizards.put("CreateXMPPAcountWizard useNow=true", new Runnable() {
			public void run() {
				WizardUtils.openCreateXMPPAccountWizard(true);
			}
		});
		wizards.put("CreateXMPPAcountWizard useNow=false", new Runnable() {
			public void run() {
				WizardUtils.openCreateXMPPAccountWizard(false);
			}
		});
		wizards.put("AddBuddyWizard", new Runnable() {
			public void run() {
				WizardUtils.openAddBuddyWizard();
			}
		});
		wizards.put("ShareProjectWizard", new Runnable() {
			public void run() {
				WizardUtils.openShareProjectWizard();
			}
		});
		wizards.put("ShareProjectAddProjectWizard", new Runnable() {
			public void run() {
				WizardUtils.openShareProjectAddProjectsWizard();
			}
		});
		wizards.put("ShareProjectAddBuddiesWizard", new Runnable() {
			public void run() {
				WizardUtils.openShareProjectAddBuddiesWizard();
			}
		});

		for (final String wizardName : wizards.keySet()) {
			Button openWizardButton = new Button(parent, SWT.PUSH);
			openWizardButton.setText("Open " + wizardName + "...");
			openWizardButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					wizards.get(wizardName).run();
				}
			});
		}
	}
}
