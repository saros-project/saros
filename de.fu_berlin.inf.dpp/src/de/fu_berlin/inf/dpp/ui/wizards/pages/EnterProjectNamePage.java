package de.fu_berlin.inf.dpp.ui.wizards.pages;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IMessageProvider;
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
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.preferencePages.GeneralPreferencePage;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
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
     * 
     * IMPORTANT: refactor this code, make every a tab a composite with its own
     * class to get rid of this hash map nightmare! The code is currently hardly
     * maintainable!
     */

    private static final Logger log = Logger
        .getLogger(EnterProjectNamePage.class.getName());

    protected final List<FileList> fileLists;
    protected final JID peer;
    protected final Map<String, String> remoteProjectNames;

    protected Map<String, Label> newProjectNameLabels = new HashMap<String, Label>();

    protected Map<String, Button> projCopies = new HashMap<String, Button>();

    protected Map<String, Text> newProjectNameTexts = new HashMap<String, Text>();

    protected Map<String, String> errorProjectNames = new LinkedHashMap<String, String>();

    protected Map<String, Button> skipCheckBoxes = new HashMap<String, Button>();

    protected Map<String, Button> copyCheckboxes = new HashMap<String, Button>();

    protected Map<String, Text> copyToBeforeUpdateTexts = new HashMap<String, Text>();

    protected Map<String, Button> projUpdates = new HashMap<String, Button>();

    protected Map<String, Text> updateProjectTexts = new HashMap<String, Text>();

    protected Map<String, Button> browseUpdateProjectButtons = new HashMap<String, Button>();

    protected Map<String, Label> updateProjectNameLabels = new HashMap<String, Label>();

    protected Map<String, String> reservedProjectNames = new HashMap<String, String>();

    protected Button disableVCSCheckbox;

    protected DataTransferManager dataTransferManager;

    protected PreferenceUtils preferenceUtils;

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
        Map<String, String> remoteProjectNames) {
        super(Messages.EnterProjectNamePage_title);
        this.dataTransferManager = dataTransferManager;
        this.preferenceUtils = preferenceUtils;
        this.peer = peer;
        this.remoteProjectNames = remoteProjectNames;

        EnterProjectNamePageUtils.setPreferenceUtils(preferenceUtils);

        this.fileLists = fileLists;

        setPageComplete(false);
        setTitle(Messages.EnterProjectNamePage_title2);

    }

    /**
     * get transfer mode and set header information of the wizard.
     */
    protected void updateConnectionStatus() {

        switch (dataTransferManager.getTransferMode(this.peer)) {
        case SOCKS5_MEDIATED:
            if (preferenceUtils.isLocalSOCKS5ProxyEnabled())
                setDescription(Messages.EnterProjectNamePage_description_socks5proxy);
            else
                setDescription(Messages.EnterProjectNamePage_description_file_transfer);
            setImageDescriptor(ImageManager
                .getImageDescriptor("icons/wizban/socks5m.png"));
            break;

        case SOCKS5:
        case SOCKS5_DIRECT:
            setDescription(Messages.EnterProjectNamePage_description_direct_filetranfser);
            setImageDescriptor(ImageManager
                .getImageDescriptor("icons/wizban/socks5.png"));
            break;

        case NONE:
            // no bytestream connection established yet (small filelist transfer
            // before was done by chat willingly), so we cant say something
            // about the transfer type so we don't give a message to not
            // worry/confuse the user
            break;

        case IBB:
            String speedInfo = "";

            if (preferenceUtils.forceFileTranserByChat()) {

                setDescription(MessageFormat
                    .format(
                        Messages.EnterProjectNamePage_direct_filetransfer_deactivated,
                        speedInfo));
            } else {
                setDescription(MessageFormat.format(
                    Messages.EnterProjectNamePage_direct_filetransfer_nan,
                    speedInfo));
            }
            startIBBLogoFlash();
            break;
        default:
            setDescription(Messages.EnterProjectNamePage_unknown_transport_method);
            break;

        }
    }

    /**
     * Starts and maintains a timer that will flash two IBB logos to make the
     * user aware of the warning.
     */
    protected void startIBBLogoFlash() {

        final Timer timer = new Timer();

        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                Utils.runSafeSWTSync(log, new Runnable() {

                    public void run() {

                        if (EnterProjectNamePage.this.getControl().isDisposed()) {
                            timer.cancel();
                            return;
                        }

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
        newProjectNameLabel.setText(Messages.EnterProjectNamePage_project_name);
        this.newProjectNameLabels.put(projectID, newProjectNameLabel);

        Text newProjectNameText = new Text(projectGroup, SWT.BORDER);
        newProjectNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
            | GridData.GRAB_HORIZONTAL));
        newProjectNameText.setFocus();
        if (!this.newProjectNameTexts.keySet().contains(projectID)) {
            newProjectNameText.setText(EnterProjectNamePageUtils
                .findProjectNameProposal(
                    this.remoteProjectNames.get(projectID),
                    this.reservedProjectNames.values().toArray(new String[0])));

            this.newProjectNameTexts.put(projectID, newProjectNameText);
            this.reservedProjectNames.put(projectID,
                newProjectNameText.getText());
        } else {
            newProjectNameText.setText(this.newProjectNameTexts.get(projectID)
                .toString());

            this.newProjectNameTexts.put(projectID, newProjectNameText);
        }
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
        updateProjectNameLabel
            .setText(Messages.EnterProjectNamePage_project_name);
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
        browseUpdateProjectButton.setText(Messages.EnterProjectNamePage_browse);
        setButtonLayoutData(browseUpdateProjectButton);
        browseUpdateProjectButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String projectName = getProjectDialog(Messages.EnterProjectNamePage_select_project_for_update);
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
        copyCheckbox.setText(Messages.EnterProjectNamePage_create_copy);
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
                + "-copy")); //$NON-NLS-1$
        this.copyToBeforeUpdateTexts.put(projectID, copyToBeforeUpdateText);
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

    @Override
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
            boolean selection = EnterProjectNamePageUtils.autoUpdateProject(
                fileList.getProjectID(),
                this.remoteProjectNames.get(fileList.getProjectID()));

            Button projCopy = new Button(tabComposite, SWT.RADIO);
            projCopy.setText(Messages.EnterProjectNamePage_create_new_project);
            projCopy.setSelection(!selection);
            this.projCopies.put(fileList.getProjectID(), projCopy);

            createNewProjectGroup(tabComposite, fileList.getProjectID());

            Button projUpd = new Button(tabComposite, SWT.RADIO);
            projUpd.setText(Messages.EnterProjectNamePage_use_existing_project);
            projUpd.setSelection(selection);
            this.projUpdates.put(fileList.getProjectID(), projUpd);

            String newProjectName = "";
            if (selection) {
                newProjectName = this.remoteProjectNames.get(fileList
                    .getProjectID());
            }
            createUpdateProjectGroup(tabComposite, newProjectName,
                fileList.getProjectID());

            if (preferenceUtils.isSkipSyncSelectable()) {
                Button skipCheckBox = new Button(tabComposite, SWT.CHECK);
                skipCheckBox
                    .setText(Messages.EnterProjectNamePage_skip_synchronizing);
                skipCheckBox.setSelection(false);
                skipCheckBox.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        updatePageComplete(fileList.getProjectID());
                    }
                });
                skipCheckBoxes.put(fileList.getProjectID(), skipCheckBox);
            }
        }

        Composite vcsComposite = new Composite(composite, SWT.NONE);
        vcsComposite.setLayout(layout);
        disableVCSCheckbox = new Button(vcsComposite, SWT.CHECK);
        disableVCSCheckbox
            .setText(GeneralPreferencePage.DISABLE_VERSION_CONTROL_TEXT);
        disableVCSCheckbox.setSelection(!preferenceUtils.useVersionControl());

        Button explainButton = new Button(vcsComposite, SWT.PUSH);
        explainButton.setText("Explain");

        final Label explanation = new Label(vcsComposite, SWT.NONE);
        explanation.setEnabled(false);
        explanation.setText(Messages.Explain_version_control);
        explanation.setVisible(false);
        explainButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                explanation.setVisible(true);
            }
        });
        explainButton.pack();
        explanation.pack();

        updateConnectionStatus();

        for (FileList fileList : this.fileLists) {
            attachListeners(fileList.getProjectID());
            updateEnabled(fileList.getProjectID());
        }
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

                    // remove the reserved name, because it's not used anymore
                    // and
                    // should not be included in tests anymore
                    EnterProjectNamePage.this.reservedProjectNames
                        .remove(projectID);

                    // quickly scan for existing project with the same name
                    Button projUpd = EnterProjectNamePage.this.projUpdates
                        .get(projectID);
                    Text updateProjectText = EnterProjectNamePage.this.updateProjectTexts
                        .get(projectID);
                    if (projUpd.getSelection()
                        && !EnterProjectNamePageUtils
                            .projectNameIsUnique(EnterProjectNamePage.this.remoteProjectNames
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

    /**
     * Sets page messages and disables finish button in case of the given
     * project name already exists. If no errors occur the finish button will be
     * enabled.
     * 
     * @param projectName
     *            project name for which to test. Must not be null.
     */
    protected String setPageCompleteTargetProject(String projectName,
        String projectID) {

        String errorMessage = null;

        if (projectName.length() == 0) {
            errorMessage = Messages.EnterProjectNamePage_set_project_name;
            this.errorProjectNames.put(projectID, errorMessage);
        } else {
            if (EnterProjectNamePageUtils.projectNameIsUnique(projectName,
                this.reservedProjectNames.values().toArray(new String[0]))) {
                this.errorProjectNames.remove(projectID);
            } else if (!EnterProjectNamePageUtils
                .projectNameIsUnique(projectName)) {
                // Project with name exists already in workspace
                errorMessage = MessageFormat.format(
                    Messages.EnterProjectNamePage_error_projectname_exists,
                    projectName);
                this.errorProjectNames.put(projectID, errorMessage);
            } else {
                // Project with name has already been declared
                errorMessage = MessageFormat.format(
                    Messages.EnterProjectNamePage_error_projectname_in_use,
                    projectName);
                this.errorProjectNames.put(projectID, errorMessage);
            }
        }

        this.updatePageState(errorMessage);
        return errorMessage;
    }

    /**
     * Updates the error message if any others still exist, otherwise activates
     * the finish button. This method also auto updates the warning message.
     * 
     * If the project name is correct, no error message should exist and it is
     * set to null. If null and there is a fault in any of the other tabs the
     * errorMessage is replaced by one of the current errors.
     * 
     * @param errorMessage
     *            the error message or <code>null</code> to clear the error
     *            message
     */
    protected void updatePageState(String errorMessage) {
        if (this.errorProjectNames.isEmpty()) {
            setPageComplete(true);
        } else {
            if (errorMessage == null && !this.errorProjectNames.isEmpty()) {
                errorMessage = this.errorProjectNames.entrySet().iterator()
                    .next().getValue();
            }
            setPageComplete(false);
        }
        setErrorMessage(errorMessage);

        findAndReportProjectArtifacts();
    }

    /**
     * Scans the current Eclipse Workspace for project artifacts and shows a
     * warning message if it will find any.
     */
    private void findAndReportProjectArtifacts() {
        IPath workspacePath = ResourcesPlugin.getWorkspace().getRoot()
            .getLocation();

        if (workspacePath == null)
            return;

        File workspaceDirectory = workspacePath.toFile();

        List<String> dirtyProjectNames = new ArrayList<String>();

        for (String projectID : remoteProjectNames.keySet()) {
            if (isUpdateSelected(projectID))
                continue;

            String projectName = newProjectNameTexts.get(projectID).getText();

            if (new File(workspaceDirectory, projectName).exists())
                dirtyProjectNames.add(projectName);
        }

        String warningMessage = null;

        if (!dirtyProjectNames.isEmpty()) {
            warningMessage = MessageFormat.format(
                Messages.EnterProjectNamePage_warning_project_artifacts_found,
                Utils.join(", ", dirtyProjectNames));
        }

        setMessage(warningMessage, WARNING);

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

        updatePageComplete(projectID);
    }

    /**
     * Updates the page for the given projectID
     * 
     * @param projectID
     *            for which the page shall be updated. Must not be null.
     */
    protected void updatePageComplete(String projectID) {

        String errorMessage = null;

        if (isSyncSkippingSelected(projectID)) {
            setMessage(Messages.EnterProjectNamePage_skip_synchronizing_warn,
                IMessageProvider.WARNING);
        } else {
            setMessage(null);
        }

        if (!isUpdateSelected(projectID)) {
            // Delete previous value first, to prevent the compare with it's own
            // value
            this.reservedProjectNames.remove(projectID);

            setPageCompleteTargetProject(this.newProjectNameTexts
                .get(projectID).getText(), projectID);

            this.reservedProjectNames.put(projectID, this.newProjectNameTexts
                .get(projectID).getText());
        } else {
            String newText = this.updateProjectTexts.get(projectID).getText();

            if (newText.length() == 0) {
                errorMessage = MessageFormat.format(
                    Messages.EnterProjectNamePage_error_set_projectname2,
                    this.remoteProjectNames.get(projectID));
                this.errorProjectNames.put(projectID, errorMessage);

            } else {

                if (ResourcesPlugin.getWorkspace().getRoot()
                    .getProject(newText).exists()) {

                    if (this.copyCheckboxes.get(projectID).getSelection()) {
                        errorMessage = setPageCompleteTargetProject(
                            this.copyToBeforeUpdateTexts.get(projectID)
                                .getText(), projectID);
                    } else {
                        this.errorProjectNames.remove(projectID);
                    }

                } else {
                    errorMessage = MessageFormat.format(
                        Messages.EnterProjectNamePage_error_wrong_name,
                        this.updateProjectTexts.get(projectID).getText());
                    this.errorProjectNames.put(projectID, errorMessage);
                }
            }

            this.updatePageState(errorMessage);
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
     * Returns whether the user has selected to skip synchronization
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
    public boolean overwriteResources(String projectID) {
        if (isUpdateSelected(projectID)
            && !copyCheckboxes.get(projectID).getSelection()
            && !isSyncSkippingSelected(projectID)) {
            return true;
        }
        return false;
    }

    @Override
    public void performHelp() {
        try {
            Desktop.getDesktop().browse(
                URI.create(Messages.EnterProjectNamePage_saros_url));
        } catch (IOException e) {
            SarosView.showNotification(Messages.EnterProjectNamePage_faq,
                Messages.EnterProjectNamePage_error_browser_open);
        }
    }
}
