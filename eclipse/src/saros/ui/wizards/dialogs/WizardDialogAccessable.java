package saros.ui.wizards.dialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

public class WizardDialogAccessable extends WizardDialog {
  public WizardDialogAccessable(Shell parentShell, IWizard newWizard) {
    super(parentShell, newWizard);
  }

  public WizardDialogAccessable(
      Shell parentShell, IWizard newWizard, int includeStyle, int excludeStyle) {
    super(parentShell, newWizard);

    setShellStyle(getShellStyle() | includeStyle);
    setShellStyle(getShellStyle() & (~excludeStyle));
  }

  public Button getWizardButton(int id) {
    return getButton(id);
  }

  public void setWizardButtonLabel(int id, String label) {
    Button btn = getButton(id);
    if (btn != null) {
      btn.setText(label);
    }
  }

  public void setWizardButtonEnabled(int id, boolean enabled) {
    Button btn = getButton(id);
    if (btn != null) {
      btn.setEnabled(enabled);
    }
  }

  @Override
  public void buttonPressed(int buttonId) {
    super.buttonPressed(buttonId);
  }

  /** Make the progress monitor accessible */
  @Override
  public IProgressMonitor getProgressMonitor() {
    return super.getProgressMonitor();
  }
}
