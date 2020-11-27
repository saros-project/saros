package saros.negotiation;

import java.io.IOException;
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
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.log4j.Logger;
import saros.editor.IEditorManager;
import saros.editor.ISharedEditorListener;
import saros.editor.remote.UserEditorStateManager;
import saros.exceptions.LocalCancellationException;
import saros.exceptions.SarosCancellationException;
import saros.filesystem.IFile;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IWorkspace;
import saros.filesystem.checksum.IChecksumCache;
import saros.monitoring.IProgressMonitor;
import saros.negotiation.NegotiationTools.CancelOption;
import saros.negotiation.stream.OutgoingStreamProtocol;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;
import saros.net.xmpp.filetransfer.XMPPFileTransfer;
import saros.net.xmpp.filetransfer.XMPPFileTransferManager;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.User;
import saros.synchronize.StartHandle;

/** Share resources to display them instant on client side using a stream based solution. */
public class InstantOutgoingResourceNegotiation extends AbstractOutgoingResourceNegotiation {

  private static final Logger log = Logger.getLogger(InstantOutgoingResourceNegotiation.class);

  /** used as LIFO queue * */
  private final Deque<IFile> openedFiles = new LinkedBlockingDeque<>();

  private Set<IFile> transferList;
  private Set<IFile> transmittedFiles;

  /** receive open editors to prioritize these files * */
  private final ISharedEditorListener listener =
      new ISharedEditorListener() {
        @Override
        public void editorActivated(User user, IFile file) {
          fileOpened(file);
        }
      };

  private List<StartHandle> stoppedUsers = null;
  private User remoteUser = null;

  public InstantOutgoingResourceNegotiation(
      final JID peer, //
      final ResourceSharingData resourceSharingData, //
      final ISarosSessionManager sessionManager, //
      final ISarosSession session, //
      final IEditorManager editorManager, //
      final IWorkspace workspace, //
      final IChecksumCache checksumCache, //
      final XMPPFileTransferManager fileTransferManager, //
      final ITransmitter transmitter, //
      final IReceiver receiver, //
      final AdditionalResourceDataFactory additionalResourceDataFactory //
      ) {
    super(
        peer,
        resourceSharingData,
        sessionManager,
        session,
        editorManager,
        workspace,
        checksumCache,
        fileTransferManager,
        transmitter,
        receiver,
        additionalResourceDataFactory);
  }

  @Override
  protected void setup(IProgressMonitor monitor) throws IOException, LocalCancellationException {
    if (fileTransferManager == null)
      throw new LocalCancellationException(
          "not connected to a XMPP server", CancelOption.DO_NOT_NOTIFY_PEER);

    /* get all opened editors */
    editorManager.addSharedEditorListener(listener);
    Set<IFile> openEditors = session.getComponent(UserEditorStateManager.class).getOpenEditors();
    for (IFile remoteOpenFile : openEditors) fileOpened(remoteOpenFile);
    for (IFile localOpenFile : editorManager.getOpenEditors()) fileOpened(localOpenFile);
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

      final String referencePointID = list.getReferencePointID();
      final IReferencePoint referencePoint =
          resourceSharingData.getReferencePoint(referencePointID);

      if (referencePoint == null)
        throw new LocalCancellationException(
            "reference point with id " + referencePointID + " was unshared during synchronization",
            CancelOption.NOTIFY_PEER);
    }

    createTransferList(fileLists, fileCount);
    transmittedFiles = new HashSet<>(fileCount * 2);
  }

  @Override
  protected void transfer(IProgressMonitor monitor, List<FileList> fileLists)
      throws SarosCancellationException, IOException {
    if (transferList.isEmpty()) return;

    log.debug(this + ": file transfer start");
    assert fileTransferManager != null;

    String message = "Sending files to " + getPeer().getName() + "...";
    monitor.beginTask(message, transferList.size());

    long writtenBytes = 0;
    try (PipedInputStream in = new PipedInputStream();
        CountingOutputStream out = new CountingOutputStream(new PipedOutputStream(in)); ) {

      monitor.subTask("waiting for client to accept file transfer");
      XMPPFileTransfer transfer =
          fileTransferManager.streamSendStart(getPeer(), TRANSFER_ID_PREFIX + getID(), in);
      transfer.waitForTransferStart(monitor::isCanceled);

      checkCancellation(CancelOption.NOTIFY_PEER);

      OutgoingStreamProtocol osp = new OutgoingStreamProtocol(out, resourceSharingData, monitor);
      sendRemainingPreferOpenedFirst(osp);
      osp.close();

      /* await sending is done before closing stream */
      try {
        while (!transfer.isDone() && transfer.getAmountWritten() != out.getByteCount()) {
          log.debug(
              "stream bytes written/planned "
                  + transfer.getAmountWritten()
                  + "/"
                  + out.getByteCount());
          Thread.sleep(100);
        }
      } catch (InterruptedException e) {
        log.error(this + ": file transfer interrupted at closing", e);
        Thread.currentThread().interrupt();
      }
      writtenBytes = out.getByteCount();
    }

    monitor.done();
    log.debug(this + ": file transfer done, " + writtenBytes + " bytes sent");
  }

  @Override
  protected void cleanup(IProgressMonitor monitor) {
    editorManager.removeSharedEditorListener(listener);
    session.userStartedQueuing(remoteUser);

    if (stoppedUsers != null) startUsers(stoppedUsers);

    super.cleanup(monitor);
  }

  private void createTransferList(List<FileList> fileLists, int fileCount) {
    List<IFile> files = new ArrayList<>(fileCount);
    for (final FileList list : fileLists) {
      IReferencePoint referencePoint =
          resourceSharingData.getReferencePoint(list.getReferencePointID());
      for (String file : list.getPaths()) {
        files.add(referencePoint.getFile(file));
      }
    }

    /* sort hierarchy based, top files are seen first in project explorer */
    Collections.sort(
        files,
        new Comparator<IFile>() {
          @Override
          public int compare(IFile a, IFile b) {
            int lenA = a.getReferencePointRelativePath().getNameCount();
            int lenB = b.getReferencePointRelativePath().getNameCount();
            return Integer.valueOf(lenA).compareTo(Integer.valueOf(lenB));
          }
        });

    /* LinkedHashSet for fast lookup while keeping sort order */
    transferList = new LinkedHashSet<>(files);
  }

  private void fileOpened(IFile file) {
    if (file != null) {
      openedFiles.addFirst(file);
      log.debug(this + ": added " + file + " to open files queue");
    }
  }

  private void sendRemainingPreferOpenedFirst(OutgoingStreamProtocol osp)
      throws IOException, LocalCancellationException {
    for (IFile file : transferList) {
      while (!openedFiles.isEmpty()) {
        IFile openFile = openedFiles.poll();
        /* open files could be changed meanwhile */
        editorManager.saveEditors(openFile.getReferencePoint());
        sendIfRequired(osp, openFile);
      }

      sendIfRequired(osp, file);
    }
  }

  private void sendIfRequired(OutgoingStreamProtocol osp, IFile file)
      throws IOException, LocalCancellationException {
    if (transferList.contains(file) && !transmittedFiles.contains(file)) {
      osp.streamFile(file);
      transmittedFiles.add(file);
    }
  }
}
