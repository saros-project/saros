package de.fu_berlin.inf.dpp.invitation;

import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.ISarosContext;
import de.fu_berlin.inf.dpp.editor.colorstorage.UserColorID;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelLocation;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.invitation.hooks.ISessionNegotiationHook;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationAcceptedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationAcknowledgedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationCompletedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationParameterExchangeExtension;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.project.internal.ColorNegotiationHook;
import de.fu_berlin.inf.dpp.ui.wizards.JoinSessionWizard;

/*
 * IMPORTANT: All messages in the cancellation exception are SHOWN to the end user !
 */
public class IncomingSessionNegotiation extends SessionNegotiation {

    private static Logger log = Logger
        .getLogger(IncomingSessionNegotiation.class);

    private ISarosSessionManager sessionManager;
    private JoinSessionWizard inInvitationUI;
    private ISarosSession sarosSession;

    private String remoteVersion;

    private boolean running;

    private SarosPacketCollector invitationDataExchangeCollector;
    private SarosPacketCollector invitationAcknowledgedCollector;

    public IncomingSessionNegotiation(ISarosSessionManager sessionManager,
        JID from, String remoteVersion, String invitationID,
        String description, ISarosContext sarosContext) {

        super(invitationID, from, description, sarosContext);

        this.sessionManager = sessionManager;
        this.remoteVersion = remoteVersion;
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

    @Override
    protected void executeCancellation() {
        sessionManager.stopSarosSession();
    }

    /**
     * Returns the Saros version of the remote side.
     */
    public String getRemoteVersion() {
        return remoteVersion;
    }

    public synchronized void setInvitationUI(JoinSessionWizard inInvitationUI) {
        this.inInvitationUI = inInvitationUI;
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
             * @JTourBusStop 9, Invitation Process:
             * 
             *               This method is called by the JoinSessionWizard
             *               after the user clicked on "Finish" (indicating that
             *               he is willing to join the session).
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

        for (ISessionNegotiationHook hook : hookManager.getHooks()) {
            Map<String, String> clientPreferences = hook
                .tellClientPreferences();
            parameters.saveHookSettings(hook, clientPreferences);
        }

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

        // HACK (Part 1/2)
        int clientColor = UserColorID.UNKNOWN;
        int hostFavoriteColor = UserColorID.UNKNOWN;

        for (ISessionNegotiationHook hook : hookManager.getHooks()) {
            Map<String, String> settings = parameters.getHookSettings(hook);
            hook.applyActualParameters(settings);

            // HACK (Part 2/2)
            if (hook instanceof ColorNegotiationHook) {
                clientColor = Integer.parseInt(settings
                    .get(ColorNegotiationHook.KEY_CLIENT_COLOR));
                hostFavoriteColor = Integer.parseInt(settings
                    .get(ColorNegotiationHook.KEY_HOST_FAV_COLOR));
            }
        }

        sarosSession = sessionManager.joinSession(parameters.getSessionHost(),
            clientColor, peer, hostFavoriteColor);
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
