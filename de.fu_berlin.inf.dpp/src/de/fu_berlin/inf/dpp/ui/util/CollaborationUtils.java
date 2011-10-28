package de.fu_berlin.inf.dpp.ui.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
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
        final SarosSessionManager sarosSessionManager,
        List<IResource> selectedResources, final List<JID> buddies) {

        final HashMap<IProject, List<IResource>> newResources = acquireResources(
            selectedResources, null);

        Utils.runSafeAsync(log, new Runnable() {
            public void run() {
                if (sarosSessionManager.getSarosSession() == null) {
                    try {
                        sarosSessionManager.startSession(newResources);
                    } catch (final XMPPException e) {
                        Utils.runSafeSWTSync(log, new Runnable() {
                            public void run() {
                                MessageDialog
                                    .openError(
                                        null,
                                        Messages.CollaborationUtils_offline,
                                        getShareResourcesFailureMessage(newResources
                                            .keySet()));
                                log.warn("Start share project failed", e); //$NON-NLS-1$
                            }
                        });
                    }

                    addBuddiesToSarosSession(sarosSessionManager, buddies);
                } else {
                    log.warn("Tried to start " //$NON-NLS-1$
                        + SarosSession.class.getSimpleName()
                        + " although one is already running"); //$NON-NLS-1$
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
        final SarosSessionManager sarosSessionManager) {
        ISarosSession sarosSession = sarosSessionManager.getSarosSession();

        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
            .getShell();

        if (sarosSession != null) {
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
                    try {
                        sarosSessionManager.stopSarosSession();
                    } catch (Exception e) {
                        log.error("Session could not be left: ", e); //$NON-NLS-1$
                    }
                }
            });
        } else {
            log.warn("Tried to leave " + SarosSession.class.getSimpleName() //$NON-NLS-1$
                + " although there is no one running"); //$NON-NLS-1$
        }
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
        final SarosSessionManager sarosSessionManager,
        List<IResource> resourcesToAdd) {
        final ISarosSession sarosSession = sarosSessionManager
            .getSarosSession();
        final HashMap<IProject, List<IResource>> projectResources = acquireResources(
            resourcesToAdd, sarosSession);
        if (projectResources.isEmpty())
            return;
        Utils.runSafeAsync(log, new Runnable() {
            public void run() {
                if (sarosSession != null) {
                    Utils.runSafeSync(log, new Runnable() {
                        public void run() {
                            sarosSessionManager
                                .addResourcesToSession(projectResources);
                        }
                    });
                } else {
                    log.warn("Tried to add project resources to " //$NON-NLS-1$
                        + SarosSession.class.getSimpleName()
                        + " although there is no one running"); //$NON-NLS-1$
                }
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
        final SarosSessionManager sarosSessionManager, final List<JID> buddies) {

        Utils.runSafeAsync(log, new Runnable() {
            public void run() {
                final ISarosSession sarosSession = sarosSessionManager
                    .getSarosSession();
                if (sarosSession != null) {
                    Utils.runSafeSync(log, new Runnable() {
                        public void run() {
                            Collection<User> addedUsers = sarosSession
                                .getParticipants();
                            List<JID> buddiesToAdd = new LinkedList<JID>();
                            for (JID buddy : buddies) {
                                boolean addBuddyToSession = true;
                                for (User addedUser : addedUsers) {
                                    JID addedBuddy = addedUser.getJID();
                                    if (buddy.equals(addedBuddy)) {
                                        addBuddyToSession = false;
                                        break;
                                    }
                                }
                                if (addBuddyToSession) {
                                    buddiesToAdd.add(buddy);
                                }
                            }

                            String description = getShareProjectDescription(sarosSession);

                            if (buddiesToAdd.size() > 0) {
                                sarosSessionManager.invite(buddiesToAdd,
                                    description);
                            }
                        }
                    });
                } else {
                    log.warn("Tried to add buddies to " //$NON-NLS-1$
                        + SarosSession.class.getSimpleName()
                        + " although there is no one running"); //$NON-NLS-1$
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
        while (projects.iterator().hasNext()) {
            IProject project = projects.iterator().next();
            message.append("\t" + project.getName() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        message.append("\n"); //$NON-NLS-1$
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
    protected static String getShareProjectDescription(
        ISarosSession sarosSession) {

        JID inviter = sarosSession.getHost().getJID();
        Set<IProject> projects = sarosSession.getProjects();

        StringBuilder result = new StringBuilder();

        result
            .append(MessageFormat.format(
                Messages.CollaborationUtils_user_invited_to_saros_session,
                inviter.getBase(),
                ((projects.size() == 1) ? Messages.CollaborationUtils_project_singular_ending
                    : Messages.CollaborationUtils_project_plural_ending)));

        for (IProject project : projects) {
            result.append("\n - ").append(project.getName()); //$NON-NLS-1$
            if (!sarosSession.isCompletelyShared(project))
                result.append(Messages.CollaborationUtils_partial);
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
     * @param iSarosSession
     * @return
     * 
     */
    protected static HashMap<IProject, List<IResource>> acquireResources(
        List<IResource> selectedResources, ISarosSession iSarosSession) {

        HashMap<IProject, List<IResource>> allResources = new HashMap<IProject, List<IResource>>();

        if (iSarosSession != null) {
            List<IResource> alreadySharedResources = iSarosSession
                .getAllSharedResources();
            selectedResources.removeAll(alreadySharedResources);
        }
        for (int i = 0; i < selectedResources.size(); i++) {
            IResource iResource = selectedResources.get(i);
            if (iResource instanceof IProject) {
                allResources.put((IProject) iResource, null);
            } else if (!allResources.containsKey(iResource.getProject())) {
                IProject project = iResource.getProject();
                List<IResource> tempResources = new ArrayList<IResource>();

                for (int j = i; j < selectedResources.size(); j++) {
                    if (!project.equals(selectedResources.get(j).getProject())) {
                        i = j - 1;
                        break;
                    }
                    tempResources.add(selectedResources.get(j));
                }
                if (!tempResources.contains(project.getFile(".project"))) //$NON-NLS-1$
                    tempResources.add(project.getFile(".project")); //$NON-NLS-1$
                if (!tempResources.contains(project.getFile(".classpath"))) //$NON-NLS-1$
                    tempResources.add(project.getFile(".classpath")); //$NON-NLS-1$
                allResources.put(project, tempResources);
            }
        }
        return allResources;
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
            Messages.CollaborationUtils_confirm_need_based_text, false, saros,
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
            SarosView.showNotification("New file shared!", messageBalloon);
            return true;
        }
    }
}
