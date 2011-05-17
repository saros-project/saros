package de.fu_berlin.inf.dpp.ui.wizards.pages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.preferencePages.GeneralPreferencePage;
import de.fu_berlin.inf.dpp.ui.wizards.dialogs.WizardDialogAccessable;
import de.fu_berlin.inf.dpp.ui.wizards.utils.EnterProjectNamePageUtils;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * A wizard page that allows to enter the new project name or to choose to
 * overwrite a project.
 */
public class EnterProjectNamePage extends WizardPage {

    /*
     * IMPORTANT: for every Map in this class that the key is the projectID.
     * Exceptions from that rule have to be declared!
     */

    private static final Logger log = Logger
        .getLogger(EnterProjectNamePage.class.getName());

    protected final List<FileList> fileLists;
    protected final JID peer;
    protected final Map<String, String> remoteProjectNames;
    protected WizardDialogAccessable wizardDialog;

    protected Map<String, Label> newProjectNameLabels = new HashMap<String, Label>();

    protected Map<String, Button> projCopies = new HashMap<String, Button>();

    protected Map<String, Text> newProjectNameTexts = new HashMap<String, Text>();

    protected Map<String, Button> skipCheckBoxes = new HashMap<String, Button>();

    protected Map<String, Button> copyCheckboxes = new HashMap<String, Button>();

    protected Map<String, Text> copyToBeforeUpdateTexts = new HashMap<String, Text>();

    protected Map<String, Button> projUpdates = new HashMap<String, Button>();

    protected Map<String, Text> updateProjectTexts = new HashMap<String, Text>();

    protected Map<String, Button> browseUpdateProjectButtons = new HashMap<String, Button>();

    protected Map<String, Label> updateProjectStatusResults = new HashMap<String, Label>();

    protected Map<String, Label> updateProjectNameLabels = new HashMap<String, Label>();

    protected Map<String, Button> scanWorkspaceProjectsButtons = new HashMap<String, Button>();

    protected Button disableVCSCheckbox;

    protected int pageChanges = 0;

    /* project for update or base project for copy into new project */
    protected Map<String, IProject> similarProjects = new HashMap<String, IProject>();

    protected DataTransferManager dataTransferManager;

    protected PreferenceUtils preferenceUtils;

    private boolean disposed;
    protected boolean flashState;

    /**
     * 
     * @param remoteProjectNames
     *            since the <code>projectID</code> is no longer the name of the
     *            project this mapping is necessary to display the names on
     *            host/inviter side instead of ugly random numbers projectID =>
     *            projectName
     */
    public EnterProjectNamePage(DataTransferManager dataTransferManager,
        PreferenceUtils preferenceUtils, List<FileList> fileLists, JID peer,
        Map<String, String> remoteProjectNames,
        WizardDialogAccessable wizardDialog) {
        super("namePage");
        // this.joinSessionWizard = joinSessionWizard;
        this.dataTransferManager = dataTransferManager;
        this.preferenceUtils = preferenceUtils;
        this.peer = peer;
        this.remoteProjectNames = remoteProjectNames;

        this.wizardDialog = wizardDialog;

        this.wizardDialog.addPageChangingListener(new IPageChangingListener() {
            public void handlePageChanging(PageChangingEvent event) {
                pageChanges++;
            }
        });

        if (this.wizardDialog == null) {
            log.warn("WizardDialog is null");
        }

        this.fileLists = fileLists;

        setPageComplete(false);
        setTitle("Select local project.");

    }

    protected void setUpdateProject(IProject project, String projectID) {
        this.similarProjects.put(projectID, project);

        if (project == null) {

            this.updateProjectStatusResults
                .get(projectID)
                .setText(
                    "No matching project found. Project download will start from scratch.");

        } else {

            this.updateProjectStatusResults.get(projectID).setText(
                "Your project "
                    + project.getName()
                    + " matches with "
                    // + this.joinSessionWizard.process.getRemoteFileList()
                    + this.fileLists.get(0).computeMatch(project)
                    + "% accuracy.\n"
                    + "This fact will be used to shorten the process of "
                    + "downloading the remote project.");

            this.updateProjectTexts.get(projectID).setText(
                this.similarProjects.get(projectID).getName());

        }
        updatePageComplete(projectID);
    }

    /**
     * get transfer mode and set header information of the wizard.
     */
    protected void updateConnectionStatus() {

        switch (dataTransferManager.getTransferMode(this.peer)) {
        case JINGLETCP:
        case JINGLEUDP:
            setDescription("P2P Connection with Jingle available.\nThis means that sharing a project from scratch will be fast.");
            setImageDescriptor(ImageManager
                .getImageDescriptor("icons/wizban/jingle.png"));
            break;
        case SOCKS5_MEDIATED:
            if (preferenceUtils.isLocalSOCKS5ProxyEnabled())
                setDescription("Attention: only a mediated file transfer connection with SOCKS5 protocol is available.\n"
                    + "Suggestions: Update an existing project or copy resources from another project.");
            else
                setDescription("Attention: direct file transfer connections with SOCKS5 protocol are deactivated.\n"
                    + "To activate uncheck 'Disable local file transfer proxy for direct connections'.");
            setImageDescriptor(ImageManager
                .getImageDescriptor("icons/wizban/socks5m.png"));
            break;
        case SOCKS5:
        case SOCKS5_DIRECT:
            setDescription("Direct file transfer connection with SOCKS5 protocol available.\nThis means that sharing a project from scratch will be fast.");
            setImageDescriptor(ImageManager
                .getImageDescriptor("icons/wizban/socks5.png"));
            break;
        case UNKNOWN:
        case HANDMADE:
        case IBB:
        default:
            String speedInfo = "";

            if (dataTransferManager.getIncomingIBBTransferSpeed(this.peer) != 0) {
                // Show throughput of recent IBB transfer in warning
                speedInfo = "("
                    + Math.round(dataTransferManager
                        .getIncomingIBBTransferSpeed(this.peer) / 1024 * 10.)
                    / 10. + " KiB/s only!)";
            }

            if (preferenceUtils.forceFileTranserByChat()) {

                setDescription("Warning: Direct file transfer deactivated. Using slow IBB instead! "
                    + speedInfo
                    + '\n'
                    + "To activate uncheck 'Force file transfer over XMPP network' in Saros preferences.");
            } else {
                setDescription("Warning : Direct file transfer not available! Using slow IBB instead! "
                    + speedInfo
                    + '\n'
                    + "Suggestions: Update an existing project or copy resources from another project.");
            }
            startIBBLogoFlash();
        }
    }

    /**
     * Starts and maintains a timer that will flash two IBB logos to make the
     * user aware of the warning.
     */
    protected void startIBBLogoFlash() {

        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                Utils.runSafeSWTSync(log, new Runnable() {

                    public void run() {
                        flashState = !flashState;
                        if (flashState)
                            setImageDescriptor(ImageManager
                                .getImageDescriptor("icons/wizban/ibb.png"));
                        else
                            setImageDescriptor(ImageManager
                                .getImageDescriptor("icons/wizban/ibbFaded.png"));
                    }
                });
            }
        }, 0, 1000);
    }

    /**
     * Create components of create new project area for EnterProjectNamePage
     */
    protected void createNewProjectGroup(Composite workArea, String projectID) {

        Composite projectGroup = new Composite(workArea, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 0;
        projectGroup.setLayout(layout);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalIndent = 10;

        projectGroup.setLayoutData(data);

        Label newProjectNameLabel = new Label(projectGroup, SWT.NONE);
        newProjectNameLabel.setText("Project name");
        this.newProjectNameLabels.put(projectID, newProjectNameLabel);

        Text newProjectNameText = new Text(projectGroup, SWT.BORDER);
        newProjectNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
            | GridData.GRAB_HORIZONTAL));
        newProjectNameText.setFocus();
        newProjectNameText.setText(EnterProjectNamePageUtils
            .findProjectNameProposal(this.remoteProjectNames.get(projectID)));

        this.newProjectNameTexts.put(projectID, newProjectNameText);
    }

    /**
     * Create components of update area for EnterProjectNamePage wizard.
     */
    protected void createUpdateProjectGroup(Composite workArea,
        String updateProjectName, final String projectID) {

        Composite projectGroup = new Composite(workArea, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.numColumns = 4;
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 0;
        projectGroup.setLayout(layout);

        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalIndent = 10;
        projectGroup.setLayoutData(data);

        Label updateProjectNameLabel = new Label(projectGroup, SWT.NONE);
        updateProjectNameLabel.setText("Project name");
        updateProjectNameLabel.setEnabled(false);
        this.updateProjectNameLabels.put(projectID, updateProjectNameLabel);

        Text updateProjectText = new Text(projectGroup, SWT.BORDER);
        updateProjectText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
            | GridData.GRAB_HORIZONTAL));
        updateProjectText.setFocus();
        updateProjectText.setEnabled(false);
        updateProjectText.setText(updateProjectName);
        this.updateProjectTexts.put(projectID, updateProjectText);

        Button browseUpdateProjectButton = new Button(projectGroup, SWT.PUSH);
        browseUpdateProjectButton.setText("Browse");
        setButtonLayoutData(browseUpdateProjectButton);
        browseUpdateProjectButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String projectName = getProjectDialog("Select project for update.");
                if (projectName != null)
                    EnterProjectNamePage.this.updateProjectTexts.get(projectID)
                        .setText(projectName);
            }
        });
        this.browseUpdateProjectButtons.put(projectID,
            browseUpdateProjectButton);

        Composite optionsGroup = new Composite(workArea, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginLeft = 20;
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 0;

        optionsGroup.setLayout(layout);
        optionsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button copyCheckbox = new Button(optionsGroup, SWT.CHECK);
        copyCheckbox
            .setText("Create copy for working distributed. New project name:");
        copyCheckbox.setSelection(false);
        copyCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateEnabled(projectID);
            }
        });
        this.copyCheckboxes.put(projectID, copyCheckbox);

        Text copyToBeforeUpdateText = new Text(optionsGroup, SWT.BORDER);
        copyToBeforeUpdateText.setLayoutData(new GridData(
            GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        copyToBeforeUpdateText.setFocus();
        copyToBeforeUpdateText.setText(EnterProjectNamePageUtils
            .findProjectNameProposal(this.remoteProjectNames.get(projectID)
                + "-copy"));
        this.copyToBeforeUpdateTexts.put(projectID, copyToBeforeUpdateText);

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

        Button scanWorkspaceProjectsButton = new Button(scanGroup, SWT.PUSH);
        scanWorkspaceProjectsButton.setText("Scan workspace");
        scanWorkspaceProjectsButton
            .setToolTipText("Scan workspace for similar projects.");
        setButtonLayoutData(scanWorkspaceProjectsButton);

        scanWorkspaceProjectsButton
            .addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    FileList fileList = new FileList();
                    for (FileList fList : EnterProjectNamePage.this.fileLists) {
                        if (fList.getProjectID().equals(projectID)) {
                            fileList = fList;
                        }
                    }
                    setUpdateProject(
                        EnterProjectNamePageUtils.getBestScanMatch(fileList),
                        projectID);
                }
            });
        this.scanWorkspaceProjectsButtons.put(projectID,
            scanWorkspaceProjectsButton);

        Label updateProjectStatusResult = new Label(scanGroup, SWT.NONE);
        updateProjectStatusResult.setText("No scan results.");
        updateProjectStatusResult.setLayoutData(new GridData(
            GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL));
        this.updateProjectStatusResults.put(projectID,
            updateProjectStatusResult);
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

        GridLayout layout = new GridLayout();
        composite.setLayout(layout);

        Composite tabs = new Composite(composite, SWT.NONE);
        tabs.setLayout(layout);

        TabFolder tabFolder = new TabFolder(tabs, SWT.BORDER);

        setControl(composite);

        for (String projectID : this.remoteProjectNames.keySet()) {
            log.debug(projectID + ": " + this.remoteProjectNames.get(projectID));
        }

        for (final FileList fileList : this.fileLists) {
            TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
            tabItem
                .setText(this.remoteProjectNames.get(fileList.getProjectID()));

            Composite tabComposite = new Composite(tabFolder, SWT.NONE);
            tabComposite.setLayout(new GridLayout());
            GridData gridData = new GridData(GridData.FILL_VERTICAL);
            gridData.verticalIndent = 20;
            tabComposite.setLayoutData(gridData);

            tabItem.setControl(tabComposite);

            Button projCopy = new Button(tabComposite, SWT.RADIO);
            projCopy.setText("Create new project");
            projCopy.setSelection(!EnterProjectNamePageUtils
                .autoUpdateProject(this.remoteProjectNames.get(fileList
                    .getProjectID())));
            this.projCopies.put(fileList.getProjectID(), projCopy);

            createNewProjectGroup(tabComposite, fileList.getProjectID());

            Button projUpd = new Button(tabComposite, SWT.RADIO);
            projUpd.setText("Use existing project");
            projUpd.setSelection(EnterProjectNamePageUtils
                .autoUpdateProject(this.remoteProjectNames.get(fileList
                    .getProjectID())));
            this.projUpdates.put(fileList.getProjectID(), projUpd);

            String newProjectName = "";
            if (EnterProjectNamePageUtils
                .autoUpdateProject(this.remoteProjectNames.get(fileList
                    .getProjectID()))) {
                newProjectName = this.remoteProjectNames.get(fileList
                    .getProjectID());
            }
            createUpdateProjectGroup(tabComposite, newProjectName,
                fileList.getProjectID());

            if (preferenceUtils.isSkipSyncSelectable()) {
                Button skipCheckBox = new Button(tabComposite, SWT.CHECK);
                skipCheckBox.setText("Skip synchronization");
                skipCheckBox.setSelection(false);
                skipCheckBox.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        updatePageComplete(fileList.getProjectID());
                    }
                });
                skipCheckBoxes.put(fileList.getProjectID(), skipCheckBox);
            }
            attachListeners(fileList.getProjectID());
            updateEnabled(fileList.getProjectID());

        }

        Composite globalSettings = new Composite(composite, SWT.NONE);
        globalSettings.setLayout(layout);
        disableVCSCheckbox = new Button(globalSettings, SWT.CHECK);
        disableVCSCheckbox
            .setText(GeneralPreferencePage.DISABLE_VERSION_CONTROL_TEXT);
        disableVCSCheckbox
            .setToolTipText(GeneralPreferencePage.DISABLE_VERSION_CONTROL_TOOLTIP);
        disableVCSCheckbox.setSelection(!preferenceUtils.useVersionControl());

        updateConnectionStatus();

        if (preferenceUtils.isAutoAcceptInvitation()) {
            pressWizardButton(IDialogConstants.FINISH_ID);
        }
    }

    private void pressWizardButton(final int buttonID) {
        final int pageChangesAtStart = pageChanges;

        Utils.runSafeAsync(log, new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Code not designed to be interruptable", e);
                    Thread.currentThread().interrupt();
                    return;
                }
                Utils.runSafeSWTAsync(log, new Runnable() {
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

    public boolean isUpdateSelected(String projectID) {
        return this.projUpdates.get(projectID).getSelection();
    }

    protected void attachListeners(final String projectID) {

        ModifyListener m = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updatePageComplete(projectID);
            }
        };

        this.newProjectNameTexts.get(projectID).addModifyListener(m);
        this.updateProjectTexts.get(projectID).addModifyListener(m);
        this.copyToBeforeUpdateTexts.get(projectID).addModifyListener(m);

        SelectionAdapter s = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateEnabled(projectID);
            }
        };

        this.projCopies.get(projectID).addSelectionListener(s);

        this.projUpdates.get(projectID).addSelectionListener(
            new SelectionListener() {

                public void widgetSelected(SelectionEvent e) {

                    // quickly scan for existing project with the same name
                    Button projUpd = EnterProjectNamePage.this.projUpdates
                        .get(projectID);
                    Text updateProjectText = EnterProjectNamePage.this.updateProjectTexts
                        .get(projectID);
                    if (projUpd.getSelection()
                        && !EnterProjectNamePageUtils
                            .projectIsUnique(EnterProjectNamePage.this.remoteProjectNames
                                .get(projectID))) {
                        updateProjectText
                            .setText(EnterProjectNamePage.this.remoteProjectNames
                                .get(projectID));
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
                setErrorMessage("A project with the name " + newText
                    + " already exists");
                setPageComplete(false);
            }
        }
    }

    protected void updateEnabled(String projectID) {

        boolean updateSelected = !this.projCopies.get(projectID).getSelection();
        boolean copySelected = this.copyCheckboxes.get(projectID)
            .getSelection();

        this.newProjectNameTexts.get(projectID).setEnabled(!updateSelected);
        this.newProjectNameLabels.get(projectID).setEnabled(!updateSelected);

        this.updateProjectTexts.get(projectID).setEnabled(updateSelected);
        this.browseUpdateProjectButtons.get(projectID).setEnabled(
            updateSelected);
        this.updateProjectNameLabels.get(projectID).setEnabled(updateSelected);
        this.copyCheckboxes.get(projectID).setEnabled(updateSelected);
        this.copyToBeforeUpdateTexts.get(projectID).setEnabled(
            updateSelected && copySelected);
        this.scanWorkspaceProjectsButtons.get(projectID).setEnabled(
            updateSelected);
        this.updateProjectStatusResults.get(projectID).setEnabled(
            updateSelected);

        updatePageComplete(projectID);
    }

    protected void updatePageComplete(String projectID) {

        if (isSyncSkippingSelected(projectID)) {
            setMessage("Skipping Synchronisation might cause inconsistencies!",
                IMessageProvider.WARNING);
        } else {
            setMessage(null);
        }

        if (!isUpdateSelected(projectID)) {
            setPageCompleteTargetProject(this.newProjectNameTexts
                .get(projectID).getText());
        } else {
            String newText = this.updateProjectTexts.get(projectID).getText();

            if (newText.length() == 0) {
                setErrorMessage("Please set a project name to update from or press 'Scan Workspace' to find best matching existing project");
                setPageComplete(false);

            } else {
                if (!EnterProjectNamePageUtils.projectIsUnique(newText)) {

                    if (this.copyCheckboxes.get(projectID).getSelection()) {
                        setPageCompleteTargetProject(this.copyToBeforeUpdateTexts
                            .get(projectID).getText());
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
     * 
     * TODO will never return null... Change behavior of "accept"
     */
    public String getTargetProjectName(String projectID) {
        if (isUpdateSelected(projectID)) {
            if (this.copyCheckboxes.get(projectID).getSelection()) {
                return this.copyToBeforeUpdateTexts.get(projectID).getText();
            } else {
                return this.updateProjectTexts.get(projectID).getText();
            }
        } else {
            return this.newProjectNameTexts.get(projectID).getText();
        }
    }

    public String getTargetProjectNames() {
        return null;
    }

    public boolean useVersionControl() {
        return !disableVCSCheckbox.getSelection();
    }

    /**
     * Will return the project corresponding to the
     * <code><b>projectID</b></code> to use as a base version during
     * synchronization or null if the user wants to start synchronization from
     * scratch.
     * 
     */
    public IProject getSourceProject(String projectID) {

        if (isUpdateSelected(projectID)) {
            return ResourcesPlugin.getWorkspace().getRoot()
                .getProject(this.updateProjectTexts.get(projectID).getText());
        } else {
            return null;
        }
    }

    /**
     * Returns whether the user has selected to skip synchronisation
     */
    public boolean isSyncSkippingSelected(String projectID) {
        if (preferenceUtils.isSkipSyncSelectable()) {
            return this.skipCheckBoxes.get(projectID).getSelection();
        }
        return false;
    }

    /**
     * @return <code>true</code> if the synchronization options chosen by the
     *         user could lead to overwriting project resources,
     *         <code>false</code> otherwise.
     */
    public boolean overwriteProjectResources(String projectID) {
        if (isUpdateSelected(projectID)
            && !copyCheckboxes.get(projectID).getSelection()
            && !isSyncSkippingSelected(projectID)) {
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