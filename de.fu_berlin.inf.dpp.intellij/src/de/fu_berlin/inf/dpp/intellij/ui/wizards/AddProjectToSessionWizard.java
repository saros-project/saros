/*
 *
 *  DPP - Serious Distributed Pair Programming
 *  (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2010
 *  (c) NFQ (www.nfq.com) - 2014
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 1, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * /
 */

package de.fu_berlin.inf.dpp.intellij.ui.wizards;

import com.intellij.util.ui.UIUtil;
import de.fu_berlin.inf.dpp.core.invitation.IncomingProjectNegotiation;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.IntelliJProjectImpl;
import de.fu_berlin.inf.dpp.intellij.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.intellij.ui.util.JobWithStatus;
import de.fu_berlin.inf.dpp.intellij.ui.util.NotificationPanel;
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
import de.fu_berlin.inf.dpp.negotiation.NegotiationTools;
import de.fu_berlin.inf.dpp.negotiation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wizard for adding projects to a session.
 *
 * It consists of a selection page, where the user can select how to handle the
 * incoming project:
 * <ul>
 *     <li> create a new project
 *     <li reuse an existing project
 * </ul>
 *
 * If the option to reuse an existing project is chosen, a second page is displayed,
 * that displays the files necessary to modify.
 *
 * FIXME: Add facility for more than one project.
 */
public class AddProjectToSessionWizard extends Wizard {
    private static final Logger LOG = Logger
        .getLogger(AddProjectToSessionWizard.class);

    public static final String SELECT_PROJECT_PAGE_ID = "selectProject";
    public static final String FILE_LIST_PAGE_ID = "fileListPage";

    private final String remoteProjectID;
    private final String remoteProjectName;

    private final IncomingProjectNegotiation negotiation;
    private final JID peer;
    private final List<FileList> fileLists;

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
            IProject project = saros.getWorkspace().getProject(newProjectName);

            localProjects.put(remoteProjectID, project);

            if (localProjects.isEmpty()) {
                return;
            }
            createAndOpenProjects(localProjects);
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
     * @param negotiation The IPN this wizard handles
     * @param peer The peer
     * @param fileLists The list of resources to be shared
     * @param projectNames The names of the projects to be shared
     */
    public AddProjectToSessionWizard(IncomingProjectNegotiation negotiation,
        JID peer, List<FileList> fileLists, Map<String, String> projectNames) {

        super(Messages.AddProjectToSessionWizard_title,
            new HeaderPanel(
                Messages.EnterProjectNamePage_title2, ""));

        this.negotiation = negotiation;
        this.peer = peer;
        this.fileLists = fileLists;
        localProjects = new HashMap<String, IProject>();

        remoteProjectID = projectNames.keySet().iterator().next();
        remoteProjectName = projectNames.values().iterator().next();

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
        final String message = "Wizard canceled "
            + (type.equals(NegotiationTools.CancelLocation.LOCAL) ?
              "locally " :
              "remotely ")
            + "by " + peer;
        UIUtil.invokeLaterIfNeeded(new Runnable() {
            @Override
            public void run() {
                DialogUtils.showInfo(AddProjectToSessionWizard.this, message,
                    message + (errorMsg != null ? "\n\n" + errorMsg : ""));
                close();
            }
        });

    }

    /**
     * Runs {@link IncomingProjectNegotiation#run(java.util.Map, IProgressMonitor, boolean)}
     * as a background task through {@link #runTask(Runnable, String)}.
     *
     * On success, a success notification is displayed, on error, a dialog is shown.
     */
    private void triggerProjectNegotiation() {

        JobWithStatus job = new JobWithStatus() {
            @Override
            public void run() {
                status = negotiation
                    .run(localProjects, new NullProgressMonitor(), false);
            }
        };
        runTask(job, "Sharing project...");
        if (job.status != ProjectNegotiation.Status.OK) {
            DialogUtils.showError(null, "Error during project negotiation",
                "The project could not be shared");
        } else {
            NotificationPanel.showNotification("Project shared",
                "Project successfully shared");
        }
        close();
    }

    private void prepareFilesChangedPage(
        final Map<String, IProject> projectMapping) {

        final Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();

        runTask(new Runnable() {
                    @Override
                    public void run() {
                        modifiedResources.putAll(
                            getModifiedResourcesFromMofifiableProjects(
                                projectMapping, new NullProgressMonitor())
                        );
                    }
                }, "Gathering files that have to be modified..."
        );
        fillFileListPage(modifiedResources);
    }

    private void fillFileListPage(Map<String, FileListDiff> modifiedResources) {
        boolean empty = true;
        for (Map.Entry<String, FileListDiff> key : modifiedResources.entrySet()) {
            fileListPage.addLine("Project [" + key.getKey() + "]:");
            FileListDiff diff = modifiedResources.get(key.getKey());
            for (String path : diff.getAlteredPaths()) {
                fileListPage.addLine("changed: " + path);
                empty = false;
            }

            for (String path : diff.getRemovedPaths()) {
                fileListPage.addLine("removed: " + path);
                empty = false;
            }

            for (String path : diff.getAddedPaths()) {
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
     *
     * Currently these are simply all projects from projectMapping.
     *
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
     * Goes through the list of local projects, calls {@link IntelliJProjectImpl#create()}
     * on non-existent ones and {@link IProject#open()} on non-opened projects.
     *
     * @param projectMapping
     */
    private void createAndOpenProjects(Map<String, IProject> projectMapping) {

        for (Map.Entry<String, IProject> entry : projectMapping.entrySet()) {
            IProject project = entry.getValue();
            try {
                if (!project.exists()) {
                    ((IntelliJProjectImpl) project).create();
                }

                if (!project.isOpen()) {
                    project.open();
                }

            } catch (IOException e) {
                LOG.error("Could not create project", e);
                DialogUtils
                    .showError(this, "Could not create project.", "Error");
            }
        }
    }

    /**
     * Returns all modified resources (either changed or deleted) for the
     * current project mapping.
     *
     * <b>Important:</b> Do not call this inside the UI Thread. This is a long
     * running operation !
     */
    private Map<String, FileListDiff> getModifiedResources(
        Map<String, IProject> projectMapping, IProgressMonitor monitor) {
        Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();

        final ISarosSession session = sessionManager.getSarosSession();

        // FIXME the wizard should handle the case that the session may stop in
        // the meantime !

        if (session == null) {
            throw new IllegalStateException("no session running");
        }

        SubProgressMonitor subMonitor = new SubProgressMonitor(monitor,
            projectMapping.size() * 2);
        subMonitor
            .setTaskName("\"Searching for files that will be modified...\",");

        for (Map.Entry<String, IProject> entry : projectMapping.entrySet()) {

            String projectID = entry.getKey();
            IProject project = entry.getValue();

            FileListDiff diff;

            FileList remoteFileList = negotiation.getRemoteFileList(projectID);

            try {
                if (session.isShared(project)) {
                    List<IResource> eclipseResources = session
                        .getSharedResources(project);

                    FileList sharedFileList = FileListFactory
                        .createFileList(project, eclipseResources,
                            checksumCache, null,
                            new SubProgressMonitor(monitor, 1,
                                SubProgressMonitor.SUPPRESS_SETTASKNAME)
                        );

                    remoteFileList.getPaths().addAll(sharedFileList.getPaths());
                } else {
                    subMonitor.worked(1);
                }

                FileList localFileList = FileListFactory
                    .createFileList(project, null, checksumCache, null,
                        new SubProgressMonitor(monitor, 1,
                            SubProgressMonitor.SUPPRESS_SETTASKNAME)
                    );
                diff = FileListDiff.diff(localFileList, remoteFileList);

                if (negotiation.isPartialRemoteProject(projectID)) {
                    diff.clearRemovedPaths();
                }

                if (!diff.getRemovedPaths().isEmpty() || !diff.getAlteredPaths()
                    .isEmpty()) {
                    modifiedResources.put(project.getName(), diff);
                }

            } catch (IOException e) {
                LOG.warn("could not refresh project: " + project, e);
            }
        }
        return modifiedResources;
    }
}
