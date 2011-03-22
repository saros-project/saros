/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.ui.wizards;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.Wizard;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccount;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.ui.wizards.pages.EnterXMPPAccountWizardPage;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * A wizard that allows to enter an existing {@link XMPPAccount} or to create
 * new one.
 * 
 * @author bkahlert
 */
public class AddXMPPAccountWizard extends Wizard {
    private static final Logger log = Logger
        .getLogger(AddXMPPAccountWizard.class);

    @Inject
    protected Saros saros;

    @Inject
    protected XMPPAccountStore accountStore;

    EnterXMPPAccountWizardPage enterXMPPAccountWizardPage = new EnterXMPPAccountWizardPage();

    public AddXMPPAccountWizard() {
        SarosPluginContext.initComponent(this);

        setWindowTitle("Add XMPP/Jabber Account");
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
        addXMPPAccount();
        return true;
    }

    @Override
    public boolean performCancel() {
        if (enterXMPPAccountWizardPage.isXMPPAccountCreated()) {
            boolean doCancel = true;
            try {
                doCancel = Utils.runSWTSync(new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        return DialogUtils.openQuestionMessageDialog(
                            getShell(), "Account Created",
                            "Your XMPP/Jabber Account has already been created\n"
                                + "and added to your Saros configuration.\n\n"
                                + "Do you really want to cancel?");
                    }
                });
            } catch (Exception e) {
                log.warn(e);
            }
            return doCancel;
        }
        return true;
    }

    /**
     * Adds the {@link EnterXMPPAccountWizardPage}'s account data to the
     * {@link XMPPAccountStore}.
     * 
     * @return
     */
    protected XMPPAccount addXMPPAccount() {
        if (enterXMPPAccountWizardPage.isXMPPAccountCreated())
            return null;

        JID jid = enterXMPPAccountWizardPage.getJID();
        String password = enterXMPPAccountWizardPage.getPassword();

        String server = enterXMPPAccountWizardPage.getServer();
        String username;
        if (server.isEmpty()) {
            // If no optional server, use server portion of jid
            server = jid.getDomain();
            username = jid.getName();
        } else {
            // If optional server, use whole jid for username
            username = jid.getBase();
        }

        boolean hasActiveAccount = accountStore.hasActiveAccount();

        XMPPAccount account = accountStore.createNewAccount(username, password,
            server);
        accountStore.saveAccounts();

        /*
         * If an active account has already been set, the user created already
         * an account with the CreateXMPPAccountWizard
         */
        if (!hasActiveAccount) {
            accountStore.setAccountActive(account);
            saros.connect(false);
        }

        return account;
    }

    /*
     * Wizard Results
     */

    public JID getJID() {
        return this.enterXMPPAccountWizardPage.getJID();
    }

}