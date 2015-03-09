/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
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

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.Wizard;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.account.XMPPAccount;
import de.fu_berlin.inf.dpp.account.XMPPAccountStore;
import de.fu_berlin.inf.dpp.communication.connection.ConnectionHandler;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.wizards.pages.EnterXMPPAccountWizardPage;
import de.fu_berlin.inf.dpp.util.ThreadUtils;

/**
 * A wizard that allows to enter an existing {@link XMPPAccount} or to create
 * new one.
 * 
 * @author bkahlert
 */
public class AddXMPPAccountWizard extends Wizard {

    private static final Logger LOG = Logger
        .getLogger(AddXMPPAccountWizard.class);

    @Inject
    private IPreferenceStore store;

    @Inject
    private XMPPAccountStore accountStore;

    @Inject
    private ConnectionHandler connectionHandler;

    protected final EnterXMPPAccountWizardPage enterXMPPAccountWizardPage = new EnterXMPPAccountWizardPage();

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
        addXMPPAccount();
        return true;
    }

    @Override
    public boolean performCancel() {

        if (!enterXMPPAccountWizardPage.isXMPPAccountCreated())
            return true;

        return MessageDialog.openQuestion(getShell(),
            Messages.AddXMPPAccountWizard_account_created,
            Messages.AddXMPPAccountWizard_account_created_text);
    }

    /**
     * Adds the {@link EnterXMPPAccountWizardPage}'s account data to the
     * {@link XMPPAccountStore}.
     * 
     */
    private void addXMPPAccount() {

        if (!enterXMPPAccountWizardPage.isXMPPAccountCreated()) {
            JID jid = enterXMPPAccountWizardPage.getJID();

            String username = jid.getName();
            String password = enterXMPPAccountWizardPage.getPassword();
            String domain = jid.getDomain().toLowerCase();
            String server = enterXMPPAccountWizardPage.getServer();

            int port;

            if (enterXMPPAccountWizardPage.getPort().length() != 0)
                port = Integer.valueOf(enterXMPPAccountWizardPage.getPort());
            else
                port = 0;

            boolean useTLS = enterXMPPAccountWizardPage.isUsingTLS();
            boolean useSASL = enterXMPPAccountWizardPage.isUsingSASL();

            accountStore.createAccount(username, password, domain, server,
                port, useTLS, useSASL);
        }

        if (accountStore.getAllAccounts().size() == 1
            && store.getBoolean(PreferenceConstants.AUTO_CONNECT))
            ThreadUtils.runSafeAsync("dpp-connect-demand", LOG, new Runnable() {
                @Override
                public void run() {
                    connectionHandler.connect(accountStore.getActiveAccount(),
                        false);
                }
            });
    }
}