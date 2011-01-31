/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * (c) Christopher Oezbek - 2006
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

import org.eclipse.jface.wizard.Wizard;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.wizards.pages.RegisterAccountPage;

/**
 * An wizard that is used to create XMPP accounts.
 * 
 * @author rdjemili
 * @author coezbek
 */
public class CreateAccountWizard extends Wizard {

    public final RegisterAccountPage page;

    protected final Saros saros;

    public CreateAccountWizard(Saros saros, PreferenceUtils preferenceUtils,
        boolean createAccount, boolean showUseNowButton, boolean useNowDefault) {

        if (createAccount) {
            setWindowTitle("Create New XMPP Account");
        } else {
            setWindowTitle("Enter User Account");
        }
        this.page = new RegisterAccountPage(saros, createAccount,
            showUseNowButton, useNowDefault, preferenceUtils);
        setNeedsProgressMonitor(true);
        setHelpAvailable(false);
        this.saros = saros;
    }

    public String getServer() {
        return this.server;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    String server, password, username;

    @Override
    public void addPages() {
        addPage(this.page);
    }

    @Override
    public boolean performFinish() {
        if (this.page.performFinish()) {
            this.server = this.page.getServer();
            this.username = this.page.getUsername();
            this.password = this.page.getPassword();

            return true;
        }
        return false;

    }

}
