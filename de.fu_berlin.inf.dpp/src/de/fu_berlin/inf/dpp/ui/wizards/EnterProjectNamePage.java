package de.fu_berlin.inf.dpp.ui.wizards;

import org.apache.log4j.Logger;
import org.eclipse.cdt.ui.templateengine.ProjectSelectionPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
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

import de.fu_berlin.inf.dpp.PreferenceUtils;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;

/**
 * A wizard page that allows to enter the new project name or to choose to
 * overwrite the project selected by the {@link ProjectSelectionPage}.
 */
class EnterProjectNamePage extends WizardPage {

    @SuppressWarnings("unused")
    private static final Logger log = Logger
        .getLogger(EnterProjectNamePage.class.getName());

    protected final JoinSessionWizard joinSessionWizard;

    protected Label newProjectNameLabel;
    protected Button projCopy;
    protected Text newProjectNameText;
    protected Button copyCheckbox;
    protected Text copyToBeforeUpdateText;

    protected Button projUpd;
    protected Text updateProjectText;
    protected Button browseUpdateProjectButton;

    protected Label updateProjectStatusResult;
    protected Label updateProjectNameLabel;
    protected Button scanWorkspaceProjectsButton;

    /* project for update or base project for copy into new project */
    protected IProject similarProject;

    protected DataTransferManager dataTransferManager;

    protected PreferenceUtils preferenceUtils;

    protected EnterProjectNamePage(JoinSessionWizard joinSessionWizard,
        DataTransferManager dataTransferManager, PreferenceUtils preferenceUtils) {
        super("namePage");
        this.joinSessionWizard = joinSessionWizard;
        this.dataTransferManager = dataTransferManager;
        this.preferenceUtils = preferenceUtils;

        setPageComplete(false);
        setTitle("Select local project.");
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
    protected void updateConnectionStatus() {

        switch (dataTransferManager
            .getIncomingTransferMode(joinSessionWizard.process.getPeer())) {
        case JINGLETCP:
        case JINGLEUDP:
            setDescription("P2P Connection with Jingle available.\nThis means that sharing a project from scratch will be fast.");
            setImageDescriptor(SarosUI
                .getImageDescriptor("icons/jingle_connection.png"));
            break;
        case UNKNOWN:
        case HANDMADE:
        case IBB:
        default:
            setDescription("Attention: No P2P connection with Jingle available! Using IBB instead!"
                + '\n'
                + "Suggestion: Update an existing project or copy resources from another project.");
            setImageDescriptor(SarosUI
                .getImageDescriptor("icons/ibb_connection.png"));
        }
    }

    /**
     * Create components of create new project area for EnterProjectNamePage
     */
    protected void createNewProjectGroup(Composite workArea) {

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
     * Create components of update area for EnterProjectNamePage wizard.
     */
    protected void createUpdateProjectGroup(Composite workArea,
        String updateProject) {

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
        this.updateProjectText.setText(updateProject);

        this.browseUpdateProjectButton = new Button(projectGroup, SWT.PUSH);
        this.browseUpdateProjectButton.setText("Browse");
        setButtonLayoutData(this.browseUpdateProjectButton);
        this.browseUpdateProjectButton
            .addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    EnterProjectNamePage.this.updateProjectText
                        .setText(getProjectDialog("Select project for update."));
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
                        .getBestScanMatch(EnterProjectNamePage.this.joinSessionWizard.process));
                }
            });

        this.updateProjectStatusResult = new Label(scanGroup, SWT.NONE);
        this.updateProjectStatusResult.setText("No scan results.");
        this.updateProjectStatusResult.setLayoutData(new GridData(
            GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL));
    }

    /**
     * Browse dialog to select project for copy.
     */
    public String getProjectDialog(String title) {
        ContainerSelectionDialog dialog = new ContainerSelectionDialog(
            getShell(), null, false, title);

        dialog.open();
        Object[] result = dialog.getResult();

        if (result == null || result.length == 0) {
            return null;
        }
        // TODO More error Checking
        return ResourcesPlugin.getWorkspace().getRoot().findMember(
            (Path) result[0]).getProject().getName();
    }

    public void createControl(Composite parent) {

        // Create the root control
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout());
        GridData gridData = new GridData(GridData.FILL_VERTICAL);
        gridData.verticalIndent = 20;
        composite.setLayoutData(gridData);

        this.projCopy = new Button(composite, SWT.RADIO);
        this.projCopy.setText("Create new project");
        this.projCopy.setSelection(!joinSessionWizard.isUpdateSelected());

        createNewProjectGroup(composite);

        this.projUpd = new Button(composite, SWT.RADIO);
        this.projUpd.setText("Use existing project");
        this.projUpd.setSelection(joinSessionWizard.isUpdateSelected());

        createUpdateProjectGroup(composite, joinSessionWizard
            .getUpdateProject());

        attachListeners();

        updateConnectionStatus();
        updateEnabled();

        if (preferenceUtils.isAutoAcceptInvitation()) {
            joinSessionWizard.pressWizardButton(IDialogConstants.FINISH_ID);
        }
    }

    public boolean isUpdateSelected() {
        return this.projUpd.getSelection();
    }

    protected void attachListeners() {

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

    /**
     * Returns the name of the project to use during the shared session.
     * 
     * Caution: This project will be synchronized with the data from the host
     * and all local changes will be lost.
     * 
     * Will return null if the getSourceProject() should be overwritten.
     */
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

    /**
     * Will return the project to use as a base version during synchronization
     * or null if the user wants to start synchronization from scratch.
     * 
     */
    public IProject getSourceProject() {

        if (isUpdateSelected()) {
            return ResourcesPlugin.getWorkspace().getRoot().getProject(
                this.updateProjectText.getText());
        } else {
            return null;
        }
    }
}