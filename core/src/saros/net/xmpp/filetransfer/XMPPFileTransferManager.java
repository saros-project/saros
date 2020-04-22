package saros.net.xmpp.filetransfer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import saros.negotiation.ProjectNegotiation;
import saros.net.ConnectionState;
import saros.net.xmpp.IConnectionListener;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;

/** This Component handles file transfers with other XMPP contacts. */
public class XMPPFileTransferManager {
  private static final int GLOBAL_SMACK_RESPONSE_TIMEOUT = (int) TimeUnit.MINUTES.toMillis(5);

  private static final Logger log = Logger.getLogger(FileTransferManager.class);

  private final AtomicReference<Consumer<XMPPFileTransferRequest>> defaultTransferHandler =
      new AtomicReference<>();

  private final AtomicReference<FileTransferManager> smackTransferManager = new AtomicReference<>();

  private final FileTransferListener smackTransferListener =
      new FileTransferListener() {
        @Override
        public void fileTransferRequest(FileTransferRequest request) {
          XMPPContact contact = contactsService.getContact(request.getRequestor()).orElse(null);
          if (contact == null) {
            log.info("received transfer request but contact unknown " + request.getRequestor());
            return;
          }

          String identifier = request.getDescription();
          log.info("received transfer request: " + identifier + " by " + contact);

          // TODO use different handling for negotiation
          if (identifier != null && identifier.startsWith(ProjectNegotiation.TRANSFER_ID_PREFIX))
            return;

          Consumer<XMPPFileTransferRequest> defaultListener = defaultTransferHandler.get();
          if (defaultListener == null) {
            request.reject();
          } else {
            defaultListener.accept(new XMPPFileTransferRequest(contact, request));
          }
        }
      };

  private final IConnectionListener connectionListener =
      (connection, state) -> {
        FileTransferManager currentManager = smackTransferManager.getAndSet(null);
        if (currentManager != null) {
          currentManager.removeFileTransferListener(smackTransferListener);
        }

        if (state == ConnectionState.CONNECTED) {
          FileTransferManager newManager = new FileTransferManager(connection);
          smackTransferManager.set(newManager);
          newManager.addFileTransferListener(smackTransferListener);
        }
      };

  private final XMPPContactsService contactsService;

  public XMPPFileTransferManager(
      XMPPConnectionService connectionService, XMPPContactsService contactsService) {
    connectionService.addListener(connectionListener);
    this.contactsService = contactsService;

    // smack only allows a global response timeout
    OutgoingFileTransfer.setResponseTimeout(GLOBAL_SMACK_RESPONSE_TIMEOUT);
  }

  /**
   * Set a default handler, which should be called if no expected transfer matches / available.
   *
   * @param defaultHandler Consumer processing a new {@link XMPPFileTransferRequest}
   */
  public void setDefaultHandler(Consumer<XMPPFileTransferRequest> defaultHandler) {
    defaultTransferHandler.set(defaultHandler);
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
