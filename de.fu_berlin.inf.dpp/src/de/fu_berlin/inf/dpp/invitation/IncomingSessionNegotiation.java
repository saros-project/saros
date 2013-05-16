package de.fu_berlin.inf.dpp.invitation;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jivesoftware.smack.packet.Packet;
import org.joda.time.DateTime;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.ISarosContext;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelLocation;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationAcceptedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationAcknowledgedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationCompletedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationParameterExchangeExtension;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.ui.wizards.JoinSessionWizard;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

/*
 * IMPORTANT: All messages in the cancellation exception are SHOWN to the end user !
 */
public class IncomingSessionNegotiation extends InvitationProcess {

    private static Logger log = Logger
        .getLogger(IncomingSessionNegotiation.class);

    private ISarosSessionManager sessionManager;
    private JoinSessionWizard inInvitationUI;
    private DateTime sessionStart;
    private ISarosSession sarosSession;

    private VersionInfo remoteVersionInfo;

    private boolean running;

    private SarosPacketCollector invitationDataExchangeCollector;
    private SarosPacketCollector invitationAcknowledgedCollector;

    @Inject
    private PreferenceUtils preferenceUtils;

    public IncomingSessionNegotiation(ISarosSessionManager sessionManager,
        JID from, VersionInfo remoteVersionInfo, DateTime sessionStart,
        String invitationID, String description, ISarosContext sarosContext) {

        super(invitationID, from, description, sarosContext);

        this.sessionStart = sessionStart;
        this.sessionManager = sessionManager;
        this.remoteVersionInfo = remoteVersionInfo;
        // FIMXE move to SarosSessionManager
        this.invitationProcesses.addInvitationProcess(this);
    }

    @Override
    public synchronized boolean remoteCancel(String errorMsg) {
        if (!super.remoteCancel(errorMsg))
            return false;

        if (inInvitationUI != null)
            inInvitationUI.cancelWizard(peer, errorMsg, CancelLocation.REMOTE);

        if (!running) {
            invitationProcesses.removeInvitationProcess(this);
            terminateProcess(null);
        }

        return true;
    }

    @Override
    public synchronized boolean localCancel(String errorMsg,
        CancelOption cancelOption) {
        if (!super.localCancel(errorMsg, cancelOption))
            return false;

        if (inInvitationUI != null)
            inInvitationUI.cancelWizard(peer, errorMsg, CancelLocation.LOCAL);

        if (!running) {
            invitationProcesses.removeInvitationProcess(this);
            terminateProcess(null);
        }

        return true;
    }

    /**
     * Returns the Saros version of the remote side.
     */
    public VersionInfo getRemoteVersionInfo() {
        return remoteVersionInfo;
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
        log.debug(this + " : invitation accepted");

        monitor.beginTask("Joining session...", IProgressMonitor.UNKNOWN);

        synchronized (this) {
            running = true;
        }

        // the process should not be cancelled manually !
        // observeMonitor(monitor);

        Exception exception = null;

        createCollectors();

        try {
            checkCancellation(CancelOption.NOTIFY_PEER);
            sendInvitationAccepted();
            createAndSendSessionNegotiationData();
            initializeSession(awaitRemoteSessionNegotiationData(monitor));
            startSession(monitor);
        } catch (Exception e) {
            exception = e;
        } finally {
            monitor.done();
            deleteCollectors();
            invitationProcesses.removeInvitationProcess(this);
        }

        return terminateProcess(exception);
    }

    @Override
    protected void executeCancellation() {
        sessionManager.stopSarosSession();
    }

    /**
     * Starts the Saros session and sends a confirmation to the host and waits
     * for further proceeding. The waiting for the acknowledgment of the host
     * cannot be canceled and therefore the packet timeout should not set be
     * higher than 1 minute !
     * 
     */
    private void startSession(IProgressMonitor monitor)
        throws SarosCancellationException {
        log.debug(this + " : starting session");

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

        log.debug(this + " : invitation has completed successfully");

        transmitter.sendMessageToUser(peer,
            InvitationCompletedExtension.PROVIDER
                .create(new InvitationCompletedExtension(invitationID)));

        log.debug(this + " : invitation complete confirmation sent");

        monitor.setTaskName("Waiting for " + peerNickname
            + " to perform final initialization...");

        Packet packet = collectPacket(invitationAcknowledgedCollector,
            PACKET_TIMEOUT);

        if (packet == null)
            throw new LocalCancellationException(peerNickname
                + " does not respond. (Timeout)",
                CancelOption.DO_NOT_NOTIFY_PEER);
    }

    private void sendInvitationAccepted() {

        log.debug(this + " : sending invitation accepted confirmation");

        transmitter.sendMessageToUser(peer,
            InvitationAcceptedExtension.PROVIDER
                .create(new InvitationAcceptedExtension(invitationID)));
    }

    /**
     * Creates and sends session negotiation data that may contain some default
     * settings that should be recognized during the negotiation by the host
     * side.
     */
    private void createAndSendSessionNegotiationData() {
        InvitationParameterExchangeExtension parameters = new InvitationParameterExchangeExtension(
            invitationID);

        log.debug(this + " : sending session negotiation data");

        parameters.setLocalColorID(preferenceUtils.getFavoriteColorID());
        parameters.setLocalFavoriteColorID(parameters.getLocalColorID());

        transmitter.sendMessageToUser(peer,
            InvitationParameterExchangeExtension.PROVIDER.create(parameters));
    }

    /**
     * Waits for the remote parameters which contains session values that must
     * be used on session start.
     */
    private InvitationParameterExchangeExtension awaitRemoteSessionNegotiationData(
        IProgressMonitor monitor) throws SarosCancellationException {

        log.debug(this + " : waiting for remote session negotiation data");

        monitor.setTaskName("Waiting for remote session configuration...");

        InvitationParameterExchangeExtension parameters;

        Packet packet = collectPacket(invitationDataExchangeCollector,
            PACKET_TIMEOUT);

        if (packet == null)
            throw new LocalCancellationException(peerNickname
                + " does not respond. (Timeout)",
                CancelOption.DO_NOT_NOTIFY_PEER);

        parameters = InvitationParameterExchangeExtension.PROVIDER
            .getPayload(packet);

        if (parameters == null)
            throw new LocalCancellationException(peer + " sent malformed data",
                CancelOption.DO_NOT_NOTIFY_PEER);

        log.debug(this + " : received remote session negotiation data");

        return parameters;
    }

    /**
     * Initializes the session and some components (currently only the settings
     * for MUC usage)
     */
    private void initializeSession(
        InvitationParameterExchangeExtension parameters) {

        mucNegotiatingManager.setSessionPreferences(parameters
            .getMUCPreferences());

        sarosSession = sessionManager.joinSession(parameters.getSessionHost(),
            parameters.getLocalColorID(), sessionStart, peer,
            parameters.getRemoteFavoriteColorID());

    }

    private void deleteCollectors() {
        invitationAcknowledgedCollector.cancel();
        invitationDataExchangeCollector.cancel();
    }

    private void createCollectors() {
        invitationAcknowledgedCollector = receiver
            .createCollector(InvitationAcknowledgedExtension.PROVIDER
                .getPacketFilter(invitationID));

        invitationDataExchangeCollector = receiver
            .createCollector(InvitationParameterExchangeExtension.PROVIDER
                .getPacketFilter(invitationID));
    }

    @Override
    public String toString() {
        return "ISN [remote side: " + peer + "]";
    }
}
