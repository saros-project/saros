/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universit�t Berlin - Fachbereich Mathematik und Informatik - 2006
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
package de.fu_berlin.inf.dpp.project.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.IInvitationUI;
import de.fu_berlin.inf.dpp.invitation.internal.OutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.net.IActivitySequencer;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.project.IActivityManager;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.InvitationDialog;

public class SharedProject implements ISharedProject {
	private static Logger log = Logger.getLogger(SharedProject.class.getName());

	private static final int REQUEST_ACTIVITY_ON_AGE = 5;
	protected static final int MILLIS_UPDATE = 50;

	protected JID myID;

	protected List<User> participants = new ArrayList<User>();

	private IProject project;

	private List<ISharedProjectListener> listeners = new ArrayList<ISharedProjectListener>();

	private User driver;

	private User host;

	private final ITransmitter transmitter;

	private ActivitySequencer activitySequencer = new ActivitySequencer();

	private static final int MAX_USERCOLORS = 5;
	private int colorlist[] = new int[MAX_USERCOLORS +1];

	
	public SharedProject(ITransmitter transmitter, IProject project, JID myID) { // host
		this.transmitter = transmitter;
		
 
		this.myID = myID;
		driver = host = new User(myID);
		participants.add(driver);

		this.project = project;
		setProjectReadonly(false);
	}

	public SharedProject(ITransmitter transmitter, IProject project, JID myID, // guest
		JID host, JID driver, List<JID> allParticipants) {

		this.transmitter = transmitter;

		this.myID = myID;

		this.host = new User(host);
		this.driver = new User(driver);
		
		for (JID jid : allParticipants) { // HACK
			User user=new User(jid);
			participants.add(user);
			assignColorId(user);
		}

		this.project = project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.ISharedProject
	 */
	public List<User> getParticipants() {
		return participants;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.ISharedProject
	 */
	public IActivitySequencer getSequencer() {
		return activitySequencer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISharedProject
	 */
	public IActivityManager getActivityManager() {
		return activitySequencer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.ISharedProject
	 */
	public void setDriver(User driver, boolean replicated) {
		assert driver != null;

		// TODO if replicated=false check for privileges

		if (driver.equals(this.driver))
			return;

		this.driver = driver;
		
		setProjectReadonly(!isDriver());

		JID jid = driver.getJid();
		for (ISharedProjectListener listener : listeners) {
			listener.driverChanged(jid, replicated);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.ISharedProject
	 */
	public User getDriver() {
		return driver;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.ISharedProject
	 */
	public boolean isDriver() {
		return driver.getJid().equals(myID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.ISharedProject
	 */
	public User getHost() {
		return host;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISharedProject
	 */
	public boolean isHost() {
		return host.getJid().equals(myID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISharedProject
	 */
	public void addUser(User user) {
		addUser(user,-1);
	}
	public void addUser(User user, int index) {
		if (participants.contains(user)) {
			if (index>=0 && participants.indexOf(user)!=index) {
				participants.remove(user);
				participants.add(index, user);				
			}
			return;
		}

		participants.add(user);

		// find free color and assign it to user
		assignColorId(user);
		
		for (ISharedProjectListener listener : listeners) {
			listener.userJoined(user.getJid());
		}

		log.info("User " + user.getJid() + " joined session");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISharedProject
	 */
	public void removeUser(User user) {
		participants.remove(user);

		// free colorid
		colorlist[user.getColorID()]=0;

		if (driver.equals(user)) {
			setDriver(participants.get(0), true);
		}

		for (ISharedProjectListener listener : listeners) {
			listener.userLeft(user.getJid());
		}
		
		log.info("User " + user.getJid() + " left session");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISharedProject
	 */
	public IOutgoingInvitationProcess invite(JID jid, String description, boolean inactive, IInvitationUI inviteUI) {
		return new OutgoingInvitationProcess(transmitter, jid, this, description, inactive, inviteUI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISharedProject
	 */
	public void addListener(ISharedProjectListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISharedProject
	 */
	public void removeListener(ISharedProjectListener listener) {
		listeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISharedProject
	 */
	public IProject getProject() {
		return project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISharedProject
	 */
	public FileList getFileList() throws CoreException {
		return new FileList(project);
	}

	public Timer flushTimer = new Timer(true);
	private static int queuedsince=0;
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISharedProject
	 */
	public void start() {

		flushTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (participants.size() <= 1) {
					activitySequencer.flush();

				} else {
					List<TimedActivity> activities = activitySequencer.flushTimed();

					if (activities != null)
						transmitter.sendActivities(SharedProject.this, activities);
				}
				
				// missing activities? (cant execute all)
				if ( activitySequencer.getQueuedActivities()>0) {
					queuedsince++;

					// if i am missing activities for REQUEST_ACTIVITY_ON_AGE seconds, ask all (because I dont know the origin)
					// to send it to me again.
					if (queuedsince >= REQUEST_ACTIVITY_ON_AGE ) {

						transmitter.sendRequestForActivity( SharedProject.this, 
								activitySequencer.getTimestamp() , 
								false);
					
						queuedsince=0;
						
						// TODO: forever?
					}
					
				} else
					queuedsince=0;
			}
		}, 0, MILLIS_UPDATE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISharedProject
	 */
	public void stop() {
		flushTimer.cancel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.fu_berlin.inf.dpp.project.ISharedProject
	 */
	public User getParticipant(JID jid) {
		for (User participant : participants) {
			if (participant.getJid().equals(jid)) {
				return participant;
			}
		}

		return null;
	}

	boolean assignColorId(User user){
		
		// already has a color assigned
		if (user.getColorID()==-1)
			return true;
		
		for (int i=0;i<MAX_USERCOLORS;i++){
			if (colorlist[i]==0) {
				user.setColorID(i);
				colorlist[user.getColorID()]=1;
				return true;
			}
		}
		
		return false;		
	}
	
	public void startInvitation(final JID jid) {
		
		Shell shell = Display.getDefault().getActiveShell();
		
		if (searchUnsavedChangesInProject(false))
		{
			if (MessageDialog.openQuestion(shell, "Unsaved file modifications",
					"Before inviting users and therefore synchronizing files, "+
					"this project needs to be saved to disk. "+
					"Do you want to save all unsaved files of this project now?")) {
				
				// save
				// PlatformUI.getWorkbench().saveAllEditors(false);	// saves all editors
				searchUnsavedChangesInProject(true);
				
			} else
				return;
		}

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					Shell shell = Display.getDefault().getActiveShell();
					Window iw = new InvitationDialog(shell,jid);
					iw.open();
				} catch (Exception e) {
					Saros.getDefault().getLog().log(
						new Status(IStatus.ERROR, Saros.SAROS, IStatus.ERROR,
							"Error while running invitation helper", e));
				}
			}
		});

	}

	boolean searchUnsavedChangesInProject(boolean save) {
		FileList flist=null;
		
		try {
			flist = new FileList(getProject());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		
		try {
			IWorkbenchWindow[] wbWindows = PlatformUI.getWorkbench().getWorkbenchWindows();
			for (IWorkbenchWindow window : wbWindows) {
				IWorkbenchPage activePage = window.getActivePage();
				IEditorReference[] editorRefs = activePage.getEditorReferences();
				for (IEditorReference editorRef : editorRefs) {
					if (editorRef.isDirty() && editorRef.getEditorInput() instanceof IFileEditorInput) {
							
						IPath fp = ((IFileEditorInput)editorRef.getEditorInput()).
									getFile().getProjectRelativePath();
						
						// is that dirty file in my project?
						if (flist.getPaths().contains(fp)) {
							if (save)
								editorRef.getEditor(false).doSave(null);
							else 
								return true;
						}
					}
				}
			}
		} catch (CoreException e1) {
			System.out.println(e1.getMessage());
		}
			
		return false;
	}

	public void setProjectReadonly(boolean readonly) {
		FileList flist=null;

		try {
			flist = new FileList( project );
			ResourceAttributes attributes = new ResourceAttributes();
			attributes.setReadOnly(readonly);
			attributes.setArchive (readonly);
	
			for (int i=0;i<flist.getPaths().size(); i++) {
				IPath path = flist.getPaths().get(i);
				path = path.makeAbsolute();
				IFile file = getProject().getFile(path);
				if (file!=null && file.exists()) {
					file.setResourceAttributes(attributes);
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}
}
