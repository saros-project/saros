package saros.net.xmpp.filetransfer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import saros.net.ConnectionState;
import saros.net.xmpp.IConnectionListener;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.net.xmpp.contact.XMPPContact;
import saros.net.xmpp.contact.XMPPContactsService;

/**
 * This Component handles file transfers with other XMPP contacts.
 *
 * <p><b>Incoming</b> file transfers:
 *
 * <ul>
 *   <li>can be noted in advance with an matching identifier via {@link
 *       #addExpectedTransferRequest(String)} returning a future object, completed on actual
 *       transfer arrival
 *   <li>otherwise when no match is found handled by a default handler which has to be set via
 *       {@link #setDefaultHandler(Consumer)}
 *   <li>or otherwise get rejected
 * </ul>
 *
 * <p><b>Outgoing</b> file transfers are possible for Files using {@link #fileSendStart(JID, File,
 * String)} or InputStreams using {@link #streamSendStart(JID, String, InputStream)}.
 */
public class XMPPFileTransferManager {
  private static final int GLOBAL_SMACK_RESPONSE_TIMEOUT = (int) TimeUnit.MINUTES.toMillis(5);

  private static final Logger log = Logger.getLogger(FileTransferManager.class);

  /** Mapping identifiers of expected transfers with consuming CompletableFuture. */
  private final ConcurrentMap<String, CompletableFuture<XMPPFileTransferRequest>>
      expectedTransfers = new ConcurrentHashMap<>();

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
          log.info(
              "received transfer request, name: "
                  + request.getFileName()
                  + ", identifier: "
                  + identifier
                  + " by "
                  + contact);

          if (identifier != null) {
            CompletableFuture<XMPPFileTransferRequest> future =
                expectedTransfers.remove(identifier);
            if (future != null) {
              future.complete(new XMPPFileTransferRequest(contact, request));
              return;
            }
          }

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
   * Add a expected transfer mapped by a identifier. This method returns a Future providing the
   * expected transfer when available.
   *
   * <p>Mappings are removed if a transfer was received, otherwise need to be canceled via {@link
   * Future#cancel(boolean)}.
   *
   * @param identifier identifier to match a transfer
   * @return Future providing {@link XMPPFileTransferRequest}
   */
  public Future<XMPPFileTransferRequest> addExpectedTransferRequest(String identifier) {
    CompletableFuture<XMPPFileTransferRequest> future = new CompletableFuture<>();

    // removes the transfer on cancel / complete
    future.whenComplete((request, exception) -> expectedTransfers.remove(identifier));

    expectedTransfers.put(identifier, future);
    return future;
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

  /**
   * Start a file stream with a contact.
   *
   * <p>This method will return while the transfer negotiation is processing. Use {@link
   * XMPPFileTransfer#waitForTransferStart(java.util.function.BooleanSupplier)} to wait till the
   * negotiation is done.
   *
   * @param remoteJID fully qualified JID of remote
   * @param streamName identifier of the stream
   * @param inputStream InputStream to send data from
   * @return {@link XMPPFileTransfer} providing information about the transfer
   * @throws IllegalArgumentException if provided JID is not fully qualified
   * @throws IOException if no connection is available or XMPP related error
   */
  public XMPPFileTransfer streamSendStart(JID remoteJID, String streamName, InputStream inputStream)
      throws IllegalArgumentException, IOException {
    if (remoteJID == null || remoteJID.isBareJID())
      throw new IllegalArgumentException("No valid remoteJID provided: " + remoteJID);

    FileTransferManager currentManager = smackTransferManager.get();
    if (currentManager == null) throw new IOException("No XMPP connection.");

    OutgoingFileTransfer transfer = currentManager.createOutgoingFileTransfer(remoteJID.getRAW());
    transfer.sendStream(inputStream, streamName, 0, streamName);

    return new XMPPFileTransfer(transfer);
  }
}
