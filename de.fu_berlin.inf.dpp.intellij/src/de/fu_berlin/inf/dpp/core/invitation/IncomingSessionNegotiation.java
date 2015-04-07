package de.fu_berlin.inf.dpp.core.invitation;

import de.fu_berlin.inf.dpp.ISarosContext;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationAcceptedExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationAcknowledgedExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationCompletedExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationParameterExchangeExtension;
import de.fu_berlin.inf.dpp.core.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.editor.colorstorage.UserColorID;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.negotiation.SessionNegotiation;
import de.fu_berlin.inf.dpp.negotiation.hooks.ISessionNegotiationHook;
import de.fu_berlin.inf.dpp.net.IConnectionManager;
import de.fu_berlin.inf.dpp.net.PacketCollector;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.annotations.Inject;

import java.io.IOException;
import java.util.Map;

/*
 * IMPORTANT: All messages in the cancellation exception are SHOWN to the end user !
 */
public class IncomingSessionNegotiation extends SessionNegotiation {

    private static Logger LOG = Logger
        .getLogger(IncomingSessionNegotiation.class);

    private final ISarosSessionManager sessionManager;
    private final String remoteVersion;

    private ISarosSession sarosSession;

    private boolean running;

    private PacketCollector invitationDataExchangeCollector;
    private PacketCollector invitationAcknowledgedCollector;

    @Inject
    private IConnectionManager connectionManager;

    public IncomingSessionNegotiation(ISarosSessionManager sessionManager,
        JID from, String remoteVersion, String remoteNegotiationID,
        String description, ISarosContext sarosContext) {

        super(remoteNegotiationID, from, description, sarosContext);

        this.sessionManager = sessionManager;
        this.remoteVersion = remoteVersion;
    }

    /*
     * TODO local/remoteCancel and terminateProcess should not be called inside
     * the monitor
     */
    @Override
    public synchronized boolean remoteCancel(String errorMsg) {
        if (!super.remoteCancel(errorMsg)) {
            return false;
        }

        if (!running) {
            terminateProcess(null);
        }

        return true;
    }

    /*
     * TODO local/remoteCancel and terminateProcess should not be called inside
     * the monitor
     */
    @Override
    public synchronized boolean localCancel(String errorMsg,
        CancelOption cancelOption) {
        if (!super.localCancel(errorMsg, cancelOption)) {
            return false;
        }

        if (!running) {
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
    public String getRemoteVersion() {
        return remoteVersion;
    }

    public Status accept(IProgressMonitor monitor) {
        LOG.debug(this + " : invitation accepted");

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
             *               (5a) Create "wishlist" with session's parameters
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

            /*
             * TODO This is very fragile and needs a better design. We must
             * connect before we start our session otherwise a component that
             * try to immediately send an activity or something over the wire
             * will trigger the ClientSessionTimeoutHandler which will just
             * terminate the session !
             */
            monitor.setTaskName("Negotiating data connection...");

            connectionManager
                .connect(ISarosSession.SESSION_CONNECTION_ID, peer);

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
        LOG.debug(this + " : sending invitation accepted confirmation");

        transmitter.sendPacketExtension(peer,
            InvitationAcceptedExtension.PROVIDER
                .create(new InvitationAcceptedExtension(getID())));
    }

    /**
     * Creates session negotiation data that may contain some default settings
     * that should be recognized by the host during the negotiation.
     */
    private InvitationParameterExchangeExtension createClientSessionPreferences() {
        InvitationParameterExchangeExtension parameters = new InvitationParameterExchangeExtension(
            getID());

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

        LOG.debug(this + " : sending session negotiation data");

        transmitter.sendPacketExtension(peer,
            InvitationParameterExchangeExtension.PROVIDER.create(parameters));
    }

    /**
     * Waits for the actual parameters which are sent back by the host. They
     * contain information that must be used on session start.
     */
    private InvitationParameterExchangeExtension awaitActualSessionParameters(
        IProgressMonitor monitor) throws SarosCancellationException {

        LOG.debug(this + " : waiting for host's session parameters");

        monitor.setTaskName("Waiting for host's session parameters...");

        Packet packet = collectPacket(invitationDataExchangeCollector,
            PACKET_TIMEOUT);

        if (packet == null) {
            throw new LocalCancellationException(peerNickname
                + " does not respond. (Timeout)",
                CancelOption.DO_NOT_NOTIFY_PEER);
        }

        InvitationParameterExchangeExtension parameters;
        parameters = InvitationParameterExchangeExtension.PROVIDER
            .getPayload(packet);

        if (parameters == null) {
            throw new LocalCancellationException(peer + " sent malformed data",
                CancelOption.DO_NOT_NOTIFY_PEER);
        }

        LOG.debug(this + " : received host's session parameters");

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
        LOG.debug(this + " : initializing session");

        monitor.setTaskName("Initializing session...");

        for (ISessionNegotiationHook hook : hookManager.getHooks()) {
            Map<String, String> settings = parameters.getHookSettings(hook);
            if (settings == null) {
                continue;
            }

            hook.applyActualParameters(settings);
        }

        sarosSession = sessionManager.joinSession(parameters.getSessionHost(),
            UserColorID.UNKNOWN, UserColorID.UNKNOWN);
    }

    /**
     * Starts the Saros session
     */
    private void startSession(IProgressMonitor monitor) {
        LOG.debug(this + " : starting session");

        monitor.setTaskName("Starting session...");

        /*
         * TODO: Wait until all of the activities in the queue (which arrived
         * during the invitation) are processed and notify the host only after
         * that.
         */

        sessionManager.sessionStarting(sarosSession);
        sarosSession.start();
        sessionManager.sessionStarted(sarosSession);

        LOG.debug(this + " : invitation has completed successfully");
    }

    /**
     * Signal the host that the session invitation is complete (from the
     * client's perspective).
     */
    private void sendInvitationCompleted(IProgressMonitor monitor)
        throws IOException {
        transmitter.send(ISarosSession.SESSION_CONNECTION_ID, peer,
            InvitationCompletedExtension.PROVIDER
                .create(new InvitationCompletedExtension(getID())));

        LOG.debug(this + " : invitation complete confirmation sent");
    }

    /**
     * Wait for the final acknowledgment by the host which indicates that this
     * client has been added to the session and will receive activities from now
     * on. The waiting for the acknowledgment of the host cannot be canceled and
     * therefore the packet timeout should not set be higher than 1 minute !
     */
    private void awaitFinalAcknowledgement(IProgressMonitor monitor)
        throws SarosCancellationException {
        monitor.setTaskName("Waiting for " + peerNickname
            + " to perform final initialization...");

        if (collectPacket(invitationAcknowledgedCollector, PACKET_TIMEOUT) == null) {
            throw new LocalCancellationException(peerNickname
                + " does not respond. (Timeout)",
                CancelOption.DO_NOT_NOTIFY_PEER);
        }
    }

    private void createCollectors() {
        invitationAcknowledgedCollector = receiver
            .createCollector(InvitationAcknowledgedExtension.PROVIDER
                .getPacketFilter(getID()));

        invitationDataExchangeCollector = receiver
            .createCollector(InvitationParameterExchangeExtension.PROVIDER
                .getPacketFilter(getID()));
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
