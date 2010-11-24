package de.fu_berlin.inf.dpp.ui.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;

/**
 * An wizard that is used to change accounts.
 * 
 * @author Sebastian Schlaak
 */
public class ChangeWizard extends Wizard {

    @Inject
    XMPPAccountStore service;
    XMPPAccount accountToChange;
    AccountPage accountPage;

    public ChangeWizard(XMPPAccount account) {
        Saros.reinject(this);
        this.accountToChange = account;
        addPageToWizard();
        setWindowTitle("Change XMPP Account");
    }

    protected void addPageToWizard() {
        this.accountPage = new AccountPage();
        this.accountPage.setPageTitle("Change XMPP Account");
        this.accountPage.setJabberAccount(accountToChange);
        addPage(accountPage);
    }

    @Override
    public boolean performFinish() {
        this.service.changeAccountData(accountToChange.getId(),
            accountPage.getUserName(), accountPage.getPassword(),
            accountPage.getServer());
        return true;
    }
}