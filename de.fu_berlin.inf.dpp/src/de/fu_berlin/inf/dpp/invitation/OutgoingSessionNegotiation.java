package de.fu_berlin.inf.dpp.invitation;

import java.util.Random;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.editor.colorstorage.UserColorID;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.discoverymanager.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationAcceptedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationAcknowledgedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationCompletedExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationOfferingExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.InvitationParameterExchangeExtension;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.project.internal.SarosSession;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.VersionManager;
import de.fu_berlin.inf.dpp.util.VersionManager.Compatibility;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

/*
 * IMPORTANT: All messages in the cancellation exception are SHOWN to the end user !
 */
public final class OutgoingSessionNegotiation extends InvitationProcess {

    private static final Logger log = Logger
        .getLogger(OutgoingSessionNegotiation.class);

    private static final boolean IGNORE_VERSION_COMPATIBILITY = Boolean
        .getBoolean("de.fu_berlin.inf.dpp.invitation.session.IGNORE_VERSION_COMPATIBILITY");

    private static final Random INVITATION_ID_GENERATOR = new Random();

    private static final Object SESSION_JOIN_LOCK = new Object();

    private ISarosSession sarosSession;

    private VersionInfo remoteVersionInfo;

    private SarosPacketCollector invitationAcceptedCollector;
    private SarosPacketCollector invitationAcknowledgedCollector;
    private SarosPacketCollector invitationDataExchangeCollector;
    private SarosPacketCollector invitationCompletedCollector;

    @Inject
    private VersionManager versionManager;

    @Inject
    private DiscoveryManager discoveryManager;

    @Inject
    private SessionIDObservable sessionID;

    @Inject
    private DataTransferManager dataTransferManager;

    @Inject
    private ISarosSessionManager sessionManager;

    private int colorID = UserColorID.UNKNOWN;
    private int favoriteColorID = UserColorID.UNKNOWN;

    public OutgoingSessionNegotiation(JID peer, ISarosSession sarosSession,
        String description, SarosContext sarosContext) {

        super(String.valueOf(INVITATION_ID_GENERATOR.nextLong()), peer,
            description, sarosContext);

        this.sarosSession = sarosSession;
    }

    /**
     * @JTourBusStop 5, Invitation Process:
     * 
     *               The details of the invitation process are implemented in
     *               the invitation package. OutgoingSessionNegotiation is an
     *               example of a class that participates in this process.
     * 
     *               The host of a session needs negotiations for:
     * 
     *               - Sending invitation to a session
     *               (OutgoingSessionNegotiation)
     * 
     *               - Sending project resources included in a session
     *               (OutgoingProjectNegotiation)
     * 
     *               All other participants need negotiations for:
     * 
     *               - Dealing with a received invitation to a session
     *               (IncomingSessionNegotiation)
     * 
     *               - Handling incoming shared project resources
     *               (IncomingProjectNegotiation)
     */

    public Status start(IProgressMonitor monitor) {
        log.debug(this + " : starting invitation");

        observeMonitor(monitor);

        monitor.beginTask("Inviting " + peerNickname + "...",
            IProgressMonitor.UNKNOWN);

        createCollectors();

        Exception exception = null;

        try {
            checkAvailability(monitor);

            checkVersion(monitor);

            sendInvitation(monitor);

            awaitAcceptation(monitor);

            modifiyAndSendSessionNegotiationData(
                awaitRemoteSessionNegotiationData(monitor), monitor);

            awaitCompletion(monitor);

            /*
             * HACK Ensure byte stream connection to peer so the project wizard
             * always show the currently used connection (IBB, Socks5(D/M)
             */

            monitor.setTaskName("Negotiating data connection...");

            dataTransferManager.connect(peer);

            User newUser = completeInvitation(monitor);

            monitor.done();

            // Whiteboard is using this listener
            sessionManager.postOutgoingInvitationCompleted(monitor, newUser);

        } catch (Exception e) {
            exception = e;
        } finally {
            deleteCollectors();
            monitor.done();
            invitationProcesses.removeInvitationProcess(this);
        }

        return terminateProcess(exception);
    }

    /**
     * Performs a discovery request on the remote side and checks for Saros
     * support. When this method returns the remote JID has been properly
     * updated to a full resource qualified JID.
     */
    private void checkAvailability(IProgressMonitor monitor)
        throws LocalCancellationException {

        log.debug(this + " : checking Saros support");
        monitor.setTaskName("Checking Saros support...");

        JID resourceQualifiedJID = discoveryManager.getSupportingPresence(peer,
            Saros.NAMESPACE);

        if (resourceQualifiedJID == null)
            throw new LocalCancellationException(
                peerNickname
                    + " does not support Saros or the request timed out. Please try again.",
                CancelOption.DO_NOT_NOTIFY_PEER);

        log.debug(this + " :  remote contact offers Saros support");

        peer = resourceQualifiedJID;
    }

    /**
     * Checks the compatibility of the local Saros version with the remote side.
     * If the versions are compatible, the invitation continues, otherwise a
     * confirmation of the user is required (a {@link MessageDialog} pops up).
     */
    private void checkVersion(IProgressMonitor monitor)
        throws SarosCancellationException {

        log.debug(this + " : checking version compatibility");
        monitor.setTaskName("Checking version compatibility...");

        VersionInfo versionInfo = versionManager.determineCompatibility(peer);

        checkCancellation(CancelOption.DO_NOT_NOTIFY_PEER);

        Compatibility comp = null;

        if (versionInfo != null) {
            comp = versionInfo.compatibility;

            if (comp == VersionManager.Compatibility.OK) {
                log.debug(this + " : Saros versions are compatible");
                this.remoteVersionInfo = versionInfo;
            } else {
                log.debug(this + " : Saros versions are not compatible");
                if (IGNORE_VERSION_COMPATIBILITY
                    && DialogUtils.confirmVersionConflict(versionInfo, peer,
                        versionManager.getVersion()))
                    this.remoteVersionInfo = versionInfo;
                else {
                    throw new LocalCancellationException(
                        "The Saros plugin of "
                            + peerNickname
                            + " (Version "
                            + versionInfo.version
                            + ") is not compatible with your installed Saros plugin (Version "
                            + versionManager.getVersion().toString() + ")",
                        CancelOption.DO_NOT_NOTIFY_PEER);
                }
            }
        } else {
            log.debug(this + " : could not obtain remote version information");
            throw new LocalCancellationException(
                "Could not obtain the version of the Saros plugin from "
                    + peerNickname + ". Please try again.",
                CancelOption.DO_NOT_NOTIFY_PEER);
        }
    }

    /**
     * Sends an invitation and waits for acknowledgment. The acknowledgment is
     * auto generated on the remote side and generates a packet reply that is
     * different as if the remote user manually accepts the invitation.
     * 
     */
    private void sendInvitation(IProgressMonitor monitor)
        throws SarosCancellationException {
        monitor.setTaskName("Sending invitation...");

        log.debug(this + " : sending invitation");
        checkCancellation(CancelOption.DO_NOT_NOTIFY_PEER);

        VersionInfo localVersionInfo = new VersionInfo();

        assert remoteVersionInfo != null;

        localVersionInfo.version = versionManager.getVersion();

        // if remote version is too new we are too old and vice versa
        localVersionInfo.compatibility = remoteVersionInfo.compatibility
            .invert();

        InvitationOfferingExtension invitationOffering = new InvitationOfferingExtension(
            invitationID, sessionID.getValue(), sarosSession.getSessionStart(),
            localVersionInfo, description);

        transmitter.sendMessageToUser(peer,
            InvitationOfferingExtension.PROVIDER.create(invitationOffering));

        log.debug(this + " : waiting for invitation acknowledgement");

        monitor.setTaskName("Waiting for " + peerNickname
            + " to acknowledge the invitation...");

        if (collectPacket(invitationAcknowledgedCollector, PACKET_TIMEOUT) == null) {
            throw new LocalCancellationException(
                "Received no invitation acknowledgement from " + peerNickname
                    + ".", CancelOption.DO_NOT_NOTIFY_PEER);
        }
    }

    /**
     * Waits until the remote side accepted manually the invitation.
     */
    private void awaitAcceptation(IProgressMonitor monitor)
        throws SarosCancellationException {

        log.debug(this + " : waiting for peer to accept the invitation");

        monitor.setTaskName("Waiting for " + peerNickname
            + " to accept invitation...");

        if (collectPacket(invitationAcceptedCollector,
            INVITATION_ACCEPTED_TIMEOUT) == null) {
            throw new LocalCancellationException(
                "Invitation was not accepted.", CancelOption.NOTIFY_PEER);
        }

        log.debug(this + " : invitation accepted");
    }

    /**
     * Waits until the remote side has completed the invitation which is the
     * case after the remote side has started its {@link SarosSession}.
     */
    private void awaitCompletion(IProgressMonitor monitor)
        throws SarosCancellationException {

        log.debug(this
            + " : waiting for remote side to start its Saros session");

        monitor.setTaskName("Waiting for " + peerNickname
            + " to perform final initialization...");

        if (collectPacket(invitationCompletedCollector, PACKET_TIMEOUT) == null) {
            throw new LocalCancellationException(
                "Invitation was not accepted.", CancelOption.NOTIFY_PEER);
        }

        log.debug(this + " : remote side started its Saros session");
    }

    /**
     * Waits for the remote parameters which may contain some desired default
     * values that should be used on session start.
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
            throw new LocalCancellationException(peerNickname
                + " sent malformed data", CancelOption.DO_NOT_NOTIFY_PEER);

        log.debug(this + " : received remote session negotiation data");

        return parameters;
    }

    /**
     * Checks and modifies the received remote parameters. The changes will be
     * send back and must then be used on the remote side to configure the
     * session environment.
     */
    private InvitationParameterExchangeExtension modifiyAndSendSessionNegotiationData(
        InvitationParameterExchangeExtension remoteParameters,
        IProgressMonitor monitor) {

        log.debug(this + " : sending updated session negotiation data");

        monitor.setTaskName("Sending local session configuration...");

        InvitationParameterExchangeExtension modifiedParameters = new InvitationParameterExchangeExtension(
            invitationID);

        modifiedParameters.setRemoteColorID(sarosSession.getLocalUser()
            .getColorID());

        modifiedParameters.setRemoteFavoriteColorID(sarosSession.getLocalUser()
            .getFavoriteColorID());

        modifiedParameters
            .setMUCPreferences(sarosSession.isHost() ? mucNegotiatingManager
                .getOwnPreferences() : mucNegotiatingManager
                .getSessionPreferences());

        modifiedParameters.setSessionHost(sarosSession.getHost().getJID());

        colorID = remoteParameters.getLocalColorID();
        favoriteColorID = remoteParameters.getLocalFavoriteColorID();

        modifiedParameters.setLocalColorID(colorID);
        modifiedParameters.setLocalFavoriteColorID(favoriteColorID);

        transmitter.sendMessageToUser(peer,
            InvitationParameterExchangeExtension.PROVIDER
                .create(modifiedParameters));

        log.debug(this + " : sent updated session negotiation data");

        return modifiedParameters;
    }

    /**
     * 
     * Adds the invited user to the current SarosSession. After the user is
     * added to the session the user list is synchronized and afterwards an
     * acknowledgment is send to the remote side that the remote user can now
     * start working in this session.
     */

    private User completeInvitation(IProgressMonitor monitor) {

        log.debug(this + " : synchronizing user list");

        monitor.setTaskName("Synchronizing user list...");

        User user = new User(sarosSession, peer, colorID, favoriteColorID);

        synchronized (SESSION_JOIN_LOCK) {

            sarosSession.addUser(user);
            log.debug(this + " : added " + Utils.prefix(peer)
                + " to the current session, colorID: " + colorID);

            transmitter.sendMessageToUser(peer,
                InvitationAcknowledgedExtension.PROVIDER
                    .create(new InvitationAcknowledgedExtension(invitationID)));
        }

        log.debug(this + " : session negotiation finished");

        return user;
    }

    @Override
    protected void executeCancellation() {
        // TODO remove the user from the session !

        if (invitationProcesses.getProcesses().size() == 0
            && sarosSession.getRemoteUsers().isEmpty())
            sarosSessionManager.stopSarosSession();
    }

    private void deleteCollectors() {
        invitationAcceptedCollector.cancel();
        invitationAcknowledgedCollector.cancel();
        invitationDataExchangeCollector.cancel();
        invitationCompletedCollector.cancel();
    }

    private void createCollectors() {
        invitationAcceptedCollector = receiver
            .createCollector(InvitationAcceptedExtension.PROVIDER
                .getPacketFilter(invitationID));

        invitationAcknowledgedCollector = receiver
            .createCollector(InvitationAcknowledgedExtension.PROVIDER
                .getPacketFilter(invitationID));

        invitationDataExchangeCollector = receiver
            .createCollector(InvitationParameterExchangeExtension.PROVIDER
                .getPacketFilter(invitationID));

        invitationCompletedCollector = receiver
            .createCollector(InvitationCompletedExtension.PROVIDER
                .getPacketFilter(invitationID));
    }

    @Override
    public String toString() {
        return "OSN [remote side: " + peer + "]";
    }
}
