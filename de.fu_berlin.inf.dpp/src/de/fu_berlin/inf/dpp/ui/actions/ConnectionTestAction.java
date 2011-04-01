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
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.ConnectionTestManager;
import de.fu_berlin.inf.dpp.net.internal.ConnectionTestManager.TestResult;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.ui.util.selection.SelectionUtils;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Action to start a test run using the {@link DataTransferManager}
 * 
 * @author szuecs
 */
public class ConnectionTestAction extends Action {

    private static final Logger log = Logger
        .getLogger(ConnectionTestAction.class);

    protected IConnectionListener connectionListener = new IConnectionListener() {
        public void connectionStateChanged(XMPPConnection connection,
            final ConnectionState newState) {
            updateEnablement();
        }
    };

    protected ISelectionListener selectionListener = new ISelectionListener() {
        public void selectionChanged(IWorkbenchPart part, ISelection selection) {
            updateEnablement();
        }
    };

    @Inject
    protected Saros saros;

    @Inject
    protected ConnectionTestManager connectionTestManager;

    public ConnectionTestAction() {
        super("Test data transfer connection...");
        setToolTipText("Test the data transfer connection to the selected buddy.");

        SarosPluginContext.initComponent(this);

        saros.addListener(connectionListener);
        SelectionUtils.getSelectionService().addSelectionListener(
            selectionListener);
        updateEnablement();
    }

    protected void updateEnablement() {
        try {
            List<JID> buddies = SelectionRetrieverFactory
                .getSelectionRetriever(JID.class).getSelection();
            this.setEnabled(saros.isConnected() && buddies.size() == 1);
        } catch (NullPointerException e) {
            this.setEnabled(false);
        } catch (Exception e) {
            if (!PlatformUI.getWorkbench().isClosing())
                log.error("Unexcepted error while updating enablement", e);
        }
    }

    /**
     * @review runSafe OK
     */
    @Override
    public void run() {

        RosterEntry rosterEntry = null;
        List<RosterEntry> selectedRosterEntries = SelectionRetrieverFactory
            .getSelectionRetriever(RosterEntry.class).getSelection();
        if (selectedRosterEntries.size() == 1) {
            rosterEntry = selectedRosterEntries.get(0);
        }

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
                        IStatus.ERROR, Utils.getMessage(e.getCause()), e
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
                + Utils.throughput(testResult[0].dataSize,
                    testResult[0].transferTime));

    }

    public void dispose() {
        SelectionUtils.getSelectionService().removeSelectionListener(
            selectionListener);
        saros.removeListener(connectionListener);
    }
}
