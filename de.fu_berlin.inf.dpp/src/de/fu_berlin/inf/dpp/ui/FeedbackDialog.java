/**
 * 
 */
package de.fu_berlin.inf.dpp.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.feedback.Messages;

/**
 * Implements a dialog that asks the user to participate in our Saros feedback
 * survey.
 * 
 * @author Lisa Dohrmann
 */
public class FeedbackDialog extends MessageDialog {

    /** the title for the dialog window */
    public static final String FEEDBACK_TITLE = Messages
        .getString("feedback.dialog.title"); //$NON-NLS-1$

    /** check box to let the user toggle "don't ask me again" */
    protected Button checkButton;

    protected Saros saros;

    protected FeedbackManager feedbackManager;

    public FeedbackDialog(Shell parentShell, Saros saros,
        FeedbackManager feedbackManager, String message) {
        // create MessageDialog with title and message, buttons are
        // added later
        super(parentShell, FEEDBACK_TITLE, null, message,
            MessageDialog.QUESTION, null, -1);
        this.saros = saros;
        this.feedbackManager = feedbackManager;
    }

    @Override
    protected Control createCustomArea(Composite parent) {
        checkButton = new Button(parent, SWT.CHECK);
        checkButton.setText(Messages.getString("feedback.dialog.never")); //$NON-NLS-1$
        return parent;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.YES_ID, Messages
            .getString("feedback.dialog.yes"), true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.NO_ID, Messages
            .getString("feedback.dialog.no"), false); //$NON-NLS-1$
    }

    @Override
    protected void buttonPressed(int buttonId) {
        switch (buttonId) {
        case IDialogConstants.YES_ID:
            setReturnCode(Window.OK);
            updatePreferences();
            break;
        case IDialogConstants.NO_ID:
            // if the user clicked no, CANCEL is returned but his decision about
            // future reminders is stored nevertheless
            setReturnCode(Window.CANCEL);
            updatePreferences();
            break;
        default:
            setReturnCode(Window.CANCEL);
        }
        close();
    }

    /**
     * Store in global and workspace preferences, if the user wants to disable
     * the automatic feedback requests.
     */
    protected void updatePreferences() {
        feedbackManager.setFeedbackDisabled(checkButton.getSelection());
    }
}
