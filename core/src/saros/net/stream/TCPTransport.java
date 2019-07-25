package saros.net.stream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Packet;
import saros.misc.xstream.XStreamExtensionProvider;
import saros.net.internal.BinaryChannelConnection;
import saros.net.internal.IByteStreamConnection;
import saros.net.internal.IByteStreamConnectionListener;
import saros.net.xmpp.JID;
import saros.util.ThreadUtils;

// TODO rewrite IStreamService interface

public class TCPTransport implements IStreamService {

  private static final String TLS_VERSION = "TLSv1.2";

  private static final int READ_TIMEOUT = 20 * 1000;

  private static final int OVERALL_CONNECT_TIMEOUT = 60 * 1000;

  private static final int DEFAULT_CONNECT_TIMEOUT = 10 * 1000;

  private static final long ACCEPT_CONNECTION_EXPIRATION_TIME = 60 * 1000;

  private static final Charset CHARSET = Charset.forName("UTF-8");

  private static final Logger log = Logger.getLogger(TCPTransport.class);

  private static final Random RANDOM = new Random();

  private static final XStreamExtensionProvider<IpLookup> provider =
      new XStreamExtensionProvider<>("http://saros/net", "iplookup");

  // as we create certificates on the fly we cannot look them up by a trusted third party
  private static final X509TrustManager TRUST_EVERY_CERTIFICATE_MANAGER =
      new X509TrustManager() {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
          // NOP

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
          // NOP

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
          return null;
        }
      };

  private static Configuration configuration = new Configuration();

  static {
    assert OVERALL_CONNECT_TIMEOUT > DEFAULT_CONNECT_TIMEOUT;
  }

  /** Contains the hashes which are used for either granting or denying connection requests. */
  private final ConcurrentHashMap<Long, Long> hashCache = new ConcurrentHashMap<>();

  private volatile IByteStreamConnectionListener listener;

  private volatile Connection connection;

  private volatile ServerSocket serverSocket;

  private List<String> addresses = new CopyOnWriteArrayList<>();

  private Thread serverSocketAcceptThread;

  /**
   * Returns the global configuration for all instances of this class.
   *
   * @return the global configuration
   */
  public static Configuration getConfiguration() {
    return configuration;
  }

  @Override
  public IByteStreamConnection connect(String connectionID, JID peer)
      throws IOException, InterruptedException {

    final IByteStreamConnectionListener currentListener = listener;

    final Connection currentConnection = connection;

    if (currentListener == null || currentConnection == null)
      throw new IOException(this + " transport is not initialized");

    final IQ request = provider.createIQ(new IpLookup());
    request.setType(Type.GET);
    request.setPacketID(Packet.nextID());
    request.setFrom(currentConnection.getUser());
    request.setTo(peer.getRAW());

    final String packetReplyId = request.getPacketID();

    final PacketCollector collector =
        currentConnection.createPacketCollector(
            new AndFilter(
                provider.getIQFilter(), (packet) -> packetReplyId.equals(packet.getPacketID())));

    final Packet packet;

    try {
      currentConnection.sendPacket(request);

      packet = collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
    } finally {
      collector.cancel();
    }

    final IpLookup lookup = provider.getPayload(packet);

    if (lookup == null || lookup.addresses == null)
      throw new IOException("invalid ip lookup reply: " + lookup);

    final int port = lookup.port;
    final List<String> remoteAddresses = lookup.addresses;

    if (remoteAddresses.isEmpty())
      throw new IOException("could not connect to " + peer + ", no addresses provided");

    final SSLSocketFactory sf;

    try {
      sf = getClientSocketFactory();
    } catch (Exception e) {
      throw new IOException("unable to create TLS support", e);
    }

    Socket socket = null;

    final int timeout = getConnectTimeout(remoteAddresses);

    if (log.isDebugEnabled())
      log.debug("available addresses for connection establishment: " + remoteAddresses);

    for (final String address : remoteAddresses) {

      try {

        if (log.isDebugEnabled())
          log.debug("connecting to " + address + ":" + port + " - timeout=" + timeout);

        final SocketAddress socketAddress = new InetSocketAddress(address, port);

        socket = sf.createSocket();

        socket.connect(socketAddress, timeout);

        if (log.isDebugEnabled()) log.debug("connected to " + address + ":" + port);

        break;
      } catch (Exception e) {
        log.warn("failed to connect to " + address + ":" + port + " - " + e.getMessage());
        try {
          if (socket != null) socket.close();
        } catch (IOException ignore) {
          // ignore
        }
        socket = null;
      }
    }

    if (socket == null) {
      throw new IOException(
          "could not connect to "
              + peer
              + ", no connection could be made to the following addresses: "
              + remoteAddresses.isEmpty());
    }

    logSslSessionInformation((SSLSocket) socket);

    socket.setTcpNoDelay(true);

    final String user = currentConnection.getUser();

    byte[] connectionIdOctets = connectionID.getBytes(CHARSET);
    byte[] jidOctets = user.getBytes(CHARSET);

    final DataOutputStream out = new DataOutputStream(socket.getOutputStream());

    out.writeLong(hash(user, lookup.salt));

    out.write(connectionIdOctets.length);
    out.write(connectionIdOctets);

    out.writeShort(jidOctets.length);
    out.write(jidOctets);

    out.flush();

    return new BinaryChannelConnection(
        new JID(currentConnection.getUser()),
        peer,
        connectionID,
        new TCPByteStream(socket),
        StreamMode.TCP,
        currentListener);
  }

  @Override
  public void initialize(Connection connection, IByteStreamConnectionListener listener) {

    log.debug("initializing transport...");

    this.listener = listener;
    this.connection = connection;

    final Configuration configuration = getConfiguration();

    addresses.addAll(configuration.addresses);

    int port = configuration.localPort;

    final boolean findUnusedPort = port < 0;

    port = Math.abs(port);

    if (port <= 0 || port > 65535) {
      log.error("invalid port given: " + port);
      return;
    }

    boolean bound = false;

    final SSLServerSocketFactory ssf;

    try {
      ssf = getServerSocketFactory();
    } catch (Exception e) {
      log.error("unable to create TLS support", e);
      return;
    }

    IOException lastError = null;

    final int maxPortNumber = findUnusedPort ? 65536 : port + 1;

    for (; port < maxPortNumber; port++) {
      try {
        serverSocket = ssf.createServerSocket(port);
        bound = true;
        break;
      } catch (IOException e) {
        lastError = e;
      }
    }

    if (!bound) {
      log.error("failed to open server socket", lastError);
      return;
    }

    log.info("server socket opened for all available interfaces on port:" + port);

    serverSocketAcceptThread =
        ThreadUtils.runSafeAsync(
            "dpp-tcp-server-socket-accept", log, () -> runAcceptLoop(serverSocket));

    connection.addPacketListener(
        this::replyToLookupRequest,
        new AndFilter(provider.getIQFilter(), (p) -> ((IQ) p).getType() == Type.GET));
  }

  @Override
  public void uninitialize() {

    log.debug("uninitializing transport...");
    listener = null;

    if (serverSocket == null) return;

    try {
      log.debug("closing server socket");
      serverSocket.close();
      serverSocket = null;
    } catch (IOException e) {
      log.error("failed to close server socket", e);
    }

    if (serverSocket == null && serverSocketAcceptThread != null) {
      try {
        serverSocketAcceptThread.join(10000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    serverSocketAcceptThread = null;
    serverSocket = null;
  }

  @Override
  public String toString() {
    return "Generic-TLS-TCP-Transport";
  }

  private void runAcceptLoop(ServerSocket socket) {

    Socket client = null;

    while (true) {
      try {
        prune();

        client = null;
        client = socket.accept();

        logSslSessionInformation((SSLSocket) client);

        // TODO the logic below should be performed by a thread pool
        client.setSoTimeout(READ_TIMEOUT);
        client.setTcpNoDelay(true);

        final DataInputStream in = new DataInputStream(client.getInputStream());

        final long secretHash = in.readLong();

        final long currentTime = System.currentTimeMillis();

        final Long expirationTime = hashCache.remove(secretHash);

        if (expirationTime == null
            || currentTime - expirationTime > ACCEPT_CONNECTION_EXPIRATION_TIME)
          throw new IOException("time expired for secret hash: " + secretHash);

        final int connectionIdLength = in.read();

        final byte[] connectionIdOctets = new byte[connectionIdLength];

        in.readFully(connectionIdOctets);

        final int jidLenght = in.readShort();

        final byte[] jidOctets = new byte[jidLenght];

        in.readFully(jidOctets);

        client.setSoTimeout(0);

        final JID remoteJID = new JID(new String(jidOctets, CHARSET));

        final BinaryChannelConnection channelConnection =
            new BinaryChannelConnection(
                new JID(connection.getUser()),
                remoteJID,
                new String(connectionIdOctets, CHARSET),
                new TCPByteStream(client),
                StreamMode.TCP,
                listener);

        listener.connectionChanged(
            new String(connectionIdOctets, CHARSET), channelConnection, true);
      } catch (IOException e) {
        if (socket.isClosed()) return;

        log.error("failed to accept incoming connection", e);

        if (client != null) {
          try {
            client.setSoLinger(true, 0);
          } catch (SocketException ignore) {
            // ignore
          }
          try {
            client.close();
          } catch (IOException ignore) {
            // ignore
          }
        }
      }
    }
  }

  private void replyToLookupRequest(final Packet request) {

    if (log.isTraceEnabled()) log.trace("received ip lookup request from:" + request.getFrom());

    final Connection currentConnection = TCPTransport.this.connection;

    if (currentConnection == null) return;

    final IpLookup result = new IpLookup();

    final ServerSocket currentServerSocket = serverSocket;

    if (currentServerSocket == null) {
      result.addresses = Collections.emptyList();
    } else {
      final long salt = RANDOM.nextLong();

      result.addresses = new ArrayList<>(addresses);
      result.port = currentServerSocket.getLocalPort();
      result.salt = salt;

      prune();

      hashCache.put(hash(request.getFrom(), salt), System.currentTimeMillis());
    }

    final IQ response = provider.createIQ(result);

    response.setFrom(request.getTo());
    response.setTo((request.getFrom()));
    response.setType(Type.RESULT);
    response.setPacketID(request.getPacketID());

    if (log.isTraceEnabled())
      log.trace("sending ip lookup reply " + result + " to " + request.getFrom());

    try {
      currentConnection.sendPacket(response);
    } catch (RuntimeException e) {
      log.error("failed to send ip lookup reply", e);
    }
  }

  private void prune() {

    if (hashCache.size() < 1000) return;

    final long currentTime = System.currentTimeMillis();

    // does not work correctly in Java 8 https://bugs.openjdk.java.net/browse/JDK-8078645
    hashCache
        .entrySet()
        .removeIf(e -> currentTime - e.getValue() > ACCEPT_CONNECTION_EXPIRATION_TIME);
  }

  private static SSLServerSocketFactory getServerSocketFactory()
      throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException,
          KeyManagementException {

    final SSLContext ctx = SSLContext.getInstance(TLS_VERSION);
    final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    final KeyStore ks = CertificateUtils.createSelfSignedForHost("saros");

    kmf.init(ks, CertificateUtils.KEY_STORE_PASSWORD.toCharArray());
    ctx.init(kmf.getKeyManagers(), null, null);

    return ctx.getServerSocketFactory();
  }

  private static SSLSocketFactory getClientSocketFactory()
      throws NoSuchAlgorithmException, KeyManagementException {

    final SSLContext ctx = SSLContext.getInstance(TLS_VERSION);
    ctx.init(null, new X509TrustManager[] {TRUST_EVERY_CERTIFICATE_MANAGER}, null);

    return ctx.getSocketFactory();
  }

  private static long hash(final String value, long salt) {
    return value.hashCode() * new StringBuilder(value).reverse().toString().hashCode() + salt;
  }

  private static int getConnectTimeout(Collection<?> addresses) {
    final int size = addresses.size();

    if (size == 0) return DEFAULT_CONNECT_TIMEOUT;

    int timeout = OVERALL_CONNECT_TIMEOUT / size;

    return Math.min(timeout, DEFAULT_CONNECT_TIMEOUT);
  }

  private static void logSslSessionInformation(final SSLSocket socket) {

    if (!log.isDebugEnabled()) return;

    final StringBuilder builder = new StringBuilder(128);

    builder.append("connection established -");

    builder.append(" local socket address: ");
    builder.append(socket.getLocalSocketAddress());

    builder.append(" remote socket address: ");
    builder.append(socket.getRemoteSocketAddress());

    builder.append(" | TLS session details -");

    SSLSession session = socket.getSession();

    builder.append(" id: ");
    builder.append(Arrays.toString(session.getId()));

    builder.append(" cipher: ");
    builder.append(session.getCipherSuite());

    builder.append(" protocol: ");
    builder.append(session.getProtocol());

    log.debug(builder.toString());
  }

  public static class Configuration {

    private Set<String> addresses = new CopyOnWriteArraySet<>();
    private volatile int localPort;

    /**
     * Sets the addresses that will send to the remote side when it performs a connection attempt.
     *
     * @param addresses the addresses to use
     */
    public void setAddresses(final Collection<String> addresses) {
      this.addresses.clear();
      this.addresses.addAll(addresses);
      log.info(this.addresses);
    }

    /**
     * Adds an address that will send to the remote side when it performs a connection attempt.
     *
     * @param address the address to use
     */
    public void addAddress(final String address) {
      addresses.add(address);
    }

    /**
     * Sets the local port to use. If the port is negative the logic will try to find an unused port
     * if the given port is already in use.
     *
     * <p>E.g: ports 5555, 5556, and 5557 are already in use. If you set the port to -5555 the local
     * port that will be used is 5558.
     *
     * @param port the port to use
     */
    public void setLocalPort(final int port) {
      localPort = port;
    }
  }

  private static class IpLookup {
    private List<String> addresses;
    private int port;
    private long salt;

    @Override
    public String toString() {
      return "IpLookup [addresses=" + addresses + ", port=" + port + ", salt=" + salt + "]";
    }
  }
}
