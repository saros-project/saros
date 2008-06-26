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

import java.io.File;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.WizardProjectsImportPage;
import org.eclipse.ui.wizards.datatransfer.WizardExternalProjectImportPage;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.IInvitationUI;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.State;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.TransferMode;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.ui.SarosUI;

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
			setImageDescriptor(SarosUI.getImageDescriptor("icons/start_invitation.png"));
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

		private String COPY_CHECKBOX_MESSAGE = "Copy files from existing project";
		
		private Label newProjectNameLabel;
		private Button projCopy;
		private Text newProjectNameText;
		private Button copyCheckbox;
		private Button browseCreateProjectButton;
		
		private Button projUpd;
		private Text updateProjectText;
		private Button browseUpdateProjectButton;
		
		
		private Label updateProjectStatusResult;
		private Label updateProjectNameLabel;
		private Button scanWorkspaceProjectsButton;
		
		/* project for update or base project for copy into new project*/
		private IProject simularProject;
		/* variable for scan process. */
		private boolean scanRun;
		
		
		protected EnterNamePage() {
			super("namePage");
			setPageComplete(false);
			
			setTitle("Select local project.");
			
			/*
			 * set connection status information.
			 */
			checkConnectionStatus();
		}

		protected void setUpdateProject(IProject project){
			this.simularProject = project;
		}
		
		/**
		 * get transfer mode and set header information of the wizard.
		 */
		private void checkConnectionStatus(){
			if(process.getTransferMode() == TransferMode.IBB){
				setDescription("Attention: No direct connection avialable!"+'\n'+ "Suggestion: Update an existing project or copy resources from another project.");
				setImageDescriptor(SarosUI.getImageDescriptor("icons/ibb_connection.png"));
			}else{
				setDescription("P2P Connection with Jingle available.");
				setImageDescriptor(SarosUI.getImageDescriptor("icons/jingle_connection.png"));
			}
		}
		
		/**
		 * create components of create new project area for enternamepage wizard.
		 * @param workArea composite of appropriate wizard
		 */
		private void createNewProjectGroup(Composite workArea){
			/*
			 * set connection status information.
			 */
			checkConnectionStatus();
			
			Composite projectGroup = new Composite(workArea, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 3;
			layout.makeColumnsEqualWidth = false;
			layout.marginWidth = 0;
			projectGroup.setLayout(layout);
			projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			newProjectNameLabel = new Label(projectGroup, SWT.NONE);
			newProjectNameLabel.setText("Project name");
//			newProjectNameLabel.setLayoutData(new GridData(
//					GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

			newProjectNameText = new Text(projectGroup, SWT.BORDER);
			newProjectNameText.setLayoutData(new GridData(
					GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
			newProjectNameText.setFocus();
			newProjectNameText.setText(findProjectNameProposal());
			
		}
		
		/**
		 * create components of option area for enternamepage wizard.
		 * @param workArea composite of appropriate wizard
		 */
		private void createOptionArea(Composite workArea) {
			
			Composite optionsGroup = new Composite(workArea, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginLeft = 20;
			layout.makeColumnsEqualWidth = false;
			layout.marginWidth = 0;
			
			optionsGroup.setLayout(layout);
			optionsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			copyCheckbox = new Button(optionsGroup, SWT.CHECK);
			copyCheckbox
					.setText(COPY_CHECKBOX_MESSAGE);
			copyCheckbox.setSelection(true);
			copyCheckbox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			copyCheckbox.setEnabled(false);
			
			browseCreateProjectButton = new Button(optionsGroup, SWT.PUSH);
			browseCreateProjectButton.setText("Browse");
			browseCreateProjectButton.setToolTipText("Select project for copy local resources into new project.");
			setButtonLayoutData(browseCreateProjectButton);
			browseCreateProjectButton.setEnabled(true);
			browseCreateProjectButton.addSelectionListener(new SelectionAdapter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.SelectionAdapter#widgetS
				 *      elected(org.eclipse.swt.events.SelectionEvent)
				 */
				public void widgetSelected(SelectionEvent e) {
					getProjectForCopyResourcesButtonPressed();
				}

			});
			
		}
		
		/**
		 * create components of update area for enternamepage wizard.
		 * @param workArea composite of appropriate wizard
		 */
		private void createUpdateProjectGroup(Composite workArea) {
			Composite projectGroup = new Composite(workArea, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 4;
			layout.makeColumnsEqualWidth = false;
			layout.marginWidth = 0;
			projectGroup.setLayout(layout);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.verticalIndent = 10;
			projectGroup.setLayoutData(data);
			
			updateProjectNameLabel = new Label(projectGroup, SWT.NONE);
			updateProjectNameLabel.setText("Project name");
//			gridData = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
//			gridData.verticalIndent = 3;
			updateProjectNameLabel.setEnabled(false);

			updateProjectText = new Text(projectGroup, SWT.BORDER);
			updateProjectText.setLayoutData(new GridData(
					GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
			updateProjectText.setFocus();
			updateProjectText.setEnabled(false);
			updateProjectText.setText("");
			
			browseUpdateProjectButton = new Button(projectGroup, SWT.PUSH);
			browseUpdateProjectButton.setText("Browse");
			setButtonLayoutData(browseUpdateProjectButton);
			browseUpdateProjectButton.setEnabled(false);
			browseUpdateProjectButton.addSelectionListener(new SelectionAdapter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.SelectionAdapter#widgetS
				 *      elected(org.eclipse.swt.events.SelectionEvent)
				 */
				public void widgetSelected(SelectionEvent e) {
					handleLocationProjectButtonPressed();
				}

			});

		}

		
		/**
		 * browse dialog to select project to update.
		 */
		private void handleLocationProjectButtonPressed() {
			DirectoryDialog dialog = new DirectoryDialog(updateProjectText.getShell());
			dialog.setMessage("Select project for update.");

			String dirName = updateProjectText.getText().trim();
			

			if (dirName.length() == 0) {
				dialog.setFilterPath(IDEWorkbenchPlugin.getPluginWorkspace()
						.getRoot().getLocation().toOSString());
			} else {
				File path = new File(dirName);
				if (path.exists()) {
					dialog.setFilterPath(new Path(dirName).toOSString());
				}
			}

			String selectedDirectory = dialog.open();
			if (selectedDirectory != null) {
				updateProjectText.setText(selectedDirectory.substring(selectedDirectory.lastIndexOf(File.separator)+1));
			}
			
		}
		
		/**
		 * browse dialog to select project for copy.
		 */
		private void getProjectForCopyResourcesButtonPressed() {
			DirectoryDialog dialog = new DirectoryDialog(updateProjectText.getShell());
			dialog.setMessage("Select project for copy.");

			String dirName = "";

			if (dirName.length() == 0) {
				dialog.setFilterPath(IDEWorkbenchPlugin.getPluginWorkspace()
						.getRoot().getLocation().toOSString());
			} else {
				File path = new File(dirName);
				if (path.exists()) {
					dialog.setFilterPath(new Path(dirName).toOSString());
				}
			}

			String selectedDirectory = dialog.open();
			if (selectedDirectory != null) {
				updateBaseProject(selectedDirectory.substring(selectedDirectory.lastIndexOf(File.separator)+1));
//				updateProjectText.setText(selectedDirectory.substring(selectedDirectory.lastIndexOf(File.separator)+1));

			}
			
		}
		
		/**
		 * set selected project for copy and set appropiate checkbox information.
		 * @param project selected project for copy
		 */
		private void updateBaseProject(String project){
			setUpdateProject(ResourcesPlugin.getWorkspace().getRoot().getProject(project));
			if(simularProject != null){
				copyCheckbox.setText(COPY_CHECKBOX_MESSAGE+" ("+simularProject.getName()+")");
				copyCheckbox.setEnabled(true);
			}
		}
		
		/**
		 * create scan elements for enternamepage wizard.
		 * @param workArea composite of appropriate wizard page
		 */
		private void createScanStatusProject(Composite workArea){
			Composite projectGroup = new Composite(workArea, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			layout.makeColumnsEqualWidth = false;
			layout.marginWidth = 10;
			projectGroup.setLayout(layout);
			GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL);
			data.verticalIndent = 1;
			projectGroup.setLayoutData(data);	
			
			
			scanWorkspaceProjectsButton = new Button(projectGroup, SWT.PUSH);
			scanWorkspaceProjectsButton.setText("Scan workspace");
			scanWorkspaceProjectsButton.setToolTipText("Scan workspace for simular projects.");
			setButtonLayoutData(scanWorkspaceProjectsButton);
			
			scanWorkspaceProjectsButton.addSelectionListener(new SelectionAdapter(){
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.SelectionAdapter#widgetS
				 *      elected(org.eclipse.swt.events.SelectionEvent)
				 */
				public void widgetSelected(SelectionEvent e) {
					scanProjectButtonPressed();
					
					updateProjectStatusResult.setText(getStatusText(simularProject));
					if(simularProject != null){
						updateProjectText.setText(simularProject.getName());
					}
					updatePageComplete();
				}

				
			});	
			
			updateProjectStatusResult = new Label(projectGroup, SWT.NONE);
			updateProjectStatusResult.setText("No scan results.");
			updateProjectStatusResult.setLayoutData(new GridData(
					GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL ));
			
			
		}
		
		/**
		 * action method for scan button.
		 */
		private void scanProjectButtonPressed() {
			/**
			 * Match all workspace project list and get the best
			 */

			scanRun = true;
			/* run project read only settings in progress monitor thread. */
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					ProgressMonitorDialog dialog = new ProgressMonitorDialog(
							Display.getDefault().getActiveShell());
					try {
						dialog.run(true, false, new IRunnableWithProgress() {
							public void run(IProgressMonitor monitor) {
								
								monitor.beginTask("Scanning workspace projects ... ",IProgressMonitor.UNKNOWN);
								final IProject project = getLocalProject(monitor);
								monitor.done();
								setUpdateProject(project);
								scanRun = false;
							}

						});
					} catch (InvocationTargetException e) {
						 log.log(Level.WARNING, "",e);
//						log.warn("", e);
						e.printStackTrace();
					} catch (InterruptedException e) {
						 log.log(Level.WARNING, "",e);
//						log.warn("", e);
						e.printStackTrace();
					}

				}
			});
			
			while(scanRun){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			/* update check box settings. */
			if(simularProject != null){
				copyCheckbox.setText(COPY_CHECKBOX_MESSAGE+" ("+simularProject.getName()+")");
				if(!projUpd.getSelection()){
					copyCheckbox.setEnabled(true);
				}
			}
				
		}
		
		/**
		 * select and unselect gui components depending on 
		 * selected option.
		 */
		private void copyRadioSelected() {
			if(projUpd.getSelection()){
				newProjectNameText.setEnabled(false);
				newProjectNameLabel.setEnabled(false);
				browseCreateProjectButton.setEnabled(false);
				updateProjectText.setEnabled(true);
				browseUpdateProjectButton.setEnabled(true);
				updateProjectNameLabel.setEnabled(true);
				this.copyCheckbox.setEnabled(false);
			}
			else{
				newProjectNameText.setEnabled(true);
				newProjectNameLabel.setEnabled(true);
				browseCreateProjectButton.setEnabled(true);
				updateProjectText.setEnabled(false);
				browseUpdateProjectButton.setEnabled(false);
				updateProjectNameLabel.setEnabled(false);
				
				if(simularProject != null){
					this.copyCheckbox.setEnabled(true);
				}
			}
			updatePageComplete();
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.dialogs.IDialogPage
		 */
		public void createControl(Composite parent) {
			
			
			if ( process.getState()==State.CANCELED)
				return;

			/* wait for getting project file list. */
			requestRemoteFileList();
			
			if (process.getRemoteFileList() == null)
				getShell().close();

			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout());
			GridData gridData = new GridData(GridData.FILL_VERTICAL);
			gridData.verticalIndent = 20;
			composite.setLayoutData(gridData);

			
			projCopy = new Button(composite, SWT.RADIO);
			projCopy.setText("Create new project copy");
			projCopy.setSelection(true);

			projCopy.addSelectionListener(new SelectionAdapter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				public void widgetSelected(SelectionEvent e) {
					copyRadioSelected();
				}
	
			});
			
			createNewProjectGroup(composite);
			createOptionArea(composite);
			
			projUpd = new Button(composite, SWT.RADIO);
			projUpd.setText("Update and use existing project");

			
			projUpd.addSelectionListener(new SelectionAdapter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				public void widgetSelected(SelectionEvent e) {
					copyRadioSelected();
				}
	
			});

			createUpdateProjectGroup(composite);
			createScanStatusProject(composite);

			
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
			return projUpd.getSelection()?updateProjectText.getText():newProjectNameText.getText();
		}
		
		/**
		 * 
		 * @return the selection value of copyCheckbox of false if copyCheckbox
		 * not enabled.
		 */
		public boolean getCopyValue(){
			boolean result = false;
			if(copyCheckbox.isEnabled()){
				result = copyCheckbox.getSelection();
			}
			return result;
		}

		/**
		 * match all project from workspace with remote project list.
		 * @return
		 */
		private IProject getLocalProject(IProgressMonitor monitor) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IProject[] projects = workspace.getRoot().getProjects();

			int maxMatch = 0;
			IProject selectedProject = null;
			for (int i = 0; i < projects.length; i++) {
				monitor.worked(1);
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
		
		private String getStatusText(IProject project) {
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
			updateProjectText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					updatePageComplete();
				}
			});
		}

		/**
		 * check for page status.
		 */
		private void updatePageComplete() {

			String newText = null;
			
			if(!projUpd.getSelection()){
				newText = newProjectNameText.getText();	
			
				
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
				
			}else{
				newText = updateProjectText.getText();
				
				if (newText.length() == 0) {
					setMessage(null);
					setErrorMessage("Please set a project name");
					setPageComplete(false);
					
				} else {
					if (!projectIsUnique(newText)) {
						setMessage(null);
						setErrorMessage(null);
						setPageComplete(true);
						
					} else {
						setMessage(null);
						setErrorMessage("No update project exists with this name");
						setPageComplete(false);
					}
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
		

		/* no additional automatic scan.*/
//		final IProject project = namePage.getLocalProject();
		final IProject project = namePage.simularProject;
		final String newProjectName = namePage.getNewProjectName();
		final boolean copyValue = namePage.getCopyValue();

		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
					InterruptedException {
					
					process.accept(project, newProjectName, monitor, copyValue );
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

