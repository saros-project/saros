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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
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

public class InvitationDialog extends Dialog implements IInvitationUI,
    IConnectionListener {

    private TableViewer tableviewer;
    private Table table;
    private ArrayList<InviterData> input;
    private Button cancelSelectedInvitationButton;

    private Roster roster = Saros.getDefault().getRoster();
    private InvState inviteStep = InvState.SELECTION;
    private JID autoinviteJID = null;
    private Display display = null;

    private static enum InvState {
        SELECTION, INVITING, DONE
    }

    // assigned to any of the entries of the invite-tableview
    private class InviterData {
        JID jid;
        String name;
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
                    if (item.outgoingProcess.getState() == IInvitationProcess.State.SYNCHRONIZING) {
                        return "" + (item.outgoingProcess.getProgressCurrent())
                            + " of " + item.outgoingProcess.getProgressMax()
                            + ": " + item.outgoingProcess.getProgressInfo();
                    } else {
                        return "";
                    }
                } else {
                    return "";
                }
            }
            return "";
        }
    }

    public InvitationDialog(Shell parentShell, JID jid) {
        super(parentShell);
        this.autoinviteJID = jid;
    }

    @Override
    protected Control createContents(Composite parent) {

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

        // avoid multi selection
        this.tableviewer = new TableViewer(comTable, SWT.FULL_SELECTION);
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
                    Display.getDefault().syncExec(new Runnable() {
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

                InviterData data = (InviterData) InvitationDialog.this.table
                    .getSelection()[0].getData();

                setInviteable(InvitationDialog.this.table.getSelectionCount() > 0
                    && data.outgoingProcess == null);
            }
        });

        this.cancelSelectedInvitationButton.setEnabled(false);

        // get online users from roster
        if (this.autoinviteJID == null) {
            attachRosterListener();
        }
        refreshRosterListRunASync(this.autoinviteJID);

        Control c = super.createContents(parent);

        getButton(IDialogConstants.OK_ID).setText("Invite");
        getButton(IDialogConstants.CANCEL_ID).setText("Close");
        setInviteable(false);

        // auto trigger automatic invite
        if (this.autoinviteJID != null) {
            performInvitation();
        }

        return c;
    }

    protected void setInviteable(boolean b) {
        getButton(IDialogConstants.OK_ID).setEnabled(b);
    }

    @Override
    protected void okPressed() {
        performInvitation();
        setInviteable(false);
    }

    public boolean performInvitation() {

        this.inviteStep = InvState.INVITING;
        this.cancelSelectedInvitationButton.setEnabled(true);
        getButton(IDialogConstants.CANCEL_ID).setEnabled(false);

        try {
            TableItem[] cursel = this.table.getSelection();

            ISharedProject project = Saros.getDefault().getSessionManager()
                .getSharedProject();
            String name = project.getProject().getName();

            for (TableItem ti : cursel) {
                Object o = ti.getData();

                InviterData invdat = (InviterData) o;
                invdat.outgoingProcess = project.invite(invdat.jid, name, true,
                    this);
            }

            return false; // we wanna wait (and block) until all invites are
            // done

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // Triggers the update of the table in a GUI thread.
    public void updateInvitationProgress(final JID jid) {
        // TODO Widget might be disposed
        this.display.asyncExec(new Runnable() {
            public void run() {
                updateInvitationProgressRunASync(jid);
            }
        });
    }

    /*
     * Updates the invitation progress for all users in the table by refreshing
     * the table. MyLabelProvider will then poll the current progresses.
     */
    private void updateInvitationProgressRunASync(JID jid) {

        boolean allSuccessfullyDone = true;
        boolean allDoneOrCanceled = true;

        InviterData invdat = null;
        int index;

        boolean atLeastOneInvitationWasStarted = false;

        for (index = 0; index < this.table.getItemCount(); index++) {

            TableItem ti = this.table.getItem(index);
            Object o = ti.getData();
            invdat = (InviterData) o;

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
                this.tableviewer.refresh(o);
            }
        }

        // force the table to update ALL labels
        if (jid == null) {
            this.tableviewer.refresh();
        }

        // are all invites done?
        if (atLeastOneInvitationWasStarted && allSuccessfullyDone) {
            this.inviteStep = InvState.DONE;
            // TODO does not seem correct
            setInviteable(false);
            this.close();
        }

        getButton(IDialogConstants.CANCEL_ID).setEnabled(allDoneOrCanceled);

        this.cancelSelectedInvitationButton.setEnabled(isSelectionCancelable()
            && (this.inviteStep != InvState.DONE));

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

    void cancelInvite() {
        TableItem[] cursel = this.table.getSelection();
        for (TableItem ti : cursel) {
            Object o = ti.getData();
            InviterData invdat = (InviterData) o;
            if (invdat.outgoingProcess == null) {
                continue;
            }

            invdat.outgoingProcess.cancel("Invitation canceled by host", false);
        }
        updateInvitationProgress(null);
    }

    boolean isJIDinList(ArrayList<JID> items, JID jid) {
        for (int i = 0; i < items.size(); i++) {

            try {
                JID curJID = items.get(i);

                if (curJID.equals(jid)) {
                    return true;
                }

            } catch (Exception e) {
                System.out.println(e);
            }
        }

        return false;
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

    public void showErrorMessage(String errorMessage) {
        ErrorMessageDialog.showErrorMessage(errorMessage);
    }

    public void cancel(String errorMsg, boolean replicated) {

        // TODO Error Message is not displayed
        updateInvitationProgress(null);
    }

    /**
     * @see IInvitationUI
     */
    public void runGUIAsynch(final Runnable runnable) {

        this.display.asyncExec(new Runnable() {
            public void run() {
                new Thread(runnable).start();
            }
        });
    }

    /**
     * @see IConnectionListener
     */
    public void connectionStateChanged(XMPPConnection connection,
        final ConnectionState newState) {

        if (newState == ConnectionState.CONNECTED) {
            this.roster = Saros.getDefault().getRoster();
            attachRosterListener();

        } else if (newState == ConnectionState.NOT_CONNECTED) {
            this.roster = null;
        }

        refreshRosterListRunASync(null);

    }

    private void attachRosterListener() {
        this.roster.addRosterListener(new RosterListener() {
            public void entriesAdded(Collection<String> addresses) {
                refreshRosterList();
            }

            public void entriesUpdated(Collection<String> addresses) {
                refreshRosterList();
            }

            public void entriesDeleted(Collection<String> addresses) {
                refreshRosterList();
            }

            public void presenceChanged(Presence presence) {
                refreshRosterList();

            }
        });
    }

    /*
     * Triggers the refresh of the roster list in a GUI thread.
     */
    private void refreshRosterList() {
        if (this.inviteStep != InvState.SELECTION) {
            return;
        }

        if (display.isDisposed())
            return;

        this.display.asyncExec(new Runnable() {
            public void run() {
                refreshRosterListRunASync(null);
            }
        });
    }

    /*
     * Clears and re-filles the table with all online users from my roster.
     * Selections are preserved.
     */
    private void refreshRosterListRunASync(JID toselect) {
        ArrayList<JID> curselA = new ArrayList<JID>();
        int[] curselNew = null;

        // save selection
        TableItem[] curselTIs = this.table.getSelection();
        for (TableItem curselTI : curselTIs) {
            curselA.add(((InviterData) curselTI.getData()).jid);
        }

        this.input.clear();
        this.table.removeAll();

        if (this.roster == null) {
            return;
        }

        Collection<RosterEntry> users = this.roster.getEntries();
        int index = -1;
        for (RosterEntry entry : users) {

            String username = entry.getUser();

            if (!Saros.getDefault().hasSarosSupport(username)) {
                continue;
            }

            Presence presence = this.roster.getPresence(username);

            User user = Saros.getDefault().getSessionManager()
                .getSharedProject().getParticipant(new JID(entry.getUser()));

            if ((presence != null)
                && presence.getType().equals(Presence.Type.available)
                && (user == null)) {
                InviterData invdat = new InviterData();
                invdat.jid = new JID(entry.getUser());
                String name = entry.getName();
                invdat.name = (name == null) ? entry.getUser() : name;
                invdat.outgoingProcess = null;

                this.input.add(invdat);
                index++;

                if (((this.autoinviteJID != null) && invdat.jid
                    .equals(this.autoinviteJID))
                    || ((this.autoinviteJID == null) && isJIDinList(curselA,
                        invdat.jid))) {
                    int curselOld[] = this.table.getSelectionIndices();
                    curselNew = new int[curselOld.length + 1];
                    System.arraycopy(curselOld, 0, curselNew, 0,
                        curselOld.length);
                    curselNew[curselNew.length - 1] = index;
                }
            }
        }

        this.tableviewer.refresh();

        if (curselNew != null) {
            this.table.setSelection(curselNew);
        }
    }
}
