package saros.negotiation.stream;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.log4j.Logger;
import saros.exceptions.LocalCancellationException;
import saros.filesystem.FileSystem;
import saros.filesystem.IFile;
import saros.monitoring.IProgressMonitor;
import saros.negotiation.NegotiationTools.CancelOption;
import saros.session.ISarosSession;

/** Implements Stream processing in {@link AbstractStreamProtocol} format. */
public class IncomingStreamProtocol extends AbstractStreamProtocol implements AutoCloseable {

  private static final Logger log = Logger.getLogger(IncomingStreamProtocol.class);

  private DataInputStream in;
  private ISarosSession session;

  public IncomingStreamProtocol(InputStream in, ISarosSession session, IProgressMonitor monitor) {
    super(monitor);
    this.session = session;
    this.in = new DataInputStream(in);
  }

  /**
   * Receive Files from {@code InputStream in} via in {@link AbstractStreamProtocol} defined
   * protocol.
   *
   * @throws IOException if any file or stream operation fails
   * @throws LocalCancellationException on local user cancellation
   */
  public void receiveStream() throws IOException, LocalCancellationException {
    while (true) {
      String projectID = in.readUTF();

      /* check stream end */
      if (projectID.isEmpty()) break;

      String fileName = in.readUTF();
      IFile file = session.getProject(projectID).getFile(fileName);

      String message = "receiving " + displayName(file);
      log.debug(message);
      monitor.subTask(message);

      /*
       * folder creation is already done after file exchange, but in
       * case of future changes
       */
      FileSystem.createFolder(file);

      long fileSize = in.readLong();
      try (BoundedInputStream fileIn = new BoundedInputStream(in, fileSize)) {
        fileIn.setPropagateClose(false);

        if (file.exists()) file.setContents(fileIn, false, true);
        else file.create(fileIn, false);
      }

      if (monitor.isCanceled()) {
        throw new LocalCancellationException(
            "User canceled transmission", CancelOption.NOTIFY_PEER);
      }

      monitor.worked(1);
    }
  }

  @Override
  public void close() {
    IOUtils.closeQuietly(in);
  }
}
