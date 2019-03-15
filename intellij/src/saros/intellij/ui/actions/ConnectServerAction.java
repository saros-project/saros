package saros.intellij.ui.actions;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import saros.SarosPluginContext;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.communication.connection.ConnectionHandler;
import saros.repackaged.picocontainer.annotations.Inject;

/** Connects to XMPP/Jabber server with given account or active account */
public class ConnectServerAction extends AbstractSarosAction {
  public static final String NAME = "connect";

  private final Project project;

  @Inject private XMPPAccountStore accountStore;

  @Inject private ConnectionHandler connectionHandler;

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
   * @param account the account to connect to
   */
  private void connectAccount(final XMPPAccount account) {
    ProgressManager.getInstance()
        .run(
            new Task.Modal(project, "Connecting...", false) {

              @Override
              public void run(@NotNull ProgressIndicator indicator) {

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
