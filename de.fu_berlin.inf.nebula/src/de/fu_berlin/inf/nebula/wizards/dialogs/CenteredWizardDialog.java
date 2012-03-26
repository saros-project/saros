package de.fu_berlin.inf.nebula.wizards.dialogs;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

/**
 * A dialog to display a wizard to the end user.
 * <p>
 * Always centers the dialog on the screen. The dialog is slightly shifted to
 * the top to give it a more comfortable look.
 * 
 * Can be modeless (eclipse window can be accessed while dialog is open), if the modeless parameter is set to true in the constructor
 * 
 * @see WizardDialog
 */
public class CenteredWizardDialog extends WizardDialog {
    protected Point initialSize = new Point(750, 550);

    public CenteredWizardDialog(Shell parentShell, IWizard wizard) {
        super(parentShell, wizard);
    }

    public CenteredWizardDialog(Shell parentShell, IWizard wizard,
        Point initialSize, boolean modeless) {
        this(parentShell, wizard);
        if(modeless){
            this.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.MODELESS | SWT.RESIZE);
        }
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
