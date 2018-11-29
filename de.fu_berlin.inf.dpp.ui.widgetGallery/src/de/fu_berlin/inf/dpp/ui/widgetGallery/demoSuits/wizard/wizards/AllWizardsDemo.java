package de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.wizard.wizards;

import de.fu_berlin.inf.dpp.ui.util.WizardUtils;
import de.fu_berlin.inf.dpp.ui.widgetGallery.annotations.Demo;
import de.fu_berlin.inf.dpp.ui.widgetGallery.demoSuits.AbstractDemo;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

@Demo("All available wizards.")
public class AllWizardsDemo extends AbstractDemo {
  @Override
  public void createDemo(final Composite parent) {
    parent.setLayout(new GridLayout(1, false));

    final Map<String, Runnable> wizards = new LinkedHashMap<String, Runnable>();
    wizards.put(
        "NewWizard (internal)",
        new Runnable() {
          @Override
          public void run() {
            WizardUtils.openNewProjectWizard();
          }
        });
    wizards.put(
        "ConfigurationWizard",
        new Runnable() {
          @Override
          public void run() {
            WizardUtils.openSarosConfigurationWizard();
          }
        });
    wizards.put(
        "AddXMPPAcountWizard",
        new Runnable() {
          @Override
          public void run() {
            WizardUtils.openAddXMPPAccountWizard();
          }
        });
    wizards.put(
        "CreateXMPPAcountWizard useNow=true",
        new Runnable() {
          @Override
          public void run() {
            WizardUtils.openCreateXMPPAccountWizard(true);
          }
        });
    wizards.put(
        "CreateXMPPAcountWizard useNow=false",
        new Runnable() {
          @Override
          public void run() {
            WizardUtils.openCreateXMPPAccountWizard(false);
          }
        });
    wizards.put(
        "AddBuddyWizard",
        new Runnable() {
          @Override
          public void run() {
            WizardUtils.openAddContactWizard();
          }
        });
    wizards.put(
        "ShareProjectWizard",
        new Runnable() {
          @Override
          public void run() {
            WizardUtils.openStartSessionWizard(null);
          }
        });
    wizards.put(
        "ShareProjectAddProjectWizard",
        new Runnable() {
          @Override
          public void run() {
            WizardUtils.openAddResourcesToSessionWizard(null);
          }
        });
    wizards.put(
        "ShareProjectAddBuddiesWizard",
        new Runnable() {
          @Override
          public void run() {
            WizardUtils.openAddContactsToSessionWizard();
          }
        });

    for (final String wizardName : wizards.keySet()) {
      Button openWizardButton = new Button(parent, SWT.PUSH);
      openWizardButton.setText("Open " + wizardName + "...");
      openWizardButton.addSelectionListener(
          new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
              wizards.get(wizardName).run();
            }
          });
    }
  }
}
