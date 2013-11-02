package de.fu_berlin.inf.dpp.ui.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.RandomAccess;
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
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.project.internal.SarosSession;
import de.fu_berlin.inf.dpp.ui.BalloonNotification;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.util.FileUtils;
import de.fu_berlin.inf.dpp.util.Pair;
import de.fu_berlin.inf.dpp.util.Utils;

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
                    sessionManager.startSession(newResources);
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

        Shell shell = EditorAPI.getShell();

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

        Utils.runSafeAsync("StopSession", LOG, new Runnable() {
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

        Utils.runSafeAsync("AddResourceToSession", LOG, new Runnable() {
            @Override
            public void run() {

                if (sarosSession.hasWriteAccess()) {
                    sessionManager.addResourcesToSession(projectResources);
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

        Utils.runSafeAsync("AddContactToSession", LOG, new Runnable() {
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

        Set<IProject> projects = sarosSession.getProjects();

        StringBuilder result = new StringBuilder();

        for (IProject project : projects) {

            Pair<Long, Long> fileCountAndSize;

            if (sarosSession.isCompletelyShared(project)) {
                fileCountAndSize = FileUtils.getFileCountAndSize(
                    Collections.singletonList(project), true,
                    IContainer.EXCLUDE_DERIVED);

                result.append(String.format(
                    "\nProject: %s, Files: %d, Size: %s", project.getName(),
                    fileCountAndSize.v, format(fileCountAndSize.p)));
            } else {
                fileCountAndSize = FileUtils.getFileCountAndSize(
                    sarosSession.getSharedResources(project), false,
                    IResource.NONE);

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

        if (sarosSession != null)
            selectedResources.removeAll(sarosSession.getSharedResources());

        // do not sort LinkedLists which would be a complete overkill
        if (!(selectedResources instanceof RandomAccess))
            selectedResources = new ArrayList<IResource>(selectedResources);

        // move projects to the front so the algorithm is working as expected
        Collections.sort(selectedResources, new Comparator<IResource>() {

            @Override
            public int compare(IResource a, IResource b) {
                if (a.getType() == b.getType())
                    return 0;

                if (a.getType() == IResource.PROJECT)
                    return -1;

                return 1;
            }

        });

        for (IResource resource : selectedResources) {

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

            IFile projectFile = project.getFile(".project");
            IFile classpathFile = project.getFile(".classpath");
            IFolder settingsFolder = project.getFolder(".settings");

            if (projectFile.exists())
                additionalFilesForPartialSharing.add(projectFile);

            if (classpathFile.exists())
                additionalFilesForPartialSharing.add(classpathFile);

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

    /**
     * Method to ask for the decision of a user, if he wanted to activate the
     * need based synchronization.
     * 
     * @param saros
     *            Saros instance is needed to store decision in
     *            {@link PreferenceStore}.
     * @return <b>true</b> if the user decides to activate the need based
     *         synchronization<br>
     *         <b>false</b> in the case the user decides not to use the need
     *         based synchronization
     */
    public static boolean activateNeedBasedSynchronization(Saros saros) {
        return (DialogUtils.popUpRememberDecisionDialog(
            Messages.CollaborationUtils_confirm_need_based,
            Messages.CollaborationUtils_confirm_need_based_text, saros,
            PreferenceConstants.NEEDS_BASED_SYNC));
    }

    /**
     * Method to ask the participants who received a need based file whether to
     * overwrite or backup the own file in workspace.
     * 
     * @param fileName
     * @param userName
     * @param showDialog
     *            <b>true</b> opens a {@link MessageDialog}, <b>false</b> opens
     *            a {@link BalloonNotification}
     * 
     * @return <b>true</b> The user wants to backup the file. (just in case
     *         {@link MessageDialog} is used)<br>
     *         <b>false</b> The user wants to overwrite the file. (just in case
     *         {@link MessageDialog} is used)
     */
    public static boolean needBasedFileHandlingDialog(String userName,
        String fileName, boolean showDialog) {
        String message = MessageFormat.format(
            Messages.CollaborationUtils_confirm_need_based_file_text, userName,
            fileName);
        String messageBalloon = MessageFormat.format(
            Messages.CollaborationUtils_confirm_need_based_file_balloon_text,
            userName, fileName);
        if (showDialog) {
            return DialogUtils.popUpCustomQuestion(
                Messages.CollaborationUtils_confirm_need_based_file, message,
                new String[] { "Create Backup", "Overwrite" }, false);
        } else {
            SarosView.showNotification("New file in session!", messageBalloon);
            return true;
        }
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
}
