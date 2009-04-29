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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.Saros;

/**
 * An wizard that is used to create Jabber accounts.
 * 
 * @author rdjemili
 * @author coezbek
 */
public class CreateAccountWizard extends Wizard {

    protected final RegisterAccountPage page;

    protected final Saros saros;

    public CreateAccountWizard(Saros saros, boolean createAccount,
        boolean showStoreInPrefsButton, boolean storeInPrefsDefault) {

        if (createAccount) {
            setWindowTitle("Create New User Account");
        } else {
            setWindowTitle("Enter User Account");
        }
        this.page = new RegisterAccountPage(saros, createAccount,
            showStoreInPrefsButton, storeInPrefsDefault);
        setNeedsProgressMonitor(true);
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

            try {
                // Open Roster so that a participant can be invited
                IWorkbench workbench = PlatformUI.getWorkbench();
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                window.getActivePage().showView(
                    "de.fu_berlin.inf.dpp.ui.RosterView", null,
                    IWorkbenchPage.VIEW_ACTIVATE);
            } catch (PartInitException e) {
                saros.getLog().log(
                    new Status(IStatus.ERROR, Saros.SAROS, IStatus.ERROR,
                        "Could not activate Roster View", e));
            }
            return true;
        } else {
            return false;
        }
    }

}
