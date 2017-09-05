package de.fu_berlin.inf.dpp.intellij.ui.wizards;

import java.awt.Window;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.util.ui.UIUtil;

import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.intellij.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.intellij.ui.util.NotificationPanel;
import de.fu_berlin.inf.dpp.intellij.ui.widgets.progress.ProgessMonitorAdapter;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.HeaderPanel;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.PageActionListener;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.SelectProjectPage;
import de.fu_berlin.inf.dpp.intellij.ui.wizards.pages.TextAreaPage;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.NullProgressMonitor;
import de.fu_berlin.inf.dpp.monitoring.SubProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.CancelListener;
import de.fu_berlin.inf.dpp.negotiation.FileList;
import de.fu_berlin.inf.dpp.negotiation.FileListDiff;
import de.fu_berlin.inf.dpp.negotiation.FileListFactory;
import de.fu_berlin.inf.dpp.negotiation.IncomingProjectNegotiation;
import de.fu_berlin.inf.dpp.negotiation.NegotiationTools;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiationData;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.util.ThreadUtils;

/**
 * Wizard for adding projects to a session.
 * <p/>
 * It consists of a selection page, where the user can select how to handle the
 * incoming project:
 * <ul>
 * <li> create a new project
 * <li reuse an existing project
 * </ul>
 * <p/>
 * If the option to reuse an existing project is chosen, a second page is displayed,
 * that displays the files necessary to modify.
 * <p/>
 *
 */

//  FIXME: Add facility for more than one project.

public class AddProjectToSessionWizard extends Wizard {
    private static final Logger LOG = Logger
        .getLogger(AddProjectToSessionWizard.class);

    public static final String SELECT_PROJECT_PAGE_ID = "selectProject";
    public static final String FILE_LIST_PAGE_ID = "fileListPage";

    private final String remoteProjectID;
    private final String remoteProjectName;

    private final IncomingProjectNegotiation negotiation;
    private final JID peer;

    private boolean triggered = false;

    /**
     * projectID => Project
     */
    private final Map<String, IProject> localProjects;

    @Inject
    private IChecksumCache checksumCache;

    @Inject
    private ISarosSessionManager sessionManager;

    @Inject
    private IWorkspace workspace;

    private final SelectProjectPage selectProjectPage;
    private final TextAreaPage fileListPage;

    private final PageActionListener selectProjectsPageListener = new PageActionListener() {
        @Override
        public void back() {
            // Nothing to do
        }

        /**
         * Extracts the local project's name (currently it has to be the same
         * as the original project) and either displays the filesChangedPage or
         * triggers the project negotiation
         */
        @Override
        public void next() {
            //FIXME: Only projects with the same name are supported,
            //because the project name is connected to the name of the .iml file
            //and it is unclear how that resolves.
            final String newProjectName = selectProjectPage
                .getLocalProjectName();
            IProject project = workspace.getProject(newProjectName);

            localProjects.put(remoteProjectID, project);

            if (localProjects.isEmpty()) {
                return;
            }
            if (!selectProjectPage.isNewProjectSelected()) {
                prepareFilesChangedPage(localProjects);
            } else {
                triggerProjectNegotiation();
            }
        }

        @Override
        public void cancel() {
            ThreadUtils.runSafeAsync(LOG, new Runnable() {
                @Override
                public void run() {
                    negotiation.localCancel("Not accepted",
                        NegotiationTools.CancelOption.NOTIFY_PEER);
                }

            });
            close();
        }
    };

    private final PageActionListener fileListPageListener = new PageActionListener() {
        @Override
        public void back() {
            // Nothing to do
        }

        @Override
        public void next() {
            triggerProjectNegotiation();
        }

        @Override
        public void cancel() {
            ThreadUtils.runSafeAsync(LOG, new Runnable() {
                @Override
                public void run() {
                    negotiation.localCancel("Not accepted",
                        NegotiationTools.CancelOption.NOTIFY_PEER);
                }

            });
            close();
        }
    };

    private final CancelListener cancelListener = new CancelListener() {

        @Override
        public void canceled(final NegotiationTools.CancelLocation location,
            final String message) {
            cancelWizard(peer, message, location);
        }
    };

    /**
     * Creates the wizard and its pages.
     *
     * @param negotiation  The IPN this wizard handles

     */
    public AddProjectToSessionWizard(Window parent,
        IncomingProjectNegotiation negotiation) {

        super(parent, Messages.AddProjectToSessionWizard_title,
            new HeaderPanel(Messages.EnterProjectNamePage_title2, ""));

        this.negotiation = negotiation;
        this.peer = negotiation.getPeer();


        List<ProjectNegotiationData> data = negotiation.getProjectNegotiationData();

        localProjects = new HashMap<String, IProject>();

        remoteProjectID = data.get(0).getProjectID();
        remoteProjectName = data.get(0).getProjectName();

        selectProjectPage = new SelectProjectPage(SELECT_PROJECT_PAGE_ID,
            remoteProjectName, remoteProjectName,
            workspace.getLocation().toOSString(), selectProjectsPageListener);

        registerPage(selectProjectPage);

        fileListPage = new TextAreaPage(FILE_LIST_PAGE_ID,
            "Local file changes:", fileListPageListener);
        registerPage(fileListPage);

        create();

        negotiation.addCancelListener(cancelListener);
    }

    /**
     * Cancels the wizard and gives an informative error message.
     */
    public void cancelWizard(final JID peer, final String errorMsg,
        NegotiationTools.CancelLocation type) {
        final String message = "Wizard canceled " + (type
            .equals(NegotiationTools.CancelLocation.LOCAL) ?
            "locally " :
            "remotely by " + peer);
        UIUtil.invokeLaterIfNeeded(new Runnable() {
            @Override
            public void run() {

                /*
                 *  if we already triggered the negotiation the message will
                 *  be displayed in the trigger logic, so do not popup another dialog here
                 */
                if (!triggered)
                    DialogUtils
                        .showInfo(AddProjectToSessionWizard.this, message,
                            message + (errorMsg != null ?
                                "\n\n" + errorMsg :
                                ""));

                close();
            }
        });

    }

    /**
     * Runs {@link IncomingProjectNegotiation#run(java.util.Map, IProgressMonitor)}
     * as a background task through {@link #runTask(Runnable, String)}.
     * <p/>
     * On success, a success notification is displayed, on error, a dialog is shown.
     */
    private void triggerProjectNegotiation() {

        if (triggered)
            return;

        triggered = true;

        ProgressManager.getInstance().run(
            new Task.Backgroundable(project, "Sharing project...", true,
                PerformInBackgroundOption.DEAF) {

                @Override
                public void run(ProgressIndicator indicator) {
                    final ProjectNegotiation.Status status = negotiation
                        .run(localProjects,
                            new ProgessMonitorAdapter(indicator));

                    indicator.stop();

                    UIUtil.invokeLaterIfNeeded(new Runnable() {
                        @Override
                        public void run() {
                            if (status == ProjectNegotiation.Status.ERROR) {
                                DialogUtils.showError(null,
                                    "Error during project negotiation",
                                    "The project could not be shared: "
                                        + negotiation.getErrorMessage());
                            } else if (status == ProjectNegotiation.Status.OK) {
                                NotificationPanel
                                    .showInformation("Project shared",
                                        "Project successfully shared");
                            } else
                                DialogUtils.showError(null,
                                    "Project negotiation aborted",
                                    "Project negotiation was canceled");
                        }
                    });
                }
            });

        close();
    }

    private void prepareFilesChangedPage(
        final Map<String, IProject> projectMapping) {

        final Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();

        runTask(new Runnable() {
            @Override
            public void run() {
                modifiedResources.putAll(
                    getModifiedResourcesFromMofifiableProjects(projectMapping,
                        new NullProgressMonitor()));
            }
        }, "Gathering files that have to be modified...");
        fillFileListPage(modifiedResources);
    }

    private void fillFileListPage(Map<String, FileListDiff> modifiedResources) {
        boolean empty = true;
        for (Map.Entry<String, FileListDiff> key : modifiedResources
            .entrySet()) {
            fileListPage.addLine("Project [" + key.getKey() + "]:");
            FileListDiff diff = modifiedResources.get(key.getKey());

            /// TODO folders

            for (String path : diff.getAlteredFiles()) {
                fileListPage.addLine("changed: " + path);
                empty = false;
            }

            for (String path : diff.getRemovedFiles()) {
                fileListPage.addLine("removed: " + path);
                empty = false;
            }

            for (String path : diff.getAddedFiles()) {
                fileListPage.addLine("added: " + path);
                empty = false;
            }
        }

        if (empty) {
            fileListPage.addLine("No files have to be modified.");
        }
    }

    /**
     * Creates a FileListDiff for all projects that will be modified.
     */
    private Map<String, FileListDiff> getModifiedResourcesFromMofifiableProjects(
        Map<String, IProject> projectMapping, IProgressMonitor monitor) {
        monitor.setTaskName("Calculating changed files...");

        final Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();
        final Map<String, IProject> modifiedProjects = new HashMap<String, IProject>();

        modifiedProjects.putAll(getModifiedProjects(projectMapping));
        modifiedResources
            .putAll(getModifiedResources(modifiedProjects, monitor));
        return modifiedResources;
    }

    /**
     * Returns a project mapping that contains all projects that will be
     * modified on synchronization.
     * <p/>
     * Currently these are simply all projects from projectMapping.
     * <p/>
     * FIXME: Add a check for non-overwritable projects.
     */
    private Map<String, IProject> getModifiedProjects(
        Map<String, IProject> projectMapping) {
        Map<String, IProject> modifiedProjects = new HashMap<String, IProject>();

        for (Map.Entry<String, IProject> entry : projectMapping.entrySet()) {
            //TODO: Add check for non-overwritable projects
            modifiedProjects.put(entry.getKey(), entry.getValue());
        }

        return modifiedProjects;
    }

    /**
     * Returns all modified resources (either changed or deleted) for the
     * current project mapping.
     * <p/>
     * <b>Important:</b> Do not call this inside the UI Thread. This is a long
     * running operation !
     */
    private Map<String, FileListDiff> getModifiedResources(
        Map<String, IProject> projectMapping, IProgressMonitor monitor) {
        Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();

        final ISarosSession session = sessionManager.getSession();

        // FIXME the wizard should handle the case that the session may stop in
        // the meantime !

        if (session == null) {
            throw new IllegalStateException("no session running");
        }

        SubProgressMonitor subMonitor = new SubProgressMonitor(monitor,
            projectMapping.size());

        subMonitor
            .setTaskName("\"Searching for files that will be modified...\",");

        for (Map.Entry<String, IProject> entry : projectMapping.entrySet()) {

            String projectID = entry.getKey();
            IProject project = entry.getValue();

            try {

                final ProjectNegotiationData data = negotiation.getProjectNegotiationData(projectID);

                if (data.isPartial())
                    throw new IllegalStateException("partial sharing is not supported");

                FileList localFileList = FileListFactory
                    .createFileList(project, null, checksumCache,
                        new SubProgressMonitor(monitor, 1,
                            SubProgressMonitor.SUPPRESS_SETTASKNAME));

                final FileListDiff diff = FileListDiff.diff(localFileList,
                    data.getFileList(),
                    false);

                if (!diff.getRemovedFolders().isEmpty()
                    || !diff.getRemovedFiles().isEmpty()
                    || !diff.getAlteredFiles().isEmpty()) {
                    modifiedResources.put(project.getName(), diff);
                }

            } catch (IOException e) {
                LOG.warn("could not refresh project: " + project, e);
            }
        }
        return modifiedResources;
    }
}
