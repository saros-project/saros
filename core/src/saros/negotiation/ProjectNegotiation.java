package saros.negotiation;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import saros.communication.extensions.CancelProjectNegotiationExtension;
import saros.exceptions.LocalCancellationException;
import saros.exceptions.RemoteCancellationException;
import saros.exceptions.SarosCancellationException;
import saros.filesystem.IChecksumCache;
import saros.filesystem.IWorkspace;
import saros.monitoring.IProgressMonitor;
import saros.monitoring.MonitorableFileTransfer;
import saros.monitoring.MonitorableFileTransfer.TransferStatus;
import saros.negotiation.NegotiationTools.CancelOption;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.net.xmpp.filetransfer.XMPPFileTransferManager;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;

/**
 * This abstract class is the superclass for {@link AbstractOutgoingProjectNegotiation} and {@link
 * AbstractIncomingProjectNegotiation}.
 */
public abstract class ProjectNegotiation extends Negotiation {

  private static final Logger log = Logger.getLogger(ProjectNegotiation.class);

  /** Prefix part of the id used in the SMACK XMPP file transfer protocol. */
  public static final String TRANSFER_ID_PREFIX = "saros-dpp-pn-server-client/";

  /**
   * Delimiter for every Zip entry to delimit the project id from the path entry.
   *
   * <p>E.g: <b>12345:foo/bar/foobar.java</b>
   */
  protected static final String PATH_DELIMITER = ":";

  /** Timeout for all packet exchanges during the project negotiation */
  protected static final long PACKET_TIMEOUT =
      Long.getLong("saros.negotiation.project.PACKET_TIMEOUT", 30000L);

  protected final ISarosSessionManager sessionManager;

  protected final ISarosSession session;

  private final String sessionID;

  protected final IWorkspace workspace;

  protected final IChecksumCache checksumCache;

  protected final XMPPFileTransferManager fileTransferManager;

  public ProjectNegotiation(
      final String id,
      final JID peer,
      final ISarosSessionManager sessionManager,
      final ISarosSession session,
      final IWorkspace workspace,
      final IChecksumCache checksumCache,
      final XMPPFileTransferManager fileTransferManager,
      final ITransmitter transmitter,
      final IReceiver receiver) {
    super(id, peer, transmitter, receiver);

    this.sessionManager = sessionManager;
    this.session = session;
    this.sessionID = session.getID();

    this.workspace = workspace;
    this.checksumCache = checksumCache;
    this.fileTransferManager = fileTransferManager;
  }

  /**
   * Returns the {@linkplain ISarosSession session} id this negotiation belongs to.
   *
   * @return the id of the current session this negotiations belongs to
   */
  public final String getSessionID() {
    return sessionID;
  }

  @Override
  protected void notifyCancellation(SarosCancellationException exception) {

    if (!(exception instanceof LocalCancellationException)) return;

    LocalCancellationException cause = (LocalCancellationException) exception;

    if (cause.getCancelOption() != CancelOption.NOTIFY_PEER) return;

    log.debug(
        "notifying remote contact " + getPeer() + " of the local project negotiation cancellation");

    PacketExtension notification =
        CancelProjectNegotiationExtension.PROVIDER.create(
            new CancelProjectNegotiationExtension(getSessionID(), getID(), cause.getMessage()));

    try {
      transmitter.send(ISarosSession.SESSION_CONNECTION_ID, getPeer(), notification);
    } catch (IOException e) {
      transmitter.sendPacketExtension(getPeer(), notification);
    }
  }

  /**
   * Monitors a {@link FileTransfer} and waits until it is completed or aborted.
   *
   * @param transfer the transfer to monitor
   * @param monitor the progress monitor that is <b>already initialized</b> to consume <b>100
   *     ticks</b> to use for reporting progress to the user. It is the caller's responsibility to
   *     call done() on the given monitor. Accepts <code>null</code>, indicating that no progress
   *     should be reported and that the operation cannot be canceled.
   * @throws SarosCancellationException if the transfer was aborted either on local side or remote
   *     side, see also {@link LocalCancellationException} and {@link RemoteCancellationException}
   * @throws IOException if an I/O error occurred
   */
  protected void monitorFileTransfer(FileTransfer transfer, IProgressMonitor monitor)
      throws SarosCancellationException, IOException {

    MonitorableFileTransfer mtf = new MonitorableFileTransfer(transfer, monitor);
    TransferStatus transferStatus = mtf.monitorTransfer();

    // some information can be directly read from the returned status
    if (transferStatus.equals(TransferStatus.OK)) return;

    if (transferStatus.equals(TransferStatus.ERROR)) {
      FileTransfer.Error error = transfer.getError();
      throw new IOException(
          error == null ? "unknown SMACK Filetransfer API error" : error.getMessage(),
          transfer.getException());
    }

    // other information needs to be read from the transfer object
    if (transfer.getStatus().equals(FileTransfer.Status.cancelled) && monitor.isCanceled())
      throw new LocalCancellationException();

    throw new RemoteCancellationException(null);
  }

  @Override
  protected void notifyTerminated(NegotiationListener listener) {
    listener.negotiationTerminated(this);
  }
}
