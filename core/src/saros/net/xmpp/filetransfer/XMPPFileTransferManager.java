package saros.net.xmpp.filetransfer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import saros.net.ConnectionState;
import saros.net.xmpp.IConnectionListener;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;

/** This Component handles file transfers with other XMPP contacts. */
public class XMPPFileTransferManager {
  private static final int GLOBAL_SMACK_RESPONSE_TIMEOUT = (int) TimeUnit.MINUTES.toMillis(5);

  private final AtomicReference<FileTransferManager> smackTransferManager = new AtomicReference<>();

  private final IConnectionListener connectionListener =
      (connection, state) -> {
        if (state == ConnectionState.CONNECTED) {
          FileTransferManager newManager = new FileTransferManager(connection);
          smackTransferManager.set(newManager);
        }
      };

  public XMPPFileTransferManager(XMPPConnectionService connectionService) {
    connectionService.addListener(connectionListener);

    // smack only allows a global response timeout
    OutgoingFileTransfer.setResponseTimeout(GLOBAL_SMACK_RESPONSE_TIMEOUT);
  }

  /**
   * Start sending a file to a contact.
   *
   * <p>This method will return while the transfer negotiation is processing. Use {@link
   * XMPPFileTransfer#waitForTransferStart(java.util.function.BooleanSupplier)} to wait till the
   * negotiation is done.
   *
   * @param remoteJID fully qualified JID of remote
   * @param file the file itself
   * @param description description can be null, used as identifier for incoming transfer
   * @return {@link XMPPFileTransfer} providing information about the transfer
   * @throws IllegalArgumentException if provided JID is not fully qualified
   * @throws IOException if no connection is available or XMPP related error
   */
  public XMPPFileTransfer fileSendStart(JID remoteJID, File file, String description)
      throws IllegalArgumentException, IOException {
    if (remoteJID == null || remoteJID.isBareJID())
      throw new IllegalArgumentException("No valid remoteJID provided: " + remoteJID);

    FileTransferManager currentManager = smackTransferManager.get();
    if (currentManager == null) throw new IOException("No XMPP connection.");

    try {
      OutgoingFileTransfer transfer = currentManager.createOutgoingFileTransfer(remoteJID.getRAW());
      transfer.sendFile(file, description);

      return new XMPPFileTransfer(transfer);
    } catch (XMPPException e) {
      throw new IOException("File send to " + remoteJID + " failed.", e);
    }
  }
}
