package saros.negotiation.stream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.log4j.Logger;
import saros.exceptions.LocalCancellationException;
import saros.filesystem.IFile;
import saros.monitoring.IProgressMonitor;
import saros.negotiation.NegotiationTools.CancelOption;
import saros.negotiation.ResourceSharingData;
import saros.util.PathUtils;

/** Implements Stream creation in {@link AbstractStreamProtocol} format. */
public class OutgoingStreamProtocol extends AbstractStreamProtocol {

  private static final Logger log = Logger.getLogger(OutgoingStreamProtocol.class);

  private static final int BUFFER_SIZE = 8192;
  /** used for copy operations between streams * */
  private final byte[] buffer = new byte[BUFFER_SIZE];

  private ResourceSharingData resourceSharingData;
  private DataOutputStream out;

  public OutgoingStreamProtocol(
      OutputStream out, ResourceSharingData resourceSharingData, IProgressMonitor monitor) {
    super(monitor);
    this.resourceSharingData = resourceSharingData;
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
  public void streamFile(IFile file) throws IOException, LocalCancellationException {
    String message = "sending " + displayName(file);
    log.debug(message);
    monitor.subTask(message);

    writeHeader(file, file.getSize());

    try (InputStream fileIn = file.getContents()) {
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

  private void writeHeader(IFile file, long fileSize) throws IOException {
    String referencePointID = resourceSharingData.getReferencePointID(file.getReferencePoint());
    String filePath = PathUtils.toPortableString(file.getReferencePointRelativePath());

    out.writeUTF(referencePointID);
    out.writeUTF(filePath);
    out.writeLong(fileSize);
  }

  /**
   * Writes a end remark to notify stream endpoint about correct end of transmission and flushes the
   * stream.
   *
   * @throws IOException if stream operation fails
   */
  public void close() throws IOException {
    out.writeUTF("");
    out.flush();
  }
}
