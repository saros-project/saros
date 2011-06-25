package de.fu_berlin.inf.dpp.ui.util;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.jivesoftware.smack.XMPPException;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.project.internal.SarosSession;
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
                                        "Offline",
                                        getShareResourcesFailureMessage(newResources
                                            .keySet()));
                                log.warn("Start share project failed", e);
                            }
                        });
                    }

                    addBuddiesToSarosSession(sarosSessionManager, buddies);
                } else {
                    log.warn("Tried to start "
                        + SarosSession.class.getSimpleName()
                        + " although one is already running");
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
                    reallyLeave = MessageDialog
                        .openQuestion(
                            shell,
                            "Confirm Closing Session",
                            "Are you sure that you want to close this Saros session? Since you are the creator of this session, it will be closed for all participants.");
                }
            } else {
                reallyLeave = MessageDialog.openQuestion(shell,
                    "Confirm Leaving Session",
                    "Are you sure that you want to leave this Saros session?");
            }

            if (!reallyLeave)
                return;

            Utils.runSafeAsync(log, new Runnable() {
                public void run() {
                    try {
                        sarosSessionManager.stopSarosSession();
                    } catch (Exception e) {
                        log.error("Session could not be left: ", e);
                    }
                }
            });
        } else {
            log.warn("Tried to leave " + SarosSession.class.getSimpleName()
                + " although there is no one running");
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
        final HashMap<IProject, List<IResource>> projectResources = acquireResources(
            resourcesToAdd, sarosSessionManager.getSarosSession());
        if (projectResources.isEmpty())
            return;
        Utils.runSafeAsync(log, new Runnable() {
            public void run() {
                final ISarosSession sarosSession = sarosSessionManager
                    .getSarosSession();
                if (sarosSession != null) {
                    Utils.runSafeSync(log, new Runnable() {
                        public void run() {
                            sarosSessionManager
                                .addResourcesToSession(projectResources);
                        }
                    });
                } else {
                    log.warn("Tried to add project resources to "
                        + SarosSession.class.getSimpleName()
                        + " although there is no one running");
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
                    log.warn("Tried to add buddies to "
                        + SarosSession.class.getSimpleName()
                        + " although there is no one running");
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

        StringBuilder message = new StringBuilder();
        message.append("You are not connected to an XMPP/Jabber server.\n");
        message.append("\n");
        message.append("The following project"
            + ((projects.size() == 1) ? "" : "s") + " could not be shared:\n");
        while (projects.iterator().hasNext()) {
            IProject project = projects.iterator().next();
            message.append("\t" + project.getName() + "\n");
        }
        message.append("\n");
        message.append("Please make sure you are currently connected "
            + "to a XMPP/Jabber server in order to share a project.");
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

        result.append(inviter.getBase()).append(
            " has invited you to a Saros session");

        if (projects.size() == 1) {
            result.append(" with the shared project\n");
        } else if (projects.size() > 1) {
            result.append(" with the shared projects\n");
        }

        for (IProject project : projects) {
            result.append("\n - ").append(project.getName());
            if (!sarosSession.isCompletelyShared(project))
                result.append(" (partial)");
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
                if (!tempResources.contains(project.getFile(".project")))
                    tempResources.add(project.getFile(".project"));
                if (!tempResources.contains(project.getFile(".classpath")))
                    tempResources.add(project.getFile(".classpath"));
                allResources.put(project, tempResources);
            }
        }
        return allResources;
    }
}
