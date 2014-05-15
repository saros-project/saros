package de.fu_berlin.inf.dpp.invitation;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.ISarosContext;
import de.fu_berlin.inf.dpp.communication.extensions.CancelProjectNegotiationExtension;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.monitoring.MonitorableFileTransfer;
import de.fu_berlin.inf.dpp.monitoring.MonitorableFileTransfer.TransferStatus;
import de.fu_berlin.inf.dpp.monitoring.ProgressMonitorAdapterFactory;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.XMPPConnectionService;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;

/**
 * This abstract class is the superclass for {@link OutgoingProjectNegotiation}
 * and {@link IncomingProjectNegotiation}.
 */
public abstract class ProjectNegotiation extends CancelableProcess {

    private static final Logger LOG = Logger
        .getLogger(ProjectNegotiation.class);

    /** Prefix part of the id used in the SMACK XMPP file transfer protocol. */
    public static final String ARCHIVE_TRANSFER_ID = "saros-dpp-pn-server-client-archive/";

    /**
     * While sending all the projects with a big archive containing the project
     * archives, we create a temp-File. This file is named "projectID" +
     * PROJECT_ID_DELIMITER + "a random number chosen by 'Java'" + ".zip" This
     * delimiter is the string that separates projectID and this random number.
     * Now we can assign the zip archive to the matching project.
     * 
     * WARNING: If changed compatibility is broken
     */
    protected static final String PROJECT_ID_DELIMITER = "&&&&";

    /**
     * Timeout for all packet exchanges during the project negotiation
     */
    protected static final long PACKET_TIMEOUT = Long.getLong(
        "de.fu_berlin.inf.dpp.negotiation.project.PACKET_TIMEOUT", 30000L);

    protected String processID;
    protected JID peer;

    protected final String sessionID;

    @Inject
    protected XMPPConnectionService connectionService;

    @Inject
    protected ITransmitter transmitter;

    @Inject
    protected IReceiver xmppReceiver;

    /**
     * The file transfer manager can be <code>null</code> if no connection was
     * established or was lost when the class was instantiated.
     */
    protected FileTransferManager fileTransferManager;

    @Inject
    protected ISarosSessionManager sessionManager;

    public ProjectNegotiation(JID peer, String sessionID,
        ISarosContext sarosContext) {
        this.peer = peer;
        this.sessionID = sessionID;
        sarosContext.initComponent(this);

        Connection connection = connectionService.getConnection();

        if (connection != null)
            fileTransferManager = new FileTransferManager(connection);
    }

    /**
     * 
     * @return the names of the projects that are shared by the peer. projectID
     *         => projectName
     */
    public abstract Map<String, String> getProjectNames();

    public String getProcessID() {
        return this.processID;
    }

    public JID getPeer() {
        return this.peer;
    }

    @Override
    protected void notifyCancellation(SarosCancellationException exception) {

        if (!(exception instanceof LocalCancellationException))
            return;

        LocalCancellationException cause = (LocalCancellationException) exception;

        if (cause.getCancelOption() != CancelOption.NOTIFY_PEER)
            return;

        LOG.debug("notifying remote contact " + peer
            + " of the local project negotiation cancellation");

        PacketExtension notification = CancelProjectNegotiationExtension.PROVIDER
            .create(new CancelProjectNegotiationExtension(sessionID, cause
                .getMessage()));

        try {
            transmitter.sendToSessionUser(ISarosSession.SESSION_CONNECTION_ID,
                getPeer(), notification);
        } catch (IOException e) {
            transmitter.sendMessageToUser(getPeer(), notification);
        }
    }

    /**
     * Monitors a {@link FileTransfer} and waits until it is completed or
     * aborted.
     * 
     * @param transfer
     *            the transfer to monitor
     * @param monitor
     *            the progress monitor that is <b>already initialized</b> to
     *            consume <b>100 ticks</b> to use for reporting progress to the
     *            user. It is the caller's responsibility to call done() on the
     *            given monitor. Accepts <code>null</code>, indicating that no
     *            progress should be reported and that the operation cannot be
     *            cancelled.
     * 
     * @throws SarosCancellationException
     *             if the transfer was aborted either on local side or remote
     *             side, see also {@link LocalCancellationException} and
     *             {@link RemoteCancellationException}
     * @throws IOException
     *             if an I/O error occurred
     */
    protected void monitorFileTransfer(FileTransfer transfer,
        IProgressMonitor monitor) throws SarosCancellationException,
        IOException {

        MonitorableFileTransfer mtf = new MonitorableFileTransfer(transfer,
            ProgressMonitorAdapterFactory.convertTo(monitor));
        TransferStatus transferStatus = mtf.monitorTransfer();

        // some information can be directly read from the returned status
        if (transferStatus.equals(TransferStatus.OK))
            return;

        if (transferStatus.equals(TransferStatus.ERROR)) {
            FileTransfer.Error error = transfer.getError();
            throw new IOException(
                error == null ? "unknown SMACK Filetransfer API error"
                    : error.getMessage(), transfer.getException());
        }

        // other information needs to be read from the transfer object
        if (transfer.getStatus().equals(FileTransfer.Status.cancelled)
            && monitor.isCanceled())
            throw new LocalCancellationException();

        throw new RemoteCancellationException(null);
    }

    /**
     * Returns the next packet from a collector.
     * 
     * @param collector
     *            the collector to monitor
     * @param timeout
     *            the amount of time to wait for the next packet (in
     *            milliseconds)
     * @return the collected packet or <code>null</code> if no packet was
     *         received
     * @throws SarosCancellationException
     *             if the process was canceled
     */
    protected final Packet collectPacket(SarosPacketCollector collector,
        long timeout) throws SarosCancellationException {

        Packet packet = null;

        while (timeout > 0) {
            checkCancellation(CancelOption.NOTIFY_PEER);

            packet = collector.nextResult(1000);

            if (packet != null)
                break;

            timeout -= 1000;
        }
        return packet;
    }
}
