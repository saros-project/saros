package de.fu_berlin.inf.dpp.negotiation;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.communication.extensions.StartActivityQueuingResponse;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.NegotiationTools.CancelOption;
import de.fu_berlin.inf.dpp.negotiation.stream.IncomingStreamProtocol;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.internal.ResourceActivityQueuer;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;

/** Receive shared Projects and display them instant using a stream based solution. */
public class InstantIncomingProjectNegotiation extends AbstractIncomingProjectNegotiation {

  private static final Logger log = Logger.getLogger(InstantIncomingProjectNegotiation.class);

  /** the one queuer used for all incoming project negotiations */
  private static final ResourceActivityQueuer activityQueuer = new ResourceActivityQueuer();

  public InstantIncomingProjectNegotiation(
      final JID peer, //
      final String negotiationID, //
      final List<ProjectNegotiationData> projectNegotiationData, //
      final ISarosSessionManager sessionManager, //
      final ISarosSession session, //
      final FileReplacementInProgressObservable fileReplacementInProgressObservable, //
      final IWorkspace workspace, //
      final IChecksumCache checksumCache, //
      final XMPPConnectionService connectionService, //
      final ITransmitter transmitter, //
      final IReceiver receiver //
      ) {
    super(
        peer,
        TransferType.INSTANT,
        negotiationID,
        projectNegotiationData,
        sessionManager,
        session,
        fileReplacementInProgressObservable,
        workspace,
        checksumCache,
        connectionService,
        transmitter,
        receiver);

    // Always registers the same, basically a NOP after the first call
    session.registerActivityQueuer(activityQueuer);
  }

  @Override
  protected void transfer(
      IProgressMonitor monitor, Map<String, IProject> projectMapping, List<FileList> missingFiles)
      throws IOException, SarosCancellationException {
    /*
     * the user who sends this ProjectNegotiation is now responsible for the
     * resources of the contained projects
     */
    for (Entry<String, IProject> entry : projectMapping.entrySet()) {
      final String projectID = entry.getKey();
      final IProject project = entry.getValue();

      session.addProjectMapping(projectID, project);
    }

    int missingFilesCount = missingFiles.stream().mapToInt(f -> f.getPaths().size()).sum();

    /* generate file lists */
    Set<SPath> files = new HashSet<>(missingFilesCount * 2);
    for (FileList list : missingFiles) {
      IProject project = session.getProject(list.getProjectID());
      for (String file : list.getPaths()) {
        files.add(new SPath(project.getFile(file)));
      }
    }

    /* enable queuing for missing files */
    awaitActivityQueueingActivation(monitor);
    activityQueuer.enableQueuing(files);

    /* notify host about queuing */
    transmitter.send(
        ISarosSession.SESSION_CONNECTION_ID,
        getPeer(),
        StartActivityQueuingResponse.PROVIDER.create(
            new StartActivityQueuingResponse(getSessionID(), getID())));

    checkCancellation(CancelOption.NOTIFY_PEER);

    if (missingFilesCount > 0) {
      receiveStream(monitor, missingFilesCount);
    }
  }

  private void receiveStream(IProgressMonitor monitor, int fileCount)
      throws SarosCancellationException, IOException {
    monitor.beginTask("Receiving files from " + getPeer().getName() + "...", fileCount);
    monitor.subTask("Waiting for Host to start...");

    awaitTransferRequest();

    monitor.subTask("Host is starting to send...");
    log.debug(this + ": Host is starting to send...");

    IncomingFileTransfer transfer = transferListener.getRequest().accept();
    InputStream in = null;
    try {
      in = transfer.recieveFile();

      IncomingStreamProtocol isp;
      isp = new IncomingStreamProtocol(in, session, monitor);
      isp.receiveStream(activityQueuer);
    } catch (XMPPException e) {
      throw new LocalCancellationException(e.getMessage(), CancelOption.NOTIFY_PEER);
    } finally {
      IOUtils.closeQuietly(in);
    }

    log.debug(this + ": stream transmission done");
    monitor.done();
  }
}
