package de.fu_berlin.inf.dpp.ui.wizards.dialogs;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

public class WizardDialogAccessable extends WizardDialog {
    public WizardDialogAccessable(Shell parentShell, IWizard newWizard) {
        super(parentShell, newWizard);
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
}
