package de.fu_berlin.inf.dpp.ui.wizards;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Version;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.net.internal.discoveryManager.DiscoveryManager;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.wizards.pages.InvitationWizardUserSelectionPage;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.VersionManager;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

public class InvitationWizard extends Wizard {

    private static final Logger log = Logger.getLogger(InvitationWizard.class);
    protected Saros saros;
    protected ISarosSession sarosSession;
    protected RosterTracker rosterTracker;
    protected DiscoveryManager discoveryManager;
    protected InvitationWizardUserSelectionPage userSelection;
    protected SarosSessionManager sessionManager;
    protected VersionManager versionManager;
    protected InvitationProcessObservable invitationProcesses;

    public InvitationWizard(Saros saros, ISarosSession sarosSession,
        RosterTracker rosterTracker, DiscoveryManager discoveryManager,
        SarosSessionManager sessionManager, VersionManager versionManager,
        InvitationProcessObservable invitationProcesses) {
        this.saros = saros;
        this.sarosSession = sarosSession;
        this.rosterTracker = rosterTracker;
        this.discoveryManager = discoveryManager;
        this.sessionManager = sessionManager;
        this.invitationProcesses = invitationProcesses;
        setWindowTitle("Invitation");
        setHelpAvailable(false);
        TrayDialog.setDialogHelpAvailable(false);
    }

    /**
     * Activate the Finish button only if at least one user is selected.
     */
    @Override
    public boolean canFinish() {
        return (userSelection.getSelectedUsers().size() > 0);
    }

    @Override
    public boolean performFinish() {
        ArrayList<JID> usersToInvite = userSelection.getSelectedUsers();

        StringBuilder projectString = new StringBuilder();
        Set<IProject> projectSet = sarosSession.getProjects();

        if (projectSet != null) {
            Iterator<IProject> projectSetIterator = projectSet.iterator();

            while (projectSetIterator.hasNext()) {
                IProject p = projectSetIterator.next();
                projectString.append("- " + p.getName());
                if (projectSetIterator.hasNext())
                    projectString.append("\n");
            }
            log.debug(projectString);
        } else {
            log.debug("There are no Projects to share");
        }

        for (JID user : usersToInvite) {
            String hostName = sarosSession.getHost().getJID().getBase();
            sessionManager.invite(user, hostName
                + " has invited you to a Saros session"
                + " on the following project(s): " + "\n\n" + projectString);

        }
        return true;
    }

    @Override
    public boolean performCancel() {
        Utils.runSafeAsync(log, new Runnable() {
            public void run() {
                /*
                 * If more than one person is in the session, it should not be
                 * closed because we are adding to an existing session not
                 * abandoning our attempt to set one up.
                 */
                ISarosSession s = sessionManager.getSarosSession();
                if (s != null && s.getParticipants().size() <= 1) {
                    sessionManager.stopSarosSession();
                }
            }
        });

        // TODO this.dispose();

        return true;
    }

    @Override
    public void addPages() {
        userSelection = new InvitationWizardUserSelectionPage(saros, sarosSession,
            rosterTracker, discoveryManager, invitationProcesses);
        addPage(userSelection);
    }

    /**
     * Asks the user for confirmation to proceed.
     * 
     * @return <code>true</code> if the user confirms to proceed,
     *         <code>false</code> otherwise.
     * 
     * 
     * @nonReentrant In order to avoid a mass of question dialogs the same time.
     *               TODO: is this the right way?
     */
    public static synchronized boolean confirmUnsupportedSaros(final JID peer) {
        try {
            return Utils.runSWTSync(new Callable<Boolean>() {
                public Boolean call() {
                    return MessageDialog.openConfirm(getAShell(),
                        "Invite buddy who does not support Saros?", "User "
                            + peer + " does not seem to use Saros "
                            + "(but rather a normal Instant Messaging client),"
                            + " invite anyway?");
                }
            });
        } catch (Exception e) {
            log.error(
                "An error ocurred while trying to open the confirm dialog.", e);
            return false;
        }
    }

    /**
     * Asks the user for confirmation to proceed. This method should only be
     * user if the versions are incompatible.
     * 
     * @param remoteVersionInfo
     *            a {@link VersionInfo} object with the local
     *            {@link VersionInfo#version} and the ultimate
     *            {@link VersionInfo#compatibility}. You can get this
     *            {@link VersionInfo} object by the method
     *            {@link VersionManager#determineCompatibility(JID)}.
     *            <code>null</code> is allowed.
     * 
     * @return <code>true</code> if the user confirms to proceed,
     *         <code>false</code> otherwise.
     * 
     *         TODO: is this the right way?
     * @nonReentrant In order to avoid a mass of question dialogs the same time.
     */
    public static synchronized boolean confirmVersionConflict(
        VersionInfo remoteVersionInfo, JID peer, Version localVersion) {
        final String title = "Saros Version Conflict with " + peer.getBase();
        final String message;
        if (remoteVersionInfo == null) {
            message = "Asking "
                + peer
                + " for the Saros version in use failed.\n\n"
                + "This probably means that the version used by your peer is\n"
                + "older than version 9.8.21 and does not support version checking.\n"
                + "It is best to ask your peer to update.\n\nDo you want to invite "
                + peer.getBase() + " anyway?";
        } else {
            switch (remoteVersionInfo.compatibility) {
            case TOO_OLD:
                message = "Your Saros version is too old: " + localVersion
                    + ".\nYour peer has a newer version: "
                    + remoteVersionInfo.version
                    + ".\nPlease check for updates!"
                    + " Proceeding with incompatible versions"
                    + " may cause malfunctions!\n\nDo you want to invite "
                    + peer.getBase() + " anyway?";
                break;
            case TOO_NEW:
                message = "Your Saros version is: " + localVersion
                    + ".\nYour peer has an older version: "
                    + remoteVersionInfo.version
                    + ".\nPlease tell your peer to check for updates!"
                    + " Proceeding with incompatible versions"
                    + " may cause malfunctions!\n\nDo you want to invite "
                    + peer.getBase() + " anyway?";
                break;
            default:
                log.warn(
                    "Warning message requested when no warning is in place!",
                    new StackTrace());
                // No warning to display
                message = "An internal error occurred.\n\nDo you want to invite "
                    + peer + " anyway?";
                break;
            }
        }

        try {
            return Utils.runSWTSync(new Callable<Boolean>() {
                public Boolean call() {
                    return MessageDialog.openQuestion(getAShell(), title,
                        message);
                }
            });
        } catch (Exception e) {
            log.error(
                "An error ocurred while trying to open the confirm dialog.", e);
            return false;
        }
    }

    public static boolean confirmUnknownVersion(JID peer, Version localVersion) {
        final String title = "Unable to determine Saros compatibility with "
            + peer.getBase();
        final String message = "Saros was unable to check the version number of your peer "
            + peer.getBase()
            + ", so it is possible that you are running incompatible versions of Saros. Please "
            + "ensure that your peer is running the same version of Saros as you. (Your version is "
            + localVersion.toString() + ".)\n\nDo you wish to proceed?";

        try {
            return Utils.runSWTSync(new Callable<Boolean>() {
                public Boolean call() {
                    return MessageDialog.openQuestion(getAShell(), title,
                        message);
                }
            });
        } catch (Exception e) {
            log.error(
                "An error ocurred while trying to open the confirm dialog.", e);
            return false;
        }
    }

    public static boolean confirmProjectSave(JID peer) {
        final String title = "Save All Resources";
        final String message = "Some resources have been modified.\n"
            + "If you want to proceed with the invitation of " + peer.getBase()
            + ", the resources have to be saved. If you press 'No', "
            + "the invitation will be cancelled.\n\nSave changes?";
        try {
            return Utils.runSWTSync(new Callable<Boolean>() {
                public Boolean call() {
                    return MessageDialog.openQuestion(getAShell(), title,
                        message);
                }
            });
        } catch (Exception e) {
            log.error(
                "An error ocurred while trying to open the confirm dialog.", e);
            return false;
        }
    }

    public static Shell getAShell() {
        Shell shell = EditorAPI.getShell();
        if (shell == null)
            shell = new Shell();
        return shell;
    }
}
