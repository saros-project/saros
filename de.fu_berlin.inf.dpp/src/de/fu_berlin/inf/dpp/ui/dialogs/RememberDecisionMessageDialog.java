package de.fu_berlin.inf.dpp.ui.dialogs;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class RememberDecisionMessageDialog extends MessageDialog {
    private static boolean rememberDecision = false;

    public RememberDecisionMessageDialog(Shell parentShell, String dialogTitle,
        Image dialogTitleImage, String dialogMessage, int dialogImageType,
        String[] dialogButtonLabels, int defaultIndex) {
        super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
            dialogImageType, dialogButtonLabels, defaultIndex);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.dialogs.MessageDialog#createCustomArea(org.eclipse.
     * swt.widgets.Composite)
     */
    @Override
    protected Control createCustomArea(Composite parent) {
        rememberDecision = false;
        final Button rememberDecisionButton = new Button(parent, SWT.CHECK);
        rememberDecisionButton.setText("Remember my decision.");
        rememberDecisionButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                rememberDecision = false;
            }

            @Override
            public void widgetSelected(SelectionEvent e) {
                rememberDecision = rememberDecisionButton.getSelection();
            }
        });
        return rememberDecisionButton;
    }

    /**
     * Get the remember decision value.
     * 
     * @return the remember decision value
     */
    public boolean isRememberDecision() {
        return rememberDecision;
    }
}
