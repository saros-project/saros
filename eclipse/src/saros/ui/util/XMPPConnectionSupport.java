package saros.ui.util;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.communication.connection.ConnectionHandler;
import saros.session.ISarosSessionManager;
import saros.util.ThreadUtils;

/** Central access point for the UI to connect and disconnect to an XMPP server. */
public class XMPPConnectionSupport {

  private static final Logger log = Logger.getLogger(XMPPConnectionSupport.class);

  private static XMPPConnectionSupport instance;

  private final XMPPAccountStore store;

  private final ISarosSessionManager sessionManager;

  private final ConnectionHandler connectionHandler;

  public static XMPPConnectionSupport getInstance() {
    return instance;
  }

  private volatile boolean isConnecting = false;
  private volatile boolean isDisconnecting = false;

  private XMPPAccount currentXmppAccount;

  public XMPPConnectionSupport(
      final XMPPAccountStore store,
      final ConnectionHandler connectionHandler,
      final ISarosSessionManager sessionManager) {
    this.store = store;
    this.connectionHandler = connectionHandler;
    this.sessionManager = sessionManager;
    instance = this;
  }

  /**
   * Returns the current XMPP account that was used for establishing a connection.
   *
   * @return the current XMPP account or <code>null</code>
   */
  public XMPPAccount getCurrentXMPPAccount() {
    if (Display.getCurrent() == null) throw new SWTException(SWT.ERROR_THREAD_INVALID_ACCESS);

    return currentXmppAccount;
  }

  /**
   * Connects with the default account.
   *
   * <p><b>Note:</b> If no default account is available this method silently returns regardless if
   * <code>
   * failSilently</code> is set to <code>false</code> !
   *
   * @param failSilently if <code>true</code> suppresses any further error handling
   */
  public void connect(boolean failSilently) {
    connect(null, false);
  }

  /**
   * Connects with given account.
   *
   * <p><b>Note:</b> If the default account should be used an no default account is available this
   * method silently returns regardless if <code>
   * failSilently</code> is set to <code>false</code> !
   *
   * @param account the account to use or <code>null</code> to use the default one
   * @param failSilently if <code>true</code> suppresses any further error handling
   */
  public void connect(final XMPPAccount account, boolean failSilently) {
    connect(account, false, failSilently);
  }

  /**
   * Connects with given account.
   *
   * <p><b>Note:</b> If the default account should be used an no default account is available this
   * method silently returns regardless if <code>
   * failSilently</code> is set to <code>false</code> !
   *
   * @param account account the account to use or <code>null</code> to use the default one
   * @param setAsDefault if <code>true</code> the account is set as the default one
   * @param failSilently if <code>true</code> suppresses any further error handling
   */
  public void connect(final XMPPAccount account, boolean setAsDefault, boolean failSilently) {
    if (Display.getCurrent() == null) throw new SWTException(SWT.ERROR_THREAD_INVALID_ACCESS);

    if (isConnecting || isDisconnecting && failSilently) return;

    if (isConnecting || isDisconnecting) {
      MessageDialog.openWarning(
          SWTUtils.getShell(),
          "Error",
          "A connection attempt is already in progress. Please try again.");
      return;
    }

    isConnecting = true;

    if (connectionHandler.isConnected() && failSilently) {
      isConnecting = false;
      return;
    }

    if (sessionManager.getSession() != null && failSilently) {
      isConnecting = false;
      return;
    }

    boolean mustDisconnect = false;

    if (sessionManager.getSession() != null) {

      final boolean disconnectFromSession =
          MessageDialog.openQuestion(
              SWTUtils.getShell(),
              "Disconnecting from the current Saros Session",
              "Connecting with a different account will disconnect you from your current Saros"
                  + " session. Do you wish to continue ?");

      if (!disconnectFromSession) {
        isConnecting = false;
        return;
      }

      mustDisconnect = true;
    }

    if (connectionHandler.isConnected()) {
      final boolean reconnect =
          MessageDialog.openQuestion(
              SWTUtils.getShell(),
              "Already connected",
              "Do you want to reconnect as "
                  + account.getUsername()
                  + "@"
                  + account.getDomain()
                  + " ?");

      if (!reconnect) {
        isConnecting = false;
        return;
      }

      mustDisconnect = true;
    }

    final XMPPAccount accountToConnect;

    accountToConnect = account != null ? account : store.getDefaultAccount();

    if (accountToConnect == null) {
      log.warn(
          "unable to establish a connection - no account was provided and no default account could"
              + " be found");

      isConnecting = false;
      return;
    }

    if (setAsDefault) store.setDefaultAccount(accountToConnect);

    currentXmppAccount = accountToConnect;

    final boolean disconnectFirst = mustDisconnect;

    ThreadUtils.runSafeAsync(
        "connect-xmpp",
        log,
        () -> {
          if (disconnectFirst) connectionHandler.disconnect();
          try {
            connectionHandler.connect(accountToConnect, failSilently);
          } finally {
            isConnecting = false;
          }
        });
  }

  /** Disconnects the currently connected account. */
  public void disconnect() {
    if (Display.getCurrent() == null) throw new SWTException(SWT.ERROR_THREAD_INVALID_ACCESS);

    if (isConnecting || isDisconnecting) return;

    isDisconnecting = true;

    ThreadUtils.runSafeAsync(
        "disconnect-xmpp",
        log,
        () -> {
          try {
            connectionHandler.disconnect();
          } finally {
            isDisconnecting = false;
          }
        });
  }
}
