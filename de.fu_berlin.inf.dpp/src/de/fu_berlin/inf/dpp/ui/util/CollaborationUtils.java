package de.fu_berlin.inf.dpp.ui.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
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
 * Offers convenient methods for collaboration actions like sharing a project.
 * 
 * @author bkahlert
 */
public class CollaborationUtils {
    private static final Logger log = Logger
        .getLogger(CollaborationUtils.class);

    /**
     * Shares given projects with given buddies.<br/>
     * Does nothing if a {@link SarosSession} is already running.
     * 
     * @param sarosSessionManager
     * @param projects
     * @param buddies
     * 
     * @nonBlocking
     */
    public static void shareProjectWith(
        final SarosSessionManager sarosSessionManager,
        final List<IProject> projects, final List<JID> buddies) {

        Utils.runSafeAsync(log, new Runnable() {
            public void run() {
                if (sarosSessionManager.getSarosSession() == null) {
                    try {
                        sarosSessionManager.startSession(projects, null);
                    } catch (final XMPPException e) {
                        Utils.runSafeSWTSync(log, new Runnable() {
                            public void run() {
                                MessageDialog.openError(null, "Offline",
                                    getShareProjectFailureMessage(projects));
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
     * Adds the given projects to the session.<br/>
     * Does nothing if no {@link SarosSession} is running.
     * 
     * @param sarosSessionManager
     * @param projects
     * 
     * @nonBlocking
     */
    public static void addProjectsToSarosSession(
        final SarosSessionManager sarosSessionManager,
        final List<IProject> projects) {

        Utils.runSafeAsync(log, new Runnable() {
            public void run() {
                final ISarosSession sarosSession = sarosSessionManager
                    .getSarosSession();
                if (sarosSession != null) {
                    Utils.runSafeSync(log, new Runnable() {
                        public void run() {
                            Set<IProject> addedProjects = sarosSession
                                .getProjects();

                            List<IProject> projectsToAdd = new LinkedList<IProject>();
                            for (IProject project : projects) {
                                if (!addedProjects.contains(project)) {
                                    projectsToAdd.add(project);
                                }
                            }

                            if (projectsToAdd.size() > 0) {
                                sarosSessionManager
                                    .addProjectsToSession(projectsToAdd);
                            }
                        }
                    });
                } else {
                    log.warn("Tried to add projects to "
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

                            String description = getShareProjectDescription(
                                sarosSession.getHost().getJID(), sarosSession
                                    .getProjects().toArray(new IProject[0]));

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
    private static String getShareProjectFailureMessage(List<IProject> projects) {
        StringBuilder message = new StringBuilder();
        message.append("You are not connected to an XMPP/Jabber server.\n");
        message.append("\n");
        message.append("The following project"
            + ((projects.size() == 1) ? "" : "s") + " could not be shared:\n");
        for (int i = 0; i < projects.size(); i++) {
            IProject project = projects.get(i);
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
     * @param inviter
     * @param projects
     * @return
     */
    protected static String getShareProjectDescription(JID inviter,
        IProject[] projects) {
        String result = inviter.getBase()
            + " has invited you to a Saros session";

        if (projects.length == 1) {
            result += " with the shared project\n";
        } else if (projects.length > 1) {
            result += " with the shared projects\n";
        }
        for (IProject project : projects) {
            result += "\n - " + project.getName();
        }

        return result;
    }
}
