package de.fu_berlin.inf.dpp.ui.wizards;

import java.text.MessageFormat;
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

import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.discoverymanager.DiscoveryManager;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.wizards.pages.InvitationWizardUserSelectionPage;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.VersionManager;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

public class InvitationWizard extends Wizard {

    private static final Logger log = Logger.getLogger(InvitationWizard.class);
    protected SarosNet sarosNet;
    protected ISarosSession sarosSession;
    protected RosterTracker rosterTracker;
    protected DiscoveryManager discoveryManager;
    protected InvitationWizardUserSelectionPage userSelection;
    protected SarosSessionManager sessionManager;
    protected VersionManager versionManager;
    protected InvitationProcessObservable invitationProcesses;

    public InvitationWizard(SarosNet sarosNet, ISarosSession sarosSession,
        RosterTracker rosterTracker, DiscoveryManager discoveryManager,
        SarosSessionManager sessionManager, VersionManager versionManager,
        InvitationProcessObservable invitationProcesses) {
        this.sarosNet = sarosNet;
        this.sarosSession = sarosSession;
        this.rosterTracker = rosterTracker;
        this.discoveryManager = discoveryManager;
        this.sessionManager = sessionManager;
        this.invitationProcesses = invitationProcesses;
        setWindowTitle(Messages.InvitationWizard_title);
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
                projectString.append("- " + p.getName()); //$NON-NLS-1$
                if (projectSetIterator.hasNext())
                    projectString.append("\n"); //$NON-NLS-1$
            }
            log.debug(projectString);
        } else {
            log.debug(Messages.InvitationWizard_no_projects);
        }

        for (JID user : usersToInvite) {
            String hostName = sarosSession.getHost().getJID().getBase();
            sessionManager
                .invite(user, MessageFormat.format(
                    Messages.InvitationWizard_invite_text, hostName,
                    projectString));

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
        userSelection = new InvitationWizardUserSelectionPage(sarosNet,
            sarosSession, rosterTracker, discoveryManager, invitationProcesses);
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
                        Messages.InvitationWizard_invite_no_support,
                        MessageFormat.format(
                            Messages.InvitationWizard_invite_no_support_text,
                            peer));
                }
            });
        } catch (Exception e) {
            log.error(
                "An error ocurred while trying to open the confirm dialog.", e); //$NON-NLS-1$
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

        final String title = MessageFormat.format(
            Messages.InvitationWizard_version_conflict, peer.getBase());
        final String message;
        if (remoteVersionInfo == null) {
            message = MessageFormat.format(
                Messages.InvitationWizard_version_request_failed, peer,
                peer.getBase());
        } else {
            switch (remoteVersionInfo.compatibility) {
            case TOO_OLD:
                message = MessageFormat.format(
                    Messages.InvitationWizard_version_too_old, localVersion,
                    remoteVersionInfo.version, peer.getBase());
                break;
            case TOO_NEW:
                message = MessageFormat.format(
                    Messages.InvitationWizard_version_too_new, localVersion,
                    remoteVersionInfo.version, peer.getBase());
                break;
            default:
                log.warn(
                    "Warning message requested when no warning is in place!", //$NON-NLS-1$
                    new StackTrace());
                // No warning to display
                message = MessageFormat.format(
                    Messages.InvitationWizard_invite_error, peer);
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
                "An error ocurred while trying to open the confirm dialog.", e); //$NON-NLS-1$
            return false;
        }
    }

    public static boolean confirmUnknownVersion(JID peer, Version localVersion) {

        final String title = MessageFormat.format(
            Messages.InvitationWizard_is_compatible, peer.getBase());
        final String message = MessageFormat.format(
            Messages.InvitationWizard_version_check_failed_text,
            peer.getBase(), localVersion.toString());

        try {
            return Utils.runSWTSync(new Callable<Boolean>() {
                public Boolean call() {
                    return MessageDialog.openQuestion(getAShell(), title,
                        message);
                }
            });
        } catch (Exception e) {
            log.error(
                "An error ocurred while trying to open the confirm dialog.", e); //$NON-NLS-1$
            return false;
        }
    }

    public static boolean confirmProjectSave(JID peer) {
        final String title = Messages.InvitationWizard_save_ressources;
        final String message = MessageFormat.format(
            Messages.InvitationWizard_save_ressources_text, peer.getBase());
        try {
            return Utils.runSWTSync(new Callable<Boolean>() {
                public Boolean call() {
                    return MessageDialog.openQuestion(getAShell(), title,
                        message);
                }
            });
        } catch (Exception e) {
            log.error(
                "An error ocurred while trying to open the confirm dialog.", e); //$NON-NLS-1$
            return false;
        }
    }

    public static void notifyUserOffline(JID peer) {
        Utils.popUpFailureMessage(Messages.InvitationWizard_buddy_offline,
            MessageFormat.format(Messages.InvitationWizard_buddy_offline_text,
                peer), false);
    }

    public static Shell getAShell() {
        Shell shell = EditorAPI.getShell();
        if (shell == null)
            shell = new Shell();
        return shell;
    }
}
