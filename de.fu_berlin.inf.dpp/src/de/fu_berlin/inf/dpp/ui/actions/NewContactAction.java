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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jivesoftware.smack.XMPPConnection;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.wizards.AddContactWizard;

public class NewContactAction extends Action implements IConnectionListener {

    public NewContactAction() {
        setToolTipText("Add a new contact");
        setImageDescriptor(SarosUI.getImageDescriptor("/icons/user_add.png"));

        Saros.getDefault().addListener(this);
        updateEnablement();
    }

    @Override
    public void run() {
        Shell shell = Display.getDefault().getActiveShell();
        new WizardDialog(shell, new AddContactWizard()).open();
    }

    public void connectionStateChanged(XMPPConnection connection,
            ConnectionState newState) {
        updateEnablement();
    }

    private void updateEnablement() {
        setEnabled(Saros.getDefault().isConnected());
    }
}
