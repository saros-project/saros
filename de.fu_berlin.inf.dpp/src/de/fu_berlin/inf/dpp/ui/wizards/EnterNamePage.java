package de.fu_berlin.inf.dpp.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import org.eclipse.cdt.ui.templateengine.ProjectSelectionPage;
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

    private final JoinSessionWizard joinSessionWizard;

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

            this.updateProjectStatusResult
                .setText("No matching project found. Project download will start from scratch.");

        } else {

            this.updateProjectStatusResult.setText("Your project "
                + project.getName()
                + " matches with "
                + JoinSessionWizardUtils
                    .getMatch(this.joinSessionWizard.process
                        .getRemoteFileList(), project) + "% accuracy.\n"
                + "This fact will used to shorten the process of "
                + "downloading the remote project.");

            this.updateProjectText.setText(this.similarProject.getName());

        }
        updatePageComplete();
    }

    /**
     * get transfer mode and set header information of the wizard.
     */
    private void setConnectionStatus() {
        if (this.joinSessionWizard.process.getTransferMode() == TransferMode.IBB) {
            setDescription("Attention: No direct connection avialable!"
                + '\n'
                + "Suggestion: Update an existing project or copy resources from another project.");
            setImageDescriptor(SarosUI
                .getImageDescriptor("icons/ibb_connection.png"));
        } else {
            setDescription("P2P Connection with Jingle available.\nThis means that sharing a project from scratch will be fast.");
            setImageDescriptor(SarosUI
                .getImageDescriptor("icons/jingle_connection.png"));
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

        this.newProjectNameLabel = new Label(projectGroup, SWT.NONE);
        this.newProjectNameLabel.setText("Project name");

        this.newProjectNameText = new Text(projectGroup, SWT.BORDER);
        this.newProjectNameText.setLayoutData(new GridData(
            GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        this.newProjectNameText.setFocus();
        this.newProjectNameText.setText(JoinSessionWizardUtils
            .findProjectNameProposal(this.joinSessionWizard.process
                .getProjectName()));
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

        this.updateProjectNameLabel = new Label(projectGroup, SWT.NONE);
        this.updateProjectNameLabel.setText("Project name");
        this.updateProjectNameLabel.setEnabled(false);

        this.updateProjectText = new Text(projectGroup, SWT.BORDER);
        this.updateProjectText.setLayoutData(new GridData(
            GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        this.updateProjectText.setFocus();
        this.updateProjectText.setEnabled(false);
        this.updateProjectText.setText("");

        this.browseUpdateProjectButton = new Button(projectGroup, SWT.PUSH);
        this.browseUpdateProjectButton.setText("Browse");
        setButtonLayoutData(this.browseUpdateProjectButton);
        this.browseUpdateProjectButton
            .addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    IProject project = getProjectDialog("Select project for update.");
                    if (project != null) {
                        EnterNamePage.this.updateProjectText.setText(project
                            .getName());
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

        this.copyCheckbox = new Button(optionsGroup, SWT.CHECK);
        this.copyCheckbox
            .setText("Create copy for working distributed. New project name:");
        this.copyCheckbox.setSelection(false);
        this.copyCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateEnabled();
            }
        });

        this.copyToBeforeUpdateText = new Text(optionsGroup, SWT.BORDER);
        this.copyToBeforeUpdateText.setLayoutData(new GridData(
            GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        this.copyToBeforeUpdateText.setFocus();
        this.copyToBeforeUpdateText.setText(JoinSessionWizardUtils
            .findProjectNameProposal(this.joinSessionWizard.process
                .getProjectName()));

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

        this.scanWorkspaceProjectsButton = new Button(scanGroup, SWT.PUSH);
        this.scanWorkspaceProjectsButton.setText("Scan workspace");
        this.scanWorkspaceProjectsButton
            .setToolTipText("Scan workspace for similar projects.");
        setButtonLayoutData(this.scanWorkspaceProjectsButton);

        this.scanWorkspaceProjectsButton
            .addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    setUpdateProject(JoinSessionWizardUtils
                        .getBestScanMatch(EnterNamePage.this.joinSessionWizard.process));
                }
            });

        this.updateProjectStatusResult = new Label(scanGroup, SWT.NONE);
        this.updateProjectStatusResult.setText("No scan results.");
        this.updateProjectStatusResult.setLayoutData(new GridData(
            GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL));
    }

    /**
     * browse dialog to select project for copy.
     */
    public IProject getProjectDialog(String title) {
        ContainerSelectionDialog dialog = new ContainerSelectionDialog(
            getShell(), null, false, title);

        dialog.open();
        Object[] result = dialog.getResult();

        if (result == null) {
            return null;
        }

        return ((IResource) result[0]).getProject();
    }

    protected void createScanStatusProject(Composite workArea) {
        // TODO?
    }

    protected void requestRemoteFileList() {
        try {
            getContainer().run(true, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) {
                    EnterNamePage.this.joinSessionWizard.process
                        .requestRemoteFileList(monitor);
                }
            });
        } catch (InvocationTargetException e) {
            JoinSessionWizard.log.log(Level.WARNING,
                "Exception while requesting remote file list", e);
        } catch (InterruptedException e) {
            JoinSessionWizard.log.log(Level.FINE,
                "Request of remote file list canceled/interrupted", e);
        }
    }

    public void createControl(Composite parent) {

        if (this.joinSessionWizard.process.getState() == State.CANCELED) {
            return;
        }

        /* wait for getting project file list. */
        requestRemoteFileList();

        if (this.joinSessionWizard.process.getRemoteFileList() == null) {
            getShell().close();
        }

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        GridData gridData = new GridData(GridData.FILL_VERTICAL);
        gridData.verticalIndent = 20;
        composite.setLayoutData(gridData);

        this.projCopy = new Button(composite, SWT.RADIO);
        this.projCopy.setText("Create new project");
        this.projCopy.setSelection(true);

        createNewProjectGroup(composite);

        this.projUpd = new Button(composite, SWT.RADIO);
        this.projUpd.setText("Use existing project");

        createUpdateProjectGroup(composite);

        attachListeners();
        setControl(composite);

        updateEnabled();
    }

    public boolean isUpdateSelected() {
        return this.projUpd.getSelection();
    }

    private void attachListeners() {

        ModifyListener m = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updatePageComplete();
            }
        };

        this.newProjectNameText.addModifyListener(m);
        this.updateProjectText.addModifyListener(m);
        this.copyToBeforeUpdateText.addModifyListener(m);

        SelectionAdapter s = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateEnabled();
            }
        };

        this.projCopy.addSelectionListener(s);

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

        boolean updateSelected = !this.projCopy.getSelection();
        boolean copySelected = this.copyCheckbox.getSelection();

        this.newProjectNameText.setEnabled(!updateSelected);
        this.newProjectNameLabel.setEnabled(!updateSelected);

        this.updateProjectText.setEnabled(updateSelected);
        this.browseUpdateProjectButton.setEnabled(updateSelected);
        this.updateProjectNameLabel.setEnabled(updateSelected);
        this.copyCheckbox.setEnabled(updateSelected);
        this.copyToBeforeUpdateText.setEnabled(updateSelected && copySelected);
        this.scanWorkspaceProjectsButton.setEnabled(updateSelected);
        this.updateProjectStatusResult.setEnabled(updateSelected);

        updatePageComplete();
    }

    protected void updatePageComplete() {

        if (!isUpdateSelected()) {

            setPageCompleteTargetProject(this.newProjectNameText.getText());

        } else {
            String newText = this.updateProjectText.getText();

            if (newText.length() == 0) {
                setMessage(null);
                setErrorMessage("Please set a project name to update from or press 'Scan Workspace' to find best matching existing project");
                setPageComplete(false);

            } else {
                if (!JoinSessionWizardUtils.projectIsUnique(newText)) {

                    if (this.copyCheckbox.getSelection()) {
                        setPageCompleteTargetProject(this.copyToBeforeUpdateText
                            .getText());
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

            if (this.copyCheckbox.getSelection()) {
                return this.copyToBeforeUpdateText.getText();
            } else {
                return null;
            }
        } else {
            return this.newProjectNameText.getText();
        }
    }

    public IProject getSourceProject() {

        if (isUpdateSelected()) {
            if (this.copyCheckbox.getSelection()) {
                return ResourcesPlugin.getWorkspace().getRoot().getProject(
                    this.copyToBeforeUpdateText.getText());
            } else {
                return ResourcesPlugin.getWorkspace().getRoot().getProject(
                    this.updateProjectText.getText());
            }
        } else {
            return null;
        }
    }
}