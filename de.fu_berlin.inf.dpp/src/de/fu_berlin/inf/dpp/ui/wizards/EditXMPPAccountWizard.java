package de.fu_berlin.inf.dpp.ui.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.wizards.pages.EditXMPPAccountWizardPage;

/**
 * Wizard for editing an existing {@link XMPPAccount}.
 * 
 * @author bkahlert
 */
public class EditXMPPAccountWizard extends Wizard {
    public static final String TITLE = "Edit XMPP/Jabber Account";
    public static final ImageDescriptor IMAGE = ImageManager.WIZBAN_EDIT_XMPP_ACCOUNT;

    @Inject
    XMPPAccountStore xmppAccountStore;
    XMPPAccount account;

    EditXMPPAccountWizardPage editXMPPAccountWizardPage;

    public EditXMPPAccountWizard(XMPPAccount account) {
        assert (account != null);

        SarosPluginContext.initComponent(this);
        this.setWindowTitle(TITLE);
        this.setDefaultPageImageDescriptor(IMAGE);

        this.setNeedsProgressMonitor(false);

        JID jid;
        String username = account.getUsername();
        String password = account.getPassword();
        String server = account.getServer();
        if (username.contains("@")) {
            jid = new JID(username);
        } else {
            jid = new JID(username + "@" + server);
            server = "";
        }

        this.account = account;
        this.editXMPPAccountWizardPage = new EditXMPPAccountWizardPage(jid,
            password, server);
    }

    @Override
    public void addPages() {
        addPage(editXMPPAccountWizardPage);
    }

    @Override
    public boolean performFinish() {
        JID jid = this.editXMPPAccountWizardPage.getJID();
        String password = this.editXMPPAccountWizardPage.getPassword();
        String server = this.editXMPPAccountWizardPage.getServer();

        String username;
        if (server.isEmpty()) {
            username = jid.getName();
            server = jid.getDomain();
        } else {
            username = jid.getBase();
        }

        this.xmppAccountStore.changeAccountData(account.getId(), username,
            password, server);
        return true;
    }
}