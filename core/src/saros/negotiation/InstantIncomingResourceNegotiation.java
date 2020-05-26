package saros.negotiation;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.log4j.Logger;
import saros.exceptions.LocalCancellationException;
import saros.exceptions.SarosCancellationException;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IWorkspace;
import saros.filesystem.checksum.IChecksumCache;
import saros.monitoring.IProgressMonitor;
import saros.negotiation.NegotiationTools.CancelOption;
import saros.negotiation.stream.IncomingStreamProtocol;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.net.xmpp.filetransfer.XMPPFileTransferManager;
import saros.observables.FileReplacementInProgressObservable;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;

/** Receive shared resources and display them instant using a stream based solution. */
public class InstantIncomingResourceNegotiation extends AbstractIncomingResourceNegotiation {

  private static final Logger log = Logger.getLogger(InstantIncomingResourceNegotiation.class);

  public InstantIncomingResourceNegotiation(
      final JID peer, //
      final String negotiationID, //
      final List<ProjectNegotiationData> resourceNegotiationData, //
      final ISarosSessionManager sessionManager, //
      final ISarosSession session, //
      final FileReplacementInProgressObservable fileReplacementInProgressObservable, //
      final IWorkspace workspace, //
      final IChecksumCache checksumCache, //
      final XMPPFileTransferManager fileTransferManager, //
      final ITransmitter transmitter, //
      final IReceiver receiver //
      ) {
    super(
        peer,
        negotiationID,
        resourceNegotiationData,
        sessionManager,
        session,
        fileReplacementInProgressObservable,
        workspace,
        checksumCache,
        fileTransferManager,
        transmitter,
        receiver);
  }

  @Override
  protected void transfer(
      IProgressMonitor monitor,
      Map<String, IReferencePoint> referencePointMapping,
      List<FileList> missingFiles)
      throws IOException, SarosCancellationException {

    /* only get files, if something is missing */
    int filesMissing = 0;
    for (FileList list : missingFiles) filesMissing += list.getPaths().size();

    if (filesMissing > 0) receiveStream(monitor, filesMissing);
  }

  private void receiveStream(IProgressMonitor monitor, int fileCount)
      throws SarosCancellationException, IOException {
    String message = "Receiving files from " + getPeer().getName() + "...";
    monitor.beginTask(message, fileCount);

    monitor.subTask("Waiting for Host to start...");
    monitor.waitForCompletion(expectedTransfer);
    monitor.subTask("Host is starting to send...");
    log.debug(this + ": Host is starting to send...");

    try (InputStream transmissionStream = expectedTransfer.get().acceptStream();
        CountingInputStream countStream = new CountingInputStream(transmissionStream);
        IncomingStreamProtocol isp = new IncomingStreamProtocol(countStream, session, monitor)) {
      isp.receiveStream();
      log.debug("stream bytes received: " + countStream.getByteCount());
    } catch (InterruptedException | ExecutionException e) {
      throw new LocalCancellationException(e.getMessage(), CancelOption.NOTIFY_PEER);
    }

    log.debug(this + ": stream transmission done");
    monitor.done();
  }
}
