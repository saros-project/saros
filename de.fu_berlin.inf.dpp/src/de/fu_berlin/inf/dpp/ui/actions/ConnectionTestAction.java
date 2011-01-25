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

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.ConnectionTestManager;
import de.fu_berlin.inf.dpp.net.internal.ConnectionTestManager.TestResult;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.ui.RosterView.TreeItem;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Action to start a test run using the {@link DataTransferManager}
 * 
 * @author szuecs
 */
public class ConnectionTestAction extends SelectionProviderAction {

    private static final Logger log = Logger
        .getLogger(ConnectionTestAction.class);

    protected RosterEntry rosterEntry;

    protected Saros saros;

    protected ConnectionTestManager connectionTestManager;

    public ConnectionTestAction(Saros saros,
        ConnectionTestManager connectionTestManager, ISelectionProvider provider) {
        super(provider, "Test data transfer connection...");

        this.saros = saros;
        this.connectionTestManager = connectionTestManager;

        selectionChanged((IStructuredSelection) provider.getSelection());

        setToolTipText("Test the data transfer connection to the selected buddy.");
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {

        if (rosterEntry == null) {
            log.error("RosterEntry should not be null at this point!");
            return;
        }

        final JID recipient = new JID(rosterEntry.getUser());

        final TestResult[] testResult = new TestResult[1];
        try {
            new ProgressMonitorDialog(null).run(true, true,
                new IRunnableWithProgress() {

                    public void run(IProgressMonitor progress)
                        throws InvocationTargetException, InterruptedException {
                        try {
                            testResult[0] = connectionTestManager
                                .runConnectionTest(recipient, 65536,
                                    SubMonitor.convert(progress));
                        } catch (XMPPException e) {
                            throw new InvocationTargetException(e);
                        }
                    }
                });
        } catch (InvocationTargetException e) {
            ErrorDialog
                .openError(
                    EditorAPI.getShell(),
                    "Connection Test failed",
                    "Connection Test with buddy " + recipient + " failed",
                    new Status(IStatus.ERROR, "de.fu_berlin.inf.dpp",
                        IStatus.ERROR, Util.getMessage(e.getCause()), e
                            .getCause()));
            return;
        } catch (InterruptedException e) {
            log.error(e);
            return;
        }
        MessageDialog.openInformation(
            EditorAPI.getShell(),
            "Connection test successful",
            "Connection Test with buddy "
                + recipient
                + " using "
                + testResult[0].mode.toString()
                + " "
                + Util.throughput(testResult[0].dataSize,
                    testResult[0].transferTime));

    }

    protected RosterEntry getSelectedForTest(IStructuredSelection selection) {

        if (selection.size() != 1)
            return null;

        TreeItem selected = (TreeItem) selection.getFirstElement();
        return selected.getRosterEntry();
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        this.rosterEntry = getSelectedForTest(selection);
        setEnabled(this.rosterEntry != null);
    }
}
