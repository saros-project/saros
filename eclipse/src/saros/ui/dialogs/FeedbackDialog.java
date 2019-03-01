/** */
package saros.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import saros.feedback.FeedbackManager;
import saros.feedback.Messages;
import saros.ui.util.LinkListener;

/**
 * Implements a dialog that asks the user to participate in our Saros feedback survey.
 *
 * @author Lisa Dohrmann
 */
public class FeedbackDialog extends MessageDialog {

  /** the title for the dialog window */
  public static final String FEEDBACK_TITLE =
      Messages.getString("feedback.dialog.title"); // $NON-NLS-1$

  /** check box to let the user toggle "don't ask me again" */
  protected Button checkButton;

  public FeedbackDialog(Shell parentShell, String message) {
    // create MessageDialog with title and message, buttons are
    // added later
    super(parentShell, FEEDBACK_TITLE, null, message, MessageDialog.QUESTION, null, -1);
  }

  @Override
  protected Control createMessageArea(Composite composite) {
    // create image
    Image image = getImage();
    if (image != null) {
      imageLabel = new Label(composite, SWT.NULL);
      image.setBackground(imageLabel.getBackground());
      imageLabel.setImage(image);
      GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.BEGINNING).applyTo(imageLabel);
    }
    // create link label to show a message with hyperlinks
    if (message != null) {
      Link messageLabel = new Link(composite, getMessageLabelStyle());
      messageLabel.setText(message);
      GridDataFactory.fillDefaults()
          .align(SWT.FILL, SWT.BEGINNING)
          .grab(true, false)
          .hint(
              convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH),
              SWT.DEFAULT)
          .applyTo(messageLabel);
      messageLabel.addListener(SWT.Selection, new LinkListener());
    }
    return composite;
  }

  @Override
  protected Control createCustomArea(Composite parent) {
    checkButton = new Button(parent, SWT.CHECK);
    checkButton.setText(Messages.getString("feedback.dialog.never")); // $NON-NLS-1$

    GridData gd = new GridData();
    gd.horizontalIndent = 26;
    gd.verticalIndent = 15;
    checkButton.setLayoutData(gd);
    return parent;
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    Button yes =
        createButton(
            parent,
            IDialogConstants.YES_ID,
            Messages.getString("feedback.dialog.yes"),
            true); //$NON-NLS-1$
    createButton(
        parent,
        IDialogConstants.NO_ID,
        Messages.getString("feedback.dialog.no"),
        false); //$NON-NLS-1$

    // set the initial keyboard focus to the yes button
    yes.setFocus();
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
   * Store in global and workspace preferences, if the user wants to disable the automatic feedback
   * requests.
   */
  protected void updatePreferences() {
    FeedbackManager.setFeedbackDisabled(checkButton.getSelection());
  }
}
