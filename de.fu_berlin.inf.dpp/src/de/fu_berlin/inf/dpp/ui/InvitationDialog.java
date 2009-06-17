/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006-2009
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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.limewire.collection.Function;
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
import de.fu_berlin.inf.dpp.net.internal.DiscoveryManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.project.internal.SharedProject;
import de.fu_berlin.inf.dpp.util.ArrayIterator;
import de.fu_berlin.inf.dpp.util.MappingIterator;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Dialog by which the hosts can invite clients to a shared project session.
 * 
 * TODO LD This should not be a dialog, but rather a window.
 * 
 * TODO CodeRules violation: Do not implement listener interfaces.
 * 
 * TODO CodeRules violation: Use private only in dedicated cases.
 * 
 * TODO Creating the FileList asynchronously should be put into a separate
 * class.
 */
public class InvitationDialog extends Dialog implements IInvitationUI,
    IConnectionListener {

    private static final Logger log = Logger.getLogger(InvitationDialog.class
        .getName());

    protected TableViewer tableViewer;
    protected Table table;
    protected ArrayList<InviterData> input;
    protected Button cancelSelectedInvitationButton;
    protected Button autoCloseButton;

    protected Roster roster;
    protected List<JID> autoinviteJID;
    protected Display display;

    protected FileList localFileList;
    protected CoreException fileListCreationError = null;

    protected ISharedProject project;

    protected SessionManager sessionManager;

    protected Saros saros;

    protected DiscoveryManager discoveryManager;

    private List<IResource> resources;

    /**
     * Object representing a row in the {@link InvitationDialog#tableViewer}
     */
    protected static class InviterData {
        JID jid;
        String name;
        String error = "";
        IOutgoingInvitationProcess outgoingProcess;
        InvitationProgressMonitor progress;
    }

    /**
     * Class for providing labels of the {@link InvitationDialog#tableViewer}
     */
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
            case 1: {
                Boolean supported = discoveryManager.isSupportedNonBlock(
                    item.jid, Saros.NAMESPACE);
                if (supported == null) {
                    updateSarosSupportLater(item.jid);
                    return "?";
                }
                return supported.booleanValue() ? "Yes" : "No";
            }
            case 2:
                if (item.outgoingProcess != null) {
                    return getStateDesc(item.outgoingProcess.getState());
                } else {
                    return "";
                }
            case 3:
                if (item.outgoingProcess != null) {

                    switch (item.outgoingProcess.getState()) {
                    case SYNCHRONIZING:
                        return "" + (item.progress.worked / 10.0) + "% "
                            + item.progress.getSubTask();
                    case CANCELED:
                        return item.error;
                    default:
                        return "";
                    }
                } else {
                    Boolean supported = discoveryManager.isSupportedNonBlock(
                        item.jid, Saros.NAMESPACE);
                    if (supported == null) {
                        return "Discovery is in progress";
                    } else {
                        return "";
                    }
                }
            }
            return "";
        }
    }

    public InvitationDialog(Saros saros, SharedProject project,
        Shell parentShell, List<JID> autoInvite, DiscoveryManager discoManager,
        List<IResource> resources) {
        super(parentShell);
        this.autoinviteJID = autoInvite;
        this.project = project;
        this.saros = saros;
        this.discoveryManager = discoManager;
        this.resources = resources;
    }

    protected ExecutorService worker = Executors
        .newSingleThreadExecutor(new NamedThreadFactory(
            "InvitationDialog-AsyncDiscovery-"));

    public void updateSarosSupportLater(final JID jid) {

        worker.execute(new Runnable() {
            public void run() {

                // Perform blocking Disco update
                discoveryManager.isSarosSupported(jid);

                // Update UI with the new information
                updateInvitationProgress(jid);
            }
        });
    }

    @Override
    protected Control createContents(Composite parent) {

        getShell().addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                worker.shutdown();
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

        this.tableViewer = new TableViewer(comTable, SWT.FULL_SELECTION
            | SWT.MULTI);
        this.table = this.tableViewer.getTable();
        this.table.setLinesVisible(true);
        this.tableViewer.setContentProvider(new ArrayContentProvider());
        this.tableViewer.setLabelProvider(new MyLabelProvider());
        this.table.setHeaderVisible(true);
        this.table.setLayoutData(gd);
        TableColumn column = new TableColumn(this.table, SWT.NONE);
        column.setText("User");
        column.setWidth(150);
        column = new TableColumn(this.table, SWT.NONE);
        column.setText("Saros-Enabled");
        column.setWidth(100);
        column = new TableColumn(this.table, SWT.NONE);
        column.setText("Status");
        column.setWidth(300);
        column = new TableColumn(this.table, SWT.NONE);
        column.setText("Progress");
        column.setWidth(200);

        // table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        this.input = new ArrayList<InviterData>();
        this.tableViewer.setInput(this.input);

        this.cancelSelectedInvitationButton = new Button(composite, SWT.NONE);
        this.cancelSelectedInvitationButton
            .setText("Cancel selected invitation");

        this.autoCloseButton = new Button(composite, SWT.CHECK);
        this.autoCloseButton
            .setText("Automatically close this dialog after all invitations are complete.");
        boolean autoCloseDialog = saros.getPreferenceStore().getBoolean(
            PreferenceConstants.AUTO_CLOSE_DIALOG);
        this.autoCloseButton.setSelection(autoCloseDialog);

        this.cancelSelectedInvitationButton
            .addSelectionListener(new SelectionAdapter() {

                @Override
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

            @Override
            public void widgetSelected(SelectionEvent event) {
                updateButtons();
            }
        });

        this.cancelSelectedInvitationButton.setEnabled(false);

        // get online users from roster
        setRoster(saros.getRoster());
        refreshRosterList();

        Control c = super.createContents(parent);

        getButton(IDialogConstants.OK_ID).setText("Invite");
        getButton(IDialogConstants.CANCEL_ID).setText("Close");

        updateButtons();

        // auto trigger automatic invite
        if (autoinviteJID != null) {
            select(autoinviteJID);
            performInvitation();
        }

        return c;
    }

    public void updateButtons() {
        setSelectedCancelable(isSelectionCancelable());
        setInviteable(isSelectionInvitable());
        this.getButton(IDialogConstants.CANCEL_ID).setEnabled(
            isAllDoneOrCanceled());
        this.getButton(IDialogConstants.OK_ID).setEnabled(
            isSelectionInvitable());
    }

    @Override
    public int open() {

        if (this.project == null) {
            throw new IllegalStateException();
        }

        startFileListCreationAsync();

        return super.open();
    }

    protected void setSelectedCancelable(boolean b) {
        cancelSelectedInvitationButton.setEnabled(b);
    }

    protected void setInviteable(boolean b) {
        getButton(IDialogConstants.OK_ID).setEnabled(b);
    }

    @Override
    protected void okPressed() {
        performInvitation();
    }

    /**
     * A Progress Monitor which tracks the progress of the invitation to a
     * single user.
     * 
     * Whenever the value of work ticks done, the taskName or the subTask
     * changes, the InvitationDialog is notified of the change.
     * 
     * The current state of the three types of information can be retrieved
     * using getWorked(), getTaskName(), getSubTask().
     */
    public class InvitationProgressMonitor extends NullProgressMonitor {

        JID toInvite;
        int worked = 0;
        String subTask = "";
        String taskName = "";

        public InvitationProgressMonitor(JID toInvite) {
            this.toInvite = toInvite;
        }

        /**
         * Returns the work ticks done such that 1000 represents the full task
         * of inviting a user.
         */
        public int getWorked() {
            return worked;
        }

        public String getTaskName() {
            return taskName;
        }

        public String getSubTask() {
            return subTask;
        }

        @Override
        public void setTaskName(String name) {
            if (ObjectUtils.equals(name, taskName))
                return;
            this.taskName = name;
            updateInvitationProgress(toInvite);
        }

        @Override
        public void worked(int work) {
            if (work <= 0)
                return;
            this.worked += work;
            updateInvitationProgress(toInvite);
        }

        @Override
        public void subTask(String name) {
            if (ObjectUtils.equals(name, subTask))
                return;
            this.subTask = name;
            updateInvitationProgress(toInvite);
        }
    }

    protected void performInvitation() {

        makeSureFileListIsAvailable();

        setInviteable(false);
        setSelectedCancelable(true);
        getButton(IDialogConstants.CANCEL_ID).setEnabled(false);

        try {
            String name = project.getProject().getName();

            // Invite all selected users...
            for (InviterData invdat : getSelectedItems()) {
                JID toInvite = discoveryManager.getSupportingPresence(
                    invdat.jid, Saros.NAMESPACE);
                if (toInvite == null) {
                    if (MessageDialog
                        .openConfirm(
                            this.getShell(),
                            "Invite user who does not support Saros?",
                            "User "
                                + invdat.jid
                                + " does not seem to use Saros "
                                + "(but rather a normal Instant Messaging client), invite anyway?")) {
                        toInvite = invdat.jid;
                    } else {
                        continue;
                    }
                }
                invdat.progress = new InvitationProgressMonitor(toInvite);
                invdat.outgoingProcess = project.invite(toInvite, name, true,
                    this, localFileList, SubMonitor.convert(invdat.progress,
                        1000));
            }

        } catch (RuntimeException e) {
            log.error("Failed to perform invitation:", e);
        }
        updateButtons();
    }

    protected void startFileListCreationAsync() {
        Util.runSafeAsync(log, new Runnable() {
            public void run() {
                try {
                    if (resources != null) {
                        localFileList = new FileList(resources
                            .toArray(new IResource[resources.size()]));
                    } else {
                        localFileList = new FileList(project.getProject());
                    }
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
                log.error(
                    "An error occurred while waiting for the file list: ", e);
                return;
            } catch (InterruptedException e) {
                log.error("Code not designed to be interruptable", e);
                Thread.currentThread().interrupt();
                return;
            }
        }
        assert localFileList != null;
    }

    /**
     * Triggers the update of the given user in the table in a GUI thread.
     * 
     * If jig == null then the whole table is refreshed.
     * 
     */
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

    protected boolean isAtLeastOneInvitationStarted() {

        for (InviterData data : getAllItems()) {
            if (data.outgoingProcess != null)
                return true;
        }
        return false;
    }

    protected boolean isAllSuccessfullyDone() {

        for (InviterData data : getAllItems()) {
            if (data.outgoingProcess != null
                && data.outgoingProcess.getState() != State.DONE)
                return false;
        }
        return true;
    }

    protected boolean isAllDoneOrCanceled() {

        for (InviterData data : getAllItems()) {
            if (data.outgoingProcess != null) {
                State state = data.outgoingProcess.getState();
                if (state != State.DONE && state != State.CANCELED) {
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean autoCloseDialog() {
        return autoCloseButton.getSelection();
    }

    protected void saveAutoCloseState() {
        saros.getPreferenceStore().setValue(
            PreferenceConstants.AUTO_CLOSE_DIALOG, autoCloseDialog());
    }

    /**
     * Updates the invitation progress for all users in the table by refreshing
     * the table. MyLabelProvider will then poll the current progresses.
     */
    protected void updateInvitationProgressInternal(JID jid) {

        if (this.table.isDisposed()) {
            setRoster(null);
            return;
        }

        if (jid == null) {
            // force the table to update ALL labels
            this.tableViewer.refresh();
        } else {
            for (InviterData data : getAllItems()) {
                if (data.jid.equals(jid)) {
                    this.tableViewer.refresh(data);
                }
            }
        }

        updateButtons();

        // Are all invites done and did the user want to close the dialog
        // automatically?
        if (isAtLeastOneInvitationStarted() && isAllSuccessfullyDone()
            && autoCloseDialog()) {
            this.close();
        }
    }

    @Override
    public boolean close() {
        // save the status of the autoCloseButton in the preferences
        saveAutoCloseState();

        // If we still have on-going invitations, cancel them
        for (InviterData data : input) {
            IOutgoingInvitationProcess process = data.outgoingProcess;
            if (process != null) {
                State state = process.getState();
                if (!(state == State.CANCELED || state == State.DONE)) {
                    log.warn("Dialog closed, but an invitation"
                        + " is still on-going with: " + process.getPeer());
                    process.cancel(null, false);
                }
            }
        }
        return super.close();
    }

    /**
     * Returns true if all users that are currently selected can be canceled.
     * 
     * Returns false if no user is selected.
     */
    protected boolean isSelectionCancelable() {

        if (table.getSelectionCount() == 0) {
            return false;
        }

        for (InviterData data : getSelectedItems()) {
            if ((data.outgoingProcess == null)
                || ((data.outgoingProcess.getState() == State.INITIALIZED)
                    || (data.outgoingProcess.getState() == State.SYNCHRONIZING_DONE)
                    || (data.outgoingProcess.getState() == State.CANCELED) || (data.outgoingProcess
                    .getState() == State.DONE))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if all users that are currently selected can be invited.
     * 
     * Returns false if no user is selected or Saros is not supported by one of
     * the selected users.
     */
    protected boolean isSelectionInvitable() {
        if (table.getSelectionCount() == 0) {
            return false;
        }

        for (InviterData data : getSelectedItems()) {

            if (data.outgoingProcess != null
                && data.outgoingProcess.getState() != State.CANCELED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns an Iterable which can be used to get an Iterator of the selection
     * at the time the iterator is requested.
     */
    public Iterable<InviterData> getSelectedItems() {
        return getIterable(table.getSelection());
    }

    protected Iterable<InviterData> getAllItems() {
        return getIterable(table.getItems());
    }

    protected static Iterable<InviterData> getIterable(final TableItem[] items) {
        return new Iterable<InviterData>() {
            public Iterator<InviterData> iterator() {
                return new MappingIterator<TableItem, InviterData>(
                    new ArrayIterator<TableItem>(items),
                    new Function<TableItem, InviterData>() {
                        public InviterData apply(TableItem arg0) {
                            return (InviterData) arg0.getData();
                        }
                    });
            }
        };
    }

    protected void cancelInvite() {
        for (InviterData invdat : getSelectedItems()) {
            if (invdat.outgoingProcess != null) {
                // Null means the host canceled locally
                invdat.outgoingProcess.cancel(null, false);
            }
        }

        updateInvitationProgress(null);
    }

    protected static final String[] StateNames = { "Initialized",
        "Invitation sent. Waiting for acknowledgement...",
        "Invitation accepted. Sending filelist...",
        "Filelist sent to client. Waiting for sync info...",
        "Sync Info received",
        "Synchronizing project files. Transfering files...",
        "Files sent. Waiting for invitee...", "Invitiation completed",
        "Invitation canceled" };

    protected String getStateDesc(IInvitationProcess.State state) {
        // TODO Use a simple switch statement instead of the ordinal() HACK
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

    /**
     * TODO Provide a better mechanisms to subscribe to the roster.
     */
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

    /**
     * Clears and re-fills the table with all online users from the roster.
     * Selections are preserved.
     */
    protected void refreshRosterList() {

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
            invdat.outgoingProcess = currentInvitations.remove(invdat.jid);

            this.input.add(invdat);
        }

        // TODO Keep in dialog and show as error to user...
        if (currentInvitations.size() > 0) {
            for (IOutgoingInvitationProcess process : currentInvitations
                .values()) {
                log.warn("User " + process.getPeer()
                    + " went offline during invitation.");
                process.cancel("User went offline", true);
            }
        }

        this.tableViewer.refresh();
        this.table.setSelection(oldSelection);
    }

    /**
     * Will programmatically select any row in the able that has a JID contained
     * in the given list.
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
