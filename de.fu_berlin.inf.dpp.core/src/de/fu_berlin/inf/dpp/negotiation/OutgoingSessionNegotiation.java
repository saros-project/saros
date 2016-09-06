package de.fu_berlin.inf.dpp.negotiation;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.ISarosContext;
import de.fu_berlin.inf.dpp.SarosConstants;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationAcceptedExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationAcknowledgedExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationCompletedExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationOfferingExtension;
import de.fu_berlin.inf.dpp.communication.extensions.InvitationParameterExchangeExtension;
import de.fu_berlin.inf.dpp.editor.colorstorage.UserColorID;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.NegotiationTools.CancelOption;
import de.fu_berlin.inf.dpp.negotiation.hooks.ISessionNegotiationHook;
import de.fu_berlin.inf.dpp.net.PacketCollector;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.discovery.DiscoveryManager;
import de.fu_berlin.inf.dpp.session.ColorNegotiationHook;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.versioning.Compatibility;
import de.fu_berlin.inf.dpp.versioning.VersionCompatibilityResult;
import de.fu_berlin.inf.dpp.versioning.VersionManager;

/*
 * IMPORTANT: All messages in the cancellation exception are SHOWN to the end user !
 */
public final class OutgoingSessionNegotiation extends SessionNegotiation {

    private static final Logger log = Logger
        .getLogger(OutgoingSessionNegotiation.class);

    private static final boolean IGNORE_VERSION_COMPATIBILITY = Boolean
        .getBoolean("de.fu_berlin.inf.dpp.negotiation.session.IGNORE_VERSION_COMPATIBILITY");

    private static final Random NEGOTIATION_ID_GENERATOR = new Random();

    private String localVersion;

    private PacketCollector invitationAcceptedCollector;
    private PacketCollector invitationAcknowledgedCollector;
    private PacketCollector invitationDataExchangeCollector;
    private PacketCollector invitationCompletedCollector;

    @Inject
    private VersionManager versionManager;

    @Inject
    private DiscoveryManager discoveryManager;

    // HACK last residue of the direct connection between SessionNegotiation and
    // the color property of users.
    private int clientColorID = UserColorID.UNKNOWN;
    private int clientFavoriteColorID = UserColorID.UNKNOWN;

    public OutgoingSessionNegotiation(JID peer, ISarosSession sarosSession,
        String description, ISarosContext sarosContext) {

        super(String.valueOf(NEGOTIATION_ID_GENERATOR.nextLong()), peer,
            description, sarosContext);

        this.sarosSession = sarosSession;
    }

    @Override
    protected void executeCancellation() {
        final User user = sarosSession.getUser(getPeer());

        if (user != null)
            sarosSession.removeUser(user);
    }

    /**
     * @JTourBusStop 4, Invitation Process:
     * 
     *               The details of the invitation process are implemented in
     *               the negotiation package. OutgoingSessionNegotiation is an
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
        log.debug(this + " : starting negotiation");

        observeMonitor(monitor);

        monitor.beginTask("Starting session negotiation...",
            IProgressMonitor.UNKNOWN);

        createCollectors();

        Exception exception = null;

        try {
            /**
             * @JTourBusStop 5, Invitation Process:
             * 
             *               For starting a session, the host does the following
             *               things (see next JTourBusStops for the
             *               corresponding steps on the client side):
             * 
             *               (1) Check whether Saros is available on the
             *               client's side (via the DiscoveryManager).
             * 
             *               (2) Check whether the client's Saros is compatible
             *               with own version (via the VersionManager).
             * 
             *               (3a) Send a session invitation offering to the
             *               client.
             * 
             *               (3b) [client side, see subsequent stops]
             * 
             *               (3c) Waits until the client automatically responds
             *               to the offering ("acknowledgement").
             * 
             *               (4a, 4b) [client side, see subsequent stops]
             * 
             *               (4c) Wait until the remote user manually accepted
             *               the session invitation ("acceptance").
             * 
             *               (5a) [client side, see subsequent stops]
             * 
             *               (5b) Wait for the client's wishlist of the
             *               session's parameters (e.g. his own favorite color).
             * 
             *               (6a) Consider these preferences and send the
             *               settled session parameters back to the client.
             * 
             *               (6b, 7, 8) [client side, see subsequent stops]
             * 
             *               (9) Wait until the client signals the session
             *               invitation is complete.
             * 
             *               (10) Formally add client to the session so he will
             *               receive activities, then send final acknowledgement
             *               to inform client about this.
             */
            checkAvailability(monitor);

            checkVersion(monitor);

            sendInvitationOffer(monitor);

            awaitAcknowledgement(monitor);

            awaitAcceptance(monitor);

            InvitationParameterExchangeExtension clientSessionPreferences;
            clientSessionPreferences = awaitClientSessionPreferences(monitor);

            InvitationParameterExchangeExtension actualSessionParameters;
            actualSessionParameters = determineSessionParameters(clientSessionPreferences);

            sendSessionParameters(actualSessionParameters, monitor);

            awaitCompletion(monitor);

            User newUser = completeInvitation(monitor);

            monitor.done();

            // Whiteboard is using this listener
            sessionManager.postOutgoingInvitationCompleted(monitor, newUser);

        } catch (Exception e) {
            exception = e;
        } finally {
            deleteCollectors();
            monitor.done();
        }

        return terminate(exception);
    }

    /**
     * Performs a discovery request on the remote side and checks for Saros
     * support. When this method returns, the remote JID (see
     * {@link SessionNegotiation#peer}) has been properly updated to a full
     * resource qualified JID.
     */
    private void checkAvailability(IProgressMonitor monitor)
        throws LocalCancellationException {

        log.debug(this + " : checking Saros support");
        monitor.setTaskName("Checking Saros support...");

        JID resourceQualifiedJID = discoveryManager.getSupportingPresence(
            getPeer(), SarosConstants.XMPP_FEATURE_NAMESPACE);

        if (resourceQualifiedJID == null)
            throw new LocalCancellationException(
                getPeer()
                    + " does not support Saros or the request timed out. Please try again.",
                CancelOption.DO_NOT_NOTIFY_PEER);

        log.debug(this + " :  remote contact offers Saros support");

        // FIXME accept only RQ JIDs
        setPeer(resourceQualifiedJID);
    }

    /**
     * Checks the compatibility of the local Saros version with the remote side.
     * If the versions are compatible, the invitation continues. Otherwise, the
     * invitation is canceled locally.
     * <p>
     * However, if
     * {@link OutgoingSessionNegotiation#IGNORE_VERSION_COMPATIBILITY} is set to
     * <code>true</code> the invitation process will continue.
     */
    private void checkVersion(IProgressMonitor monitor)
        throws SarosCancellationException {

        log.debug(this + " : checking version compatibility");
        monitor.setTaskName("Checking version compatibility...");

        VersionCompatibilityResult result = versionManager
            .determineVersionCompatibility(getPeer());

        checkCancellation(CancelOption.DO_NOT_NOTIFY_PEER);

        if (result == null) {
            log.error(this + " : could not obtain remote Saros version");
            throw new LocalCancellationException(
                "Could not obtain the version of the Saros plugin from "
                    + getPeer().getBareJID() + ". Please try again.",
                CancelOption.DO_NOT_NOTIFY_PEER);
        }

        Compatibility comp = result.getCompatibility();

        if (comp != Compatibility.OK && !IGNORE_VERSION_COMPATIBILITY) {
            log.error(this + " : Saros versions are not compatible");
            throw new LocalCancellationException(
                "The Saros plugin of "
                    + getPeer().getBareJID()
                    + " (Version "
                    + result.getRemoteVersion()
                    + ") is not compatible with your installed Saros plugin (Version "
                    + result.getLocalVersion() + ")",
                CancelOption.DO_NOT_NOTIFY_PEER);
        }

        if (comp == Compatibility.OK)
            log.debug(this + " : Saros versions are compatible");
        else
            log.warn(this + " : Saros versions are not compatible");

        localVersion = result.getLocalVersion().toString();
    }

    /**
     * Sends an invitation offer to the client.
     */
    private void sendInvitationOffer(IProgressMonitor monitor)
        throws SarosCancellationException {
        monitor.setTaskName("Sending negotiation request...");

        log.debug(this + " : sending negotiation request");
        checkCancellation(CancelOption.DO_NOT_NOTIFY_PEER);

        InvitationOfferingExtension invitationOffering = new InvitationOfferingExtension(
            getID(), sarosSession.getID(), localVersion, description);

        transmitter.sendPacketExtension(getPeer(),
            InvitationOfferingExtension.PROVIDER.create(invitationOffering));
    }

    /**
     * Waits for the client's acknowledgment for the invitation offering. The
     * acknowledgment is auto-generated on the remote side.
     */
    private void awaitAcknowledgement(IProgressMonitor monitor)
        throws SarosCancellationException {
        log.debug(this + " : waiting for negotiation acknowledgement");

        monitor.setTaskName("Waiting for negotiation acknowledgement...");

        if (collectPacket(invitationAcknowledgedCollector, PACKET_TIMEOUT) == null) {
            throw new LocalCancellationException(
                "Received no negotiation acknowledgement from " + getPeer()
                    + ".", CancelOption.DO_NOT_NOTIFY_PEER);
        }

        log.debug(this + " : negotiation acknowledged");
    }

    /**
     * Waits until the remote side manually accepts the invitation.
     */
    private void awaitAcceptance(IProgressMonitor monitor)
        throws SarosCancellationException {

        log.debug(this + " : waiting for peer to accept the negotiation");

        /*
         * TODO get rid of the wording "invitation" and remove the remote peer
         * name here as it is part of the UI to present this name
         */
        monitor.setTaskName("Waiting for " + getPeer().getBareJID()
            + " to accept invitation...");

        if (collectPacket(invitationAcceptedCollector,
            INVITATION_ACCEPTED_TIMEOUT) == null) {
            throw new LocalCancellationException(
                "Negotiation was not accepted.", CancelOption.NOTIFY_PEER);
        }

        log.debug(this + " : negotiation accepted");
    }

    /**
     * Waits for the client's session parameters. They may contain some desired
     * default values that should be used on session start.
     */
    private InvitationParameterExchangeExtension awaitClientSessionPreferences(
        IProgressMonitor monitor) throws SarosCancellationException {

        log.debug(this
            + " : waiting for client's session negotiation configuration data");

        monitor.setTaskName("Waiting for remote session configuration data...");

        Packet packet = collectPacket(invitationDataExchangeCollector,
            PACKET_TIMEOUT);

        if (packet == null)
            throw new LocalCancellationException(getPeer()
                + " does not respond. (Timeout)",
                CancelOption.DO_NOT_NOTIFY_PEER);

        InvitationParameterExchangeExtension parameters;
        parameters = InvitationParameterExchangeExtension.PROVIDER
            .getPayload(packet);

        if (parameters == null)
            throw new LocalCancellationException(getPeer()
                + " sent malformed data", CancelOption.DO_NOT_NOTIFY_PEER);

        log.debug(this + " : received client's session parameters");

        return parameters;
    }

    /**
     * Checks and modifies the received remote parameters.
     */
    private InvitationParameterExchangeExtension determineSessionParameters(
        InvitationParameterExchangeExtension clientParameters) {

        // general purpose
        InvitationParameterExchangeExtension hostParameters = new InvitationParameterExchangeExtension(
            getID());

        hostParameters.setSessionHost(sarosSession.getHost().getJID());

        // call each hook to do its magic
        for (ISessionNegotiationHook hook : hookManager.getHooks()) {
            Map<String, String> preferredSettings = clientParameters
                .getHookSettings(hook);

            Map<String, String> finalSettings = hook.considerClientPreferences(
                getPeer(), preferredSettings);

            if (finalSettings == null)
                continue;

            hostParameters.saveHookSettings(hook, finalSettings);

            // HACK A User object representing the client needs to access these
            // two values in completeInvitation(). Color management should work
            // differently.
            if (hook instanceof ColorNegotiationHook) {
                clientColorID = Integer.parseInt(finalSettings
                    .get(ColorNegotiationHook.KEY_CLIENT_COLOR));
                clientFavoriteColorID = Integer.parseInt(finalSettings
                    .get(ColorNegotiationHook.KEY_CLIENT_FAV_COLOR));
            }
        }

        return hostParameters;
    }

    /**
     * The changes will be send back and must then be used on the remote side to
     * configure the session environment.
     */
    private void sendSessionParameters(
        InvitationParameterExchangeExtension modifiedParameters,
        IProgressMonitor monitor) {

        log.debug(this + " : sending updated session negotiation data");

        monitor.setTaskName("Sending final session configuration data...");
        transmitter.sendPacketExtension(getPeer(),
            InvitationParameterExchangeExtension.PROVIDER
                .create(modifiedParameters));

        log.debug(this + " : sent updated session negotiation data");
    }

    /**
     * Waits until the remote side has completed the invitation. This is the
     * case after the remote side has started its {@link ISarosSession}.
     */
    private void awaitCompletion(IProgressMonitor monitor)
        throws SarosCancellationException {

        log.debug(this
            + " : waiting for remote side to start its Saros session");

        monitor
            .setTaskName("Establishing connection and performing final initialization...");

        if (collectPacket(invitationCompletedCollector, PACKET_TIMEOUT) == null) {
            throw new LocalCancellationException(
                "Invitation was not accepted.", CancelOption.NOTIFY_PEER);
        }

        log.debug(this + " : remote side started its Saros session");
    }

    private static final Object REMOVE_ME_IF_SESSION_ADD_USER_IS_THREAD_SAFE = new Object();

    /**
     * 
     * Adds the invited user to the current SarosSession. After the user is
     * added to the session the user list is synchronized and afterwards an
     * acknowledgment is send to the remote side that the remote user can now
     * start working in this session.
     * 
     * @throws IOException
     */
    private User completeInvitation(IProgressMonitor monitor)
        throws IOException {

        log.debug(this + " : synchronizing user list");

        monitor.setTaskName("Synchronizing user list...");

        User user = new User(getPeer(), false, false, clientColorID,
            clientFavoriteColorID);

        synchronized (REMOVE_ME_IF_SESSION_ADD_USER_IS_THREAD_SAFE) {

            sarosSession.addUser(user);
            log.debug(this + " : added " + getPeer()
                + " to the current session, colorID: " + clientColorID);

            /* *
             * 
             * @JTourBusStop 7, Creating custom network messages, Sending custom
             * messages:
             * 
             * This is pretty straight forward. Create an instance of your
             * extension with the proper arguments and use the provider to
             * create a (marshalled) packet extension. The extension can now be
             * send using the various methods of the ITransmitted interface.
             */

            transmitter.send(ISarosSession.SESSION_CONNECTION_ID, getPeer(),
                InvitationAcknowledgedExtension.PROVIDER
                    .create(new InvitationAcknowledgedExtension(getID())));
        }

        log.debug(this + " : session negotiation finished");

        return user;
    }

    private void createCollectors() {

        /* *
         * 
         * @JTourBusStop 9, Creating custom network messages, Receiving custom
         * messages - Part 2:
         * 
         * Another way to receive custom message is to use a collector which you
         * can poll instead. The same rules as in step 7 applies to the
         * collector as well. Pay attention to the filter you use and avoid
         * using the collector when the current thread context is the context
         * for dispatching messages.
         * 
         * IMPORTANT: Your logic must ensure that the collector is canceled
         * after it is no longer used. Failing to do so will result in memory
         * leaks.
         */

        invitationAcceptedCollector = receiver
            .createCollector(InvitationAcceptedExtension.PROVIDER
                .getPacketFilter(getID()));

        invitationAcknowledgedCollector = receiver
            .createCollector(InvitationAcknowledgedExtension.PROVIDER
                .getPacketFilter(getID()));

        invitationDataExchangeCollector = receiver
            .createCollector(InvitationParameterExchangeExtension.PROVIDER
                .getPacketFilter(getID()));

        invitationCompletedCollector = receiver
            .createCollector(InvitationCompletedExtension.PROVIDER
                .getPacketFilter(getID()));
    }

    private void deleteCollectors() {
        invitationAcceptedCollector.cancel();
        invitationAcknowledgedCollector.cancel();
        invitationDataExchangeCollector.cancel();
        invitationCompletedCollector.cancel();
    }

    @Override
    public String toString() {
        return "OSN [remote side: " + getPeer() + "]";
    }
}
