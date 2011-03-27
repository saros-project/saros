package de.fu_berlin.inf.dpp.invitation;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;
import org.joda.time.DateTime;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosContext;
import de.fu_berlin.inf.dpp.User.Permission;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelLocation;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DefaultInvitationInfo;
import de.fu_berlin.inf.dpp.net.internal.DefaultInvitationInfo.UserListRequestExtensionProvider;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.wizards.JoinSessionWizard;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.VersionManager;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

public class IncomingSessionNegotiation extends InvitationProcess {

    private static Logger log = Logger
        .getLogger(IncomingSessionNegotiation.class);

    protected SarosSessionManager sessionManager;
    protected JoinSessionWizard inInvitationUI;
    protected VersionManager versionManager;
    protected DateTime sessionStart;
    protected ISarosSession sarosSession;
    protected String invitationID;
    protected Saros saros;

    @Inject
    SessionIDObservable sessionID;

    protected SarosCancellationException cancellationCause;
    public VersionInfo versionInfo;

    protected AtomicBoolean cancelled = new AtomicBoolean(false);

    protected SubMonitor monitor;

    public IncomingSessionNegotiation(SarosSessionManager sessionManager,
        ITransmitter transmitter, JID from, int colorID,
        InvitationProcessObservable invitationProcesses,
        VersionManager versionManager, VersionInfo remoteVersionInfo,
        DateTime sessionStart, SarosUI sarosUI, String invitationID,
        Saros saros, String description, SarosContext sarosContext) {
        super(transmitter, from, description, colorID, invitationProcesses,
            sarosContext);

        this.versionInfo = determineVersion(remoteVersionInfo);
        this.sessionStart = sessionStart;
        this.invitationID = invitationID;
        this.saros = saros;
        this.sessionManager = sessionManager;
        this.versionManager = versionManager;

    }

    protected VersionInfo determineVersion(VersionInfo remoteVersionInfo) {
        log.debug("Inv" + Utils.prefix(peer) + ": Determining version...");
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

    public void acknowledgeInvitation() {
        transmitter.sendInvitationAcknowledgement(peer, invitationID);
    }

    /**
     * This method does <strong>not</strong> execute the cancellation but only
     * sets the {@link #cancellationCause}. It should be called if the
     * cancellation was initated by the <strong>local</strong> user. The
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
     * 
     * @param cancelOption
     *            If <code>NOTIFY_PEER</code> we send a cancellation message to
     *            our peer.
     */
    public void localCancel(String errorMsg, CancelOption cancelOption) {
        if (!cancelled.compareAndSet(false, true))
            return;
        log.debug("Inv" + Utils.prefix(peer) + ": localCancel: " + errorMsg);
        if (monitor != null)
            monitor.setCanceled(true);
        cancellationCause = new LocalCancellationException(errorMsg,
            cancelOption);
        if (monitor == null) {
            log.debug("Inv" + Utils.prefix(peer)
                + ": Closing JoinSessionWizard manually.");
            try {
                executeCancellation();
            } catch (SarosCancellationException e) {
                /**
                 * This happens if the JoinSessionWizard is currently waiting
                 * for user input.
                 */
                if (inInvitationUI != null)
                    inInvitationUI.cancelWizard(peer, e.getMessage(),
                        CancelLocation.LOCAL);
                else
                    log.error("The inInvitationUI is null, could not"
                        + " close the JoinSessionWizard.");
            }
        }
    }

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
            } else {
                cancelMessage = "Invitation was cancelled by local user.";
                log.debug("Inv" + Utils.prefix(peer) + ": " + cancelMessage);
            }

        } else if (cancellationCause instanceof RemoteCancellationException) {
            RemoteCancellationException e = (RemoteCancellationException) cancellationCause;
            errorMsg = e.getMessage();
            if (errorMsg != null) {
                cancelMessage = "Invitation was cancelled by the remote user "
                    + " because of an error on his/her side: " + errorMsg;
                log.error("Inv" + Utils.prefix(peer) + ": " + cancelMessage);
            } else {
                cancelMessage = "Invitation was cancelled by the remote user.";
                log.debug("Inv" + Utils.prefix(peer) + ": " + cancelMessage);
            }
        } else {
            log.error("This type of exception is not expected here: ",
                cancellationCause);
        }

        sessionManager.stopSarosSession();
        /*
         * If the sarosSession is null, stopSarosSession() does not clear the
         * sessionID, so we have to do this manually.
         */
        sessionManager.clearSessionID();
        invitationProcesses.removeInvitationProcess(this);
        throw cancellationCause;
    }

    public void setInvitationUI(JoinSessionWizard inInvitationUI) {
        this.inInvitationUI = inInvitationUI;
    }

    public void accept(SubMonitor monitor) throws SarosCancellationException {

        log.debug("Inv" + Utils.prefix(peer) + ": Invitation accepted.");
        // The second monitor we use during the invitation.
        this.monitor = monitor;

        try {
            checkCancellation();
            sarosSession = sessionManager.joinSession(peer, colorID,
                sessionStart);
            log.debug("Inv" + Utils.prefix(peer) + ": Joined the session.");
            checkCancellation();
            completeInvitation();

        } catch (Exception e) {
            processException(e);
        } finally {
            monitor.done();
        }
    }

    /**
     * Ends the incoming invitation process. Sends a confirmation to the host
     * and starts the shared project.
     */
    protected void completeInvitation() {
        log.debug("Inv" + Utils.prefix(peer) + ": Completing invitation...");
        UserListRequestExtensionProvider extProv = new UserListRequestExtensionProvider();
        transmitter.sendMessageToUser(peer,
            extProv.create(new DefaultInvitationInfo(sessionID, invitationID)));

        /*
         * TODO: Wait until all of the activities in the queue (which arrived
         * during the invitation) are processed and notify the host only after
         * that.
         */

        sessionManager.notifySarosSessionStarting(sarosSession);
        sarosSession.start();
        sessionManager.notifySarosSessionStarted(sarosSession);

        sessionManager.notifyPreIncomingInvitationCompleted(monitor);

        sarosSession.userInvitationCompleted(sarosSession.getLocalUser());
        log.debug("Inv" + Utils.prefix(peer)
            + ": isInvitationComplete has been set to true.");

        transmitter.sendInvitationCompleteConfirmation(peer, invitationID);
        log.debug("Inv" + Utils.prefix(peer)
            + ": Invitation complete confirmation sent.");

        invitationProcesses.removeInvitationProcess(this);
        monitor.done();
        log.debug("Inv" + Utils.prefix(peer)
            + ": Invitation has completed successfully.");
        if (sarosSession.getLocalUser().getPermission()
            .equals(Permission.WRITE_ACCESS)) {
            log.debug("I am a driver");
        } else if (sarosSession.getLocalUser().getPermission()
            .equals(Permission.READONLY_ACCESS)) {
            log.debug("I am an observer");
        } else {
            log.debug("I am not anything at all");
        }
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
    protected void checkCancellation() throws SarosCancellationException {
        if (cancelled.get()) {
            log.debug("Inv" + Utils.prefix(peer) + ": Cancellation checkpoint");
            throw new SarosCancellationException();
        }

        if (monitor == null)
            return;

        if (monitor.isCanceled()) {
            log.debug("Inv" + Utils.prefix(peer) + ": Cancellation checkpoint");
            localCancel(null, CancelOption.NOTIFY_PEER);
            throw new SarosCancellationException();
        }

        return;
    }

    protected void processException(Exception e)
        throws SarosCancellationException {
        if (e instanceof LocalCancellationException) {
            localCancel(e.getMessage(), CancelOption.NOTIFY_PEER);
        } else if (e instanceof RemoteCancellationException) {
            remoteCancel(e.getMessage());
        } else if (e instanceof SarosCancellationException) {
            /**
             * If this exception is thrown because of a local cancellation, we
             * initiate a localCancel here.
             * 
             * If this exception is thrown because of a remote cancellation, the
             * call of localCancel will be ignored.
             */
            localCancel(e.getMessage(), CancelOption.NOTIFY_PEER);
        } else if (e instanceof IOException) {
            String errorMsg = "Unknown error: " + e;
            if (e.getMessage() != null)
                errorMsg = e.getMessage();
            localCancel(errorMsg, CancelOption.NOTIFY_PEER);
        } else {
            log.warn("Inv" + Utils.prefix(peer)
                + ": This type of Exception is not expected: ", e);
            String errorMsg = "Unknown error: " + e;
            if (e.getMessage() != null)
                errorMsg = e.getMessage();
            localCancel(errorMsg, CancelOption.NOTIFY_PEER);
        }
        executeCancellation();
    }

    @Override
    public void remoteCancel(String errorMsg) {
        if (!cancelled.compareAndSet(false, true))
            return;
        log.debug("Inv"
            + Utils.prefix(peer)
            + ": remoteCancel "
            + (errorMsg == null ? " by user" : " because of error: " + errorMsg));
        if (monitor != null)
            monitor.setCanceled(true);
        cancellationCause = new RemoteCancellationException(errorMsg);
        if (monitor == null) {
            log.debug("Inv" + Utils.prefix(peer)
                + ": Closing JoinSessionWizard manually.");
            try {
                executeCancellation();
            } catch (SarosCancellationException e) {
                /**
                 * This happens if the JoinSessionWizard is currently waiting
                 * for user input.
                 */
                if (inInvitationUI != null)
                    inInvitationUI.cancelWizard(peer, e.getMessage(),
                        CancelLocation.REMOTE);
                else
                    log.error("The inInvitationUI is null, could not"
                        + " close the JoinSessionWizard.");
            }
        }
    }
}
