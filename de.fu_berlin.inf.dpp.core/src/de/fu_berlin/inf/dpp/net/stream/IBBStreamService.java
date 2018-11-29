package de.fu_berlin.inf.dpp.net.stream;

import de.fu_berlin.inf.dpp.net.internal.BinaryChannelConnection;
import de.fu_berlin.inf.dpp.net.internal.IByteStreamConnection;
import de.fu_berlin.inf.dpp.net.internal.IByteStreamConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.bytestreams.BytestreamListener;
import org.jivesoftware.smackx.bytestreams.BytestreamManager;
import org.jivesoftware.smackx.bytestreams.BytestreamRequest;
import org.jivesoftware.smackx.bytestreams.BytestreamSession;
import org.jivesoftware.smackx.bytestreams.ibb.InBandBytestreamManager;

/**
 * This stream service establishes IBB sessions.
 *
 * <p>See http://xmpp.org/extensions/xep-0047.html for more details.
 */
public class IBBStreamService implements IStreamService, BytestreamListener {

  private static final Logger LOG = Logger.getLogger(IBBStreamService.class);

  private volatile InBandBytestreamManager manager;
  private volatile IByteStreamConnectionListener connectionListener;
  private JID localAddress;

  public IBBStreamService() {
    // NOP
  }

  @Override
  public IByteStreamConnection connect(String connectionID, JID remoteAddress)
      throws IOException, InterruptedException {

    if (connectionID == null) throw new NullPointerException("connectionID is null");

    if (remoteAddress == null) throw new NullPointerException("remoteAddress is null");

    if (connectionID.isEmpty())
      throw new IllegalArgumentException("connectionID must not be empty");

    if (connectionID.contains(String.valueOf(IStreamService.SESSION_ID_DELIMITER)))
      throw new IllegalArgumentException(
          "connectionID must not contain '" + IStreamService.SESSION_ID_DELIMITER + "'");

    LOG.debug("establishing IBB bytestream to: " + remoteAddress);

    final BytestreamManager currentManager = manager;
    final IByteStreamConnectionListener currentConnectionListener = connectionListener;

    if (currentManager == null || currentConnectionListener == null)
      throw new IOException(this + " is not initialized");

    final BytestreamSession session;

    try {
      session = manager.establishSession(remoteAddress.toString(), connectionID);
    } catch (XMPPException e) {
      throw new IOException(e);
    }

    return new BinaryChannelConnection(
        localAddress,
        remoteAddress,
        connectionID,
        new XMPPByteStreamAdapter(session),
        StreamMode.IBB,
        currentConnectionListener);
  }

  @Override
  public synchronized void initialize(
      Connection connection, IByteStreamConnectionListener listener) {
    localAddress = new JID(connection.getUser());
    connectionListener = listener;
    manager = InBandBytestreamManager.getByteStreamManager(connection);
    manager.addIncomingBytestreamListener(this);
  }

  @Override
  public synchronized void uninitialize() {
    if (manager == null) return;

    manager.removeIncomingBytestreamListener(this);
    manager = null;
    connectionListener = null;
  }

  // ***************** BytestreamListener interface impl start

  @Override
  public void incomingBytestreamRequest(BytestreamRequest request) {

    LOG.debug("accepting IBB bytestream from: " + request.getFrom());

    final IByteStreamConnectionListener currentConnectionListener = connectionListener;

    if (currentConnectionListener == null) {
      LOG.warn(this + " is not initialized, rejecting connection...");
      request.reject();
    }

    final BytestreamSession session;

    try {
      session = request.accept();
    } catch (XMPPException e) {
      LOG.error("failed to accept IBB bytestream from: " + request.getFrom(), e);
      return;
    } catch (InterruptedException e) {
      /*
       * do not interrupt here as this is called by SMACK and nobody knows
       * how SMACK handle thread interruption
       */

      LOG.error("interrupted while accepting IBB bytestream from: " + request.getFrom(), e);
      return;
    }

    final IByteStreamConnection connection;

    try {
      connection =
          new BinaryChannelConnection(
              localAddress,
              new JID(request.getFrom()),
              request.getSessionID(),
              new XMPPByteStreamAdapter(session),
              StreamMode.IBB,
              connectionListener);
    } catch (IOException e) {
      LOG.error("failed to initialize connection for IBB stream", e);
      try {
        session.close();
      } catch (IOException ignore) {
        LOG.error(ignore);
      }
      return;
    }

    connectionListener.connectionChanged(request.getSessionID(), connection, true);
  }

  // ***************** BytestreamListener interface impl end

  @Override
  public String toString() {
    return "XMPP-IBB-Stream-Service";
  }
}
