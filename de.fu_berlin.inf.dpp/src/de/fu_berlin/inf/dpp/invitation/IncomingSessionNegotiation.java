package de.fu_berlin.inf.dpp.invitation;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.joda.time.DateTime;

import de.fu_berlin.inf.dpp.SarosContext;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelLocation;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationAcceptedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationAcknowledgedExtension;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.ui.wizards.JoinSessionWizard;
import de.fu_berlin.inf.dpp.util.VersionManager;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

public class IncomingSessionNegotiation extends InvitationProcess {

    private static Logger log = Logger
        .getLogger(IncomingSessionNegotiation.class);

    private ISarosSessionManager sessionManager;
    private JoinSessionWizard inInvitationUI;
    private VersionManager versionManager;
    private DateTime sessionStart;
    private ISarosSession sarosSession;
    private JID host;
    private int peerColorID;

    private VersionInfo versionInfo;

    private IProgressMonitor monitor;

    private boolean running;

    public IncomingSessionNegotiation(ISarosSessionManager sessionManager,
        JID from, int colorID, VersionManager versionManager,
        VersionInfo remoteVersionInfo, DateTime sessionStart,
        String invitationID, String description, SarosContext sarosContext,
        int peerColorID, JID host) {

        super(invitationID, from, description, colorID, sarosContext);

        this.versionInfo = determineVersion(remoteVersionInfo);
        this.sessionStart = sessionStart;

        this.sessionManager = sessionManager;
        this.versionManager = versionManager;
        this.host = host;
        this.peerColorID = peerColorID;

    }

    @Override
    public synchronized boolean remoteCancel(String errorMsg) {
        if (!super.remoteCancel(errorMsg))
            return false;

        if (inInvitationUI != null)
            inInvitationUI.cancelWizard(peer, errorMsg, CancelLocation.REMOTE);

        if (!running)
            terminateProcess(null);

        return true;
    }

    @Override
    public synchronized boolean localCancel(String errorMsg,
        CancelOption cancelOption) {
        if (!super.localCancel(errorMsg, cancelOption))
            return false;

        if (inInvitationUI != null)
            inInvitationUI.cancelWizard(peer, errorMsg, CancelLocation.LOCAL);

        if (!running)
            terminateProcess(null);

        return true;
    }

    public VersionInfo getVersionInfo() {
        return versionInfo;
    }

    public synchronized void setInvitationUI(JoinSessionWizard inInvitationUI) {
        this.inInvitationUI = inInvitationUI;
    }

    public void acknowledgeInvitation() {
        transmitter.sendMessageToUser(peer,
            InvitationAcknowledgedExtension.PROVIDER
                .create(new InvitationAcknowledgedExtension(invitationID)));
    }

    public Status accept(IProgressMonitor monitor) {
        log.debug(this + " : Invitation accepted");

        synchronized (this) {
            running = true;
        }

        this.monitor = monitor;
        observeMonitor(monitor);

        Exception exception = null;

        try {
            checkCancellation(CancelOption.NOTIFY_PEER);
            sarosSession = sessionManager.joinSession(host, colorID,
                sessionStart, peer, peerColorID);
            completeInvitation();
        } catch (Exception e) {
            exception = e;
        } finally {
            monitor.done();
            invitationProcesses.removeInvitationProcess(this);
        }

        return terminateProcess(exception);
    }

    @Override
    protected void executeCancellation() {
        sessionManager.stopSarosSession();
    }

    private VersionInfo determineVersion(VersionInfo remoteVersionInfo) {
        log.debug(this + " : Determining version");
        // The host could not determine the compatibility, so we do it.
        if (remoteVersionInfo.compatibility == null) {
            remoteVersionInfo.compatibility = versionManager
                .determineCompatibility(remoteVersionInfo.version.toString());
            return remoteVersionInfo;
        }

        // Invert the compatibility information so it applies to our client.
        remoteVersionInfo.compatibility = remoteVersionInfo.compatibility
            .invert();

        return remoteVersionInfo;
    }

    /**
     * Ends the incoming invitation process. Sends a confirmation to the host
     * and starts the shared project.
     */
    private void completeInvitation() {
        log.debug(this + " : Completing invitation");

        /*
         * TODO: Wait until all of the activities in the queue (which arrived
         * during the invitation) are processed and notify the host only after
         * that.
         */

        sessionManager.sessionStarting(sarosSession);
        sarosSession.start();
        sessionManager.sessionStarted(sarosSession);

        /*
         * Only the Witheboard plugin uses this listener to enable incoming
         * requests
         */
        sessionManager.preIncomingInvitationCompleted(monitor);

        sarosSession.userInvitationCompleted(sarosSession.getLocalUser());
        log.debug(this + " : isInvitationComplete has been set to true");

        transmitter.sendMessageToUser(peer,
            InvitationAcceptedExtension.PROVIDER
                .create(new InvitationAcceptedExtension(invitationID)));
        log.debug(this + " : Invitation complete confirmation sent");
        log.debug(this + " : Invitation has completed successfully");
    }

    @Override
    public String toString() {
        return "ISN [remote side: " + peer + "]";
    }
}
