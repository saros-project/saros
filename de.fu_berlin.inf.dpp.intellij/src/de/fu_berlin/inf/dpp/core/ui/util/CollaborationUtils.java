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

package de.fu_berlin.inf.dpp.core.ui.util;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.core.Saros;
import de.fu_berlin.inf.dpp.core.monitoring.IStatus;
import de.fu_berlin.inf.dpp.core.monitoring.Status;
import de.fu_berlin.inf.dpp.core.util.FileUtils;
import de.fu_berlin.inf.dpp.filesystem.IContainer;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.IntelliJFolderImpl;
import de.fu_berlin.inf.dpp.intellij.project.filesystem.IntelliJProjectImpl;
import de.fu_berlin.inf.dpp.intellij.runtime.UIMonitoredJob;
import de.fu_berlin.inf.dpp.intellij.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.internal.SarosSession;
import de.fu_berlin.inf.dpp.util.Pair;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Offers convenient methods for collaboration actions like sharing a project
 * resources.
 *
 * @author bkahlert
 * @author kheld
 */
public class CollaborationUtils {

    private static final Logger LOG = Logger
        .getLogger(CollaborationUtils.class);

    @Inject
    private static ISarosSessionManager sessionManager;

    static {
        SarosPluginContext.initComponent(new CollaborationUtils());
    }

    private CollaborationUtils() {

    }

    /**
     * Starts a new session and shares the given resources with given contacts.<br/>
     * Does nothing if a {@link ISarosSession session} is already running.
     *
     * @param resources
     * @param contacts
     * @nonBlocking
     */
    public static void startSession(List<IResource> resources,
        final List<JID> contacts) {

        final Map<IProject, List<IResource>> newResources = acquireResources(
            resources, null);

        UIMonitoredJob sessionStartupJob = new UIMonitoredJob(
            "Session Startup") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor
                    .beginTask("Starting session...", IProgressMonitor.UNKNOWN);
                try {
                    sessionManager.startSession(newResources);
                    Set<JID> participantsToAdd = new HashSet<JID>(contacts);

                    monitor.worked(50);

                    ISarosSession session = sessionManager.getSarosSession();

                    if (session == null) {
                        return Status.CANCEL_STATUS;
                    }
                    monitor.setTaskName("Inviting participants...");
                    sessionManager.invite(participantsToAdd,
                        getShareProjectDescription(session));

                    monitor.done();

                } catch (Exception e) {

                    LOG.error("could not start a Saros session", e);
                    return new Status(IStatus.ERROR, Saros.PLUGIN_ID,
                        e.getMessage(), e);
                }

                return Status.OK_STATUS;
            }
        };

        sessionStartupJob.schedule();

    }

    /**
     * Leaves the currently running {@link SarosSession}<br/>
     * Does nothing if no {@link SarosSession} is running.
     */
    public static void leaveSession() {

        ISarosSession sarosSession = sessionManager.getSarosSession();

        if (sarosSession == null) {
            LOG.warn("cannot leave a non-running session");
            return;
        }

        boolean reallyLeave;

        if (sarosSession.isHost()) {
            if (sarosSession.getUsers().size() == 1) {
                // Do not ask when host is alone...
                reallyLeave = true;
            } else {
                reallyLeave = DialogUtils.showConfirm(null,
                    Messages.CollaborationUtils_confirm_closing,
                    Messages.CollaborationUtils_confirm_closing_text);
            }
        } else {
            reallyLeave = DialogUtils
                .showConfirm(null, Messages.CollaborationUtils_confirm_leaving,
                    Messages.CollaborationUtils_confirm_leaving_text);
        }

        if (!reallyLeave) {
            return;
        }

        ThreadUtils.runSafeAsync("StopSession", LOG, new Runnable() {
            @Override
            public void run() {
                sessionManager.stopSarosSession(SessionEndReason.LOCAL_USER_LEFT);
            }
        });
    }

    /**
     * Adds the given project resources to the session.<br/>
     * Does nothing if no {@link SarosSession session} is running.
     *
     * @param resourcesToAdd
     * @nonBlocking
     */
    public static void addResourcesToSession(List<IResource> resourcesToAdd) {

        final ISarosSession sarosSession = sessionManager.getSarosSession();

        if (sarosSession == null) {
            LOG.warn("cannot add resources to a non-running session");
            return;
        }

        final Map<IProject, List<IResource>> projectResources;

        projectResources = acquireResources(resourcesToAdd, sarosSession);

        if (projectResources.isEmpty()) {
            return;
        }

        ThreadUtils.runSafeAsync("AddResourceToSession", LOG, new Runnable() {
            @Override
            public void run() {

                if (sarosSession.hasWriteAccess()) {
                    sessionManager.addResourcesToSession(projectResources);
                    return;
                }

                DialogUtils.showError(null,
                    Messages.CollaborationUtils_insufficient_privileges,
                    Messages.CollaborationUtils_insufficient_privileges_text);
            }
        });
    }

    /**
     * Adds the given contacts to the session.<br/>
     * Does nothing if no {@link ISarosSession session} is running.
     *
     * @param contacts
     * @nonBlocking
     */
    public static void addContactsToSession(final List<JID> contacts) {

        final ISarosSession sarosSession = sessionManager.getSarosSession();

        if (sarosSession == null) {
            LOG.warn("cannot add contacts to a non-running session");
            return;
        }

        ThreadUtils.runSafeAsync("AddContactToSession", LOG, new Runnable() {
            @Override
            public void run() {

                Set<JID> participantsToAdd = new HashSet<JID>(contacts);

                for (User user : sarosSession.getUsers()) {
                    participantsToAdd.remove(user.getJID());
                }

                if (participantsToAdd.size() > 0) {
                    sessionManager.invite(participantsToAdd,
                        getShareProjectDescription(sarosSession));

                }
            }
        });
    }

    /**
     * Creates the message that invitees see on an incoming project share
     * request. Currently it contains the project names along with the number of
     * shared files and total file size for each shared project.
     *
     * @param sarosSession
     * @return
     */
    private static String getShareProjectDescription(
        ISarosSession sarosSession) {

        Set<IProject> projects = sarosSession.getProjects();

        StringBuilder result = new StringBuilder();

        try {
            for (IProject project : projects) {

                Pair<Long, Long> fileCountAndSize;

                if (sarosSession.isCompletelyShared(project)) {
                    fileCountAndSize = FileUtils
                        .getFileCountAndSize(Arrays.asList(project.members()),
                            true, IContainer.FILE);

                    result.append(String
                        .format("\nProject: %s, Files: %d, Size: %s",
                            project.getName(), fileCountAndSize.v,
                            format(fileCountAndSize.p)));
                } else {
                    List<IResource> resources = sarosSession
                        .getSharedResources(project);

                    fileCountAndSize = FileUtils
                        .getFileCountAndSize(resources, false, IResource.NONE);

                    result.append(String
                        .format("\nProject: %s, Files: %s, Size: %s",
                            project.getName() + " "
                                + Messages.CollaborationUtils_partial,
                            fileCountAndSize.v, format(fileCountAndSize.p)
                        ));
                }
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return "Could not get description";
        }

        return result.toString();
    }

    /**
     * Decides if selected resource is a complete shared project in contrast to
     * partial shared ones. The result is stored in {@link HashMap}:
     * <ul>
     * <li>complete shared project: {@link IProject} --> null
     * <li>partial shared project: {@link IProject} --> List<IResource>
     * </ul>
     * Adds to partial shared projects additional files which are needed for
     * proper project synchronization.
     *
     * @param selectedResources
     * @param sarosSession
     * @return
     */
    private static Map<IProject, List<IResource>> acquireResources(
        List<IResource> selectedResources, ISarosSession sarosSession) {

        Map<IProject, Set<IResource>> projectsResources = new HashMap<IProject, Set<IResource>>();

        if (sarosSession != null) {
            selectedResources.removeAll(sarosSession.getSharedResources());
        }

        final int resourcesSize = selectedResources.size();

        IResource[] preSortedResources = new IResource[resourcesSize];

        int frontIdx = 0;
        int backIdx = resourcesSize - 1;

        // move projects to the front so the algorithm is working as expected
        for (IResource resource : selectedResources) {
            if (resource.getType() == IResource.PROJECT) {
                preSortedResources[frontIdx++] = resource;
            } else {
                preSortedResources[backIdx--] = resource;
            }
        }

        for (IResource resource : preSortedResources) {

            if (resource.getType() == IResource.PROJECT) {
                projectsResources.put((IProject) resource, null);
                continue;
            }

            IProject project = resource.getProject();

            if (project == null) {
                continue;
            }

            if (!projectsResources.containsKey(project)) {
                projectsResources.put(project, new HashSet<IResource>());
            }

            Set<IResource> resources = projectsResources.get(project);

            // if the resource set is null, it is a full shared project
            if (resources != null) {
                resources.add(resource);
            }
        }

        for (Entry<IProject, Set<IResource>> entry : projectsResources
            .entrySet()) {

            IProject project = entry.getKey();
            Set<IResource> resources = entry.getValue();

            if (resources == // * full shared *//*
                null) {
                continue;
            }

            List<IResource> additionalFilesForPartialSharing = new ArrayList<IResource>();

            /*
             * we need the .iml file, otherwise the project type will not be set
             * correctly on the other side
             */
            IFolder projectFolder = new IntelliJFolderImpl((IntelliJProjectImpl) project,
                project.getFullPath().toFile());
            try {
                for (IResource pFile : projectFolder.members(IResource.FILE)) {
                    String sFileName = pFile.getName().toLowerCase();
                    if (sFileName.endsWith(".iml")) {
                        additionalFilesForPartialSharing.add(pFile);
                    }
                }
            } catch (IOException e) {
                LOG.warn("could not open .iml file in " + projectFolder, e);
            }

            resources.addAll(additionalFilesForPartialSharing);

        }

        HashMap<IProject, List<IResource>> resources = new HashMap<IProject, List<IResource>>();

        for (Entry<IProject, Set<IResource>> entry : projectsResources
            .entrySet()) {
            resources.put(entry.getKey(), entry.getValue() == null ?
                null :
                new ArrayList<IResource>(entry.getValue()));
        }

        return resources;
    }

    private static void addRecursively(List<IResource> fileList,
        IResource resource) throws Exception {

        if (resource.getType() == IResource.FILE) {
            fileList.add(resource);
        }
        if (resource.getType() == IResource.FOLDER) {
            fileList.add(resource);
            for (IResource myResource : ((IntelliJFolderImpl) resource).members()) {
                addRecursively(fileList, myResource);
            }
        }

    }

    private static String format(long size) {

        if (size < 1000) {
            return "< 1 KB";
        }

        if (size < 1000 * 1000) {
            return String.format(Locale.US, "%.2f KB", size / (1000F));
        }

        if (size < 1000 * 1000 * 1000) {
            return String.format(Locale.US, "%.2f MB", size / (1000F * 1000F));
        }

        return String
            .format(Locale.US, "%.2f GB", size / (1000F * 1000F * 1000F));
    }

}
