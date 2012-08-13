package de.fu_berlin.inf.dpp.ui.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.project.internal.SarosSession;
import de.fu_berlin.inf.dpp.ui.BalloonNotification;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Offers convenient methods for collaboration actions like sharing a project
 * resources.
 * 
 * @author bkahlert
 * @author kheld
 */
public class CollaborationUtils {
    private static final Logger log = Logger
        .getLogger(CollaborationUtils.class);

    /**
     * Shares given project resources with given buddies.<br/>
     * Does nothing if a {@link SarosSession} is already running.
     * 
     * @param sarosSessionManager
     * @param selectedResources
     * @param buddies
     * 
     * @nonBlocking
     */
    public static void shareResourcesWith(
        final ISarosSessionManager sarosSessionManager,
        List<IResource> selectedResources, final List<JID> buddies) {

        final HashMap<IProject, List<IResource>> newResources = acquireResources(
            selectedResources, null);

        Utils.runSafeAsync(log, new Runnable() {
            public void run() {

                try {
                    sarosSessionManager.startSession(newResources);
                    addBuddiesToSarosSession(sarosSessionManager, buddies);
                } catch (final XMPPException e) {
                    log.error("starting the session failed", e);

                    Utils.popUpFailureMessage(
                        Messages.CollaborationUtils_offline,
                        getShareResourcesFailureMessage(newResources.keySet()),
                        false);
                }
            }
        });
    }

    /**
     * Leaves the currently running {@link SarosSession}<br/>
     * Does nothing if no {@link SarosSession} is running.
     * 
     * @param sarosSessionManager
     */
    public static void leaveSession(
        final ISarosSessionManager sarosSessionManager) {
        ISarosSession sarosSession = sarosSessionManager.getSarosSession();

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
            .getShell();

        if (sarosSession == null) {
            log.warn("cannot leave a non-running session");
            return;
        }

        boolean reallyLeave;

        if (sarosSession.isHost()) {
            if (sarosSession.getParticipants().size() == 1) {
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

        Utils.runSafeAsync(log, new Runnable() {
            public void run() {
                sarosSessionManager.stopSarosSession();
            }
        });
    }

    /**
     * Adds the given project resources to the session.<br/>
     * Does nothing if no {@link SarosSession} is running.
     * 
     * @param sarosSessionManager
     * @param resourcesToAdd
     * 
     * @nonBlocking
     */
    public static void addResourcesToSarosSession(
        final ISarosSessionManager sarosSessionManager,
        List<IResource> resourcesToAdd) {

        final ISarosSession sarosSession = sarosSessionManager
            .getSarosSession();

        if (sarosSession == null) {
            log.warn("cannot add resources to a non-running session");
            return;
        }

        final HashMap<IProject, List<IResource>> projectResources = acquireResources(
            resourcesToAdd, sarosSession);

        if (projectResources.isEmpty())
            return;

        Utils.runSafeAsync(log, new Runnable() {
            public void run() {

                if (sarosSession.hasWriteAccess()) {
                    sarosSessionManager.addResourcesToSession(projectResources);
                    return;
                }

                Utils.popUpFailureMessage(
                    Messages.CollaborationUtils_insufficient_privileges,
                    Messages.CollaborationUtils_insufficient_privileges_text,
                    false);
            }
        });
    }

    /**
     * Adds the given buddies to the session.<br/>
     * Does nothing if no {@link SarosSession} is running.
     * 
     * @param sarosSessionManager
     * @param buddies
     * 
     * @nonBlocking
     */
    public static void addBuddiesToSarosSession(
        final ISarosSessionManager sarosSessionManager, final List<JID> buddies) {

        final ISarosSession sarosSession = sarosSessionManager
            .getSarosSession();

        if (sarosSession == null) {
            log.warn("cannot add buddies to a non-running session");
            return;
        }

        Utils.runSafeAsync(log, new Runnable() {
            public void run() {

                Set<JID> participantsToAdd = new HashSet<JID>(buddies);

                for (User user : sarosSession.getParticipants())
                    participantsToAdd.remove(user.getJID());

                if (participantsToAdd.size() > 0) {
                    sarosSessionManager.invite(participantsToAdd,
                        getShareProjectDescription(sarosSession));
                }
            }
        });
    }

    /**
     * Creates the error message in case the user is offline.
     * 
     * @param projects
     * @return
     */
    private static String getShareResourcesFailureMessage(Set<IProject> projects) {

        String msg = MessageFormat
            .format(
                Messages.CollaborationUtils_error_not_connected,
                ((projects.size() == 1) ? Messages.CollaborationUtils_project_singular_ending
                    : Messages.CollaborationUtils_project_plural_ending));

        StringBuilder message = new StringBuilder();
        message.append(msg);

        for (IProject project : projects)
            message.append("\t" + project.getName() + "\n");

        message.append("\n");
        message.append(Messages.CollaborationUtils_make_sure_connected_to);
        return message.toString();
    }

    /**
     * Creates the message that invitees see on an incoming project share
     * request.
     * 
     * @param sarosSession
     * @return
     */
    private static String getShareProjectDescription(ISarosSession sarosSession) {

        JID inviter = sarosSession.getLocalUser().getJID();
        Set<IProject> projects = sarosSession.getProjects();

        StringBuilder result = new StringBuilder();

        result
            .append(MessageFormat.format(
                Messages.CollaborationUtils_user_invited_to_saros_session,
                inviter.getBase(),
                ((projects.size() == 1) ? Messages.CollaborationUtils_project_singular_ending
                    : Messages.CollaborationUtils_project_plural_ending)));

        for (IProject project : projects) {

            long projectSize = 0;
            long files = 0;

            Deque<IResource> stack = new LinkedList<IResource>();

            if (sarosSession.isCompletelyShared(project))
                stack.push(project);

            while (!stack.isEmpty()) {
                IResource resource = stack.pop();

                IContainer container = (IContainer) resource
                    .getAdapter(IContainer.class);

                if (container != null) {
                    try {
                        stack.addAll(Arrays.asList(container.members()));
                    } catch (CoreException e) {
                        log.warn("cannot calculate the correct project size", e);
                    }
                    continue;
                }

                IFile file = (IFile) resource.getAdapter(IFile.class);
                if (file != null) {
                    projectSize += file.getLocation().toFile().length();
                    files++;
                }
            }

            if (sarosSession.isCompletelyShared(project)) {
                result.append(String.format(
                    "\nProject: %s, Files: %d, Size: %s", project.getName(),
                    files, format(projectSize)));
            } else {
                result.append(String.format(
                    "\nProject: %s, Files: %s, Size: %s", project.getName()
                        + " " + Messages.CollaborationUtils_partial, "N/A",
                    "N/A"));
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
     * Adds to partial shared projects ".project" and ".classpath" files for
     * project recognition.
     * 
     * @param selectedResources
     * @param sarosSession
     * @return
     * 
     */
    private static HashMap<IProject, List<IResource>> acquireResources(
        List<IResource> selectedResources, ISarosSession sarosSession) {

        HashMap<IProject, List<IResource>> newResources = new HashMap<IProject, List<IResource>>();

        if (sarosSession != null)
            selectedResources.removeAll(sarosSession.getAllSharedResources());

        for (int i = 0; i < selectedResources.size(); i++) {
            IResource resource = selectedResources.get(i);

            if (resource instanceof IProject) {
                newResources.put((IProject) resource, null);
                continue;
            }

            // partial sharing stuff
            if (!newResources.containsKey(resource.getProject())) {
                IProject project = resource.getProject();
                List<IResource> projectResources = new ArrayList<IResource>();

                for (int j = i; j < selectedResources.size(); j++) {
                    if (!project.equals(selectedResources.get(j).getProject())) {
                        i = j - 1;
                        break;
                    }
                    projectResources.add(selectedResources.get(j));
                }
                if (!projectResources.contains(project.getFile(".project")))
                    projectResources.add(project.getFile(".project"));
                if (!projectResources.contains(project.getFile(".classpath")))
                    projectResources.add(project.getFile(".classpath"));
                newResources.put(project, projectResources);
            }
        }
        return newResources;
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
        return (Utils.popUpRememberDecisionDialog(
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
            return Utils.popUpCustomQuestion(
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
