package de.fu_berlin.inf.dpp.ui.wizards;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;

/**
 * An wizard that is used to create accounts.
 * 
 * @author Sebastian Schlaak
 */
public class CreateNewAccountWizard extends Wizard {

    public static final String CREATE_XMPP_ACCOUNT = "Create XMPP account";

    @Inject
    XMPPAccountStore service;
    AccountPage accountPage;
    XMPPAccount newAccount;

    public CreateNewAccountWizard() {
        Saros.reinject(this);
        setWindowTitle(CREATE_XMPP_ACCOUNT);
        addPageToWizard();
    }

    protected void addPageToWizard() {
        this.accountPage = new AccountPage();
        this.accountPage.setPageTitle("Add new XMPP account");
        addPage(accountPage);
    }

    private boolean createNewAccount() {

        assert (this.accountPage != null);

        XMPPAccount testAccount = new XMPPAccount(accountPage.getUserName(),
            accountPage.getPassword(), accountPage.getServer());

        if (service.isAccountInList(testAccount)) {
            MessageDialog.openError(getShell(), "Duplicate account",
                "An account with these details already exists.");
        } else {
            this.newAccount = this.service.createNewAccount(
                accountPage.getUserName(), accountPage.getPassword(),
                accountPage.getServer());

            return true;
        }

        return false;
    }

    @Override
    public boolean performFinish() {

        if (createNewAccount() == false)
            return false;

        if (service.getAllAccounts().size() == 1) {
            service.setAccountActive(this.newAccount);
        }
        return true;
    }
}
