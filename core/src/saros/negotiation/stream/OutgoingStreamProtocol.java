package saros.negotiation.stream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import saros.activities.SPath;
import saros.exceptions.LocalCancellationException;
import saros.filesystem.IFile;
import saros.monitoring.IProgressMonitor;
import saros.negotiation.NegotiationTools.CancelOption;
import saros.negotiation.ProjectSharingData;

/** Implements Stream creation in {@link AbstractStreamProtocol} format. */
public class OutgoingStreamProtocol extends AbstractStreamProtocol implements AutoCloseable {

  private static final Logger log = Logger.getLogger(OutgoingStreamProtocol.class);

  private static final int BUFFER_SIZE = 8192;
  /** used for copy operations between streams * */
  private final byte[] buffer = new byte[BUFFER_SIZE];

  private ProjectSharingData projectSharingData;
  private DataOutputStream out;

  public OutgoingStreamProtocol(
      OutputStream out, ProjectSharingData projectSharingData, IProgressMonitor monitor) {
    super(monitor);
    this.projectSharingData = projectSharingData;
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
    String message = "sending " + displayName(file.getFile());
    log.debug(message);
    monitor.subTask(message);

    IFile fileHandle = file.getFile();

    writeHeader(file, fileHandle.getSize());

    try (InputStream fileIn = fileHandle.getContents()) {
      int readBytes = 0;
      /* buffer the file content and send to stream */
      while (readBytes != -1) {
        out.write(buffer, 0, readBytes);
        readBytes = fileIn.read(buffer);

        if (monitor.isCanceled())
          throw new LocalCancellationException(
              "transmission was canceled", CancelOption.NOTIFY_PEER);
      }
    }

    monitor.worked(1);
  }

  private void writeHeader(SPath file, long fileSize) throws IOException {
    String projectID = projectSharingData.getProjectID(file.getProject());
    String fileName = file.getProjectRelativePath().toPortableString();

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
  @Override
  public void close() throws IOException {
    try {
      out.writeUTF("");
    } finally {
      IOUtils.closeQuietly(out);
    }
  }
}
