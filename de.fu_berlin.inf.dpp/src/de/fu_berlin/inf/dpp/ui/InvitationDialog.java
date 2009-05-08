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
package de.fu_berlin.inf.dpp.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.picocontainer.annotations.Nullable;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.IInvitationUI;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.State;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.project.internal.SharedProject;
import de.fu_berlin.inf.dpp.util.Util;

public class InvitationDialog extends Dialog implements IInvitationUI,
    IConnectionListener {

    private static final Logger log = Logger.getLogger(InvitationDialog.class
        .getName());

    private TableViewer tableviewer;
    private Table table;
    private ArrayList<InviterData> input;
    private Button cancelSelectedInvitationButton;

    private Roster roster;
    private List<JID> autoinviteJID;
    private Display display;

    protected SessionManager sessionManager;

    protected Saros saros;

    // assigned to any of the entries of the invite-tableview
    protected static class InviterData {
        JID jid;
        String name;
        String error = "";
        IOutgoingInvitationProcess outgoingProcess;
    }

    // Class for providing labels of my Tableview
    private class MyLabelProvider extends LabelProvider implements
        ITableLabelProvider {

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {

            InviterData item = (InviterData) element;

            switch (columnIndex) {
            case 0:
                return item.name;
            case 1:
                if (item.outgoingProcess != null) {
                    return getStateDesc(item.outgoingProcess.getState());
                } else {
                    return "";
                }
            case 2:
                if (item.outgoingProcess != null) {

                    switch (item.outgoingProcess.getState()) {
                    case SYNCHRONIZING:
                        return "" + (item.outgoingProcess.getProgressCurrent())
                            + " of " + item.outgoingProcess.getProgressMax()
                            + ": " + item.outgoingProcess.getProgressInfo();
                    case CANCELED:
                        return item.error;
                    default:
                        return "";
                    }
                } else {
                    return "";
                }
            }
            return "";
        }
    }

    public InvitationDialog(Saros saros, SharedProject project,
        Shell parentShell, List<JID> autoInvite) {
        super(parentShell);
        this.autoinviteJID = autoInvite;
        this.project = project;
        this.saros = saros;
    }

    @Override
    protected Control createContents(Composite parent) {

        getShell().addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                setRoster(null);
            }
        });

        getShell().setText("Invitation Helper");
        this.display = getShell().getDisplay();

        Composite composite = new Composite(parent, SWT.NONE);

        GridLayout gl = new GridLayout();
        composite.setLayout(gl);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.minimumHeight = 200;

        Label usersLabel = new Label(composite, SWT.NONE);
        usersLabel.setText("Select users to invite:");

        Composite comTable = new Composite(composite, SWT.NONE);
        comTable.setLayout(gl);
        comTable.setLayoutData(gd);

        this.tableviewer = new TableViewer(comTable, SWT.FULL_SELECTION
            | SWT.MULTI);
        this.table = this.tableviewer.getTable();
        this.table.setLinesVisible(true);
        this.tableviewer.setContentProvider(new ArrayContentProvider());
        this.tableviewer.setLabelProvider(new MyLabelProvider());
        this.table.setHeaderVisible(true);
        this.table.setLayoutData(gd);
        TableColumn column = new TableColumn(this.table, SWT.NONE);
        column.setText("User");
        column.setWidth(150);
        column = new TableColumn(this.table, SWT.NONE);
        column.setText("Status");
        column.setWidth(300);
        column = new TableColumn(this.table, SWT.NONE);
        column.setText("Progress");
        column.setWidth(200);

        // table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        this.input = new ArrayList<InviterData>();
        this.tableviewer.setInput(this.input);

        this.cancelSelectedInvitationButton = new Button(composite, SWT.NONE);
        this.cancelSelectedInvitationButton
            .setText("Cancel selected invitation");
        this.cancelSelectedInvitationButton
            .addSelectionListener(new SelectionListener() {

                public void widgetDefaultSelected(SelectionEvent e) {
                    // Everything done in widgetSelected
                }

                public void widgetSelected(SelectionEvent e) {
                    cancelInvite();
                    Util.runSafeSWTSync(log, new Runnable() {
                        public void run() {
                            // Block until all SWT events have been processed
                        }
                    });
                }
            });

        this.table.addSelectionListener(new SelectionAdapter() {
            // TODO Use SelectionAdapter instead of SelectionListener everywhere

            @Override
            public void widgetSelected(SelectionEvent event) {
                InvitationDialog.this.cancelSelectedInvitationButton
                    .setEnabled(isSelectionCancelable());

                setInviteable(isSelectionInvitable());
            }
        });

        this.cancelSelectedInvitationButton.setEnabled(false);

        // get online users from roster
        setRoster(saros.getRoster());
        refreshRosterList();

        Control c = super.createContents(parent);

        getButton(IDialogConstants.OK_ID).setText("Invite");
        getButton(IDialogConstants.CANCEL_ID).setText("Close");
        setInviteable(false);

        // auto trigger automatic invite
        if (autoinviteJID != null) {
            select(autoinviteJID);
            performInvitation();
        }

        return c;
    }

    protected ISharedProject project;

    protected FileList localFileList;

    protected CoreException fileListCreationError = null;

    @Override
    public int open() {

        if (this.project == null) {
            throw new IllegalStateException();
        }

        startFileListCreationAsync();

        return super.open();
    }

    protected void setInviteable(boolean b) {
        getButton(IDialogConstants.OK_ID).setEnabled(b);
    }

    @Override
    protected void okPressed() {
        performInvitation();
        setInviteable(false);
    }

    public void performInvitation() {

        makeSureFileListIsAvailable();

        this.cancelSelectedInvitationButton.setEnabled(true);
        getButton(IDialogConstants.CANCEL_ID).setEnabled(false);

        try {
            TableItem[] cursel = this.table.getSelection();

            String name = project.getProject().getName();

            for (TableItem ti : cursel) {
                Object o = ti.getData();

                InviterData invdat = (InviterData) o;
                invdat.outgoingProcess = project.invite(invdat.jid, name, true,
                    this, localFileList);
            }

        } catch (RuntimeException e) {
            log.error("Failed to perform invitation:", e);
        }
    }

    protected void startFileListCreationAsync() {
        Util.runSafeAsync(log, new Runnable() {
            public void run() {
                try {
                    localFileList = new FileList(project.getProject());
                } catch (CoreException e) {
                    fileListCreationError = e;
                }
            }
        });
    }

    protected void makeSureFileListIsAvailable() {
        if (localFileList == null) {

            ProgressMonitorDialog dialog = new ProgressMonitorDialog(this
                .getShell());
            try {
                dialog.run(true, false, new IRunnableWithProgress() {
                    public void run(final IProgressMonitor monitor)
                        throws InterruptedException, InvocationTargetException {
                        while (localFileList == null
                            && fileListCreationError == null) {
                            Thread.sleep(100);
                        }
                        if (fileListCreationError != null)
                            throw new InvocationTargetException(
                                fileListCreationError);
                    }
                });
            } catch (InvocationTargetException e) {
                return;
            } catch (InterruptedException e) {
                return;
            }
        }
        assert localFileList != null;
    }

    // Triggers the update of the table in a GUI thread.
    public void updateInvitationProgress(final JID jid) {
        runGUIAsynch(new Runnable() {
            public void run() {
                updateInvitationProgressInternal(jid);
            }
        });
    }

    /**
     * Implemented for InvitationUI Interface
     */
    public void runGUIAsynch(final Runnable r) {

        if (this.display == null || this.display.isDisposed())
            return;

        this.display.asyncExec(Util.wrapSafe(log, r));
    }

    /*
     * Updates the invitation progress for all users in the table by refreshing
     * the table. MyLabelProvider will then poll the current progresses.
     */
    private void updateInvitationProgressInternal(JID jid) {

        boolean atLeastOneInvitationWasStarted = false;
        boolean allSuccessfullyDone = true;
        boolean allDoneOrCanceled = true;

        if (this.table.isDisposed()) {
            setRoster(null);
            return;
        }

        for (int index = 0; index < this.table.getItemCount(); index++) {

            InviterData invdat = (InviterData) this.table.getItem(index)
                .getData();

            if (invdat.outgoingProcess != null) {
                atLeastOneInvitationWasStarted = true;
                if (invdat.outgoingProcess.getState() != IInvitationProcess.State.DONE) {
                    allSuccessfullyDone = false;
                }

                if (invdat.outgoingProcess.getState() != IInvitationProcess.State.DONE
                    && invdat.outgoingProcess.getState() != IInvitationProcess.State.CANCELED) {
                    allDoneOrCanceled = false;
                }
            }

            if ((jid != null) && invdat.jid.equals(jid)) {
                this.tableviewer.refresh(invdat);
            }
        }

        // force the table to update ALL labels
        if (jid == null) {
            this.tableviewer.refresh();
        }

        this.getButton(IDialogConstants.CANCEL_ID)
            .setEnabled(allDoneOrCanceled);
        this.cancelSelectedInvitationButton.setEnabled(isSelectionCancelable());
        this.getButton(IDialogConstants.OK_ID).setEnabled(
            isSelectionInvitable());

        // Are all invites done?
        if (atLeastOneInvitationWasStarted && allSuccessfullyDone) {
            this.close();
        }
    }

    boolean isSelectionCancelable() {

        if (this.table.getSelectionCount() == 0) {
            return false;
        }

        TableItem[] cursel = this.table.getSelection();
        for (TableItem ti : cursel) {
            Object o = ti.getData();
            InviterData invdat = (InviterData) o;
            if ((invdat.outgoingProcess == null)
                || ((invdat.outgoingProcess.getState() == State.INITIALIZED)
                    || (invdat.outgoingProcess.getState() == State.SYNCHRONIZING_DONE)
                    || (invdat.outgoingProcess.getState() == State.CANCELED) || (invdat.outgoingProcess
                    .getState() == State.DONE))) {
                return false;
            }
        }
        return true;
    }

    protected boolean isSelectionInvitable() {
        if (InvitationDialog.this.table.getSelectionCount() == 0) {
            return false;
        }

        for (TableItem o : table.getSelection()) {
            InviterData data = (InviterData) o.getData();
            if (data.outgoingProcess != null
                && data.outgoingProcess.getState() != State.CANCELED) {
                return false;
            }
        }
        return true;
    }

    protected void cancelInvite() {
        TableItem[] selection = this.table.getSelection();

        for (TableItem item : selection) {
            InviterData invdat = (InviterData) item.getData();
            if (invdat.outgoingProcess != null) {
                // Null means the host canceled locally
                invdat.outgoingProcess.cancel(null, false);
            }
        }

        updateInvitationProgress(null);
    }

    static final String[] StateNames = { "Initialized",
        "Invitation sent. Waiting for acknowledgement...",
        "Filelist of inviter requested", "Filelist of inviter sent",
        "Filelist of invitee sent",
        "Synchronizing project files. Transfering files...",
        "Files sent. Waiting for invitee...", "Invitiation completed",
        "Invitation canceled" };

    private String getStateDesc(IInvitationProcess.State state) {
        return InvitationDialog.StateNames[state.ordinal()];
    }

    public void cancel(JID jid, String errorMsg, boolean replicated) {

        for (InviterData data : input) {
            if (data.jid.equals(jid)) {
                data.error = errorMsg;
            }
        }
        updateInvitationProgress(jid);
    }

    /**
     * @see IConnectionListener
     */
    public void connectionStateChanged(XMPPConnection connection,
        final ConnectionState newState) {

        setRoster(saros.getRoster());
        refreshRosterList();
    }

    RosterListener rosterListener = new RosterListener() {

        public void refreshRoster() {
            runGUIAsynch(new Runnable() {
                public void run() {
                    refreshRosterList();
                }
            });
        }

        public void entriesAdded(Collection<String> addresses) {
            refreshRoster();
        }

        public void entriesUpdated(Collection<String> addresses) {
            refreshRoster();
        }

        public void entriesDeleted(Collection<String> addresses) {
            refreshRoster();
        }

        public void presenceChanged(Presence presence) {
            refreshRoster();
        }
    };

    public void setRoster(Roster newRoster) {
        if (this.roster != null) {
            this.roster.removeRosterListener(rosterListener);
        }
        this.roster = newRoster;
        if (this.roster != null) {
            this.roster.addRosterListener(rosterListener);
        }
    }

    /*
     * Clears and re-filles the table with all online users from my roster.
     * Selections are preserved.
     */
    private void refreshRosterList() {

        // save selection
        TableItem[] oldSelection = table.getSelection();

        this.table.removeAll();

        if (this.roster == null)
            return;

        // Save currently running Invitations...
        HashMap<JID, IOutgoingInvitationProcess> currentInvitations = new HashMap<JID, IOutgoingInvitationProcess>();
        for (InviterData data : input) {
            if (data.outgoingProcess != null)
                currentInvitations.put(data.jid, data.outgoingProcess);
        }
        this.input.clear();

        for (RosterEntry entry : this.roster.getEntries()) {

            Presence presence = this.roster.getPresence(entry.getUser());
            if (presence == null || !presence.isAvailable())
                continue;

            User user = project.getParticipant(new JID(entry.getUser()));
            if (user != null)
                continue;

            InviterData invdat = new InviterData();
            invdat.jid = new JID(entry.getUser());
            invdat.name = (entry.getName() == null) ? entry.getUser() : entry
                .getName();
            // Check if we have a running invitation for the user
            invdat.outgoingProcess = currentInvitations.get(invdat.jid);

            this.input.add(invdat);
        }

        this.tableviewer.refresh();
        this.table.setSelection(oldSelection);
    }

    /**
     * Will select any row in the able that has a JID contained in the given
     * list.
     */
    public void select(@Nullable List<JID> toSelect) {

        table.deselectAll();

        if (toSelect == null)
            return;

        int index = 0;
        for (InviterData invdat : input) {
            if (toSelect.contains(invdat.jid)) {
                table.select(index);
            }
            index++;
        }
    }
}
