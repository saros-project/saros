package saros.net.xmpp.filetransfer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import saros.net.xmpp.contact.XMPPContact;

/**
 * Class describing an incoming file transfer request.
 *
 * <p>Use {@link #acceptFile(File)} or {@link #acceptStream()} to start transfer or {@link
 * #reject()} to cancel.
 */
public class XMPPFileTransferRequest {
  private final XMPPContact contact;
  private final FileTransferRequest request;

  XMPPFileTransferRequest(XMPPContact contact, FileTransferRequest request) {
    this.contact = contact;
    this.request = request;
  }

  /**
   * Accept a incoming file transmission and save to provided file.
   *
   * @param file file to save transmission
   * @return {@link XMPPFileTransfer} with info about transmission status
   * @throws IOException if transmission fails
   */
  public XMPPFileTransfer acceptFile(File file) throws IOException {
    try {
      IncomingFileTransfer transfer = request.accept();
      transfer.recieveFile(file);
      return new XMPPFileTransfer(transfer);
    } catch (XMPPException e) {
      throw new IOException(e);
    }
  }

  /**
   * Accept a incoming file transmission and returns a InputStream with the received data.
   *
   * @return InputStream providing transmitted data
   * @throws IOException if transmission fails
   */
  public InputStream acceptStream() throws IOException {
    try {
      IncomingFileTransfer transfer = request.accept();
      return transfer.recieveFile();
    } catch (XMPPException e) {
      throw new IOException(e);
    }
  }

  /** Reject the incoming transfer request. */
  public void reject() {
    request.reject();
  }

  public String getFileName() {
    return request.getFileName();
  }

  public long getFileSize() {
    return request.getFileSize();
  }

  public XMPPContact getContact() {
    return contact;
  }

  @Override
  public String toString() {
    return String.format(
        "XMPPTransferRequest [contact=%s, filename=]", contact, request.getFileName());
  }
}
