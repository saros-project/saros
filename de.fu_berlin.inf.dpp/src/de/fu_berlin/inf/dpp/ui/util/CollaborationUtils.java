package de.fu_berlin.inf.dpp.ui.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.filesystem.EclipseProjectImpl;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.project.internal.SarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.util.FileUtils;
import de.fu_berlin.inf.dpp.util.Pair;
import de.fu_berlin.inf.dpp.util.ThreadUtils;

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
        // NOP
    }

    /**
     * Starts a new session and shares the given resources with given contacts.<br/>
     * Does nothing if a {@link ISarosSession session} is already running.
     * 
     * @param resources
     * @param contacts
     * 
     * @nonBlocking
     */
    public static void startSession(List<IResource> resources,
        final List<JID> contacts) {

        final Map<IProject, List<IResource>> newResources = acquireResources(
            resources, null);

        Job sessionStartupJob = new Job("Session Startup") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask("Starting session...",
                    IProgressMonitor.UNKNOWN);

                try {
                    sessionManager.startSession(convert(newResources));
                    Set<JID> participantsToAdd = new HashSet<JID>(contacts);

                    ISarosSession session = sessionManager.getSarosSession();

                    if (session == null)
                        return Status.CANCEL_STATUS;

                    sessionManager.invite(participantsToAdd,
                        getShareProjectDescription(session));

                } catch (Exception e) {

                    LOG.error("could not start a Saros session", e);
                    return new Status(IStatus.ERROR, Saros.SAROS,
                        e.getMessage(), e);
                }

                return Status.OK_STATUS;
            }
        };

        sessionStartupJob.setPriority(Job.SHORT);
        sessionStartupJob.setUser(true);
        sessionStartupJob.schedule();
    }

    /**
     * Leaves the currently running {@link SarosSession}<br/>
     * Does nothing if no {@link SarosSession} is running.
     * 
     */
    public static void leaveSession() {

        ISarosSession sarosSession = sessionManager.getSarosSession();

        Shell shell = SWTUtils.getShell();

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
                reallyLeave = MessageDialog.openQuestion(shell,
                    Messages.CollaborationUtils_confirm_closing,
                    Messages.CollaborationUtils_confirm_closing_text);
            }
        } else {
            reallyLeave = MessageDialog.openQuestion(shell,
                Messages.CollaborationUtils_confirm_leaving,
                Messages.CollaborationUtils_confirm_leaving_text);
        }

        if (!reallyLeave)
            return;

        ThreadUtils.runSafeAsync("StopSession", LOG, new Runnable() {
            @Override
            public void run() {
                sessionManager.stopSarosSession();
            }
        });
    }

    /**
     * Adds the given project resources to the session.<br/>
     * Does nothing if no {@link SarosSession session} is running.
     * 
     * @param resourcesToAdd
     * 
     * @nonBlocking
     */
    public static void addResourcesToSession(List<IResource> resourcesToAdd) {

        final ISarosSession sarosSession = sessionManager.getSarosSession();

        if (sarosSession == null) {
            LOG.warn("cannot add resources to a non-running session");
            return;
        }

        final Map<IProject, List<IResource>> projectResources = acquireResources(
            resourcesToAdd, sarosSession);

        if (projectResources.isEmpty())
            return;

        ThreadUtils.runSafeAsync("AddResourceToSession", LOG, new Runnable() {
            @Override
            public void run() {

                if (sarosSession.hasWriteAccess()) {
                    sessionManager
                        .addResourcesToSession(convert(projectResources));
                    return;
                }

                DialogUtils.popUpFailureMessage(
                    Messages.CollaborationUtils_insufficient_privileges,
                    Messages.CollaborationUtils_insufficient_privileges_text,
                    false);
            }
        });
    }

    /**
     * Adds the given contacts to the session.<br/>
     * Does nothing if no {@link ISarosSession session} is running.
     * 
     * @param contacts
     * 
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

                for (User user : sarosSession.getUsers())
                    participantsToAdd.remove(user.getJID());

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
    private static String getShareProjectDescription(ISarosSession sarosSession) {

        Set<de.fu_berlin.inf.dpp.filesystem.IProject> projects = sarosSession
            .getProjects();

        StringBuilder result = new StringBuilder();

        for (de.fu_berlin.inf.dpp.filesystem.IProject project : projects) {

            Pair<Long, Long> fileCountAndSize;

            if (sarosSession.isCompletelyShared(project)) {
                fileCountAndSize = FileUtils.getFileCountAndSize(
                    Collections.singletonList(((EclipseProjectImpl) project)
                        .getDelegate()), true, IContainer.EXCLUDE_DERIVED);

                result.append(String.format(
                    "\nProject: %s, Files: %d, Size: %s", project.getName(),
                    fileCountAndSize.v, format(fileCountAndSize.p)));
            } else {
                List<IResource> resources = ResourceAdapterFactory
                    .convertBack(sarosSession.getSharedResources(project));

                fileCountAndSize = FileUtils.getFileCountAndSize(resources,
                    false, IResource.NONE);

                result.append(String.format(
                    "\nProject: %s, Files: %s, Size: %s", project.getName()
                        + " " + Messages.CollaborationUtils_partial,
                    fileCountAndSize.v, format(fileCountAndSize.p)));
            }
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
     * 
     */
    private static Map<IProject, List<IResource>> acquireResources(
        List<IResource> selectedResources, ISarosSession sarosSession) {

        Map<IProject, Set<IResource>> projectsResources = new HashMap<IProject, Set<IResource>>();

        /*
         * TODO Check whether this works, because the different IResources look
         * like they are conflicting here.
         */
        if (sarosSession != null)
            selectedResources.removeAll(sarosSession.getSharedResources());

        final int resourcesSize = selectedResources.size();

        IResource[] preSortedResources = new IResource[resourcesSize];

        int frontIdx = 0;
        int backIdx = resourcesSize - 1;

        // move projects to the front so the algorithm is working as expected
        for (IResource resource : selectedResources) {
            if (resource.getType() == IResource.PROJECT)
                preSortedResources[frontIdx++] = resource;
            else
                preSortedResources[backIdx--] = resource;
        }

        for (IResource resource : preSortedResources) {

            if (resource.getType() == IResource.PROJECT) {
                projectsResources.put((IProject) resource, null);
                continue;
            }

            IProject project = resource.getProject();

            if (project == null)
                continue;

            if (!projectsResources.containsKey(project))
                projectsResources.put(project, new HashSet<IResource>());

            Set<IResource> resources = projectsResources.get(project);

            // if the resource set is null, it is a full shared project
            if (resources != null)
                resources.add(resource);
        }

        List<IResource> additionalFilesForPartialSharing = new ArrayList<IResource>();

        for (Entry<IProject, Set<IResource>> entry : projectsResources
            .entrySet()) {

            IProject project = entry.getKey();
            Set<IResource> resources = entry.getValue();

            if (resources == /* full shared */null)
                continue;

            additionalFilesForPartialSharing.clear();

            /*
             * we need this file otherwise creating a new project on the remote
             * will produce garbage because the project nature is not set /
             * updated correctly
             */
            IFile projectFile = project.getFile(".project");

            if (projectFile.exists())
                additionalFilesForPartialSharing.add(projectFile);

            // do not include them, this is causing malfunctions if developers
            // do
            // not use variables in their classpath but absolute paths.

            // IFile classpathFile = project.getFile(".classpath");

            // if (classpathFile.exists())
            // additionalFilesForPartialSharing.add(classpathFile);

            /*
             * FIXME adding files from this folder may "corrupt" a lot of remote
             * files. The byte content will not be corrupted, but the document
             * provider (editor) will fail to render the file input correctly. I
             * think we should negotiate the project encodings and forbid
             * further proceeding if they do not match ! The next step should be
             * to also transmit the encoding in FileActivites, because it is
             * possible to change the encoding of files independently of the
             * project encoding settings.
             */

            IFolder settingsFolder = project.getFolder(".settings");

            if (settingsFolder.exists() /* remove to execute block */&& false) {

                additionalFilesForPartialSharing.add(settingsFolder);

                try {
                    for (IResource resource : settingsFolder.members()) {
                        // TODO are sub folders possible ?
                        if (resource.getType() == IResource.FILE)
                            additionalFilesForPartialSharing.add(resource);
                    }
                } catch (CoreException e) {
                    LOG.warn(
                        "could not read the contents of the settings folder", e);
                }
            }

            resources.addAll(additionalFilesForPartialSharing);
        }

        HashMap<IProject, List<IResource>> resources = new HashMap<IProject, List<IResource>>();

        for (Entry<IProject, Set<IResource>> entry : projectsResources
            .entrySet())
            resources.put(entry.getKey(), entry.getValue() == null ? null
                : new ArrayList<IResource>(entry.getValue()));

        return resources;
    }

    private static String format(long size) {

        if (size < 1000)
            return "< 1 KB";

        if (size < 1000 * 1000)
            return String.format(Locale.US, "%.2f KB", size / (1000F));

        if (size < 1000 * 1000 * 1000)
            return String.format(Locale.US, "%.2f MB", size / (1000F * 1000F));

        return String.format(Locale.US, "%.2f GB", size
            / (1000F * 1000F * 1000F));
    }

    private static Map<de.fu_berlin.inf.dpp.filesystem.IProject, List<de.fu_berlin.inf.dpp.filesystem.IResource>> convert(
        Map<IProject, List<IResource>> data) {

        Map<de.fu_berlin.inf.dpp.filesystem.IProject, List<de.fu_berlin.inf.dpp.filesystem.IResource>> result = new HashMap<de.fu_berlin.inf.dpp.filesystem.IProject, List<de.fu_berlin.inf.dpp.filesystem.IResource>>();

        for (Entry<IProject, List<IResource>> entry : data.entrySet())
            result.put(ResourceAdapterFactory.create(entry.getKey()),
                ResourceAdapterFactory.convertTo(entry.getValue()));

        return result;
    }
}
