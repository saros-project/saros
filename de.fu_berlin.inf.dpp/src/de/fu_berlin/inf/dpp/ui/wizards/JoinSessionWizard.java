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
package de.fu_berlin.inf.dpp.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.IInvitationUI;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.State;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * A wizard that guides the user through an incoming invitiation process.
 * 
 * Todo: - Enhance Usability of this dialog: - Automatically switch to follow
 * mode - Make a suggestion for the name of the project - Suggest if the project
 * is a CVS project that the user checks it out and offers an option to transfer
 * the settings
 * 
 * @author rdjemili
 */
public class JoinSessionWizard extends Wizard implements IInvitationUI {
	private static Logger log = Logger.getLogger(JoinSessionWizard.class.getName());

	private ShowDescriptionPage descriptionPage;
	
    private WizardDialogAccessable myWizardDlg;
	
	private EnterNamePage namePage;

	private final IIncomingInvitationProcess process;
	
	private Display display = null;

	/**
	 * A wizard page that displays the name of the inviter and the description
	 * provided with the invitation.
	 */
	private class ShowDescriptionPage extends WizardPage {
		protected ShowDescriptionPage() {
			super("firstPage");

			setTitle("Session Invitation");
			setDescription("You have been invited to join on a session for a "
				+ "shared project. Click next if you want to accept the invitation.");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.IDialogPage
		 */
		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout(2, false));

			Label inviterLabel = new Label(composite, SWT.NONE);
			inviterLabel.setText("Inviter");

			Text inviterText = new Text(composite, SWT.READ_ONLY | SWT.SINGLE | SWT.BORDER);

			inviterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			inviterText.setText(process.getPeer().getBase());

			Label descriptionLabel = new Label(composite, SWT.NONE);
			descriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
			descriptionLabel.setText("Project");

			Text descriptionText = new Text(composite, SWT.READ_ONLY | SWT.BORDER);
			descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			descriptionText.setText(process.getDescription());

			setControl(composite);
		}
	}

	/**
	 * A wizard page that allows to enter the new project name or to choose to
	 * overwrite the project selected by the {@link ProjectSelectionPage}.
	 */
	private class EnterNamePage extends WizardPage {

		private Text newProjectNameText;
		private Button projUpd;
		
		
		protected EnterNamePage() {
			super("namePage");
			setPageComplete(false);

			setTitle("Session Invitation");
			setDescription("Enter the name of the new project.");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.IDialogPage
		 */
		public void createControl(Composite parent) {

			if ( process.getState()==State.CANCELED)
				return;

			requestRemoteFileList();
			
			if (process.getRemoteFileList() == null)
				getShell().close();

			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout(2, false));

			GridData gridData;
			
			IProject project = getLocalProject();

			Label helpLabel = new Label(composite, SWT.WRAP);
			helpLabel.setText(getHelpText(project));
			helpLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));

			Button projCopy = new Button(composite, SWT.RADIO);
			projCopy.setText("Create new project copy");
			projCopy.setSelection(true);
			gridData = new GridData(SWT.FILL, SWT.BEGINNING, false, false,2,1);
			gridData.verticalIndent = 20;
			projCopy.setLayoutData(gridData);
			
			Label newProjectNameLabel = new Label(composite, SWT.NONE);
			newProjectNameLabel.setText("Project name");
			gridData = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
			gridData.verticalIndent = 3;
			newProjectNameLabel.setLayoutData(gridData);

			newProjectNameText = new Text(composite, SWT.BORDER);
			gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
			gridData.verticalIndent = 1;
			newProjectNameText.setLayoutData(gridData);
			newProjectNameText.setFocus();
			newProjectNameText.setText(findProjectNameProposal());
			
			projUpd = new Button(composite, SWT.RADIO);
			projUpd.setText("Update and use existing project");
			if (project==null)
				projUpd.setEnabled(false);
			else
				projUpd.setText("Update and use existing project ("+ project.getName() +")");
			gridData = new GridData(SWT.FILL, SWT.BEGINNING, false, false,2,1);
			gridData.verticalIndent = 20;
			projUpd.setLayoutData(gridData);

			attachListeners();
			setControl(composite);

			updatePageComplete();
		}

		/**
		 * @return the project name of the project that should be created or
		 *         <code>null</code> if the user chose to overwrite an
		 *         existing project.
		 */
		public String getNewProjectName() {
			return projUpd.getSelection()?null:newProjectNameText.getText();
		}

		private IProject getLocalProject() {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IProject[] projects = workspace.getRoot().getProjects();

			int maxMatch = 0;
			IProject selectedProject = null;
			for (int i = 0; i < projects.length; i++) {
				if (!projects[i].isOpen())
					continue;

				int match = getMatch(projects[i]);

				if (match > maxMatch) {
					maxMatch = match;
					selectedProject = projects[i];
				}
			}

			return selectedProject;
		}

		private int getMatch(IProject project) {
			try {
				FileList remoteFileList = process.getRemoteFileList();
				return remoteFileList.match(new FileList(project));
			} catch (CoreException e) {
				log.log(Level.FINE, "Couldn't calculate match for project " + project, e);

				return 0;
			}
		}

		private void requestRemoteFileList() {
			try {
				getContainer().run(true, true, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) {

						process.requestRemoteFileList(monitor);
					}
				});

			} catch (InvocationTargetException e) {
				log.log(Level.WARNING, "Exception while requesting remote file list", e);

			} catch (InterruptedException e) {
				log.log(Level.FINE, "Request of remote file list canceled/interrupted", e);
			}
		}

		private String getHelpText(IProject project) {
			if (project == null) {
				return "Project replication will start from scratch.";
			}

			return "It has been detected that one of your local projects (" + project.getName()
				+ ") has an identicallness of " + getMatch(project) + "%.\n"
				+ "This fact will used to shorten the process of "
				+ "replicating the remote project.";
		}

		private void attachListeners() {
			newProjectNameText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					updatePageComplete();
				}
			});
		}

		private void updatePageComplete() {

			String newText = newProjectNameText.getText();

			if (newText.length() == 0) {
				setMessage(null);
				setErrorMessage("Please set a project name");
				setPageComplete(false);
				
			} else {
				if (projectIsUnique(newText)) {
					setMessage(null);
					setErrorMessage(null);
					setPageComplete(true);
					
				} else {
					setMessage(null);
					setErrorMessage("A project with this name already exists");
					setPageComplete(false);
				}
			}
		}
	}

	public JoinSessionWizard(IIncomingInvitationProcess process) {
		this.process = process;

		setWindowTitle("Session Invitation");
		setHelpAvailable(false);
		setNeedsProgressMonitor(true);
		display=Display.getCurrent();
	}

	public boolean projectIsUnique(String name) {

		// Then check with all the projects
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = workspace.getRoot().getProjects();

		return projectIsUnique(name, projects);
	}

	public boolean projectIsUnique(String name, IProject[] projects) {

		for (int i = 0; i < projects.length; i++) {
			IProject p = projects[i];
			if (p.getName().equals(name))
				return false;
		}
		return true;
	}

	public String findProjectNameProposal() {
		// Start with the projects name
		String projectProposal = process.getProjectName();

		// Then check with all the projects
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = workspace.getRoot().getProjects();

		if (projectIsUnique(projectProposal, projects)) {
			return projectProposal;
			
		} else {
			// Name is already in use!
			Pattern p = Pattern.compile("^(.*)(\\d+)$");
			Matcher m = p.matcher(projectProposal);

			int i;
			// Check whether the name ends in a number or not
			if (m.find()) {
				projectProposal = m.group(1).trim();
				i = Integer.parseInt(m.group(2));
			} else {
				i = 2;
			}

			// Then find the next available number
			while (!projectIsUnique(projectProposal + " " + i, projects)) {
				i++;
			}

			return projectProposal + " " + i;
		}
	}

	@Override
	public void addPages() {
		descriptionPage = new ShowDescriptionPage();
		namePage = new EnterNamePage();

		addPage(descriptionPage);
		addPage(namePage);
	}

	@Override
	public void createPageControls(Composite pageContainer) {
		descriptionPage.createControl(pageContainer);
		// create namePage lazily
	}

	@Override
	public boolean performFinish() {
		
		if ( process.getState()==State.CANCELED)
			return true;
		
		final IProject project = namePage.getLocalProject();
		final String newProjectName = namePage.getNewProjectName();

		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
					InterruptedException {

					process.accept(project, newProjectName, monitor);
				}
			});
		} catch (InvocationTargetException e) {
			log.log(Level.WARNING, "Exception while requesting remote file list", e);
		} catch (InterruptedException e) {
			log.log(Level.FINE, "Request of remote file list canceled/interrupted", e);
		}

		return true;
	}

	@Override
	public boolean performCancel() {
		process.cancel("Cancel Invitation Process!", false);

		return super.performCancel();
	}

	public void cancel(final String errorMsg, final boolean replicated) {
		display.asyncExec(new Runnable() {
			public void run() {
				cancelRunASync(errorMsg, replicated);
		}} );	
	}

	private void cancelRunASync(String errorMsg, boolean replicated){
		if (replicated) {
			if (errorMsg != null) {
				MessageDialog.openError(getShell(), "Invitation aborted",
					"Could not complete invitation. ("+ errorMsg + ")");

			} else {
				MessageDialog.openInformation(getShell(), "Invitation cancelled",
					"Invitation was cancelled by peer.");
			}
		}
		myWizardDlg.setWizardButtonEnabled(IDialogConstants.BACK_ID, false);
		myWizardDlg.setWizardButtonEnabled(IDialogConstants.NEXT_ID, false);
		myWizardDlg.setWizardButtonEnabled(IDialogConstants.FINISH_ID, false);
	}

	public void updateInvitationProgress(JID jid) {
		// ignored, not needed atm
	}

	public void setWizardDlg(WizardDialogAccessable wd) {
    	myWizardDlg=wd;
	}

	public void runGUIAsynch(Runnable runnable) {
		// TODO Auto-generated method stub
		
	}
}
