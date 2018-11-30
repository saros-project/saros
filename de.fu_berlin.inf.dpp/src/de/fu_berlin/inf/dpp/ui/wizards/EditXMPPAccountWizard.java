package de.fu_berlin.inf.dpp.ui.wizards;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.wizards.pages.EditXMPPAccountWizardPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.picocontainer.annotations.Inject;

/**
 * Wizard for editing an existing {@link XMPPAccount}.
 *
 * @author bkahlert
 */
public class EditXMPPAccountWizard extends Wizard {
  public static final String TITLE = Messages.EditXMPPAccountWizard_title;
  public static final ImageDescriptor IMAGE = ImageManager.WIZBAN_EDIT_XMPP_ACCOUNT;

  @Inject XMPPAccountStore xmppAccountStore;
  XMPPAccount account;

  EditXMPPAccountWizardPage editXMPPAccountWizardPage;

  public EditXMPPAccountWizard(XMPPAccount account) {
    assert (account != null);

    SarosPluginContext.initComponent(this);
    this.setWindowTitle(TITLE);
    this.setDefaultPageImageDescriptor(IMAGE);

    this.setNeedsProgressMonitor(false);

    this.account = account;
    editXMPPAccountWizardPage = new EditXMPPAccountWizardPage(account);
  }

  @Override
  public void addPages() {
    addPage(editXMPPAccountWizardPage);
  }

  @Override
  public boolean performFinish() {
    JID jid = editXMPPAccountWizardPage.getJID();

    String username = jid.getName();
    String password = editXMPPAccountWizardPage.getPassword();
    String domain = jid.getDomain().toLowerCase();
    String server = editXMPPAccountWizardPage.getServer();

    int port;

    if (editXMPPAccountWizardPage.getPort().length() != 0)
      port = Integer.valueOf(editXMPPAccountWizardPage.getPort());
    else port = 0;

    boolean useTLS = editXMPPAccountWizardPage.isUsingTLS();
    boolean useSASL = editXMPPAccountWizardPage.isUsingSASL();

    xmppAccountStore.changeAccountData(
        account, username, password, domain, server, port, useTLS, useSASL);

    return true;
  }
}
