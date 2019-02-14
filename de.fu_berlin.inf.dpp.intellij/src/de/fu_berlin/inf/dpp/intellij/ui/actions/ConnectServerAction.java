package de.fu_berlin.inf.dpp.intellij.ui.actions;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import org.picocontainer.annotations.Inject;

/** Connects to XMPP/Jabber server with given account or active account */
public class ConnectServerAction extends AbstractSarosAction {
  public static final String NAME = "connect";

  @Inject private XMPPAccountStore accountStore;

  @Inject private ConnectionHandler connectionHandler;

  @Override
  public String getActionName() {
    return NAME;
  }

  /** Connects with the given user. */
  public void executeWithUser(String user) {
    XMPPAccount account = accountStore.findAccount(user);
    accountStore.setAccountActive(account);
    connectAccount(account);
  }

  /** Connects with active account from the {@link XMPPAccountStore}. */
  @Override
  public void execute() {
    XMPPAccount account = accountStore.getActiveAccount();
    connectAccount(account);
  }

  /**
   * Connects an Account to the XMPPService and sets it as active.
   *
   * @param account
   */
  private void connectAccount(final XMPPAccount account) {

    // FIXME use the project from the action event !
    // AnActionEvent.getDataContext().getData(DataConstants.PROJECT)
    ProgressManager.getInstance()
        .run(
            new Task.Modal(project, "Connecting...", false) {

              @Override
              public void run(ProgressIndicator indicator) {

                LOG.info(
                    "Connecting server: ["
                        + account.getUsername()
                        + "@"
                        + account.getServer()
                        + "]");

                indicator.setIndeterminate(true);

                try {
                  connectionHandler.connect(account, false);
                } finally {
                  indicator.stop();
                }
              }
            });
  }
}
