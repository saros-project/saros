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

    @Override
    protected void executeCancellation() {
        sessionManager.stopSarosSession();
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

    /**
     * @JTourBusStop 7, Invitation Process:
     * 
     *               These are the first few steps on the client side during a
     *               session invitation. The method below is called by the
     *               NegotiationHandler which (among other things) handles
     *               incoming SessionInvitations.
     * 
     *               (3b) Acknowledge the offer, so the host knows that we
     *               received his invitation (this method).
     * 
     *               (4a) Show dialog for user to decide whether to accept the
     *               invitation (also called by the NegotiationHandler)
     */
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

            /**
             * @JTourBusStop 8, Invitation Process:
             * 
             *               These are the next steps on the client side during
             *               a session invitation. This method is called by
             *               JoinSessionWizard.finish(), i.e. after the user
             *               clicked on "Finish".
             * 
             *               (4b) Send acceptance to host.
             * 
             *               (5a) Create "whishlist" with session's parameters
             *               (e.g. preferred color) and send it.
             * 
             *               (6b) Wait for host's response.
             * 
             *               (7) Initialize the session and related components
             *               (e.g. chat, color management) with the parameters
             *               as defined by the host.
             * 
             *               (8) Start the session accordingly, inform the host
             *               and wait for his final acknowledgement (which
             *               indicates, that this client has been successfully
             *               added to the session and will receive activities
             *               from now on).
             */
            sendInvitationAccepted();

            InvitationParameterExchangeExtension clientSessionPreferences;
            clientSessionPreferences = createClientSessionPreferences();

            sendSessionPreferences(clientSessionPreferences);

            InvitationParameterExchangeExtension actualSessionParameters;
            actualSessionParameters = awaitActualSessionParameters(monitor);

            initializeSession(actualSessionParameters, monitor);

            startSession(monitor);

            sendInvitationCompleted(monitor);

            awaitFinalAcknowledgement(monitor);
        } catch (Exception e) {
            exception = e;
        } finally {
            monitor.done();
            deleteCollectors();
            invitationProcesses.removeInvitationProcess(this);
        }

        return terminateProcess(exception);
    }

    /**
     * Informs the session host that the user has accepted the invitation.
     */
    private void sendInvitationAccepted() {
        log.debug(this + " : sending invitation accepted confirmation");

        transmitter.sendMessageToUser(peer,
            InvitationAcceptedExtension.PROVIDER
                .create(new InvitationAcceptedExtension(invitationID)));
    }

    /**
     * Creates session negotiation data that may contain some default settings
     * that should be recognized by the host during the negotiation.
     */
    private InvitationParameterExchangeExtension createClientSessionPreferences() {
        InvitationParameterExchangeExtension parameters = new InvitationParameterExchangeExtension(
            invitationID);

        parameters.setLocalColorID(preferenceUtils.getFavoriteColorID());
        parameters.setLocalFavoriteColorID(parameters.getLocalColorID());

        return parameters;
    }

    /**
     * Sends session negotiation data that should be recognized by the host
     * during the negotiation.
     */
    private void sendSessionPreferences(
        InvitationParameterExchangeExtension parameters) {

        log.debug(this + " : sending session negotiation data");

        transmitter.sendMessageToUser(peer,
            InvitationParameterExchangeExtension.PROVIDER.create(parameters));
    }

    /**
     * Waits for the actual parameters which are sent back by the host. They
     * contain information that must be used on session start.
     */
    private InvitationParameterExchangeExtension awaitActualSessionParameters(
        IProgressMonitor monitor) throws SarosCancellationException {

        log.debug(this + " : waiting for host's session parameters");

        monitor.setTaskName("Waiting for host's session parameters...");

        Packet packet = collectPacket(invitationDataExchangeCollector,
            PACKET_TIMEOUT);

        if (packet == null)
            throw new LocalCancellationException(peerNickname
                + " does not respond. (Timeout)",
                CancelOption.DO_NOT_NOTIFY_PEER);

        InvitationParameterExchangeExtension parameters;
        parameters = InvitationParameterExchangeExtension.PROVIDER
            .getPayload(packet);

        if (parameters == null)
            throw new LocalCancellationException(peer + " sent malformed data",
                CancelOption.DO_NOT_NOTIFY_PEER);

        log.debug(this + " : received host's session parameters");

        return parameters;
    }

    /**
     * Initializes some components that rely on the session parameters as
     * defined by the host. This includes MUC settings and color settings.
     * Finally the session is created locally (but not started yet).
     */
    private void initializeSession(
        InvitationParameterExchangeExtension parameters,
        IProgressMonitor monitor) {
        log.debug(this + " : initializing session");

        monitor.setTaskName("Initializing session...");

        mucNegotiatingManager.setSessionPreferences(parameters
            .getMUCPreferences());

        sarosSession = sessionManager.joinSession(parameters.getSessionHost(),
            parameters.getLocalColorID(), sessionStart, peer,
            parameters.getRemoteFavoriteColorID());
    }

    /**
     * Starts the Saros session
     */
    private void startSession(IProgressMonitor monitor) {
        log.debug(this + " : starting session");

        monitor.setTaskName("Starting session...");

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
    }

    /**
     * Signal the host that the session invitation is complete (from the
     * client's perspective).
     */
    private void sendInvitationCompleted(IProgressMonitor monitor) {
        transmitter.sendMessageToUser(peer,
            InvitationCompletedExtension.PROVIDER
                .create(new InvitationCompletedExtension(invitationID)));

        log.debug(this + " : invitation complete confirmation sent");
    }

    /**
     * Wait for the final acknowledgement by the host which indicates that this
     * client has been added to the session and will receive activities from now
     * on. The waiting for the acknowledgment of the host cannot be canceled and
     * therefore the packet timeout should not set be higher than 1 minute !
     */
    private void awaitFinalAcknowledgement(IProgressMonitor monitor)
        throws SarosCancellationException {
        monitor.setTaskName("Waiting for " + peerNickname
            + " to perform final initialization...");

        if (collectPacket(invitationAcknowledgedCollector, PACKET_TIMEOUT) == null)
            throw new LocalCancellationException(peerNickname
                + " does not respond. (Timeout)",
                CancelOption.DO_NOT_NOTIFY_PEER);
    }

    private void createCollectors() {
        invitationAcknowledgedCollector = receiver
            .createCollector(InvitationAcknowledgedExtension.PROVIDER
                .getPacketFilter(invitationID));

        invitationDataExchangeCollector = receiver
            .createCollector(InvitationParameterExchangeExtension.PROVIDER
                .getPacketFilter(invitationID));
    }

    private void deleteCollectors() {
        invitationAcknowledgedCollector.cancel();
        invitationDataExchangeCollector.cancel();
    }

    @Override
    public String toString() {
        return "ISN [remote side: " + peer + "]";
    }
}
