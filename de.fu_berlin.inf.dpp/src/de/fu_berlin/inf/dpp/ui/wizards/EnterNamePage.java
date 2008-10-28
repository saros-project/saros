package de.fu_berlin.inf.dpp.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.State;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.TransferMode;
import de.fu_berlin.inf.dpp.ui.SarosUI;

/**
 * A wizard page that allows to enter the new project name or to choose to
 * overwrite the project selected by the {@link ProjectSelectionPage}.
 */
class EnterNamePage extends WizardPage {

	private JoinSessionWizard joinSessionWizard;

	private Label newProjectNameLabel;
	private Button projCopy;
	private Text newProjectNameText;
	private Button copyCheckbox;
	private Text copyToBeforeUpdateText;

	private Button projUpd;
	private Text updateProjectText;
	private Button browseUpdateProjectButton;

	private Label updateProjectStatusResult;
	private Label updateProjectNameLabel;
	private Button scanWorkspaceProjectsButton;

	/* project for update or base project for copy into new project */
	private IProject similarProject;

	protected EnterNamePage(JoinSessionWizard joinSessionWizard) {
		super("namePage");
		this.joinSessionWizard = joinSessionWizard;
		setPageComplete(false);

		setTitle("Select local project.");

		setConnectionStatus();
	}

	protected void setUpdateProject(IProject project) {
		this.similarProject = project;

		if (project == null) {

			updateProjectStatusResult
				.setText("No matching project found. Project download will start from scratch.");

		} else {

			updateProjectStatusResult.setText("Your project "
				+ project.getName()
				+ " matches with "
				+ JoinSessionWizardUtils.getMatch(this.joinSessionWizard.process
					.getRemoteFileList(), project) + "% accuracy.\n"
				+ "This fact will used to shorten the process of "
				+ "downloading the remote project.");

			updateProjectText.setText(similarProject.getName());

		}
		updatePageComplete();
	}

	/**
	 * get transfer mode and set header information of the wizard.
	 */
	private void setConnectionStatus() {
		if (this.joinSessionWizard.process.getTransferMode() == TransferMode.IBB) {
			setDescription("Attention: No direct connection avialable!" + '\n'
				+ "Suggestion: Update an existing project or copy resources from another project.");
			setImageDescriptor(SarosUI.getImageDescriptor("icons/ibb_connection.png"));
		} else {
			setDescription("P2P Connection with Jingle available.\nThis means that sharing a project from scratch will be fast.");
			setImageDescriptor(SarosUI.getImageDescriptor("icons/jingle_connection.png"));
		}
	}

	/**
	 * create components of create new project area for enternamepage wizard.
	 * 
	 * @param workArea
	 *            composite of appropriate wizard
	 */
	private void createNewProjectGroup(Composite workArea) {

		Composite projectGroup = new Composite(workArea, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = false;
		layout.marginWidth = 0;
		projectGroup.setLayout(layout);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalIndent = 10;
		
		projectGroup.setLayoutData(data);

		newProjectNameLabel = new Label(projectGroup, SWT.NONE);
		newProjectNameLabel.setText("Project name");

		newProjectNameText = new Text(projectGroup, SWT.BORDER);
		newProjectNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
			| GridData.GRAB_HORIZONTAL));
		newProjectNameText.setFocus();
		newProjectNameText.setText(JoinSessionWizardUtils
			.findProjectNameProposal(this.joinSessionWizard.process.getProjectName()));
	}

	/**
	 * create components of update area for enternamepage wizard.
	 * 
	 * @param workArea
	 *            composite of appropriate wizard
	 */
	private void createUpdateProjectGroup(Composite workArea) {
		
		Composite projectGroup = new Composite(workArea, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		layout.makeColumnsEqualWidth = false;
		layout.marginWidth = 0;
		projectGroup.setLayout(layout);
		
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalIndent = 10;
		projectGroup.setLayoutData(data);

		updateProjectNameLabel = new Label(projectGroup, SWT.NONE);
		updateProjectNameLabel.setText("Project name");
		updateProjectNameLabel.setEnabled(false);

		updateProjectText = new Text(projectGroup, SWT.BORDER);
		updateProjectText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
			| GridData.GRAB_HORIZONTAL));
		updateProjectText.setFocus();
		updateProjectText.setEnabled(false);
		updateProjectText.setText("");

		browseUpdateProjectButton = new Button(projectGroup, SWT.PUSH);
		browseUpdateProjectButton.setText("Browse");
		setButtonLayoutData(browseUpdateProjectButton);
		browseUpdateProjectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IProject project = getProjectDialog("Select project for update.");
				if (project != null) {
					updateProjectText.setText(project.getName());
				}
			}
		});

		Composite optionsGroup = new Composite(workArea, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginLeft = 20;
		layout.makeColumnsEqualWidth = false;
		layout.marginWidth = 0;

		optionsGroup.setLayout(layout);
		optionsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		copyCheckbox = new Button(optionsGroup, SWT.CHECK);
		copyCheckbox.setText("Create copy for working distributed. New project name:");
		copyCheckbox.setSelection(false);
		copyCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnabled();
			}
		});

		copyToBeforeUpdateText = new Text(optionsGroup, SWT.BORDER);
		copyToBeforeUpdateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
			| GridData.GRAB_HORIZONTAL));
		copyToBeforeUpdateText.setFocus();
		copyToBeforeUpdateText.setText(JoinSessionWizardUtils
			.findProjectNameProposal(joinSessionWizard.process.getProjectName()));
		
		Composite scanGroup = new Composite(workArea, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		layout.marginWidth = 10;
		scanGroup.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL);
		data.verticalIndent = 10;
		data.horizontalIndent = 10;
		scanGroup.setLayoutData(data);

		scanWorkspaceProjectsButton = new Button(scanGroup, SWT.PUSH);
		scanWorkspaceProjectsButton.setText("Scan workspace");
		scanWorkspaceProjectsButton.setToolTipText("Scan workspace for similar projects.");
		setButtonLayoutData(scanWorkspaceProjectsButton);

		scanWorkspaceProjectsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setUpdateProject(JoinSessionWizardUtils.getBestScanMatch(joinSessionWizard.process));
			}
		});

		updateProjectStatusResult = new Label(scanGroup, SWT.NONE);
		updateProjectStatusResult.setText("No scan results.");
		updateProjectStatusResult.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
			| GridData.GRAB_VERTICAL));
	}

	/**
	 * browse dialog to select project for copy.
	 */
	public IProject getProjectDialog(String title) {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), null, false,
			title);

		dialog.open();
		Object[] result = dialog.getResult();

		if (result == null && result.length == 0) {
			return null;
		}

		return ((IResource) result[0]).getProject();
	}

	protected void createScanStatusProject(Composite workArea) {
		

	}

	protected void requestRemoteFileList() {
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					EnterNamePage.this.joinSessionWizard.process.requestRemoteFileList(monitor);
				}
			});
		} catch (InvocationTargetException e) {
			JoinSessionWizard.log.log(Level.WARNING, "Exception while requesting remote file list",
				e);
		} catch (InterruptedException e) {
			JoinSessionWizard.log.log(Level.FINE,
				"Request of remote file list canceled/interrupted", e);
		}
	}

	public void createControl(Composite parent) {

		if (joinSessionWizard.process.getState() == State.CANCELED)
			return;

		/* wait for getting project file list. */
		requestRemoteFileList();

		if (joinSessionWizard.process.getRemoteFileList() == null)
			getShell().close();

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		GridData gridData = new GridData(GridData.FILL_VERTICAL);
		gridData.verticalIndent = 20;
		composite.setLayoutData(gridData);

		projCopy = new Button(composite, SWT.RADIO);
		projCopy.setText("Create new project");
		projCopy.setSelection(true);
		
		createNewProjectGroup(composite);

		projUpd = new Button(composite, SWT.RADIO);
		projUpd.setText("Use existing project");
		
		createUpdateProjectGroup(composite);

		attachListeners();
		setControl(composite);

		updateEnabled();
	}

	public boolean isUpdateSelected() {
		return projUpd.getSelection();
	}

	private void attachListeners() {
		
		ModifyListener m = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updatePageComplete();
			}
		};
		
		newProjectNameText.addModifyListener(m);
		updateProjectText.addModifyListener(m);
		copyToBeforeUpdateText.addModifyListener(m);
		
		SelectionAdapter s = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateEnabled();
			}
		};

		projCopy.addSelectionListener(s);
				
		s.widgetSelected(null);
	}

	public void setPageCompleteTargetProject(String newText) {
		
		if (newText.length() == 0) {
			setMessage(null);
			setErrorMessage("Please set a project name");
			setPageComplete(false);
		} else {
			if (JoinSessionWizardUtils.projectIsUnique(newText)) {
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

	protected void updateEnabled() {

		boolean updateSelected = !projCopy.getSelection();
		boolean copySelected = copyCheckbox.getSelection();

		newProjectNameText.setEnabled(!updateSelected);
		newProjectNameLabel.setEnabled(!updateSelected);

		updateProjectText.setEnabled(updateSelected);
		browseUpdateProjectButton.setEnabled(updateSelected);
		updateProjectNameLabel.setEnabled(updateSelected);
		copyCheckbox.setEnabled(updateSelected);
		copyToBeforeUpdateText.setEnabled(updateSelected && copySelected);
		scanWorkspaceProjectsButton.setEnabled(updateSelected);
		updateProjectStatusResult.setEnabled(updateSelected);

		updatePageComplete();
	}

	protected void updatePageComplete() {

		if (!isUpdateSelected()) {

			setPageCompleteTargetProject(newProjectNameText.getText());

		} else {
			String newText = updateProjectText.getText();

			if (newText.length() == 0) {
				setMessage(null);
				setErrorMessage("Please set a project name to update from or press 'Scan Workspace' to find best matching existing project");
				setPageComplete(false);

			} else {
				if (!JoinSessionWizardUtils.projectIsUnique(newText)) {

					if (copyCheckbox.getSelection()) {
						setPageCompleteTargetProject(copyToBeforeUpdateText.getText());
					} else {
						setMessage(null);
						setErrorMessage(null);
						setPageComplete(true);
					}

				} else {
					setMessage(null);
					setErrorMessage("No project exists with this name to update from");
					setPageComplete(false);
				}
			}
		}
	}

	public String getTargetProjectName() {
		if (isUpdateSelected()) {

			if (copyCheckbox.getSelection()) {
				return copyToBeforeUpdateText.getText();
			} else {
				return null;
			}
		} else {
			return newProjectNameText.getText();
		}
	}

	public IProject getSourceProject() {

		if (isUpdateSelected()) {
			if (copyCheckbox.getSelection()) {
				return ResourcesPlugin.getWorkspace().getRoot().getProject(
					copyToBeforeUpdateText.getText());
			} else {
				return ResourcesPlugin.getWorkspace().getRoot().getProject(
					updateProjectText.getText());
			}
		} else {
			return null;
		}
	}
}