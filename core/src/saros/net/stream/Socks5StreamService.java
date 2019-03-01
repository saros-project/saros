package de.fu_berlin.inf.dpp.net.stream;

import de.fu_berlin.inf.dpp.net.internal.BinaryChannelConnection;
import de.fu_berlin.inf.dpp.net.internal.IByteStreamConnection;
import de.fu_berlin.inf.dpp.net.internal.IByteStreamConnectionListener;
import de.fu_berlin.inf.dpp.net.util.NetworkingUtils;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.util.NamedThreadFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
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

/**
 * Transport class for SOCKS5 bytestreams. When a Request is received always it is tried to
 * establish a connection to the side peer, too. A special ID is used to distinguish connect
 * requests and response requests. If there is a direct connection, we keep it, the other one
 * discarded. If the is no one, a SMACK will establish a mediated connection by the server.
 *
 * <p>Are both connection direct, we use the one of the connect request (same for mediated).
 *
 * <p>However, still there might be a server that only supports unidirectional SOCKS5 bytestreams
 * (i.e. OpenFire). In that case both mediated unidirectional connections are wrapped into a
 * bidirectional one. (see {#link WrappedBidirectionalSocks5BytestreamSession})
 */
/*
 * FIXME DELETE this service, it try to solve things that nowadays are really
 * uncommon, i.e on side is reachable and the other side is not. Normally both
 * sides are unreachable either due to NAT or firewall settings and so
 * connecting to each other will not gain us any advantage. Furthermore the
 * implementation is broken by design, i.e how Socks5 connections are
 * established. Calling #connect(TO) will not connect us TO but ask TO to
 * connect to US ! This is not the fault of this service, it is how XEP-65 is
 * defined.
 *
 * The code is full of race conditions (regarding time) and is hardly
 * maintainable.
 *
 * There are enough software solutions out there that are able to create VPN
 * environments (and the Smack Socks5 implementation can use them) that perform
 * this task ways better than this logic.
 */
public class Socks5StreamService implements IStreamService, BytestreamListener {

  private static final Logger LOG = Logger.getLogger(Socks5StreamService.class);

  private static final Random ID_GENERATOR = new Random();

  private static final String RESPONSE_SESSION_ID_PREFIX = "response-socks5";

  private static final byte BIDIRECTIONAL_TEST_BYTE = 0x1A;

  private static final int MEDIATED_BIDIRECTIONAL_TEST_TIMEOUT = 5000;

  private static final boolean TCP_NODELAY =
      Boolean.valueOf(System.getProperty("de.fu_berlin.inf.dpp.net.socks5.TCP_NODELAY", "true"));

  /**
   * Timeout for the local side on how long it should wait for the remote side to report which
   * stream host it has connected to.
   *
   * <p>Must <b>always</b> be equal or greater than the {@linkplain #TOTAL_CONNECT_TIMEOUT remote
   * connect timeout} !
   */
  private static final int TARGET_RESPONSE_TIMEOUT =
      Integer.getInteger("de.fu_berlin.inf.dpp.net.socks5.TARGET_RESPONSE_TIMEOUT", 30000);

  /**
   * Timeout for the remote side on how long it should try to connect to all offered stream hosts.
   *
   * <p>Must <b>always</b> be lower than the {@linkplain #TARGET_RESPONSE_TIMEOUT response timeout}
   * !
   */
  private static final int TOTAL_CONNECT_TIMEOUT =
      Integer.getInteger("de.fu_berlin.inf.dpp.net.socks5.TOTAL_CONNECT_TIMEOUT", 20000);

  private HashMap<String, Exchanger<Socks5BytestreamSession>> runningRemoteConnects =
      new HashMap<String, Exchanger<Socks5BytestreamSession>>();
  private ExecutorService executorService;

  private volatile Socks5BytestreamManager socks5Manager;
  private volatile IByteStreamConnectionListener connectionListener;

  private JID localAddress;

  /**
   * @param sessionID
   * @param peer
   * @return a Future that tries to establish a second connection to the peer's local SOCKS5 proxy
   */
  private Future<Socks5BytestreamSession> futureToEstablishResponseSession(
      final String sessionID, final String peer) {

    return executorService.submit(
        new Callable<Socks5BytestreamSession>() {
          @Override
          public Socks5BytestreamSession call() throws Exception {
            return (Socks5BytestreamSession) establishResponseSession(sessionID, peer);
          }
        });
  }

  /**
   * Starts a new thread that waits until the connection is established to close it correctly.
   *
   * @param future
   */
  private void waitToCloseResponse(final Future<Socks5BytestreamSession> future) {
    LOG.debug(prefix() + "canceling response connection as it is not needed");

    Thread waitToCloseResponse =
        new Thread("CloseUnneededResponseConnection") {

          @Override
          public void run() {
            try {
              closeQuietly(future.get());
            } catch (InterruptedException e) {
              // nothing to do here
            } catch (ExecutionException e) {
              LOG.debug(
                  prefix()
                      + "Exception while waiting to close unneeded connection: "
                      + e.getMessage());
            }
          }
        };
    waitToCloseResponse.start();
  }

  private String verboseLocalProxyInfo() {
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

  /*
   * the Smack API does not tell us, if a mediated Socks5 connection is
   * unidirectional. But some server implementation (OpenFire) may return such
   * a connection. In this case we have to wrap the unidirectional
   * connections.
   */

  /**
   * Tests one of the bytestreams != null in the opposite direction. It returns it if bidirectional
   * or tries to wrap two unidirectional streams if possible. Else an exception is thrown. The
   * testing order is defined by the boolean preferInSession.
   *
   * @pre inSession!=null || outSession!=null
   * @param inSession
   * @param outSession
   * @param preferInSession which stream to test preferable (if != null)
   * @return a bidirectional BytestreamSession
   * @throws IOException if there is only one unidirectional session
   */
  private BytestreamSession testAndGetMediatedBidirectionalBytestream(
      Socks5BytestreamSession inSession,
      Socks5BytestreamSession outSession,
      boolean preferInSession)
      throws IOException {

    String msg = prefix() + "response connection is mediated, too, ";

    Socks5BytestreamSession session = preferInSession ? inSession : outSession;

    // if preferable session did not work, try the other
    if (session == null) {
      preferInSession = !preferInSession;
      session = preferInSession ? inSession : outSession;
    }

    LOG.debug(
        prefix()
            + "trying if "
            + (preferInSession ? "incoming " : "outgoing")
            + " session is bidirectional");

    if (streamIsBidirectional(session, preferInSession)) {
      LOG.debug(
          msg
              + "but at least the server allows bidirectional connections. (using "
              + (preferInSession ? "incoming session" : "outgoing session")
              + ")");
      closeQuietly(preferInSession ? outSession : inSession);
      return session;
    }

    if (inSession == null || outSession == null) {
      closeQuietly(inSession);
      closeQuietly(outSession);
      throw new IOException(
          "Could only establish one unidirectional connection but need two for wrapping.");
    }

    LOG.debug(
        msg
            + "and the server does not allow bidirectional connections. Wrapped session established.");

    return new WrappedBidirectionalSocks5BytestreamSession(inSession, outSession);
  }

  /**
   * Sends and receives an INT to distinguish between bidirectional and unidirectional streams.
   *
   * @param session
   * @param sendFirst
   * @return whether a stream is bidirectional
   * @throws IOException
   */
  private boolean streamIsBidirectional(Socks5BytestreamSession session, boolean sendFirst)
      throws IOException {

    try {
      OutputStream out = session.getOutputStream();
      InputStream in = session.getInputStream();
      int test = 0;

      session.setReadTimeout(MEDIATED_BIDIRECTIONAL_TEST_TIMEOUT);

      if (sendFirst) {
        out.write(BIDIRECTIONAL_TEST_BYTE);
        test = in.read();
      } else {
        test = in.read();
        out.write(BIDIRECTIONAL_TEST_BYTE);
      }

      if (test == BIDIRECTIONAL_TEST_BYTE) {
        LOG.trace(
            prefix() + "stream is bidirectional. (" + (sendFirst ? "sending" : "receiving") + ")");
        return true;
      } else {
        LOG.error(prefix() + "stream can send and receive but got wrong result: " + test);
        throw new IOException("SOCKS5 bytestream connections got mixed up. Try another transport.");
        /*
         * Note: a reason here might be a too low TEST_TIMEOUT but the
         * exception enables fallback to IBB instead of having the
         * stream crash on first use.
         */
      }

    } catch (SocketTimeoutException ste) {
      /*
       * At least we have to wait TEST_STREAM_TIMEOUT to cause a timeout
       * on the peer side, too.
       *
       * Else the first package might be read and the above error occurs
       * (test != BIDIRECTIONAL_TEST_BYTE).
       */
      try {
        Thread.sleep(MEDIATED_BIDIRECTIONAL_TEST_TIMEOUT);
      } catch (InterruptedException e) {
        // nothing to do here
      }
    }

    /*
     * Note: the streams cannot be closed here - even not the unused ones -
     * as setting the timeout later on would throw an exception
     */

    LOG.debug(prefix() + "stream is unidirectional. Trying to wrap bidirectional one.");

    return false;
  }

  /**
   * Handles a response request.
   *
   * <p>The session is exchanged to the connecting thread.
   *
   * @param request
   * @throws XMPPException
   * @throws InterruptedException
   */
  private void handleResponse(BytestreamRequest request)
      throws XMPPException, InterruptedException {

    String peer = request.getFrom();
    LOG.debug(
        prefix() + "receiving response connection from " + peer + ", " + verboseLocalProxyInfo());

    Socks5BytestreamSession inSession = (Socks5BytestreamSession) request.accept();

    String sessionID = getSessionID(request.getSessionID());

    // get running connect
    Exchanger<Socks5BytestreamSession> exchanger = runningRemoteConnects.get(sessionID);

    if (exchanger == null) {
      LOG.warn(prefix() + "Received response connection without a running connect");
      closeQuietly(inSession);
      return;
    }

    try {
      exchanger.exchange(inSession, TARGET_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      LOG.debug(prefix() + "Wrapping bidirectional stream was interrupted.");
      closeQuietly(inSession);
    } catch (TimeoutException e) {
      LOG.error(
          prefix()
              + "Wrapping bidirectional stream timed out in Request! Shouldn't have happened.");
      closeQuietly(inSession);
    }
  }

  /**
   * Accepts a Request and returns an established IByteStreamConnection.
   *
   * <p>Immediately tries to establish a second session to the requesting peer but also accepts this
   * request to achieve a direct connection although one peer might be behind a NAT.
   *
   * <p>A direct connection is used, the other discarded where the requesting session is preferred.
   *
   * <p>In case of unidirectional connections both sessions a wrapped into a bidirectional one.
   *
   * @param request
   * @return established BinaryChannel
   * @throws XMPPException
   * @throws InterruptedException
   * @throws IOException
   */
  private IByteStreamConnection acceptNewRequest(BytestreamRequest request)
      throws XMPPException, IOException, InterruptedException {
    String peer = request.getFrom();

    LOG.debug(prefix() + "receiving request from " + peer + ", " + verboseLocalProxyInfo());

    IByteStreamConnectionListener listener = connectionListener;

    if (listener == null) throw new IOException(this + " transport is not initialized");

    String sessionID = request.getSessionID();

    String connectionIdentifier = getConnectionIdentifier(sessionID);

    if (connectionIdentifier == null) {
      LOG.warn(
          "rejecting request from " + peer + " , no connection identifier found: " + sessionID);
      request.reject();
      return null;
    }

    assert sessionID != null;

    // start to establish response
    Future<Socks5BytestreamSession> responseFuture =
        futureToEstablishResponseSession(sessionID, peer);

    Socks5BytestreamSession inSession = null;

    try {

      inSession = (Socks5BytestreamSession) request.accept();

      if (inSession.isDirect()) {
        waitToCloseResponse(responseFuture);
        configureSocks5Socket(inSession);

        return new BinaryChannelConnection(
            localAddress,
            new JID(peer),
            connectionIdentifier,
            new XMPPByteStreamAdapter(inSession),
            StreamMode.SOCKS5_DIRECT,
            listener);
      } else {
        LOG.debug(prefix() + "incoming connection is mediated.");
      }

    } catch (Exception e) {
      LOG.warn(
          prefix()
              + "Couldn't accept request but still trying to establish a response connection: "
              + e.getMessage());
    }

    Socks5BytestreamSession outSession = null;

    try {

      outSession = responseFuture.get();

      if (outSession.isDirect()) {
        LOG.debug(prefix() + "newly established session is direct! Discarding the other.");
        closeQuietly(inSession);
        configureSocks5Socket(outSession);

        return new BinaryChannelConnection(
            localAddress,
            new JID(peer),
            connectionIdentifier,
            new XMPPByteStreamAdapter(outSession),
            StreamMode.SOCKS5_DIRECT,
            listener);
      }

    } catch (IOException e) {
      LOG.error(prefix() + "Socket crashed while initiating sending session (for wrapping)", e);
    } catch (ExecutionException e) {
      LOG.error("An error occurred while establishing a response connection ", e.getCause());
    }

    if (inSession == null && outSession == null)
      throw new IOException(prefix() + "Neither connection could be established. ");

    BytestreamSession session =
        testAndGetMediatedBidirectionalBytestream(inSession, outSession, true);

    return new BinaryChannelConnection(
        localAddress,
        new JID(peer),
        connectionIdentifier,
        new XMPPByteStreamAdapter(session),
        StreamMode.SOCKS5_MEDIATED,
        listener);
  }

  /**
   * Handles the SOCKS5Bytestream Request and distinguishes between connect requests and response
   * requests.
   *
   * <p>see handleResponse() and acceptNewRequest()
   */
  private IByteStreamConnection acceptRequest(BytestreamRequest request)
      throws XMPPException, IOException, InterruptedException {

    ((Socks5BytestreamRequest) request).setTotalConnectTimeout(TOTAL_CONNECT_TIMEOUT);

    if (isResponse(request)) {
      handleResponse(request);
      return null;
    } else {
      return acceptNewRequest(request);
    }
  }

  /**
   * Tries to establish a connection to peer and waits for peer to connect. See handleResponse().
   */
  private IByteStreamConnection establishBinaryChannel(String connectionIdentifier, String peer)
      throws XMPPException, IOException, InterruptedException {

    Socks5BytestreamManager manager = socks5Manager;
    IByteStreamConnectionListener listener = connectionListener;

    if (manager == null || listener == null)
      throw new IOException(this + " transport is not initialized");

    LOG.debug(
        prefix() + "establishing connection to " + peer + ", " + verboseLocalProxyInfo() + "...");

    // before establishing, we have to put the exchanger to the map
    Exchanger<Socks5BytestreamSession> exchanger = new Exchanger<Socks5BytestreamSession>();

    String sessionID = generateSessionID(connectionIdentifier, getNextNegotiationID());

    runningRemoteConnects.put(sessionID, exchanger);

    try {

      Exception exception = null;
      Socks5BytestreamSession outSession = null;
      // Do we get a working connection?
      try {

        outSession = manager.establishSession(peer, sessionID);

        if (outSession.isDirect()) {
          configureSocks5Socket(outSession);
          return new BinaryChannelConnection(
              localAddress,
              new JID(peer),
              connectionIdentifier,
              new XMPPByteStreamAdapter(outSession),
              StreamMode.SOCKS5_DIRECT,
              listener);
        }

        LOG.debug(
            prefix()
                + "connection/session is mediated, performing additional connection optimization...");

      } catch (IOException e) {
        exception = e;
        LOG.warn(
            prefix()
                + "could not establish a connection to "
                + peer
                + " due to an error in the socket communictation",
            e);
      } catch (XMPPException e) {
        exception = e;
        XMPPError error = e.getXMPPError();

        if (error != null && error.getCode() == 406) {
          LOG.warn(
              prefix()
                  + "could not establish a connection to "
                  + peer
                  + ", remote Socks5 transport is disabled or encountered an error: "
                  + e.getMessage());
          /*
           * quit here as it makes no sense to wait for the remote
           * side to connect because this will never happen !
           */
          throw e;
        } else if (error != null && error.getCode() == 404) {
          LOG.warn(
              prefix()
                  + "could not establish a connection to "
                  + peer
                  + ", remote side could not connect to any offered stream hosts: "
                  + e.getMessage());
        } else {
          LOG.error(prefix() + "could not establish a connection to " + peer, e);
        }
      } catch (Exception e) {
        // FIXME handle the InterruptedException correctly !
        exception = e;

        /*
         * catch any possible RuntimeException because we must wait for
         * the peer that may attempt to connect
         */
        LOG.error(
            prefix()
                + "could not connect to "
                + peer
                + " because of an internal error: "
                + exception.getMessage(),
            e);
      }

      LOG.debug(prefix() + "waiting for " + peer + " to establish a connection...");
      Socks5BytestreamSession inSession = null;

      // else wait for request
      try {
        inSession = exchanger.exchange(null, TARGET_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS);

        if (inSession.isDirect()) {
          LOG.debug(prefix() + "response connection is direct! Discarding the other.");
          closeQuietly(outSession);
          configureSocks5Socket(inSession);

          return new BinaryChannelConnection(
              localAddress,
              new JID(peer),
              connectionIdentifier,
              new XMPPByteStreamAdapter(inSession),
              StreamMode.SOCKS5_DIRECT,
              listener);
        }

      } catch (TimeoutException e) {
        closeQuietly(outSession);
        String msg = "waiting for a response session timed out (" + TARGET_RESPONSE_TIMEOUT + "ms)";
        if (exception != null)
          throw new IOException(
              prefix() + msg + " and could not establish a connection from this side, too:",
              exception);
        else LOG.debug(msg);
      }

      BytestreamSession session =
          testAndGetMediatedBidirectionalBytestream(inSession, outSession, false);

      return new BinaryChannelConnection(
          localAddress,
          new JID(peer),
          connectionIdentifier,
          new XMPPByteStreamAdapter(session),
          StreamMode.SOCKS5_MEDIATED,
          listener);

    } finally {
      runningRemoteConnects.remove(sessionID);
    }
  }

  /**
   * @param peer
   * @return a BytestreamSession with a response ID
   * @throws XMPPException
   * @throws IOException
   * @throws InterruptedException
   */
  private BytestreamSession establishResponseSession(String sessionID, String peer)
      throws XMPPException, IOException, InterruptedException {

    LOG.debug(prefix() + "Start to establish new response connection");

    Socks5BytestreamManager manager = socks5Manager;

    if (manager == null) throw new IOException(this + " transport is not initialized");

    return manager.establishSession(peer, getResponseSessionID(sessionID));
  }

  // *********** IStreamService impl start
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

    LOG.debug("establishing Socks5 bytestream to: " + remoteAddress);

    try {
      return establishBinaryChannel(connectionID, remoteAddress.toString());
    } catch (XMPPException e) {
      throw new IOException(e);
    }
  }

  @Override
  public void initialize(Connection connection, IByteStreamConnectionListener listener) {

    synchronized (this) {
      localAddress = new JID(connection.getUser());
      socks5Manager = createManager(connection);
      connectionListener = listener;
      socks5Manager.addIncomingBytestreamListener(this);
    }

    executorService =
        Executors.newCachedThreadPool(new NamedThreadFactory("SOCKS5ConnectionResponse-"));
  }

  @Override
  public void uninitialize() {

    synchronized (this) {
      if (socks5Manager == null) return;

      socks5Manager.removeIncomingBytestreamListener(this);
      socks5Manager = null;
      connectionListener = null;
    }

    assert (executorService != null);

    List<Runnable> notCommenced = executorService.shutdownNow();
    if (notCommenced.size() > 0)
      LOG.warn(prefix() + "threads for response connections found that didn't commence yet");
    executorService = null;
  }

  // *********** IStreamService impl end

  @Override
  public void incomingBytestreamRequest(BytestreamRequest request) {
    LOG.debug(
        "received request to establish a Socks5 bytestream to "
            + request.getFrom()
            + " ["
            + this
            + "]");

    try {

      IByteStreamConnection connection = acceptRequest(request);

      if (connection == null) return;

      IByteStreamConnectionListener listener = connectionListener;

      if (listener == null) {
        LOG.warn(
            "closing bytestream connection "
                + connection
                + " because transport "
                + this
                + " was uninitilized during connection establishment");
        connection.close();
        return;
      }

      listener.connectionChanged(connection.getConnectionID(), connection, true);

    } catch (InterruptedException e) {
      /*
       * do not interrupt here as this is called by SMACK and nobody knows
       * how SMACK handle thread interruption
       */
      LOG.warn("interrupted while establishing bytestream connection to " + request.getFrom());
    } catch (Exception e) {
      LOG.error("could not establish bytestream connection to " + request.getFrom(), e);
    }
  }

  private Socks5BytestreamManager createManager(Connection connection) {
    Socks5BytestreamManager socks5ByteStreamManager =
        Socks5BytestreamManager.getBytestreamManager(connection);

    socks5ByteStreamManager.setTargetResponseTimeout(TARGET_RESPONSE_TIMEOUT);

    return socks5ByteStreamManager;
  }

  /**
   * Wraps two Socks5BytestreamSessions in one, where for the first one, "in", the InputStream has
   * to work, for the second one, "out", the OutputStream.
   */
  private static class WrappedBidirectionalSocks5BytestreamSession implements BytestreamSession {

    private Socks5BytestreamSession in;
    private Socks5BytestreamSession out;

    public WrappedBidirectionalSocks5BytestreamSession(
        Socks5BytestreamSession in, Socks5BytestreamSession out) {
      this.in = in;
      this.out = out;
    }

    @Override
    public void close() throws IOException {
      IOException e = null;

      try {
        in.close();
      } catch (IOException e1) {
        e = e1;
      }

      try {
        out.close();
      } catch (IOException e1) {
        e = e1;
      }

      if (e != null) throw e;
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return in.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
      return out.getOutputStream();
    }

    @Override
    public int getReadTimeout() throws IOException {
      return in.getReadTimeout();
    }

    @Override
    public void setReadTimeout(int timeout) throws IOException {
      in.setReadTimeout(timeout);
    }
  }

  private void configureSocks5Socket(Socks5BytestreamSession session) {

    Field socketField = null;
    Socket socket;

    try {
      socketField = Socks5BytestreamSession.class.getDeclaredField("socket");
      socketField.setAccessible(true);
      socket = (Socket) socketField.get(session);
    } catch (Exception e) {
      LOG.warn("Smack API has changed, cannot access socket options", e);
      return;
    }

    try {
      socket.setTcpNoDelay(TCP_NODELAY);
      LOG.debug("nagle algorithm for socket disabled: " + TCP_NODELAY);
    } catch (Exception e) {
      LOG.warn("could not modifiy TCP_NODELAY socket option", e);
    }

    /*
     * avoid lingering to long (is this needed at all, Saros is not a high
     * performance server application at all
     */

    // final int lingerTimeout = 10; // seconds
    //
    // try {
    // socket.setSoLinger(true, lingerTimeout);
    // LOG.debug("socket is configured with SO_LINGER timeout: "
    // + socket.getSoLinger() + " s");
    // } catch (Exception e) {
    // LOG.warn("could not modify SO_LINGER socket option", e);
    // }

  }

  private String prefix() {
    return "[SOCKS5Transport] ";
  }

  private long getNextNegotiationID() {
    return ID_GENERATOR.nextLong() & Long.MAX_VALUE;
  }

  private static String generateSessionID(String connectionIdentifier, long negotiationID) {
    return connectionIdentifier + SESSION_ID_DELIMITER + negotiationID;
  }

  private static String getResponseSessionID(String sessionID) {

    if (sessionID == null || sessionID.isEmpty()) return null;

    if (sessionID.startsWith(RESPONSE_SESSION_ID_PREFIX)) return sessionID;

    return RESPONSE_SESSION_ID_PREFIX + SESSION_ID_DELIMITER + sessionID;
  }

  // [response]:connectionID:negotiationID
  private static String getConnectionIdentifier(String sessionID) {

    if (sessionID == null || sessionID.isEmpty()) return null;

    String[] sessionIDTokens = sessionID.split(Pattern.quote(String.valueOf(SESSION_ID_DELIMITER)));

    if (sessionIDTokens.length == 2) return sessionIDTokens[0];

    if (sessionIDTokens.length == 3 && sessionIDTokens[0].equals(RESPONSE_SESSION_ID_PREFIX))
      return sessionIDTokens[1];

    return null;
  }

  // response:connectionID:negotiationID
  private static String getSessionID(String responseSessionID) {

    if (responseSessionID == null || responseSessionID.isEmpty()) return null;

    String[] sessionIDTokens =
        responseSessionID.split(Pattern.quote(String.valueOf(SESSION_ID_DELIMITER)));

    if (sessionIDTokens.length != 3 || !sessionIDTokens[0].equals(RESPONSE_SESSION_ID_PREFIX))
      return null;

    return sessionIDTokens[1] + SESSION_ID_DELIMITER + sessionIDTokens[2];
  }

  private static boolean isResponse(BytestreamRequest request) {
    return request.getSessionID().startsWith(RESPONSE_SESSION_ID_PREFIX);
  }

  private static void closeQuietly(BytestreamSession stream) {
    if (stream == null) return;
    try {
      stream.close();
    } catch (Exception e) {
      // NOP
    }
  }

  @Override
  public String toString() {
    return "XMPP-Socks5-Stream-Service";
  }
}
