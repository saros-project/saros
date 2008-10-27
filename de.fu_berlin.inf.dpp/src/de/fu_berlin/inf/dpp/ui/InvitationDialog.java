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

import org.apache.log4j.Logger;
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

    private static Logger logger = Logger.getLogger(InvitationDialog.class
	    .getName());

    // assigned to any of the entries of the invite-tableview
    private class inviterdata {
	JID jid;
	String name;
	IOutgoingInvitationProcess outginvatationProc;
    }

    private static enum InvState {
	DONE, INVITING, SELECTION
    }

    // Class for providing labels of my Tableview
    private class MyLabelProvider extends LabelProvider implements
	    ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
	    return null;
	}

	public String getColumnText(Object element, int columnIndex) {
	    inviterdata item = (inviterdata) element;

	    switch (columnIndex) {
	    case 0:
		return item.name;
	    case 1:
		if (item.outginvatationProc != null) {
		    return getStateDesc(item.outginvatationProc.getState());
		} else {
		    return "";
		}
	    case 2:
		if (item.outginvatationProc != null) {
		    if (item.outginvatationProc.getState() == IInvitationProcess.State.SYNCHRONIZING) {
			return "Transfering file "
				+ (item.outginvatationProc.getProgressCurrent())
				+ " of "
				+ item.outginvatationProc.getProgressMax()
				+ ": "
				+ item.outginvatationProc.getProgressInfo();
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

    static final String[] StateNames = { "Initialized",
	    "Invitation sent. Waiting for acknowledgement...",
	    "Filelist of inviter requested", "Filelist of inviter sent",
	    "Filelist of invitee sent",
	    "Synchornizing project files. Transfering files...",
	    "Files sent. Waiting for invitee...", "Invitiation completed",
	    "Invitation canceled" };

    private JID autoinviteJID = null;
    private Button cancelButton;
    private Display display = null;
    private ArrayList<inviterdata> input;

    private InvState inviteStep = InvState.SELECTION;

    private Roster roster = Saros.getDefault().getRoster();

    private Table table;

    private TableViewer tableviewer;

    public InvitationDialog(Shell parentShell, JID jid) {
	super(parentShell);
	this.autoinviteJID = jid;

	// TODO Auto-generated constructor stub
    }

    private void attachRosterListener() {
	this.roster.addRosterListener(new RosterListener() {
	    public void entriesAdded(Collection<String> addresses) {
		refreshRosterList();
	    }

	    public void entriesDeleted(Collection<String> addresses) {
		refreshRosterList();
	    }

	    public void entriesUpdated(Collection<String> addresses) {
		refreshRosterList();
	    }

	    public void presenceChanged(Presence presence) {
		presenceChanged(presence.getFrom());

	    }

	    public void presenceChanged(String XMPPAddress) {
		refreshRosterList();
	    }
	});
    }

    public void cancel(String errorMsg, boolean replicated) {
	updateInvitationProgress(null);
    }

    void cancelInvite() {
	TableItem[] cursel = this.table.getSelection();
	for (TableItem ti : cursel) {
	    Object o = ti.getData();
	    inviterdata invdat = (inviterdata) o;
	    if (invdat.outginvatationProc == null) {
		continue;
	    }

	    invdat.outginvatationProc.cancel(null, false);
	}
	updateInvitationProgress(null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.listeners.IConnectionListener
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
	column.setText("Action");
	column.setWidth(200);

	// table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

	this.input = new ArrayList<inviterdata>();
	this.tableviewer.setInput(this.input);

	this.cancelButton = new Button(composite, SWT.NONE);
	this.cancelButton.setText("Cancel selected invitation");
	this.cancelButton.addSelectionListener(new SelectionListener() {

	    public void widgetDefaultSelected(SelectionEvent e) {
	    }

	    public void widgetSelected(SelectionEvent e) {
		cancelInvite();
		Display.getDefault().syncExec(new Runnable() {
		    public void run() {
		    }
		});
	    }
	});

	this.table.addSelectionListener(new SelectionAdapter() {
	    @Override
	    public void widgetDefaultSelected(SelectionEvent event) {
	    }

	    @Override
	    public void widgetSelected(SelectionEvent event) {
		if (InvitationDialog.this.inviteStep == InvState.SELECTION) {
		    setInviteable((InvitationDialog.this.table
			    .getSelectionCount() > 0));
		} else if (InvitationDialog.this.inviteStep == InvState.INVITING) {
		    InvitationDialog.this.cancelButton
			    .setEnabled(isSelectionCancelable());
		} else {
		    InvitationDialog.this.cancelButton.setEnabled(false);
		}
	    }
	});

	this.cancelButton.setEnabled(false);

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

    private String getStateDesc(IInvitationProcess.State state) {
	return InvitationDialog.StateNames[state.ordinal()];
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

    boolean isSelectionCancelable() {

	if (this.table.getSelectionCount() == 0) {
	    return false;
	}

	TableItem[] cursel = this.table.getSelection();
	for (TableItem ti : cursel) {
	    Object o = ti.getData();
	    inviterdata invdat = (inviterdata) o;
	    if ((invdat.outginvatationProc == null)
		    || ((invdat.outginvatationProc.getState() == State.INITIALIZED)
			    || (invdat.outginvatationProc.getState() == State.SYNCHRONIZING_DONE)
			    || (invdat.outginvatationProc.getState() == State.CANCELED) || (invdat.outginvatationProc
			    .getState() == State.DONE))) {
		return false;
	    }
	}
	return true;
    }

    @Override
    protected void okPressed() {
	performInvitation();
    }

    public boolean performInvitation() {

	this.inviteStep = InvState.INVITING;
	setInviteable(false);
	this.cancelButton.setEnabled(true);
	getButton(IDialogConstants.CANCEL_ID).setEnabled(false);

	try {
	    TableItem[] cursel = this.table.getSelection();

	    ISharedProject project = Saros.getDefault().getSessionManager()
		    .getSharedProject();
	    String name = project.getProject().getName();

	    for (TableItem ti : cursel) {
		Object o = ti.getData();

		inviterdata invdat = (inviterdata) o;
		invdat.outginvatationProc = project.invite(invdat.jid, name,
			true, this);
	    }

	    return false; // we wanna wait (and block) until all invites are
	    // done

	} catch (Exception e) {
	    e.printStackTrace();
	}

	return false;
    }

    /*
     * Triggers the refresh of the roster list in a GUI thread.
     */
    private void refreshRosterList() {
	if (this.inviteStep != InvState.SELECTION) {
	    return;
	}

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
	    curselA.add(((inviterdata) curselTI.getData()).jid);
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
	    Presence presence = this.roster.getPresence(username);

	    User user = Saros.getDefault().getSessionManager()
		    .getSharedProject()
		    .getParticipant(new JID(entry.getUser()));

	    if ((presence != null)
		    && presence.getType().equals(Presence.Type.available)
		    && (user == null)) {
		inviterdata invdat = new inviterdata();
		invdat.jid = new JID(entry.getUser());
		String name = entry.getName();
		invdat.name = (name == null) ? entry.getUser() : name;
		invdat.outginvatationProc = null;

		this.input.add(invdat);
		index++;

		if (((this.autoinviteJID != null) && invdat.jid
			.equals(this.autoinviteJID))
			|| ((this.autoinviteJID == null) && isJIDinList(
				curselA, invdat.jid))) {
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess.IInvitationUI
     */
    public void runGUIAsynch(final Runnable runnable) {

	this.display.asyncExec(new Runnable() {
	    public void run() {
		new Thread(runnable).start();
	    }
	});
    }

    protected void setInviteable(boolean b) {
	getButton(IDialogConstants.OK_ID).setEnabled(b);
    }

    public void showErrorMessage(String errorMessage) {
	ErrorMessageDialog.showErrorMessage(errorMessage);
    }

    // Triggers the update of the table in a GUI thread.
    public void updateInvitationProgress(final JID jid) {
	this.display.asyncExec(new Runnable() {
	    public void run() {
		updateInvitationProgressRunASyn(jid);
	    }
	});
    }

    /*
     * Updates the invitation progress for all users in the table by refreshing
     * the table. MyLabelProvider will then poll the current progresses.
     */
    private void updateInvitationProgressRunASyn(JID jid) {
	boolean alldone = true;
	inviterdata invdat = null;
	int index;

	for (index = 0; index < this.table.getItemCount(); index++) {

	    TableItem ti = this.table.getItem(index);
	    Object o = ti.getData();
	    invdat = (inviterdata) o;

	    if ((invdat.outginvatationProc != null)
		    && (invdat.outginvatationProc.getState() != IInvitationProcess.State.DONE)
		    && (invdat.outginvatationProc.getState() != IInvitationProcess.State.CANCELED)) {
		alldone = false;
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
	if (alldone) {
	    // if(invdat.outginvatationProc.getState() ==
	    // IInvitationProcess.State.DONE){
	    // inviteStep= InvState.SELECTION;
	    // }
	    this.inviteStep = InvState.DONE;
	    getButton(IDialogConstants.CANCEL_ID).setEnabled(true);

	}
	this.cancelButton.setEnabled(isSelectionCancelable()
		&& (this.inviteStep != InvState.DONE));

    }

}
