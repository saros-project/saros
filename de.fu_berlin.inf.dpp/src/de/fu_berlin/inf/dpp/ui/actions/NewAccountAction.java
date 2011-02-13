/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
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
package de.fu_berlin.inf.dpp.ui.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.jivesoftware.smack.Roster;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.wizards.CreateAccountWizard;
import de.fu_berlin.inf.dpp.util.Utils;

@Component(module = "ui")
public class NewAccountAction implements IWorkbenchWindowActionDelegate {

    private static final Logger log = Logger.getLogger(NewAccountAction.class
        .getName());

    protected IWorkbenchWindow window;

    @Inject
    protected Saros saros;

    @Inject
    protected SarosUI sarosUI;

    @Inject
    protected PreferenceUtils preferenceUtils;

    /**
     * This class is created by Eclipse to hook it up in the Menu bar.
     */
    public NewAccountAction() {
        Saros.injectDependenciesOnly(this);
    }

    /**
     * @review runSafe OK
     */
    public void run(IAction action) {
        Utils.runSafeSync(log, new Runnable() {
            public void run() {
                runNewAccount();
            }
        });
    }

    public void runNewAccount() {
        Shell shell = this.window.getShell();
        WizardDialog wd = new WizardDialog(shell, new CreateAccountWizard(saros, preferenceUtils, true, true, false));
        wd.setHelpAvailable(false);
        // open() blocks until the user closed the dialog
        wd.open();
        /*
         * activate Roster and bring it to front (if not done already) to let
         * user start inviting buddies
         */
        sarosUI.activateRosterView();
        Roster sarosRoster = saros.getRoster();
        if (sarosRoster != null)
            sarosRoster.reload();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate
     */
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // We don't need to update on a selectionChanged
    }

    public void dispose() {
        // Nothing to dispose
    }
}
