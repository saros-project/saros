package saros.ui.wizards;

import org.eclipse.jface.wizard.Wizard;
import saros.SarosPluginContext;
import saros.account.XMPPAccount;
import saros.account.XMPPAccountStore;
import saros.net.xmpp.JID;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.wizards.pages.EnterXMPPAccountWizardPage;

/** A wizard that allows to enter an existing {@link XMPPAccount} or to create new one. */
public final class AddXMPPAccountWizard extends Wizard {

  @Inject private XMPPAccountStore accountStore;

  private final EnterXMPPAccountWizardPage enterXMPPAccountWizardPage =
      new EnterXMPPAccountWizardPage();

  public AddXMPPAccountWizard() {
    SarosPluginContext.initComponent(this);

    setWindowTitle(Messages.AddXMPPAccountWizard_title);
    setHelpAvailable(false);
    setNeedsProgressMonitor(false);
    setDefaultPageImageDescriptor(
        ImageManager.getImageDescriptor(ImageManager.WIZBAN_CREATE_XMPP_ACCOUNT));
  }

  @Override
  public void addPages() {
    addPage(enterXMPPAccountWizardPage);
  }

  @Override
  public boolean performFinish() {
    addXMPPAccount();
    return true;
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
}
