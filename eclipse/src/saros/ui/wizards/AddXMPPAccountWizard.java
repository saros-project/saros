package saros.ui.wizards;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import saros.SarosPluginContext;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.communication.connection.ConnectionHandler;
import saros.net.xmpp.JID;
import saros.preferences.Preferences;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.wizards.pages.EnterXMPPAccountWizardPage;
import saros.util.ThreadUtils;

/**
 * A wizard that allows to enter an existing {@link XMPPAccount} or to create new one.
 *
 * @author bkahlert
 */
public class AddXMPPAccountWizard extends Wizard {

  private static final Logger LOG = Logger.getLogger(AddXMPPAccountWizard.class);

  @Inject private Preferences preferences;

  @Inject private XMPPAccountStore accountStore;

  @Inject private ConnectionHandler connectionHandler;

  protected final EnterXMPPAccountWizardPage enterXMPPAccountWizardPage =
      new EnterXMPPAccountWizardPage();

  public AddXMPPAccountWizard() {
    SarosPluginContext.initComponent(this);

    setWindowTitle(Messages.AddXMPPAccountWizard_title);
    setHelpAvailable(false);
    setNeedsProgressMonitor(false);
    setDefaultPageImageDescriptor(ImageManager.WIZBAN_CONFIGURATION);
  }

  @Override
  public void addPages() {
    addPage(enterXMPPAccountWizardPage);
  }

  @Override
  public boolean performFinish() {
    if (!enterXMPPAccountWizardPage.isExistingAccount()) addXMPPAccount();

    assert accountStore.getActiveAccount() != null;

    if (preferences.isAutoConnecting()) autoConnectXMPPAccount();

    return true;
  }

  @Override
  public boolean performCancel() {

    if (!enterXMPPAccountWizardPage.isExistingAccount()) return true;

    return MessageDialog.openQuestion(
        getShell(),
        Messages.AddXMPPAccountWizard_account_created,
        Messages.AddXMPPAccountWizard_account_created_text);
  }

  /** Adds the {@link EnterXMPPAccountWizardPage}'s account data to the {@link XMPPAccountStore}. */
  private void addXMPPAccount() {

    JID jid = enterXMPPAccountWizardPage.getJID();

    String username = jid.getName();
    String password = enterXMPPAccountWizardPage.getPassword();
    String domain = jid.getDomain().toLowerCase();
    String server = enterXMPPAccountWizardPage.getServer();

    int port;

    if (enterXMPPAccountWizardPage.getPort().length() != 0)
      port = Integer.valueOf(enterXMPPAccountWizardPage.getPort());
    else port = 0;

    boolean useTLS = enterXMPPAccountWizardPage.isUsingTLS();
    boolean useSASL = enterXMPPAccountWizardPage.isUsingSASL();

    accountStore.createAccount(username, password, domain, server, port, useTLS, useSASL);
  }

  private void autoConnectXMPPAccount() {
    ThreadUtils.runSafeAsync(
        "dpp-connect-demand",
        LOG,
        new Runnable() {
          @Override
          public void run() {
            connectionHandler.connect(accountStore.getActiveAccount(), false);
          }
        });
  }
}
