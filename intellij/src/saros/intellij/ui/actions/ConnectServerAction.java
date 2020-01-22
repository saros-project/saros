package saros.intellij.ui.actions;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import java.text.MessageFormat;
import org.jetbrains.annotations.NotNull;
import saros.SarosPluginContext;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.communication.connection.ConnectionHandler;
import saros.exceptions.IllegalAWTContextException;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.NotificationPanel;
import saros.intellij.ui.util.SafeDialogUtils;
import saros.net.xmpp.JID;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.SessionEndReason;
import saros.util.ThreadUtils;

/** Connects to XMPP/Jabber server with given account or active account */
public class ConnectServerAction extends AbstractSarosAction {
  public static final String NAME = "connect";

  private final Project project;

  @Inject private XMPPAccountStore accountStore;
  @Inject private ConnectionHandler connectionHandler;
  @Inject private ISarosSessionManager sarosSessionManager;

  public ConnectServerAction(@NotNull Project project) {
    this.project = project;

    SarosPluginContext.initComponent(this);
  }

  @Override
  public String getActionName() {
    return NAME;
  }

  /** Connects with the given user. */
  public void executeWithUser(String user) {
    JID jid = new JID(user);
    XMPPAccount account = accountStore.getAccount(jid.getName(), jid.getDomain());

    if (account == null) return;

    accountStore.setDefaultAccount(account);
    processConnectionRequest(account);
  }

  /** Connects with active account from the {@link XMPPAccountStore}. */
  @Override
  public void execute() {
    XMPPAccount account = accountStore.getDefaultAccount();

    if (account == null) return;

    processConnectionRequest(account);
  }

  /**
   * Connects to the given XMPP account after checking the validity of the connection request.
   *
   * <p>If the user is already connected to an account, the request is dropped if requested account
   * matches the connected account.
   *
   * <p>Otherwise, if the user is already connected to an account and in an active session, the user
   * is questioned whether they really want to change accounts. If this is confirmed by the user,
   * the session is left in the process of re-connecting with the new account.
   *
   * @param account the requested account to connect to
   */
  private void processConnectionRequest(final XMPPAccount account) {
    JID currentJID = connectionHandler.getLocalJID();
    JID newJID = new JID(account.getUsername(), account.getDomain());

    if (currentJID == null) {
      connectToAccount(account, false, false);

      return;
    }

    if (newJID.equals(currentJID)) {
      LOG.debug(
          "Ignoring connection action as account is already connected. current JID: "
              + currentJID
              + ", new JID: "
              + newJID);

      return;
    }

    ISarosSession sarosSession = sarosSessionManager.getSession();

    if (sarosSession == null) {
      connectToAccount(account, true, false);

      return;
    }

    String message = Messages.ConnectServerAction_leave_session_confirmation_message;

    if (sarosSession.isHost()) {
      message += Messages.ConnectServerAction_leave_session_confirmation_host_addendum_message;
    }

    boolean reallyReconnect;

    try {
      reallyReconnect =
          SafeDialogUtils.showYesNoDialog(
              project, message, Messages.ConnectServerAction_leave_session_confirmation_title);

    } catch (IllegalAWTContextException e) {
      LOG.error("Failed to show question on whether to really switch accounts to user", e);

      NotificationPanel.showError(
          MessageFormat.format(
              Messages.ConnectServerAction_leave_session_confirmation_host_addendum_error_message,
              e.getMessage()),
          Messages.ConnectServerAction_leave_session_confirmation_host_addendum_error_title);

      return;
    }

    if (reallyReconnect) {
      connectToAccount(account, true, true);
    }
  }

  /**
   * Connects to the given XMPP account.
   *
   * <p>If the information is passed that there already is an existing connection, the previous
   * connection will be ended before connecting to the new account.
   *
   * <p>If the information is passed that there already is a running session, it will also be left
   * before disconnecting from the currently connected XMPP account.
   *
   * @param account the account to connect to
   * @param isAlreadyConnected whether there currently is an existing connection
   * @param isInSession whether there currently is a running session
   */
  private void connectToAccount(
      @NotNull XMPPAccount account, boolean isAlreadyConnected, boolean isInSession) {

    ThreadUtils.runSafeAsync(
        "Connecting to XMPP account " + account,
        LOG,
        () -> {
          if (isInSession) {
            sarosSessionManager.stopSession(SessionEndReason.LOCAL_USER_LEFT);
          }

          if (isAlreadyConnected) {
            connectionHandler.disconnect();
          }

          String qualifiedAccountName = account.getUsername() + '@' + account.getDomain();

          String message =
              MessageFormat.format(
                  Messages.ConnectServerAction_progress_message, qualifiedAccountName);

          ProgressManager.getInstance()
              .run(
                  new Task.Modal(project, message, false) {

                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {

                      indicator.setIndeterminate(true);

                      LOG.info("Connecting to account: [" + qualifiedAccountName + "]");

                      try {
                        connectionHandler.connect(account, false);
                      } finally {
                        indicator.stop();
                      }
                    }
                  });
        });
  }
}
