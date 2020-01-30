package saros.intellij.ui.views.buttons;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.util.TextRange;
import java.text.MessageFormat;
import java.util.function.BiPredicate;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import saros.SarosPluginContext;
import saros.communication.connection.ConnectionHandler;
import saros.communication.connection.IConnectionStateListener;
import saros.exceptions.IllegalAWTContextException;
import saros.exceptions.OperationCanceledException;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.IconManager;
import saros.intellij.ui.util.NotificationPanel;
import saros.intellij.ui.util.SafeDialogUtils;
import saros.net.ConnectionState;
import saros.net.xmpp.JID;
import saros.net.xmpp.contact.XMPPContactsService;
import saros.repackaged.picocontainer.annotations.Inject;

/** Button to add a contact to the currently connected account. */
public class AddContactButton extends AbstractToolbarButton {
  private static final Logger log = Logger.getLogger(AddContactButton.class);

  @Inject private XMPPContactsService xmppContactsService;
  @Inject private ConnectionHandler connectionHandler;

  private final IConnectionStateListener connectionStateListener =
      (state, error) -> setEnabled(state == ConnectionState.CONNECTED);

  public AddContactButton(@NotNull Project project) {
    super(project, null, Messages.AddContactButton_tooltip, IconManager.ADD_CONTACT_ICON);

    SarosPluginContext.initComponent(this);

    connectionHandler.addConnectionStateListener(connectionStateListener);

    addActionListener(actionEvent -> showAddContactDialog());

    if (!connectionHandler.isConnected()) {
      setEnabled(false);
    }
  }

  @Override
  public void dispose() {
    connectionHandler.removeConnectionStateListener(connectionStateListener);
  }

  /**
   * Shows the dialogs requesting the JID and (optional) nickname for the account to add as a
   * contact.
   *
   * <p>Calls {@link XMPPContactsService#addContact(JID, String, BiPredicate)} if neither of the
   * dialogs were canceled.
   */
  private void showAddContactDialog() {

    JID jid;
    try {
      jid = getJID();
    } catch (IllegalAWTContextException e) {
      log.error("Requesting JID for contact addition failed", e);

      showAccountCreationFailedErrorNotification(e);

      return;
    }

    if (jid == null) {
      log.debug("Adding contact canceled by user during user id entry.");

      return;
    }

    String nickname;
    try {
      nickname = getNickname();

    } catch (IllegalAWTContextException e) {
      log.error("Requesting nickname for contact addition failed", e);

      showAccountCreationFailedErrorNotification(e);

      return;
    }

    if (nickname == null) {
      log.debug("Adding contact canceled by user during user nickname entry.");

      return;
    }

    addContact(jid, nickname);
  }

  /**
   * Opens and input dialog that allows the user to enter a JID to add as a contact.
   *
   * @return the entered JID or <code>null</code> if the dialog was canceled
   * @throws IllegalAWTContextException if the method was called in an illegal context
   * @see SafeDialogUtils#showInputDialog(Project, String, String, String, InputValidator,
   *     TextRange)
   */
  private JID getJID() throws IllegalAWTContextException {
    InputValidator inputValidator =
        new InputValidator() {
          @Override
          public boolean checkInput(String inputString) {
            return true;
          }

          /**
           * {@inheritDoc}
           *
           * <p>Prevents the dialog from being closed on pressing "OK" and shows an error dialog if
           * the entered JID is invalid.
           */
          @Override
          public boolean canClose(String inputString) {
            JID jid = new JID(inputString);

            boolean validInput = jid.isValid() && !jid.getName().isEmpty();

            if (!validInput) {
              SafeDialogUtils.showError(
                  project,
                  Messages.AddContactButton_contact_jid_dialog_illegal_input_message,
                  Messages.AddContactButton_contact_jid_dialog_illegal_input_title);
            }

            return validInput;
          }
        };

    String userID =
        SafeDialogUtils.showInputDialog(
            project,
            Messages.AddContactButton_contact_jid_dialog_message,
            Messages.AddContactButton_contact_jid_dialog_initial_input,
            Messages.AddContactButton_contact_jid_dialog_title,
            inputValidator,
            new TextRange(0, 0));

    if (userID == null) {
      return null;
    }

    return new JID(userID);
  }

  /**
   * Opens an input dialog that allows the user to enter a nickname for the contact.
   *
   * @return the entered nickname or <code>null</code> if the dialog was canceled
   * @throws IllegalAWTContextException if the method was called in an illegal context
   * @see SafeDialogUtils#showInputDialog(Project, String, String, String)
   */
  private String getNickname() throws IllegalAWTContextException {
    return SafeDialogUtils.showInputDialog(
        project,
        Messages.AddContactButton_contact_nickname_dialog_message,
        "",
        Messages.AddContactButton_contact_nickname_dialog_title);
  }

  /**
   * Adds the given JID and nickname to the contact of the current account.
   *
   * @param jid the JID to add as a contact
   * @param nickname the nickname to use or an empty string if no nickname was specified
   * @see XMPPContactsService#addContact(JID, String, BiPredicate)
   */
  private void addContact(@NotNull JID jid, @NotNull String nickname) {
    BiPredicate<String, String> dialog =
        (title, message) -> {
          try {
            return SafeDialogUtils.showYesNoDialog(project, message, title);

          } catch (IllegalAWTContextException e) {
            log.error("Tried to run dialog in an illegal context", e);

            showAccountCreationFailedErrorNotification(e);

            return false;
          }
        };

    try {
      xmppContactsService.addContact(jid, nickname, dialog);

    } catch (OperationCanceledException e) {
      log.debug("Adding contact canceled by user during XMPP request: " + e.getMessage());
    }
  }

  private void showAccountCreationFailedErrorNotification(Exception e) {
    NotificationPanel.showError(
        MessageFormat.format(
            Messages.AddContactButton_contact_addition_failed_error_notification_message,
            e.getMessage()),
        Messages.AddContactButton_contact_addition_failed_error_notification_title);
  }
}
