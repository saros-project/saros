package saros.ui.wizards.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import saros.SarosPluginContext;
import saros.ui.util.WizardUtils;
import saros.ui.wizards.ConfigurationWizard;
import saros.ui.wizards.CreateXMPPAccountWizard;
import saros.ui.wizards.pages.EnterXMPPAccountWizardPage;

public class ConfigurationWizardDialog extends WizardDialog {

  private Button createAccountButton;

  private boolean accountCreated;

  public ConfigurationWizardDialog(Shell parent, ConfigurationWizard wizard) {
    super(parent, wizard);
    SarosPluginContext.initComponent(this);
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {

    /*
     * TODO move this button to the left most edge ... may require layout
     * changes in the parent or further method overrides ... see JFace
     * source code
     */
    createAccountButton =
        createButton(parent, IDialogConstants.CLIENT_ID, "Create Account...", false);

    createAccountButton.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            openCreateXMPPAccountWizard();
          }
        });

    setButtonLayoutData(createAccountButton);

    super.createButtonsForButtonBar(parent);
  }

  @Override
  protected void firePageChanging(PageChangingEvent event) {
    createAccountButton.setVisible(
        (!accountCreated) && event.getTargetPage() instanceof EnterXMPPAccountWizardPage);
    super.firePageChanging(event);
  }

  /** Opens a {@link CreateXMPPAccountWizard} and takes over the created account. */
  private void openCreateXMPPAccountWizard() {

    IWizardPage page = getCurrentPage();

    if (!(page instanceof EnterXMPPAccountWizardPage)) return;

    getShell().setVisible(false);

    CreateXMPPAccountWizard createXMPPAccountWizard =
        WizardUtils.openCreateXMPPAccountWizard(false);

    if (createXMPPAccountWizard != null
        && createXMPPAccountWizard.getCreatedXMPPAccount() != null) {
      accountCreated = true;
      createAccountButton.setVisible(false);

      ((EnterXMPPAccountWizardPage) page)
          .setAccount(createXMPPAccountWizard.getCreatedXMPPAccount(), true);
    }

    getShell().setVisible(true);
    getShell().setFocus();
  }
}
