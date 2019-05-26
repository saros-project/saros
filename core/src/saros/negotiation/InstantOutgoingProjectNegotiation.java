package saros.negotiation;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import saros.activities.SPath;
import saros.editor.IEditorManager;
import saros.editor.ISharedEditorListener;
import saros.editor.remote.UserEditorStateManager;
import saros.exceptions.LocalCancellationException;
import saros.exceptions.SarosCancellationException;
import saros.filesystem.IChecksumCache;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IWorkspace;
import saros.monitoring.IProgressMonitor;
import saros.negotiation.NegotiationTools.CancelOption;
import saros.negotiation.stream.OutgoingStreamProtocol;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.User;
import saros.synchronize.StartHandle;

/** Share Projects to display them instant on client side using a stream based solution. */
public class InstantOutgoingProjectNegotiation extends AbstractOutgoingProjectNegotiation {

  private static final Logger log = Logger.getLogger(InstantOutgoingProjectNegotiation.class);

  /** used as LIFO queue * */
  private final Deque<SPath> openedFiles = new LinkedBlockingDeque<SPath>();

  private Set<SPath> transferList;
  private Set<SPath> transmittedFiles;

  /** receive open editors to prioritize these files * */
  private final ISharedEditorListener listener =
      new ISharedEditorListener() {
        @Override
        public void editorActivated(User user, SPath file) {
          fileOpened(file);
        }
      };

  private List<StartHandle> stoppedUsers = null;
  private User remoteUser = null;

  public InstantOutgoingProjectNegotiation(
      final JID peer, //
      final ProjectSharingData referencePoints, //
      final ISarosSessionManager sessionManager, //
      final ISarosSession session, //
      final IEditorManager editorManager, //
      final IWorkspace workspace, //
      final IChecksumCache checksumCache, //
      final XMPPConnectionService connectionService, //
      final ITransmitter transmitter, //
      final IReceiver receiver) //
      {
    super(
        peer,
        referencePoints,
        sessionManager,
        session,
        editorManager,
        workspace,
        checksumCache,
        connectionService,
        transmitter,
        receiver);
  }

  @Override
  protected void setup(IProgressMonitor monitor) throws IOException, LocalCancellationException {
    if (fileTransferManager == null)
      throw new LocalCancellationException(
          "not connected to a XMPP server", CancelOption.DO_NOT_NOTIFY_PEER);

    /* get all opened editors */
    editorManager.addSharedEditorListener(listener);
    Set<SPath> openEditors = session.getComponent(UserEditorStateManager.class).getOpenEditors();
    for (SPath remoteOpenFile : openEditors) fileOpened(remoteOpenFile);
    for (SPath localOpenFile : editorManager.getOpenEditors()) fileOpened(localOpenFile);
  }

  @Override
  protected void prepareTransfer(IProgressMonitor monitor, List<FileList> fileLists)
      throws IOException, SarosCancellationException {

    /* until further patch, lock the complete session */
    stoppedUsers = stopUsers(monitor);
    sendAndAwaitActivityQueueingActivation(monitor);

    remoteUser = session.getUser(getPeer());
    if (remoteUser == null)
      throw new LocalCancellationException(null, CancelOption.DO_NOT_NOTIFY_PEER);

    int fileCount = 0;
    for (final FileList list : fileLists) {
      fileCount += list.getPaths().size();

      final String referencePointID = list.getProjectID();
      final IReferencePoint referencePoint = referencePoints.getReferencePoint(referencePointID);

      if (referencePoint == null)
        throw new LocalCancellationException(
            "reference point with id " + referencePointID + " was unshared during synchronization",
            CancelOption.NOTIFY_PEER);
    }

    createTransferList(fileLists, fileCount);
    transmittedFiles = new HashSet<SPath>(fileCount * 2);
  }

  @Override
  protected void transfer(IProgressMonitor monitor, List<FileList> fileLists)
      throws SarosCancellationException, IOException {
    if (transferList.isEmpty()) return;

    log.debug(this + ": file transfer start");
    assert fileTransferManager != null;

    String message = "Sending files to " + getPeer().getName() + "...";
    monitor.beginTask(message, transferList.size());

    /* use piped stream to communicate with transfer thread */
    PipedInputStream in = new PipedInputStream();
    OutputStream out = new PipedOutputStream(in);

    String userID = getPeer().toString();
    OutgoingFileTransfer transfer;
    transfer = fileTransferManager.createOutgoingFileTransfer(userID);

    try {
      OutgoingStreamProtocol osp;
      osp = new OutgoingStreamProtocol(out, referencePoints, monitor);

      /* id in description needed to bypass SendFileAction handler */
      String streamName = TRANSFER_ID_PREFIX + getID();
      transfer.sendStream(in, streamName, 0, streamName);

      awaitNegotation(transfer, monitor);

      sendProjectConfigFiles(osp);
      sendRemainingPreferOpenedFirst(osp);

      osp.close();
    } finally {
      IOUtils.closeQuietly(out);
    }

    monitor.done();
    log.debug(this + ": file transfer done");
  }

  @Override
  protected void cleanup(IProgressMonitor monitor) {
    editorManager.removeSharedEditorListener(listener);
    session.userStartedQueuing(remoteUser);

    if (stoppedUsers != null) startUsers(stoppedUsers);

    super.cleanup(monitor);
  }

  private void createTransferList(List<FileList> fileLists, int fileCount) {
    List<SPath> files = new ArrayList<SPath>(fileCount);
    for (final FileList list : fileLists) {
      IReferencePoint referencePoint = referencePoints.getReferencePoint(list.getProjectID());
      for (String file : list.getPaths()) {
        files.add(new SPath(referencePointManager.getFile(referencePoint, file)));
      }
    }

    /* sort hierarchy based, top files are seen first in project explorer */
    Collections.sort(
        files,
        new Comparator<SPath>() {
          @Override
          public int compare(SPath a, SPath b) {
            int lenA = a.getProjectRelativePath().segmentCount();
            int lenB = b.getProjectRelativePath().segmentCount();
            return Integer.valueOf(lenA).compareTo(Integer.valueOf(lenB));
          }
        });

    /* LinkedHashSet for fast lookup while keeping sort order */
    transferList = new LinkedHashSet<SPath>(files);
  }

  private void fileOpened(SPath file) {
    if (file != null) {
      openedFiles.addFirst(file);
      log.debug(this + ": added " + file + " to open files queue");
    }
  }

  private void sendProjectConfigFiles(OutgoingStreamProtocol osp)
      throws IOException, LocalCancellationException {
    /* TODO should be configurable in future */
    String[] eclipseProjFiles = {
      ".settings/org.eclipse.core.resources.prefs" /* for file encoding! */,
      ".classpath",
      ".project",
      ".settings/org.eclipse.core.runtime.prefs",
      ".settings/org.eclipse.jdt.core.prefs",
      ".settings/org.eclipse.jdt.ui.prefs"
    };

    for (String string : eclipseProjFiles) {
      for (IReferencePoint referencePoint : referencePoints) {
        SPath file = new SPath(referencePointManager.getFile(referencePoint, string));
        sendIfRequired(osp, file);
      }
    }
  }

  private void sendRemainingPreferOpenedFirst(OutgoingStreamProtocol osp)
      throws IOException, LocalCancellationException {
    for (SPath file : transferList) {
      while (!openedFiles.isEmpty()) {
        SPath openFile = openedFiles.poll();
        /* open files could be changed meanwhile */
        editorManager.saveEditors(openFile.getProject().getReferencePoint());
        sendIfRequired(osp, openFile);
      }

      sendIfRequired(osp, file);
    }
  }

  private void sendIfRequired(OutgoingStreamProtocol osp, SPath file)
      throws IOException, LocalCancellationException {
    if (transferList.contains(file) && !transmittedFiles.contains(file)) {
      osp.streamFile(file);
      transmittedFiles.add(file);
    }
  }

  private void awaitNegotation(OutgoingFileTransfer transfer, IProgressMonitor monitor)
      throws SarosCancellationException {
    while (transfer.getStatus() != FileTransfer.Status.in_progress) {
      monitor.subTask("waiting for client to accept file transfer");
      try {
        checkCancellation(CancelOption.NOTIFY_PEER);
        Thread.sleep(200);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new LocalCancellationException();
      }
    }
  }
}
