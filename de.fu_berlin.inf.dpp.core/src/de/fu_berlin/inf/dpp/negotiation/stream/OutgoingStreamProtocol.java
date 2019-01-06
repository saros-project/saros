package de.fu_berlin.inf.dpp.negotiation.stream;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.negotiation.NegotiationTools.CancelOption;
import de.fu_berlin.inf.dpp.session.IReferencePointManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/** Implements Stream creation in {@link AbstractStreamProtocol} format. */
public class OutgoingStreamProtocol extends AbstractStreamProtocol {

  private static final Logger log = Logger.getLogger(OutgoingStreamProtocol.class);

  private static final int BUFFER_SIZE = 8192;
  /** used for copy operations between streams * */
  private final byte[] buffer = new byte[BUFFER_SIZE];

  private DataOutputStream out;

  public OutgoingStreamProtocol(OutputStream out, ISarosSession session, IProgressMonitor monitor) {
    super(session, monitor);
    this.out = new DataOutputStream(out);
  }

  /**
   * Sends a File to {@code OutputStream out} via in {@link AbstractStreamProtocol} defined
   * protocol.
   *
   * @param file the file to send
   * @throws IOException if any file or stream operation fails
   * @throws LocalCancellationException on local user cancellation
   */
  public void streamFile(SPath file) throws IOException, LocalCancellationException {
    String message = "sending " + file.getFullPath();
    log.debug(message);
    monitor.subTask(message);

    IReferencePointManager referencePointManager =
        session.getComponent(IReferencePointManager.class);

    IReferencePoint referencePoint = file.getReferencePoint();

    IPath referencePointRelativePath = file.getReferencePointRelativePath();

    IFile fileHandle =
        referencePointManager.get(referencePoint).getFile(referencePointRelativePath);

    writeHeader(file, fileHandle.getSize());

    InputStream fileIn = null;
    try {
      fileIn = fileHandle.getContents();
      int readBytes = 0;
      /* buffer the file content and send to stream */
      while (readBytes != -1) {
        out.write(buffer, 0, readBytes);
        readBytes = fileIn.read(buffer);

        if (monitor.isCanceled())
          throw new LocalCancellationException(
              "transmission was canceled", CancelOption.NOTIFY_PEER);
      }
    } catch (IOException e) {
      IOUtils.closeQuietly(out);
      throw e;
    } catch (LocalCancellationException e) {
      IOUtils.closeQuietly(out);
      throw e;
    } finally {
      IOUtils.closeQuietly(fileIn);
    }
    monitor.worked(1);
  }

  private void writeHeader(SPath path, long fileSize) throws IOException {
    String projectID = session.getReferencePointID(path.getReferencePoint());
    String fileName = path.getReferencePointRelativePath().toPortableString();

    out.writeUTF(projectID);
    out.writeUTF(fileName);
    out.writeLong(fileSize);
  }

  /**
   * Writes a end remark to notify stream endpoint about correct end of transmission and closes the
   * stream correctly.
   *
   * @throws IOException if stream operation fails
   */
  public void close() throws IOException {
    try {
      out.writeUTF("");
    } finally {
      IOUtils.closeQuietly(out);
    }
  }
}
