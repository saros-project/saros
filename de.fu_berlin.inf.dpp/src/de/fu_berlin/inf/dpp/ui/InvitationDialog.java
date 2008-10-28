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

public class InvitationDialog extends Dialog implements IInvitationUI, IConnectionListener {
	
	private TableViewer tableviewer;
    private Table 		table;
    private ArrayList<inviterdata> 	input;
    private Button		cancelButton;
    
	private Roster 	roster=Saros.getDefault().getRoster();
	private InvState inviteStep		= InvState.SELECTION;
	private JID 	autoinviteJID	= null;
	private Display display 		= null;

	private static enum InvState {
		SELECTION, INVITING, DONE
	}
	
	// assigned to any of the entries of the invite-tableview
	private class inviterdata {
		JID 	jid;
		String 	name;
		IOutgoingInvitationProcess outginvatationProc;
	}

	
	// Class for providing labels of my Tableview
	private class MyLabelProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	
		public String getColumnText(Object element, int columnIndex) {
			inviterdata item = (inviterdata)element;
			
			switch(columnIndex) {
			case 0:
				return item.name;
			case 1:
				if (item.outginvatationProc!=null)
					return getStateDesc(item.outginvatationProc.getState());
				else
					return "";
			case 2:
				if (item.outginvatationProc!=null){
					if (item.outginvatationProc.getState()==IInvitationProcess.State.SYNCHRONIZING)
						return "Transfering file "+
							(item.outginvatationProc.getProgressCurrent()) +" of "+
							item.outginvatationProc.getProgressMax()+": "+
							item.outginvatationProc.getProgressInfo();
					else 
						return "";
				} else
					return "";
			}
			return "";
		}
	}
	
	public InvitationDialog(Shell parentShell, JID jid) {
		super(parentShell);
		autoinviteJID=jid;

		// TODO Auto-generated constructor stub
	}


	@Override
	protected Control createContents(Composite parent) {
		
		getShell().setText("Invitation Helper");
		display = getShell().getDisplay();
			
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout gl = new GridLayout();
		composite.setLayout(gl);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.minimumHeight=200;

		Label usersLabel = new Label(composite, SWT.NONE);
		usersLabel.setText("Select users to invite:");

	    Composite comTable = new Composite(composite , SWT.NONE);
		comTable .setLayout(gl);
		comTable .setLayoutData(gd);

//		tableviewer = new TableViewer(comTable, SWT.FULL_SELECTION | SWT.MULTI);
		//avoid multi selection
		tableviewer = new TableViewer(comTable, SWT.FULL_SELECTION);
	    table = tableviewer.getTable();
	    table.setLinesVisible(true);
	    tableviewer.setContentProvider(new ArrayContentProvider());
	    tableviewer.setLabelProvider(new MyLabelProvider());
	    table.setHeaderVisible(true);
	    table.setLayoutData(gd);
	    TableColumn column = new TableColumn(table,SWT.NONE);
	    column.setText("User");
	    column.setWidth(150);
	    column = new TableColumn(table,SWT.NONE);
	    column.setText("Status");
	    column.setWidth(300);
	    column = new TableColumn(table,SWT.NONE);
	    column.setText("Action");
	    column.setWidth(200);
	    
		
//	    table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    
	    input = new ArrayList<inviterdata>();
	    tableviewer.setInput(input);

	    cancelButton = new Button(composite, SWT.NONE);
	    cancelButton.setText("Cancel selected invitation");
	    cancelButton.addSelectionListener(new SelectionListener() {
	
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
	    
		table.addSelectionListener(new SelectionAdapter()
		{
	        public void widgetSelected(SelectionEvent event) {
        		if (inviteStep==InvState.SELECTION) 
        			setInviteable( (table.getSelectionCount()>0) );
        		else if (inviteStep==InvState.INVITING)
        			cancelButton.setEnabled(isSelectionCancelable());
        		else
        			cancelButton.setEnabled(false);
	        }
	        public void widgetDefaultSelected(SelectionEvent event) {
	        }
		} ) ;
		
	    cancelButton.setEnabled(false);

	    // get online users from roster
		if (autoinviteJID==null)
			attachRosterListener();
		refreshRosterListRunASync(autoinviteJID);
		
		Control c=super.createContents(parent);

		getButton(IDialogConstants.OK_ID).setText("Invite");
	    getButton(IDialogConstants.CANCEL_ID).setText("Close");
	    setInviteable(false);
	    
		// auto trigger automatic invite
		if (autoinviteJID!=null)
			performInvitation();

		return c;
	}

	protected void setInviteable(boolean b) {
	    getButton(IDialogConstants.OK_ID).setEnabled(b);
	}

	@Override
	protected void okPressed() {
		performInvitation();
	}
	public boolean performInvitation() {
		
		inviteStep=InvState.INVITING;
		setInviteable(false);
		cancelButton.setEnabled(true);
	    getButton(IDialogConstants.CANCEL_ID).setEnabled(false);
		
		try {	
			TableItem[] cursel = table.getSelection();

			ISharedProject project = Saros.getDefault().getSessionManager().getSharedProject();
			String name = project.getProject().getName();
	
			for (int i=0; i < cursel.length; i++) {
				TableItem ti = cursel[i];
				Object o=ti.getData();
				
				inviterdata invdat = (inviterdata)o;
				invdat.outginvatationProc=project.invite(invdat.jid, name, true, this);
			}
			
			return false; // we wanna wait (and block) until all invites are done

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	
	// Triggers the update of the table in a GUI thread.
	public void updateInvitationProgress(final JID jid) {
		display.asyncExec(new Runnable() {
			public void run() {
				updateInvitationProgressRunASyn(jid);
		}} );	
	}
	
	/*
	 * Updates the invitation progress for all users in the table by refreshing
	 * the table. MyLabelProvider will then poll the current progresses. 
	 */
	private void updateInvitationProgressRunASyn(JID jid) {
		boolean alldone		= true;
		inviterdata invdat	= null;
		int index;

		for (index=0; index<table.getItemCount(); index++) {

			TableItem ti=table.getItem(index);
			Object o=ti.getData();
			invdat=(inviterdata)o;

			if (invdat.outginvatationProc!=null &&  
					invdat.outginvatationProc.getState()!=IInvitationProcess.State.DONE && 
					invdat.outginvatationProc.getState()!=IInvitationProcess.State.CANCELED ) {
				alldone=false;
			}
			
			if (jid!=null && invdat.jid.equals(jid))
				tableviewer.refresh(o);
		}

		// force the table to update ALL labels
		if (jid==null)
			tableviewer.refresh();
		
		// are all invites done?
		if (alldone) {
//			if(invdat.outginvatationProc.getState() == IInvitationProcess.State.DONE){
//				inviteStep= InvState.SELECTION;
//			}
			inviteStep=InvState.DONE;
		    getButton(IDialogConstants.CANCEL_ID).setEnabled(true);

		}
		cancelButton.setEnabled(isSelectionCancelable() && inviteStep!=InvState.DONE );
		
	}
	
	boolean isSelectionCancelable(){

		if (table.getSelectionCount()==0)
			return false;
		
		TableItem[] cursel = table.getSelection();
		for (int i=0;i<cursel.length;i++) {
			TableItem ti=cursel[i];
			Object o=ti.getData();
			inviterdata invdat=(inviterdata)o;
			if (invdat.outginvatationProc==null ||
				(invdat.outginvatationProc.getState()==State.INITIALIZED || 
				invdat.outginvatationProc.getState()==State.SYNCHRONIZING_DONE ||
				invdat.outginvatationProc.getState()==State.CANCELED ||
				invdat.outginvatationProc.getState()==State.DONE
				) )
				return false;
		}
		return true;
	}
	
	void cancelInvite() {
		TableItem[] cursel = table.getSelection();
		for (int i=0;i<cursel.length;i++) {
			TableItem ti=cursel[i];
			Object o=ti.getData();
			inviterdata invdat=(inviterdata)o;
			if (invdat.outginvatationProc==null)
				continue;
			
			invdat.outginvatationProc.cancel(null, false);
		}
		updateInvitationProgress(null);
	}


    boolean isJIDinList(ArrayList<JID> items, JID jid) {
    	for (int i=0;i<items.size();i++) {

    		try {
    		JID curJID = items.get(i);
    		
    		if (curJID.equals(jid))
    			return true;
    		
    		} catch(Exception e) {
    			System.out.println(e);
    		}
    	}
    	
    	return false;
    }
    
	static final String[] StateNames = {
		"Initialized",
		"Invitation sent. Waiting for acknowledgement...",
		"Filelist of inviter requested",
		"Filelist of inviter sent",
		"Filelist of invitee sent",
		"Synchornizing project files. Transfering files...",
		"Files sent. Waiting for invitee...", 
		"Invitiation completed",
		"Invitation canceled"
	};
    private String getStateDesc(IInvitationProcess.State state){
    	return StateNames[state.ordinal()];
    }
    
    public void showErrorMessage(String errorMessage) {
    	ErrorMessageDialog.showErrorMessage(errorMessage);
    }
    
	public void cancel(String errorMsg, boolean replicated) {
		updateInvitationProgress(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess.IInvitationUI
	 */
	public void runGUIAsynch(final Runnable runnable) {

		display.asyncExec(new Runnable() {
			public void run() {
				new Thread(runnable).start();
		}} );	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.listeners.IConnectionListener
	 */
	public void connectionStateChanged(XMPPConnection connection, final ConnectionState newState) {
		
		if (newState == ConnectionState.CONNECTED) {
			roster = Saros.getDefault().getRoster();
			attachRosterListener();

		} else if (newState == ConnectionState.NOT_CONNECTED) {
			roster = null;
		}

		refreshRosterListRunASync(null);

	}
	
	private void attachRosterListener() {
		roster.addRosterListener(new RosterListener() {
			public void entriesAdded(Collection<String> addresses) {
				refreshRosterList();
			}

			public void entriesUpdated(Collection<String> addresses) {
				refreshRosterList();
			}

			public void entriesDeleted(Collection<String> addresses) {
				refreshRosterList();
			}

			public void presenceChanged(String XMPPAddress) {
				refreshRosterList();
			}
			
			
			public void presenceChanged(Presence presence) {
				//TODO: new Method for Smack 3
				presenceChanged(presence.getFrom());
				
			}
		});
	}
	
	/*
	 * Triggers the refresh of the roster list in a GUI thread.
	 */
	private void refreshRosterList() {
		if (inviteStep!=InvState.SELECTION)
			return;
		
		display.asyncExec(new Runnable() {
			public void run() {
				refreshRosterListRunASync(null);
		}} );	
	}
	
	/*
	 * Clears and re-filles the table with all online users from my roster.
	 * Selections are preserved.
	 */
	private void refreshRosterListRunASync(JID toselect) {
		ArrayList<JID> curselA = new ArrayList<JID>();
		int[] curselNew=null;

		// save selection
		TableItem[] curselTIs = table.getSelection();
		for (int i=0;i<curselTIs.length;i++)
			curselA.add( ((inviterdata)curselTIs[i].getData()).jid );

		input.clear();
		table.removeAll();
		
		if (roster==null)
			return;
		
		//TODO: Änderung für smack 3
//		Iterator iter = roster.getEntries();
		Collection<RosterEntry> users = roster.getEntries();
		int index=-1;
//	    while (iter.hasNext()) {
//	        RosterEntry entry = (RosterEntry) iter.next();
		for(RosterEntry entry : users){
			
	        String username = entry.getUser();
	        Presence presence = roster.getPresence(username); 

	        User user = Saros.getDefault().getSessionManager().
	        	getSharedProject().getParticipant(new JID(entry.getUser()));
	        /*TODO: Änderung: presence.type available hinzugefügt.
	         * 		Es ist hier jedoch zu prüfen, inwieweit auch offline user eingeladen werden könnten.
	         * */
	        if (presence != null && presence.getType().equals(Presence.Type.available)&& user==null )
	        {
	        	inviterdata invdat = new inviterdata();
	        	invdat.jid= new JID(entry.getUser());
	        	invdat.name=entry.getName();
	        	invdat.outginvatationProc=null;
	 
	        	input.add(invdat);
	        	index++;
	        	
	        	if ( (autoinviteJID!=null && invdat.jid.equals(autoinviteJID) ) ||
	        	     (autoinviteJID==null && isJIDinList(curselA, invdat.jid) )  )
	        	{
	        		int curselOld[]=table.getSelectionIndices();
	        		curselNew=new int[curselOld.length+1];
	        		System.arraycopy(curselOld, 0, curselNew, 0, curselOld.length);
	        		curselNew[curselNew.length-1]=index;
	        	}
	        }
	    }			
	    		    
	    tableviewer.refresh();

	    if (curselNew!=null)
	    	table.setSelection(curselNew);
	}

}
