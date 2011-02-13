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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.jivesoftware.smack.XMPPConnection;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.wizards.AddBuddyWizard;
import de.fu_berlin.inf.dpp.util.Utils;

public class NewContactAction extends Action {

    private static final Logger log = Logger.getLogger(NewContactAction.class
        .getName());

    protected Saros saros;

    public NewContactAction(Saros saros) {
        setToolTipText("Add a new buddy");
        setImageDescriptor(SarosUI
            .getImageDescriptor("/icons/elcl16/addcontact.png"));

        this.saros = saros;

        saros.addListener(new IConnectionListener() {
            public void connectionStateChanged(XMPPConnection connection,
                ConnectionState newState) {
                updateEnablement();
            }
        });
        updateEnablement();
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {
        Utils.runSafeSync(log, new Runnable() {
            public void run() {
                runNewContact();
            }
        });
    }

    public void runNewContact() {
        Shell shell = EditorAPI.getShell();
        WizardDialog wd = new WizardDialog(shell, new AddBuddyWizard(saros));
        wd.setHelpAvailable(false);
        wd.open();
    }

    protected void updateEnablement() {
        setEnabled(saros.isConnected());
    }
}
