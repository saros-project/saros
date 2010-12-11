package de.fu_berlin.inf.dpp.ui.wizards;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.GeneralPreferencePage;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * A wizard page that allows to enter the new project name or to choose to
 * overwrite a project.
 */
class EnterProjectNamePage extends WizardPage {

    private static final Logger log = Logger
        .getLogger(EnterProjectNamePage.class.getName());

    protected final FileList fileList;
    protected final JID peer;
    protected final String remoteProjectName;
    protected WizardDialogAccessable wizardDialog;

    protected Label newProjectNameLabel;
    protected Button projCopy;
    protected Text newProjectNameText;
    protected Button skipCheckbox;
    protected Button copyCheckbox;
    protected Text copyToBeforeUpdateText;

    protected Button projUpd;
    protected Text updateProjectText;
    protected Button browseUpdateProjectButton;

    protected Label updateProjectStatusResult;
    protected Label updateProjectNameLabel;
    protected Button scanWorkspaceProjectsButton;

    protected Button disableVCSCheckbox;

    protected int pageChanges = 0;

    /* project for update or base project for copy into new project */
    protected IProject similarProject;

    protected DataTransferManager dataTransferManager;

    protected PreferenceUtils preferenceUtils;

    private boolean disposed;

    protected EnterProjectNamePage(DataTransferManager dataTransferManager,
        PreferenceUtils preferenceUtils, FileList fileList, JID peer,
        String remoteProjectName, WizardDialogAccessable wizardDialog) {
        super("namePage");
        // this.joinSessionWizard = joinSessionWizard;
        this.dataTransferManager = dataTransferManager;
        this.preferenceUtils = preferenceUtils;
        this.peer = peer;
        this.remoteProjectName = remoteProjectName;

        this.wizardDialog = wizardDialog;

        this.wizardDialog.addPageChangingListener(new IPageChangingListener() {
            public void handlePageChanging(PageChangingEvent event) {
                pageChanges++;
            }
        });

        if (this.wizardDialog == null) {
            log.warn("WizardDialog is null");
        }

        this.fileList = fileList;

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
                // + this.joinSessionWizard.process.getRemoteFileList()
                + this.fileList.computeMatch(project) + "% accuracy.\n"
                + "This fact will be used to shorten the process of "
                + "downloading the remote project.");

            this.updateProjectText.setText(this.similarProject.getName());

        }
        updatePageComplete();
    }

    /**
     * get transfer mode and set header information of the wizard.
     */
    protected void updateConnectionStatus() {

        switch (dataTransferManager.getTransferMode(this.peer)) {
        case JINGLETCP:
        case JINGLEUDP:
            setDescription("P2P Connection with Jingle available.\nThis means that sharing a project from scratch will be fast.");
            setImageDescriptor(SarosUI
                .getImageDescriptor("icons/jingle_connection.png"));
            break;
        case SOCKS5_MEDIATED:
            if (preferenceUtils.isLocalSOCKS5ProxyEnabled())
                setDescription("Attention: only a mediated file transfer connection with SOCKS5 protocol is available.\n"
                    + "Suggestions: Update an existing project or copy resources from another project.");
            else
                setDescription("Attention: direct file transfer connections with SOCKS5 protocol are deactivated.\n"
                    + "To activate uncheck 'Disable local file transfer proxy for direct connections'.");
            setImageDescriptor(SarosUI
                .getImageDescriptor("icons/ibb_connection.png"));
            break;
        case SOCKS5:
        case SOCKS5_DIRECT:
            setDescription("Direct file transfer connection with SOCKS5 protocol available.\nThis means that sharing a project from scratch will be fast.");
            setImageDescriptor(SarosUI
                .getImageDescriptor("icons/jingle_connection.png"));
            break;
        case UNKNOWN:
        case HANDMADE:
        case IBB:
        default:
            if (preferenceUtils.forceFileTranserByChat()) {
                setDescription("Attention: Direct file transfer connections are deactivated. Using IBB instead!"
                    + '\n'
                    + "To activate uncheck 'Force file transfer over XMPP network' in Saros preferences.");
            } else {
                setDescription("Attention: No direct file transfer connection available! Using IBB instead!"
                    + '\n'
                    + "Suggestions: Update an existing project or copy resources from another project.");
            }
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
        this.newProjectNameText.setText(EnterProjectNamePageUtils
            .findProjectNameProposal(this.remoteProjectName));
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
                    String projectName = getProjectDialog("Select project for update.");
                    if (projectName != null)
                        EnterProjectNamePage.this.updateProjectText
                            .setText(projectName);
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
        this.copyToBeforeUpdateText.setText(EnterProjectNamePageUtils
            .findProjectNameProposal(this.remoteProjectName));

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
                    setUpdateProject(EnterProjectNamePageUtils
                        .getBestScanMatch(EnterProjectNamePage.this.fileList));
                }
            });

        this.updateProjectStatusResult = new Label(scanGroup, SWT.NONE);
        this.updateProjectStatusResult.setText("No scan results.");
        this.updateProjectStatusResult.setLayoutData(new GridData(
            GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL));
    }

    /**
     * Browse dialog to select project for copy.
     * 
     * Returns null if the dialog was canceled.
     */
    public String getProjectDialog(String title) {
        ContainerSelectionDialog dialog = new ContainerSelectionDialog(
            getShell(), null, false, title);

        dialog.open();
        Object[] result = dialog.getResult();

        if (result == null || result.length == 0) {
            return null;
        }

        return ResourcesPlugin.getWorkspace().getRoot()
            .findMember((Path) result[0]).getProject().getName();
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
        this.projCopy.setSelection(!EnterProjectNamePageUtils
            .autoUpdateProject(this.remoteProjectName));

        createNewProjectGroup(composite);

        this.projUpd = new Button(composite, SWT.RADIO);
        this.projUpd.setText("Use existing project");
        this.projUpd.setSelection(EnterProjectNamePageUtils
            .autoUpdateProject(this.remoteProjectName));

        String newProjectName = "";
        if (EnterProjectNamePageUtils.autoUpdateProject(remoteProjectName)) {
            newProjectName = this.remoteProjectName;
        }
        createUpdateProjectGroup(composite, newProjectName);

        if (preferenceUtils.isSkipSyncSelectable()) {
            this.skipCheckbox = new Button(composite, SWT.CHECK);
            this.skipCheckbox.setText("Skip synchronization");
            this.skipCheckbox.setSelection(false);
            this.skipCheckbox.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    updatePageComplete();
                }
            });
        }

        disableVCSCheckbox = new Button(composite, SWT.CHECK);
        disableVCSCheckbox
            .setText(GeneralPreferencePage.DISABLE_VERSION_CONTROL_TEXT);
        disableVCSCheckbox
            .setToolTipText(GeneralPreferencePage.DISABLE_VERSION_CONTROL_TOOLTIP);
        disableVCSCheckbox.setSelection(!preferenceUtils.useVersionControl());

        attachListeners();

        updateConnectionStatus();
        updateEnabled();

        if (preferenceUtils.isAutoAcceptInvitation()) {
            // joinSessionWizard.pressWizardButton(IDialogConstants.FINISH_ID);
            pressWizardButton(IDialogConstants.FINISH_ID);
        }
    }

    private void pressWizardButton(final int buttonID) {
        final int pageChangesAtStart = pageChanges;

        Util.runSafeAsync(log, new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Code not designed to be interruptable", e);
                    Thread.currentThread().interrupt();
                    return;
                }
                Util.runSafeSWTAsync(log, new Runnable() {
                    public void run() {

                        // User clicked next in the meantime
                        if (pageChangesAtStart != pageChanges)
                            return;

                        // Dialog already closed
                        if (disposed)
                            return;

                        // Button not enabled
                        if (!wizardDialog.getWizardButton(buttonID).isEnabled())
                            return;

                        wizardDialog.buttonPressed(buttonID);
                    }
                });
            }
        });
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

        this.projUpd.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {

                // quickly scan for existing project with the same name
                if (projUpd.getSelection()
                    && !EnterProjectNamePageUtils
                        .projectIsUnique(EnterProjectNamePage.this.remoteProjectName)) {
                    updateProjectText
                        .setText(EnterProjectNamePage.this.remoteProjectName);
                }

            }

            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing

            }
        });

        s.widgetSelected(null);
    }

    public void setPageCompleteTargetProject(String newText) {

        if (newText.length() == 0) {
            setErrorMessage("Please set a project name");
            setPageComplete(false);
        } else {
            if (EnterProjectNamePageUtils.projectIsUnique(newText)) {
                setErrorMessage(null);
                setPageComplete(true);
            } else {
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

        if (isSyncSkippingSelected()) {
            setMessage("Skipping Synchronisation might cause inconsistencies!",
                IMessageProvider.WARNING);
        } else {
            setMessage(null);
        }

        if (!isUpdateSelected()) {
            setPageCompleteTargetProject(this.newProjectNameText.getText());
        } else {
            String newText = this.updateProjectText.getText();

            if (newText.length() == 0) {
                setErrorMessage("Please set a project name to update from or press 'Scan Workspace' to find best matching existing project");
                setPageComplete(false);

            } else {
                if (!EnterProjectNamePageUtils.projectIsUnique(newText)) {

                    if (this.copyCheckbox.getSelection()) {
                        setPageCompleteTargetProject(this.copyToBeforeUpdateText
                            .getText());
                    } else {
                        setErrorMessage(null);
                        setPageComplete(true);
                    }

                } else {
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

    public boolean useVersionControl() {
        return !disableVCSCheckbox.getSelection();
    }

    /**
     * Will return the project to use as a base version during synchronization
     * or null if the user wants to start synchronization from scratch.
     * 
     */
    public IProject getSourceProject() {

        if (isUpdateSelected()) {
            return ResourcesPlugin.getWorkspace().getRoot()
                .getProject(this.updateProjectText.getText());
        } else {
            return null;
        }
    }

    /**
     * Returns whether the user has selected to skip synchronisation
     */
    public boolean isSyncSkippingSelected() {
        if (preferenceUtils.isSkipSyncSelectable()) {
            return this.skipCheckbox.getSelection();
        }
        return false;
    }

    /**
     * @return <code>true</code> if the synchronization options chosen by the
     *         user could lead to overwriting project resources,
     *         <code>false</code> otherwise.
     */
    public boolean overwriteProjectResources() {
        if (isUpdateSelected() && !copyCheckbox.getSelection()
            && !isSyncSkippingSelected()) {
            return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        this.disposed = true;
        super.dispose();
    }
}