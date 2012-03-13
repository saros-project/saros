package de.fu_berlin.inf.dpp.invitation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.ProjectExchangeInfo;
import de.fu_berlin.inf.dpp.communication.muc.negotiation.MUCSessionPreferences;
import de.fu_berlin.inf.dpp.communication.muc.negotiation.MUCSessionPreferencesNegotiatingManager;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.DefaultInvitationInfo.InvitationAcknowledgementExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.DefaultInvitationInfo.InvitationCompleteExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.DefaultInvitationInfo.UserListRequestExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.InvitationInfo;
import de.fu_berlin.inf.dpp.net.internal.InvitationInfo.InvitationExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.internal.XMPPReceiver;
import de.fu_berlin.inf.dpp.net.internal.XMPPTransmitter;
import de.fu_berlin.inf.dpp.net.internal.discoveryManager.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensionUtils;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.ui.wizards.InvitationWizard;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.VersionManager;
import de.fu_berlin.inf.dpp.util.VersionManager.Compatibility;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

public class OutgoingSessionNegotiation extends InvitationProcess {

    private final static Logger log = Logger
        .getLogger(OutgoingSessionNegotiation.class);

    private final static Random INVITATION_RAND = new Random();

    protected ISarosSession sarosSession;
    protected SubMonitor monitor;
    protected String invitationID;
    protected boolean peerAdvertisesSarosSupport = true;
    protected AtomicBoolean cancelled = new AtomicBoolean(false);
    protected SarosCancellationException cancellationCause;

    protected VersionInfo versionInfo;

    protected SarosPacketCollector invitationCompleteCollector;
    protected SarosPacketCollector invitationAcknowledgedCollector;
    protected SarosPacketCollector userListRequestCollector;

    @Inject
    protected VersionManager versionManager;

    @Inject
    protected DiscoveryManager discoveryManager;

    @Inject
    protected MUCSessionPreferencesNegotiatingManager comNegotiatingManager;

    @Inject
    protected SessionIDObservable sessionID;

    @Inject
    protected XMPPTransmitter xmppTransmitter;

    @Inject
    protected XMPPReceiver xmppReceiver;

    @Inject
    protected EditorManager editorManager;

    @Inject
    protected DataTransferManager dataTransferManager;

    public OutgoingSessionNegotiation(JID peer, int colorID,
        ISarosSession sarosSession, String description,
        SarosContext sarosContext) {
        super(peer, description, colorID, sarosContext);

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
     *               (OutgoingProcjectNegotiation)
     * 
     *               All other participants need negotiations for:
     * 
     *               - Dealing with a received invitation to a session
     *               (IncomingSessionNegotiation)
     * 
     *               - Handling incoming shared project resources
     *               (IncomingProjectNegotiation)
     */

    public void start(SubMonitor monitor) throws SarosCancellationException {
        log.debug("Inv" + Utils.prefix(peer) + ": Invitation has started.");

        monitor.beginTask("Negotiating session...", 100);
        this.invitationID = String.valueOf(INVITATION_RAND.nextLong());
        this.monitor = monitor;

        createCollectors();

        try {
            checkAvailability(monitor.newChild(1, SubMonitor.SUPPRESS_NONE));

            checkVersion(monitor.newChild(1, SubMonitor.SUPPRESS_NONE));

            sendInvitation(monitor.newChild(1, SubMonitor.SUPPRESS_NONE));

            User newUser = addUserToSession(monitor.newChild(0,
                SubMonitor.SUPPRESS_ALL_LABELS));

            monitor.setTaskName("Negotiating data connection...");

            /*
             * HACK Ensure byte stream connection to peer so the project wizard
             * always show the currently used connection (IBB, Socks5(D/M)
             */

            dataTransferManager.getConnection(
                peer,
                monitor.newChild(0, SubMonitor.SUPPRESS_BEGINTASK
                    | SubMonitor.SUPPRESS_SETTASKNAME));

            monitor.subTask("");
            monitor.worked(7);

            editorManager.setAllLocalOpenedEditorsLocked(true);

            // FIXME lock the projects on the workspace !
            List<ProjectExchangeInfo> projectExchangeInfos = sarosSessionManager
                .createProjectExchangeInfoList(new ArrayList<IProject>(
                    sarosSession.getProjects()), monitor.newChild(85,
                    SubMonitor.SUPPRESS_NONE));

            monitor.subTask("");

            completeInvitation(monitor.newChild(5, SubMonitor.SUPPRESS_NONE),
                projectExchangeInfos);

            sarosSessionManager.notifyPostOutgoingInvitationCompleted(
                monitor.newChild(0, SubMonitor.SUPPRESS_ALL_LABELS), newUser);

        } catch (LocalCancellationException e) {
            localCancel(e.getMessage(), e.getCancelOption());
            executeCancellation();
        } catch (RemoteCancellationException e) {
            remoteCancel(e.getMessage());
            executeCancellation();
        } catch (SarosCancellationException e) {
            /**
             * If this exception is thrown because of a local cancellation, we
             * initiate a localCancel here.
             * 
             * If this exception is thrown because of a remote cancellation, the
             * call of localCancel will be ignored.
             */
            localCancel(e.getMessage(), CancelOption.NOTIFY_PEER);
            executeCancellation();
        } catch (IOException e) {
            String errorMsg = "Unknown error: " + e;
            if (e.getMessage() != null)
                errorMsg = e.getMessage();
            localCancel(errorMsg, CancelOption.NOTIFY_PEER);
            executeCancellation();
        } catch (Exception e) {
            log.warn("Inv" + Utils.prefix(peer)
                + ": This type of Exception is not expected: ", e);
            String errorMsg = "Unknown error: " + e;
            if (e.getMessage() != null)
                errorMsg = e.getMessage();
            localCancel(errorMsg, CancelOption.NOTIFY_PEER);
            executeCancellation();
        } finally {
            deleteCollectors();
            monitor.done();
        }
    }

    protected void checkAvailability(SubMonitor subMonitor)
        throws LocalCancellationException {

        log.debug("Inv" + Utils.prefix(peer) + ": Checking Saros support...");
        subMonitor.beginTask("Checking Saros support...", 1);

        JID rqPeer = discoveryManager.getSupportingPresence(peer,
            Saros.NAMESPACE);

        if (rqPeer == null) {
            log.debug("Inv" + Utils.prefix(peer)
                + ": Saros is not supported or User is offline.");

            if (!discoveryManager.isOnline(peer)) {
                InvitationWizard.notifyUserOffline(peer);
                localCancel(null, CancelOption.DO_NOT_NOTIFY_PEER);
                throw new LocalCancellationException();
            } else if (!InvitationWizard.confirmUnsupportedSaros(peer)) {
                localCancel(null, CancelOption.DO_NOT_NOTIFY_PEER);
                throw new LocalCancellationException();
            }
            /**
             * In order to avoid inviting other XMPP clients, we construct an
             * RQ-JID.
             */
            rqPeer = new JID(peer.getBareJID() + "/" + Saros.RESOURCE);
            peerAdvertisesSarosSupport = false;
        } else {
            log.debug("Inv" + Utils.prefix(peer) + ": Saros is supported.");
        }
        peer = rqPeer;
        subMonitor.worked(1);
        subMonitor.done();
    }

    /**
     * Checks the compatibility of the local Saros version with the peer's one.
     * If the versions are compatible, the invitation continues, otherwise a
     * confirmation of the user is required (a {@link MessageDialog} pops up).
     */
    protected void checkVersion(SubMonitor subMonitor)
        throws SarosCancellationException {

        log.debug("Inv" + Utils.prefix(peer) + ": Checking peer's version...");
        subMonitor.beginTask("Checking version compatibility...", 1);

        VersionInfo versionInfo = versionManager.determineCompatibility(peer);

        checkCancellation(CancelOption.DO_NOT_NOTIFY_PEER);

        Compatibility comp = null;

        if (versionInfo != null) {
            comp = versionInfo.compatibility;

            if (comp == VersionManager.Compatibility.OK) {
                log.debug("Inv" + Utils.prefix(peer)
                    + ": Saros versions are compatible, proceeding...");
                this.versionInfo = versionInfo;
            } else {
                log.debug("Inv" + Utils.prefix(peer)
                    + ": Saros versions are not compatible.");
                if (InvitationWizard.confirmVersionConflict(versionInfo, peer,
                    versionManager.getVersion()))
                    this.versionInfo = versionInfo;
                else {
                    localCancel(null, CancelOption.DO_NOT_NOTIFY_PEER);
                    throw new LocalCancellationException();
                }
            }
        } else {
            log.debug("Inv" + Utils.prefix(peer)
                + ": Unable to obtain peer's version information.");
            if (!InvitationWizard.confirmUnknownVersion(peer,
                versionManager.getVersion()))
                localCancel(null, CancelOption.DO_NOT_NOTIFY_PEER);
        }
        subMonitor.worked(1);
        subMonitor.done();
    }

    /**
     * Send an invitation, then wait for the peer to accept and request the file
     * list.
     * 
     * @param subMonitor
     * @throws SarosCancellationException
     * @throws IOException
     */
    protected void sendInvitation(SubMonitor subMonitor)
        throws SarosCancellationException, IOException {

        subMonitor.beginTask("Sending invitation...", 3);

        log.debug("Inv" + Utils.prefix(peer) + ": Sending invitation...");
        checkCancellation(CancelOption.DO_NOT_NOTIFY_PEER);

        /*
         * TODO: this method should get a complete VersionInfo object from the
         * checkVersion() method.
         */
        VersionInfo hostVersionInfo = versionInfo;
        if (hostVersionInfo == null) {
            hostVersionInfo = new VersionInfo();
            hostVersionInfo.compatibility = null;
        }

        hostVersionInfo.version = versionManager.getVersion();

        MUCSessionPreferences comPrefs = comNegotiatingManager
            .getOwnPreferences();

        InvitationInfo invInfo = new InvitationInfo(sessionID, invitationID,
            colorID, description, versionInfo, sarosSession.getSessionStart(),
            comPrefs);

        xmppTransmitter.sendMessageToUser(peer,
            new InvitationExtensionProvider().create(invInfo));

        subMonitor.worked(1);

        subMonitor
            .setTaskName("Invitation sent. Waiting for acknowledgement...");

        if (collectPacket(invitationAcknowledgedCollector,
            ITransmitter.INVITATION_ACKNOWLEDGEMENT_TIMEOUT, subMonitor) == null) {
            throw new LocalCancellationException(
                peerAdvertisesSarosSupport ? "No invitation acknowledgement received."
                    : "Missing Saros support.", CancelOption.DO_NOT_NOTIFY_PEER);
        }

        subMonitor
            .setTaskName("Invitation acknowledged. Waiting for user list request...");

        if (collectPacket(userListRequestCollector, 10000, subMonitor) == null) {
            throw new LocalCancellationException(
                peerAdvertisesSarosSupport ? "No user list request received."
                    : "Missing Saros support.", CancelOption.DO_NOT_NOTIFY_PEER);
        }

        // Reply is send in addUserToSession !

        log.debug("Inv" + Utils.prefix(peer)
            + ": User list request has received.");

        subMonitor.done();
    }

    /**
     * Adds the invited user to the current SarosSession.<br>
     */
    // TODO move to SarosSession.
    protected User addUserToSession(SubMonitor subMonitor)
        throws SarosCancellationException {
        synchronized (sarosSession) {
            User newUser = new User(sarosSession, peer, colorID);
            this.sarosSession.addUser(newUser);
            log.debug(Utils.prefix(peer) + " added to project, colorID: "
                + colorID);

            checkCancellation(CancelOption.NOTIFY_PEER);
            sarosSession.synchronizeUserList(xmppTransmitter, peer,
                invitationID, subMonitor);
            return newUser;
        }
    }

    /**
     * This method does <strong>not</strong> execute the cancellation but only
     * sets the {@link #cancellationCause}. It should be called if the
     * cancellation was initated by the <strong>remote</strong> user. The
     * cancellation will be ignored if the invitation has already been cancelled
     * before. <br>
     * In order to cancel the invitation process {@link #executeCancellation()}
     * should be called.
     * 
     * @param errorMsg
     *            the error that caused the cancellation. This should be some
     *            user-friendly text as it might be presented to the user.
     *            <code>null</code> if the cancellation was caused by the user's
     *            request and not by some error.
     */
    @Override
    public void remoteCancel(String errorMsg) {
        if (!cancelled.compareAndSet(false, true))
            return;
        log.debug("Inv" + Utils.prefix(peer) + ": remoteCancel: " + errorMsg);
        if (monitor != null)
            monitor.setCanceled(true);
        cancellationCause = new RemoteCancellationException(errorMsg);
    }

    protected void completeInvitation(SubMonitor subMonitor,
        List<ProjectExchangeInfo> projectExchangeInfos)
        throws SarosCancellationException {

        log.debug("Inv" + Utils.prefix(peer)
            + ": Waiting for invitation complete confirmation...");

        subMonitor.beginTask("Waiting for peer to complete invitation...", 2);

        if (collectPacket(invitationCompleteCollector, 10000, subMonitor) == null) {
            throw new LocalCancellationException(
                peerAdvertisesSarosSupport ? "no invitation complete response received"
                    : "Missing Saros support.", CancelOption.DO_NOT_NOTIFY_PEER);
        }

        log.debug("Inv" + Utils.prefix(peer)
            + ": Notifying participants that the invitation is complete.");

        subMonitor.setTaskName("Completing invitation...");
        synchronized (sarosSession) {
            sarosSession.userInvitationCompleted(sarosSession.getUser(peer));
            checkCancellation(CancelOption.NOTIFY_PEER);
            sarosSession.synchronizeUserList(xmppTransmitter, peer,
                invitationID,
                subMonitor.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS));
        }

        subMonitor.setTaskName("Invitation has completed successfully.");

        invitationProcesses.removeInvitationProcess(this);
        log.debug("Inv" + Utils.prefix(peer)
            + ": Invitation has completed successfully.");

        sarosSessionManager.startSharingProjects(peer, projectExchangeInfos);
        subMonitor.done();

    }

    public void localCancel(String errorMsg, CancelOption cancelOption) {
        if (!cancelled.compareAndSet(false, true))
            return;
        log.debug("Inv" + Utils.prefix(peer) + ": localCancel: " + errorMsg);
        if (monitor != null)
            monitor.setCanceled(true);
        cancellationCause = new LocalCancellationException(errorMsg,
            cancelOption);
    }

    /**
     * Cancels the invitation process based on the exception stored in
     * {@link #cancellationCause}. This method is always called by this local
     * object, so even if an another object "cancels" the invitation (
     * {@link #localCancel(String, CancelOption)}, {@link #remoteCancel(String)}
     * ), the exceptions will be thrown up on the stack to the caller of
     * {@link #start(SubMonitor)}, and not to the object which has "cancelled"
     * the process. The cancel methods (
     * {@link #localCancel(String, CancelOption)}, {@link #remoteCancel(String)}
     * ) do not cancel the invitation alone, but they set the
     * {@link #cancellationCause} and cancel the {@link #monitor}. Now it is the
     * responsibility of the objects which use the {@link #monitor} to throw a
     * {@link SarosCancellationException} (or it's subclasses), which will be
     * caught by this object causing a call to this method. If this does not
     * happen, the next {@link #checkCancellation(CancelOption)} cancels the
     * invitation.
     */
    protected void executeCancellation() throws SarosCancellationException {

        log.debug("Inv" + Utils.prefix(peer) + ": executeCancellation");
        if (!cancelled.get())
            throw new IllegalStateException(
                "executeCancellation should only be called after localCancel or remoteCancel!");

        String errorMsg;
        String cancelMessage;
        if (cancellationCause instanceof LocalCancellationException) {
            LocalCancellationException e = (LocalCancellationException) cancellationCause;
            errorMsg = e.getMessage();

            switch (e.getCancelOption()) {
            case NOTIFY_PEER:
                transmitter.sendCancelInvitationMessage(peer, errorMsg);
                break;
            case DO_NOT_NOTIFY_PEER:
                break;
            default:
                log.warn("Inv" + Utils.prefix(peer)
                    + ": This case is not expected here.");
            }

            if (errorMsg != null) {
                cancelMessage = "Invitation was cancelled locally"
                    + " because of an error: " + errorMsg;
                log.error("Inv" + Utils.prefix(peer) + ": " + cancelMessage);
                monitor.setTaskName("Invitation failed. (" + errorMsg + ")");
            } else {
                cancelMessage = "Invitation was cancelled by local user.";
                log.debug("Inv" + Utils.prefix(peer) + ": " + cancelMessage);
                monitor.setTaskName("Invitation has been cancelled.");
            }

        } else if (cancellationCause instanceof RemoteCancellationException) {
            RemoteCancellationException e = (RemoteCancellationException) cancellationCause;

            errorMsg = e.getMessage();
            if (errorMsg != null) {
                cancelMessage = "Invitation was cancelled by the remote user "
                    + " because of an error on his/her side: " + errorMsg;
                log.error("Inv" + Utils.prefix(peer) + ": " + cancelMessage);
                monitor.setTaskName("Invitation failed.");
            } else {
                cancelMessage = "Invitation was cancelled by the remote user.";
                log.debug("Inv" + Utils.prefix(peer) + ": " + cancelMessage);
                monitor.setTaskName("Invitation has been cancelled.");
            }
        } else {
            log.error("This type of exception is not expected here: ",
                cancellationCause);
            monitor.setTaskName("Invitation failed.");
        }
        sarosSession.returnColor(this.colorID);

        if (sarosSession.getRemoteUsers().isEmpty())
            sarosSessionManager.stopSarosSession();

        if (invitationProcesses.getProcesses().containsValue(this))
            invitationProcesses.removeInvitationProcess(this);
        throw cancellationCause;
    }

    /**
     * Checks whether the invitation process or the monitor has been cancelled.
     * If the monitor has been cancelled but the invitation process has not yet,
     * it cancels the invitation process.
     * 
     * @throws SarosCancellationException
     *             if the invitation process or the monitor has already been
     *             cancelled.
     */
    protected void checkCancellation(CancelOption cancelOption)
        throws SarosCancellationException {
        if (cancelled.get()) {
            log.debug("Inv" + Utils.prefix(peer) + ": Cancellation checkpoint");
            throw new SarosCancellationException();
        }

        if (monitor == null) {
            log.warn("Inv" + Utils.prefix(peer) + ": The monitor is null.");
            return;
        }

        if (monitor.isCanceled()) {
            log.debug("Inv" + Utils.prefix(peer) + ": Cancellation checkpoint");
            localCancel(null, cancelOption);
            throw new SarosCancellationException();
        }
    }

    protected Packet collectPacket(SarosPacketCollector collector,
        long timeout, IProgressMonitor monitor)
        throws SarosCancellationException {

        Packet packet = null;

        while (timeout > 0) {
            if (monitor != null && monitor.isCanceled())
                checkCancellation(CancelOption.NOTIFY_PEER);

            packet = collector.nextResult(1000);

            if (packet != null)
                break;

            timeout -= 1000;
        }

        return packet;
    }

    protected void deleteCollectors() {
        invitationCompleteCollector.cancel();
        invitationAcknowledgedCollector.cancel();
        userListRequestCollector.cancel();
    }

    protected void createCollectors() {
        invitationCompleteCollector = xmppReceiver
            .createCollector(PacketExtensionUtils.getInvitationFilter(
                new InvitationCompleteExtensionProvider(), sessionID,
                invitationID));

        invitationAcknowledgedCollector = xmppReceiver
            .createCollector(PacketExtensionUtils.getInvitationFilter(
                new InvitationAcknowledgementExtensionProvider(), sessionID,
                invitationID));

        userListRequestCollector = xmppReceiver
            .createCollector(PacketExtensionUtils
                .getInvitationFilter(new UserListRequestExtensionProvider(),
                    sessionID, invitationID));
    }
}
