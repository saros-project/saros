package de.fu_berlin.inf.dpp.ui.wizards.dialogs;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

/**
 * A dialog to display a wizard to the end user.
 * <p>
 * Always centers the dialog on the screen. The dialog is slightly shifted to
 * the top to give it a more comfortable look.
 * 
 * @see WizardDialog
 */
public class CenteredWizardDialog extends WizardDialog {
    protected Point initialSize = new Point(750, 550);

    public CenteredWizardDialog(Shell parentShell, IWizard wizard) {
        super(parentShell, wizard);
    }

    public CenteredWizardDialog(Shell parentShell, IWizard wizard,
        Point initialSize) {
        this(parentShell, wizard);
        this.initialSize = initialSize;
    }

    @Override
    protected Point getInitialLocation(Point initialSize) {
        Shell shell = this.getShell();
        Rectangle monitorBounds = shell.getMonitor().getBounds();
        Point rect = getInitialSize();
        int x = monitorBounds.x + (monitorBounds.width - rect.x) / 2;
        int y = monitorBounds.y + (monitorBounds.height - rect.y) / 2;
        y -= rect.y / 4;
        return new Point(x, y);
    }

    @Override
    protected Point getInitialSize() {
        return initialSize;
    }
}
