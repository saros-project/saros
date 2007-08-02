/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
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
package de.fu_berlin.inf.dpp.ui.wizards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.IInvitationUI;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.State;
import de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess;

/**
 * An wizard that invites a number of peers to a shared project.
 * Currently with just one page but implemented as wizard for further enhancements. 
 * 
 * @author gustavs
 */
public class InvitationWizard extends Wizard implements IInvitationUI {
	private static enum InvState {
		SELECTION, INVITING, DONE
	}

	private TableViewer tableviewer;
    private Table 		table;
    private ArrayList<inviterdata> 	input;
    private Button		cancelButton;
    private WizardDialogAccessable myWizardDlg;
    
	private Roster 	roster=Saros.getDefault().getRoster();
	private InvState inviteStep		= InvState.SELECTION;
	private JID 	autoinviteJID	= null;
	private Display display 		= null;

	
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
	
	// assigned to any of the entries of the invite-tableview
	private class inviterdata {
		JID 	jid;
		String 	name;
		IOutgoingInvitationProcess outginvatationProc;
	}

	
	/*
	 * Implements a tableview filled with all online roster contacts to be selected
	 * and invited to the shared project.
	 */
	private class InvitePageSelection extends WizardPage implements IConnectionListener {
		
		protected InvitePageSelection() {
			super("invitationSelection");

			setTitle("Invitation Wizard");
			setDescription("A project is shared. Now you can select and invite users from your rosters to join this session. You can also invite users later by your roster.");
		}

		public void createControl(Composite parent) {
			
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout(1, false));

			Label usersLabel = new Label(composite, SWT.NONE);
			usersLabel.setText("Select users to invite:");

		    tableviewer = new TableViewer(composite);
		    table = tableviewer.getTable();
		    table.setLinesVisible(true);
		    tableviewer.setContentProvider(new ArrayContentProvider());
		    tableviewer.setLabelProvider(new MyLabelProvider());
		    table.setHeaderVisible(true);
		    TableColumn column = new TableColumn(table,SWT.NONE);
		    column.setText("User");
		    column.setWidth(150);
		    column = new TableColumn(table,SWT.NONE);
		    column.setText("Status");
		    column.setWidth(300);
		    column = new TableColumn(table,SWT.NONE);
		    column.setText("Action");
		    column.setWidth(200);
		    table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		    
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
	        			setPageComplete( (table.getSelectionCount()>0) );
	        		else if (inviteStep==InvState.INVITING)
	        			cancelButton.setEnabled(isSelectionCancelable());
	        		else
	        			cancelButton.setEnabled(false);
		        }
		        public void widgetDefaultSelected(SelectionEvent event) {
		        }
			} ) ;
			
		    setPageComplete(false);
		    cancelButton.setVisible(false);

		    // get online users from roster
			if (autoinviteJID==null)
				attachRosterListener();
			refreshRosterListRunASync(autoinviteJID);
			
			// auto trigger automatic invite
			if (autoinviteJID!=null)
				performFinish();

			myWizardDlg.setWizardButtonLabel(IDialogConstants.FINISH_ID, "Invite");
			myWizardDlg.setWizardButtonLabel(IDialogConstants.CANCEL_ID, "Close");
			
			setControl(composite);
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
				setPageComplete( false );
			}

			refreshRosterListRunASync(null);

		}
		
		private void attachRosterListener() {
			roster.addRosterListener(new RosterListener() {
				public void entriesAdded(Collection addresses) {
					refreshRosterList();
				}

				public void entriesUpdated(Collection addresses) {
					refreshRosterList();
				}

				public void entriesDeleted(Collection addresses) {
					refreshRosterList();
				}

				public void presenceChanged(String XMPPAddress) {
					refreshRosterList();
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
			
			Iterator iter = roster.getEntries();
			int index=-1;
		    while (iter.hasNext()) {
		        RosterEntry entry = (RosterEntry) iter.next();
		        String username = entry.getUser();
		        Presence presence = roster.getPresence(username); 
		        if (presence != null )
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
		
	}	// page1
	
	
	
	private InvitePageSelection pageSel = new InvitePageSelection();

	public InvitationWizard(JID jid) {
		display=Display.getCurrent();

		setWindowTitle("Invite users");
		setHelpAvailable(false);
		autoinviteJID=jid;
	}

	@Override
	public void addPages() {
		addPage(pageSel);
	}
	
	@Override
	/*
	 * Performs the invitation itself, sends an invite to all selected users.
	 */
	public boolean performFinish() {
		
		if (inviteStep==InvState.DONE) // Close wizard
			return true;
		
		inviteStep=InvState.INVITING;
		pageSel.setPageComplete(false);
		cancelButton.setVisible(true);
		myWizardDlg.setWizardButtonEnabled(IDialogConstants.CANCEL_ID, false);
		
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
			inviteStep=InvState.DONE;
			myWizardDlg.setWizardButtonEnabled(IDialogConstants.CANCEL_ID, true);
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
			
			invdat.outginvatationProc.cancel("Canceled by inviter", false);
		}
		updateInvitationProgress(null);
	}


    boolean isJIDinList(ArrayList items, JID jid) {
    	for (int i=0;i<items.size();i++) {

    		try {
    		JID curJID= (JID)items.get(i);
    		
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
    
    public void setWizardDlg(WizardDialogAccessable wd) {
    	myWizardDlg=wd;
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

}
