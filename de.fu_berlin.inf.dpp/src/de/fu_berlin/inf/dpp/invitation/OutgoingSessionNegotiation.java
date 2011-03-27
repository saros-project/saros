package de.fu_berlin.inf.dpp.invitation;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.communication.muc.MUCManager;
import de.fu_berlin.inf.dpp.communication.muc.negotiation.MUCSessionPreferences;
import de.fu_berlin.inf.dpp.communication.muc.negotiation.MUCSessionPreferencesNegotiatingManager;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DefaultInvitationInfo.UserListRequestExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.InvitationInfo;
import de.fu_berlin.inf.dpp.net.internal.InvitationInfo.InvitationExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.internal.XMPPTransmitter;
import de.fu_berlin.inf.dpp.net.internal.discoveryManager.DiscoveryManager;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.ui.wizards.InvitationWizard;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.VersionManager;
import de.fu_berlin.inf.dpp.util.VersionManager.Compatibility;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

public class OutgoingSessionNegotiation extends InvitationProcess {

    private static Logger log = Logger
        .getLogger(OutgoingSessionNegotiation.class);

    protected ISarosSession sarosSession;
    protected VersionManager versionManager;
    protected SubMonitor monitor;
    protected String invitationID;
    protected final static Random INVITATION_RAND = new Random();
    protected DiscoveryManager discoveryManager;
    protected boolean peerAdvertisesSarosSupport = true;
    protected AtomicBoolean cancelled = new AtomicBoolean(false);
    protected SarosCancellationException cancellationCause;
    protected MUCManager mucManager;
    protected MUCSessionPreferencesNegotiatingManager comNegotiatingManager;
    protected VersionInfo versionInfo;
    protected SarosPacketCollector invitationCompleteCollector;

    protected Thread receiveInvitationCompleteThread = new Thread(
        new Runnable() {

            public void run() {
                try {
                    transmitter.receiveInvitationCompleteConfirmation(
                        monitor.newChild(50, SubMonitor.SUPPRESS_ALL_LABELS),
                        invitationCompleteCollector);
                } catch (LocalCancellationException e) {
                    log.debug("", e);
                    localCancel(e.getMessage(), CancelOption.NOTIFY_PEER);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    log.debug("", e);
                }
                log.debug("Inv"
                    + Utils.prefix(peer)
                    + ": Notifying participants that the invitation is complete.");
            }
        });

    @Inject
    protected SessionIDObservable sessionID;

    protected static InvitationInfo.InvitationExtensionProvider invExtProv;

    @Inject
    protected XMPPTransmitter xmppTransmitter;

    public OutgoingSessionNegotiation(ITransmitter transmitter, JID peer,
        int colorID, InvitationProcessObservable invitationProcesses,
        ISarosSession sarosSession, String description,
        VersionManager versionManager, DiscoveryManager discoveryManager,
        MUCSessionPreferencesNegotiatingManager comNegotiatingManager,
        SarosContext sarosContext) {
        super(transmitter, peer, description, colorID, invitationProcesses,
            sarosContext);

        this.sarosSession = sarosSession;
        this.versionManager = versionManager;
        this.comNegotiatingManager = comNegotiatingManager;
        this.discoveryManager = discoveryManager;

    }

    public void start(SubMonitor monitor) throws SarosCancellationException {

        log.debug("Inv" + Utils.prefix(peer) + ": Invitation has started.");

        monitor.beginTask("Invitation has started.", 101);
        this.invitationID = String.valueOf(INVITATION_RAND.nextLong());
        this.monitor = monitor;
        invitationCompleteCollector = transmitter
            .getInvitationCompleteCollector(invitationID);
        receiveInvitationCompleteThread.start();
        try {
            checkAvailability(monitor.newChild(1));

            checkVersion(monitor.newChild(1));

            sendInvitation(monitor.newChild(1));

            User newUser = addUserToSession();

            completeInvitation(monitor.newChild(3));

            sarosSessionManager.notifyPostOutgoingInvitationCompleted(monitor.newChild(1),
                newUser);

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
            monitor.done();
        }
    }

    protected void checkAvailability(SubMonitor subMonitor)
        throws LocalCancellationException {

        log.debug("Inv" + Utils.prefix(peer) + ": Checking Saros support...");
        subMonitor.setTaskName("Checking Saros support...");

        JID rqPeer = discoveryManager.getSupportingPresence(peer,
            Saros.NAMESPACE);
        if (rqPeer == null) {
            log.debug("Inv" + Utils.prefix(peer) + ": Saros is not supported.");
            if (!InvitationWizard.confirmUnsupportedSaros(peer)) {
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
    }

    /**
     * Checks the compatibility of the local Saros version with the peer's one.
     * If the versions are compatible, the invitation continues, otherwise a
     * confirmation of the user is required (a {@link MessageDialog} pops up).
     */
    protected void checkVersion(SubMonitor subMonitor)
        throws SarosCancellationException {

        log.debug("Inv" + Utils.prefix(peer) + ": Checking peer's version...");
        subMonitor.setTaskName("Checking version...");
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

        log.debug("Inv" + Utils.prefix(peer) + ": Sending invitation...");
        checkCancellation(CancelOption.DO_NOT_NOTIFY_PEER);
        subMonitor.setWorkRemaining(100);
        subMonitor.setTaskName("Sending invitation...");

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

        invExtProv = new InvitationExtensionProvider();

        xmppTransmitter.sendMessageToUser(peer, invExtProv.create(invInfo));

        // transmitter.sendInvitation(sarosSession.getProjectID(this.project),
        // peer, description, colorID, hostVersionInfo, invitationID,
        // sarosSession.getSessionStart(), doStream, comPrefs);

        subMonitor.worked(25);
        subMonitor
            .setTaskName("Invitation sent. Waiting for acknowledgement...");

        if (!transmitter.receivedInvitationAcknowledgment(invitationID,
            subMonitor.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS))) {
            throw new LocalCancellationException(
                peerAdvertisesSarosSupport ? "No invitation acknowledgement received."
                    : "Missing Saros support.", CancelOption.DO_NOT_NOTIFY_PEER);
        }

        subMonitor.worked(2);
        subMonitor
            .setTaskName("Invitation acknowledged. Waiting for user list request...");
        UserListRequestExtensionProvider userListRequestExtProv = new UserListRequestExtensionProvider();
        transmitter.receive(subMonitor.newChild(75,
            SubMonitor.SUPPRESS_ALL_LABELS), transmitter
            .getUserListRequestCollector(invitationID, userListRequestExtProv),
            10000, true);

        log.debug("Inv" + Utils.prefix(peer)
            + ": User list request has received.");
    }

    /**
     * Adds the invited user to the current SarosSession.<br>
     */
    // TODO move to SarosSession.
    protected User addUserToSession() throws SarosCancellationException {
        synchronized (sarosSession) {
            User newUser = new User(sarosSession, peer, colorID);
            this.sarosSession.addUser(newUser);
            log.debug(Utils.prefix(peer) + " added to project, colorID: "
                + colorID);

            checkCancellation(CancelOption.NOTIFY_PEER);
            sarosSession.synchronizeUserList(xmppTransmitter, peer,
                invitationID, monitor);
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

    protected void completeInvitation(SubMonitor subMonitor)
        throws SarosCancellationException {

        log.debug("Inv" + Utils.prefix(peer)
            + ": Waiting for invitation complete confirmation...");
        subMonitor.setWorkRemaining(100);
        subMonitor.setTaskName("Waiting for peer to complete invitation...");

        try {
            receiveInvitationCompleteThread.join();
        } catch (InterruptedException e) {
            log.debug("Code not designed to be interrupted", e);
            localCancel(e.getMessage(), CancelOption.NOTIFY_PEER);
            executeCancellation();
        }

        subMonitor.setTaskName("Completing invitation...");
        synchronized (sarosSession) {
            sarosSession.userInvitationCompleted(sarosSession.getUser(peer));
            checkCancellation(CancelOption.NOTIFY_PEER);
            sarosSession.synchronizeUserList(xmppTransmitter, peer,
                invitationID, subMonitor);
        }
        subMonitor.setTaskName("Invitation has completed successfully.");

        invitationProcesses.removeInvitationProcess(this);
        log.debug("Inv" + Utils.prefix(peer)
            + ": Invitation has completed successfully.");

        sarosSessionManager.startSharingProjects(peer);

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
}
