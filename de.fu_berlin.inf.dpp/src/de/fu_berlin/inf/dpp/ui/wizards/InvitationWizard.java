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

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
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
import de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess.IInvitationUI;

public class InvitationWizard extends Wizard implements IInvitationUI {

	private List 	inviteList;
	private Roster 	roster=Saros.getDefault().getRoster();

	private class InvitePage extends WizardPage implements IConnectionListener {
		Display display = null;//new Display();	

		protected InvitePage() {
			super("invitation");

			setTitle("Invitation Wizard");
			setDescription("A project was shared. Now you can select and invite users from your rosters to join this session. You can also invite users later by your roster.");
		}

		public void createControl(Composite parent) {
			display=Display.getCurrent();
			
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout(1, false));

			Label usersLabel = new Label(composite, SWT.NONE);
			//usersLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
			usersLabel.setText("Select users to invite:");

			inviteList = new List(composite, SWT.MULTI | SWT.BORDER);
			inviteList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			// get online users from roster
			attachRosterListener();
			refreshRosterList();
			
			inviteList.addSelectionListener(new SelectionListener() {
		        public void widgetSelected(SelectionEvent event) {
	        		setPageComplete( (inviteList.getSelectionCount()>0) );
		        }
		        public void widgetDefaultSelected(SelectionEvent event) {
		        }
			} ) ;

			setControl(composite);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.fu_berlin.inf.dpp.listeners.IConnectionListener
		 */
		public void connectionStateChanged(XMPPConnection connection, final ConnectionState newState) {
			System.out.println("xmpconnection changed to:"+newState.toString());
			
			if (newState == ConnectionState.CONNECTED) {
				roster = Saros.getDefault().getRoster();
				attachRosterListener();

			} else if (newState == ConnectionState.NOT_CONNECTED) {
				roster = null;
				setPageComplete( false );
			}

			refreshRosterList();

		}
		
		private void attachRosterListener() {
			roster.addRosterListener(new RosterListener() {
				public void entriesAdded(Collection addresses) {
					refreshRosterListRunASync();
				}

				public void entriesUpdated(Collection addresses) {
					refreshRosterListRunASync();
				}

				public void entriesDeleted(Collection addresses) {
					refreshRosterListRunASync();
				}

				public void presenceChanged(String XMPPAddress) {
					refreshRosterListRunASync();
				}
			});
		}
		
		private void refreshRosterListRunASync() {
			display.asyncExec(new Runnable() {
				public void run() {
				refreshRosterList();
			}} );	
		}
		private void refreshRosterList() {
			String[] cursel = inviteList.getSelection();
			
			inviteList.removeAll();
			
			if (roster==null)
				return;
			
			Iterator iter = roster.getEntries();
		    while (iter.hasNext()) {
		        RosterEntry entry = (RosterEntry) iter.next();
		        String username = entry.getUser();
		        Presence presence = roster.getPresence(username); 
		        if (presence != null )    // && presence.getType() == Presence.Type.AVAILABLE)
		        {
		        	inviteList.add(entry.getName());
		        	inviteList.setData(entry.getName(), entry.getUser() );
		        }
		    }			
		    inviteList.setSelection(cursel);
		}
	}

	private InvitePage page = new InvitePage();

	public InvitationWizard() {
		setWindowTitle("Invite users");
		setHelpAvailable(false);
	}

	@Override
	public void addPages() {
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		
		try {	
			String[] cursel = inviteList.getSelection();

			ISharedProject project = Saros.getDefault().getSessionManager().getSharedProject();
			String name = project.getProject().getName();
	
			for (int i=0; i < cursel.length; i++) {
				String id   = inviteList.getData(cursel[i]).toString();
				JID jid=new JID(id);
				IOutgoingInvitationProcess process = project.invite(jid, name);
				process.setInvitationUI(this);
			}
			return true;

		} catch (Exception e) {
			page.setMessage(e.getMessage(), IMessageProvider.ERROR);
			e.printStackTrace();
		}

		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess.IInvitationUI
	 */
	public void runWithProgressBar(final IRunnableWithProgress runnable) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					IWorkbench wb = PlatformUI.getWorkbench();
					IProgressService ps = wb.getProgressService();
					ps.run(true, true, runnable);

				} catch (InvocationTargetException e) {
					e.printStackTrace();

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}	
}
