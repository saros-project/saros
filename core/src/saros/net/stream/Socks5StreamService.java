package saros.net.stream;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.Objects;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smackx.bytestreams.BytestreamListener;
import org.jivesoftware.smackx.bytestreams.BytestreamRequest;
import org.jivesoftware.smackx.bytestreams.BytestreamSession;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5BytestreamManager;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5BytestreamRequest;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5BytestreamSession;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5Proxy;
import saros.net.internal.BinaryChannelConnection;
import saros.net.internal.IByteStreamConnection;
import saros.net.internal.IByteStreamConnectionListener;
import saros.net.util.NetworkingUtils;
import saros.net.xmpp.JID;

/**
 * This stream service establishes Socks5 streams.
 *
 * <p>See https://xmpp.org/extensions/xep-0065.html for more details.
 */
public class Socks5StreamService implements IStreamService, BytestreamListener {

  private static final Logger log = Logger.getLogger(Socks5StreamService.class);

  /**
   * Property to either enable or disable the Nagle algorithm for Socks5 byte stream. The default
   * value is to disable the Nagle algorithm.
   */
  private static final boolean TCP_NODELAY =
      Boolean.valueOf(System.getProperty("saros.net.socks5.TCP_NODELAY", "true"));

  /**
   * Timeout for the local side on how long it should wait for the remote side to report which
   * stream host it has connected to.
   *
   * <p>Must <b>always</b> be equal or greater than the {@linkplain #TOTAL_CONNECT_TIMEOUT remote
   * connect timeout} !
   */
  private static final int TARGET_RESPONSE_TIMEOUT =
      Integer.getInteger("saros.net.socks5.TARGET_RESPONSE_TIMEOUT", 30000);

  /**
   * Timeout for the remote side on how long it should try to connect to all offered stream hosts.
   *
   * <p>Must <b>always</b> be lower than the {@linkplain #TARGET_RESPONSE_TIMEOUT response timeout}
   * !
   */
  private static final int TOTAL_CONNECT_TIMEOUT =
      Integer.getInteger("saros.net.socks5.TOTAL_CONNECT_TIMEOUT", 20000);

  private volatile Socks5BytestreamManager socks5Manager;
  private volatile IByteStreamConnectionListener connectionListener;
  private volatile JID localAddress;

  // *********** IStreamService impl start
  @Override
  public IByteStreamConnection connect(final String connectionId, final JID remoteAddress)
      throws IOException, InterruptedException {

    Objects.requireNonNull(connectionId, "connectionId must not be null");
    Objects.requireNonNull(remoteAddress, "remoteAddress must not be null");

    if (connectionId.isEmpty())
      throw new IllegalArgumentException("connectionId must not be empty");

    log.debug(
        "establishing Socks5 bytestream to: "
            + remoteAddress
            + ", "
            + verboseLocalProxyInfo()
            + "...");

    final Socks5BytestreamManager currentSocks5Manager;
    final IByteStreamConnectionListener currentConnectionListener;
    final JID currentLocalAddress;

    synchronized (this) {
      currentSocks5Manager = socks5Manager;
      currentConnectionListener = connectionListener;
      currentLocalAddress = localAddress;
    }

    if (currentSocks5Manager == null
        || currentConnectionListener == null
        || currentLocalAddress == null) {
      throw new IOException(
          "service is not initialized - aborting establishing a Socks5 bytestream to: "
              + remoteAddress);
    }

    final Socks5BytestreamSession session;

    try {
      session = currentSocks5Manager.establishSession(remoteAddress.toString(), connectionId);
    } catch (XMPPException e) {
      throw translateXmppException(e, remoteAddress.toString());
    }

    try {
      setNagleAlgorithm(session);

      return new BinaryChannelConnection(
          currentLocalAddress,
          remoteAddress,
          connectionId,
          new XMPPByteStreamAdapter(session),
          session.isDirect() ? StreamMode.SOCKS5_DIRECT : StreamMode.SOCKS5_MEDIATED,
          currentConnectionListener);

    } catch (IOException e) {
      closeSessionQuietly(session);
      throw e;
    }
  }

  @Override
  public synchronized void initialize(
      Connection connection, IByteStreamConnectionListener listener) {

    localAddress = new JID(connection.getUser());
    socks5Manager = Socks5BytestreamManager.getBytestreamManager(connection);
    socks5Manager.setTargetResponseTimeout(TARGET_RESPONSE_TIMEOUT);
    connectionListener = listener;
    socks5Manager.addIncomingBytestreamListener(this);
  }

  @Override
  public synchronized void uninitialize() {
    if (socks5Manager == null) return;
    socks5Manager.removeIncomingBytestreamListener(this);
    socks5Manager = null;
    connectionListener = null;
    localAddress = null;
  }

  // *********** IStreamService impl end

  @Override
  public void incomingBytestreamRequest(final BytestreamRequest request) {

    final String remoteAddress = request.getFrom();

    log.debug("received request to establish a Socks5 bytestream to: " + remoteAddress);

    final IByteStreamConnectionListener currentConnectionListener;
    final JID currentLocalAddress;

    synchronized (this) {
      currentConnectionListener = connectionListener;
      currentLocalAddress = localAddress;
    }

    if (currentConnectionListener == null || currentLocalAddress == null) {
      log.warn("service is not initialized - rejecting request from: " + remoteAddress);
      request.reject();
      return;
    }

    final Socks5BytestreamSession session;

    try {
      ((Socks5BytestreamRequest) request).setTotalConnectTimeout(TOTAL_CONNECT_TIMEOUT);
      session = (Socks5BytestreamSession) request.accept();
    } catch (InterruptedException e) {
      log.warn("interrupted while accepting request from: " + remoteAddress);

      /* This is called by Smack so avoid interrupting the thread again as nobody knows what will happen. */
      // Thread.currentThread().interrupt();
      return;
    } catch (XMPPException e) {
      log.error("failed to accept request from from: " + remoteAddress, e);
      return;
    }

    try {
      setNagleAlgorithm(session);

      final IByteStreamConnection connection =
          new BinaryChannelConnection(
              currentLocalAddress,
              new JID(remoteAddress),
              request.getSessionID(),
              new XMPPByteStreamAdapter(session),
              session.isDirect() ? StreamMode.SOCKS5_DIRECT : StreamMode.SOCKS5_MEDIATED,
              currentConnectionListener);

      /* FIXME race condition, connection fails, triggers connectionClosed
       * on the listener and afterwards this is called
       */
      currentConnectionListener.connectionChanged(connection.getConnectionID(), connection, true);

    } catch (IOException e) {
      log.error("failed to setup connection for request from: " + remoteAddress, e);
      closeSessionQuietly(session);
    }
  }

  @Override
  public String toString() {
    return "XMPP-Socks5-Stream-Service";
  }

  private static String verboseLocalProxyInfo() {
    final Socks5Proxy proxy = NetworkingUtils.getSocks5ProxySafe();
    final StringBuilder builder = new StringBuilder();

    builder.append("local SOCKS5 proxy");

    if (!proxy.isRunning()) {
      builder.append(" not running");
      return builder.toString();
    }

    builder.append(" running [");
    builder.append("port=").append(proxy.getPort()).append(",");
    builder.append(" configured streamhosts=");
    builder.append(proxy.getLocalAddresses());
    builder.append("]");
    return builder.toString();
  }

  private static void closeSessionQuietly(BytestreamSession session) {
    try {
      session.close();
    } catch (IOException e) {
      // swallow
    }
  }

  private static IOException translateXmppException(
      final XMPPException exception, final String remoteAddress) throws IOException {
    final XMPPError error = exception.getXMPPError();

    if (error != null && error.getCode() == 406) {
      throw new IOException(
          "could not establish a Socks5 bytestream to "
              + remoteAddress
              + ", remote Socks5 support is disabled or encountered an error",
          exception);
    } else if (error != null && error.getCode() == 404) {
      throw new IOException(
          "could not establish a Socks5 bytestream to "
              + remoteAddress
              + ", remote side could not connect to any offered stream hosts",
          exception);

    } else {
      throw new IOException(
          "could not establish a Socks5 bytestream to " + remoteAddress, exception);
    }
  }

  /**
   * Enables or disables the Nagle algorithm depending on the value of {@linkplain #TCP_NODELAY}.
   *
   * @param session the byte stream session to modify
   */
  private static void setNagleAlgorithm(final Socks5BytestreamSession session) {

    final Field socketField;
    final Socket socket;

    try {
      socketField = Socks5BytestreamSession.class.getDeclaredField("socket");
      socketField.setAccessible(true);
      socket = (Socket) socketField.get(session);
    } catch (Exception e) {
      log.warn("Smack API has changed, cannot access socket options", e);
      return;
    }

    try {
      socket.setTcpNoDelay(TCP_NODELAY);
      log.debug("Nagle algorithm for session " + session + " disabled: " + TCP_NODELAY);
    } catch (Exception e) {
      log.warn("could not modifiy TCP_NODELAY socket option", e);
    }
  }
}
