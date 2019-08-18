package saros.net.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.jivesoftware.smack.Connection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import saros.net.ConnectionState;
import saros.net.IConnectionManager;
import saros.net.stream.ByteStream;
import saros.net.stream.IStreamService;
import saros.net.stream.IStreamServiceListener;
import saros.net.stream.StreamMode;
import saros.net.xmpp.IConnectionListener;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.test.util.TestThread;

public class DataTransferManagerTest {

  private static class Transport implements IStreamService {

    private List<ByteStream> establishedStreams = new ArrayList<ByteStream>();

    private IStreamServiceListener listener;

    private StreamMode mode;

    public Transport(StreamMode mode) {
      this.mode = mode;
    }

    @Override
    public synchronized ByteStream connect(String connectionIdentifier, JID peer)
        throws IOException, InterruptedException {

      ByteStream byteStream = new DummyByteStream(peer, connectionIdentifier, mode);

      establishedStreams.add(byteStream);
      return byteStream;
    }

    public synchronized void announceIncomingRequest(String connectionIdentifier, JID peer) {

      ByteStream byteStream = new DummyByteStream(peer, connectionIdentifier, mode);

      establishedStreams.add(byteStream);
      listener.connectionEstablished(byteStream);
    }

    @Override
    public void initialize(Connection connection, IStreamServiceListener listener) {
      this.listener = listener;
    }

    @Override
    public void uninitialize() {
      this.listener = null;
    }

    public synchronized List<ByteStream> getEstablishedStreams() {
      return establishedStreams;
    }

    public synchronized void close() {
      for (ByteStream byteStream : establishedStreams)
        try {
          byteStream.close();
        } catch (IOException e) { // ignore}
        }
    }
  }

  private static class BlockableTransport extends Transport {

    private CountDownLatch acknowledge;

    private CountDownLatch proceed;

    private volatile boolean isConnecting;

    private Set<JID> jidsToIgnore;

    public BlockableTransport(
        Set<JID> jidsToIgnore,
        StreamMode mode,
        CountDownLatch acknowledge,
        CountDownLatch proceed) {
      super(mode);
      this.acknowledge = acknowledge;
      this.proceed = proceed;
      this.jidsToIgnore = jidsToIgnore;
    }

    @Override
    public ByteStream connect(String connectionIdentifier, JID peer)
        throws IOException, InterruptedException {

      if (jidsToIgnore.contains(peer)) return super.connect(connectionIdentifier, peer);

      synchronized (this) {
        if (isConnecting)
          throw new IllegalStateException("connect must not be called concurrently");
        isConnecting = true;
      }

      acknowledge.countDown();
      proceed.await();
      ByteStream byteStream = super.connect(connectionIdentifier, peer);
      isConnecting = false;
      return byteStream;
    }
  }

  private static class DummyByteStream implements ByteStream {

    private final JID local = new JID("example.org");
    private final JID remote;
    private final String id;
    private final StreamMode mode;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    private volatile boolean isClosed;

    private InputStream blockingInputStream =
        new InputStream() {

          @Override
          public int read() throws IOException {
            while (!isClosed) {
              synchronized (this) {
                try {
                  wait();
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                  return -1;
                }
              }
            }
            return -1;
          }
        };

    private DummyByteStream(final JID remote, final String id, StreamMode mode) {
      this.remote = remote;
      this.id = id;
      this.mode = mode;
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return blockingInputStream;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
      return out;
    }

    public boolean isClosed() {
      return isClosed;
    }

    @Override
    public void close() throws IOException {
      synchronized (blockingInputStream) {
        isClosed = true;
        blockingInputStream.notifyAll();
      }
    }

    @Override
    public int getReadTimeout() throws IOException {
      return 0;
    }

    @Override
    public void setReadTimeout(int timeout) throws IOException {
      // NOP
    }

    @Override
    public Object getLocalAddress() {
      return local;
    }

    @Override
    public Object getRemoteAddress() {
      return remote;
    }

    @Override
    public StreamMode getMode() {
      return mode;
    }

    @Override
    public String getId() {
      return id;
    }

    public boolean wasUsed() {
      return out.size() != 0;
    }
  }

  private XMPPConnectionService connectionServiceStub;

  private Capture<IConnectionListener> connectionListener = Capture.newInstance();

  private Connection connectionMock;

  private Transport mainTransport;

  private Transport fallbackTransport;

  {
    connectionMock = EasyMock.createMock(Connection.class);
    EasyMock.expect(connectionMock.getUser()).andReturn("local@host").anyTimes();
    EasyMock.replay(connectionMock);
  }

  private XMPPConnectionService createConnectionsServiceMock(
      Capture<IConnectionListener> connectionListener) {
    XMPPConnectionService net = EasyMock.createMock(XMPPConnectionService.class);
    net.addListener(
        EasyMock.and(
            EasyMock.isA(IConnectionListener.class), EasyMock.capture(connectionListener)));
    EasyMock.expectLastCall().once();
    EasyMock.replay(net);
    return net;
  }

  @Before
  public void setUp() {
    connectionServiceStub = createConnectionsServiceMock(connectionListener);
  }

  @After
  public void tearDown() {
    if (mainTransport != null) mainTransport.close();

    if (fallbackTransport != null) fallbackTransport.close();

    mainTransport = fallbackTransport = null;
  }

  @Test(expected = NullPointerException.class)
  public void testEstablishConnectionWithNullPeer() throws Exception {

    IConnectionManager dtm = new DataTransferManager(connectionServiceStub, null, null);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    dtm.connect("foo", null);
  }

  @Test(expected = NullPointerException.class)
  public void testEstablishConnectionWithNullConnectionID() throws Exception {

    IConnectionManager dtm = new DataTransferManager(connectionServiceStub, null, null);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    dtm.connect(null, new JID("foo@bar.com"));
  }

  @Test(expected = IOException.class)
  public void testEstablishConnectionWithNoTransports() throws Exception {

    IConnectionManager dtm = new DataTransferManager(connectionServiceStub, null, null);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    dtm.connect(new JID("foo@bar.com"));
  }

  @Test
  public void testEstablishConnectionWithMainAndFallbackTransport() throws Exception {

    mainTransport = new Transport(StreamMode.SOCKS5_DIRECT);
    fallbackTransport = new Transport(StreamMode.IBB);

    IConnectionManager dtm =
        new DataTransferManager(connectionServiceStub, mainTransport, fallbackTransport);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    dtm.connect(new JID("foo@bar.com"));
    assertEquals(StreamMode.SOCKS5_DIRECT, dtm.getTransferMode(new JID("foo@bar.com")));
  }

  @Test
  public void testEstablishConnectionWithMainAndFallbackTransportAndUsingFallback()
      throws Exception {

    mainTransport = EasyMock.createMock(Transport.class);

    fallbackTransport = new Transport(StreamMode.IBB);

    EasyMock.expect(mainTransport.connect(EasyMock.isA(String.class), EasyMock.isA(JID.class)))
        .andThrow(new IOException())
        .anyTimes();

    mainTransport.close();

    EasyMock.expectLastCall().anyTimes();

    mainTransport.initialize(
        EasyMock.isA(Connection.class), EasyMock.isA(IStreamServiceListener.class));

    EasyMock.expectLastCall().once();

    EasyMock.replay(mainTransport);

    IConnectionManager dtm =
        new DataTransferManager(connectionServiceStub, mainTransport, fallbackTransport);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    dtm.connect(new JID("foo@bar.com"));

    EasyMock.verify(mainTransport);

    assertEquals(
        "Wrong transport fallback", StreamMode.IBB, dtm.getTransferMode(new JID("foo@bar.com")));
  }

  @Test
  public void testForceIBBOnly() throws Exception {

    mainTransport = new Transport(StreamMode.SOCKS5_DIRECT);
    fallbackTransport = new Transport(StreamMode.IBB);

    DataTransferManager dtm =
        new DataTransferManager(connectionServiceStub, mainTransport, fallbackTransport);

    dtm.setServices(IConnectionManager.IBB_SERVICE);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    dtm.connect(new JID("foo@bar.com"));

    assertEquals(
        "only IBB transport should be used",
        StreamMode.IBB,
        dtm.getTransferMode(new JID("foo@bar.com")));
  }

  @Test
  public void testConnectionCaching() throws Exception {

    mainTransport = new Transport(StreamMode.SOCKS5_DIRECT);

    IConnectionManager dtm = new DataTransferManager(connectionServiceStub, mainTransport, null);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    dtm.connect(new JID("foo@bar.com"));
    dtm.connect(new JID("foo@bar.de"));
    dtm.connect(new JID("foo@bar.com"));

    assertEquals("connection caching failed", 2, mainTransport.getEstablishedStreams().size());

    assertNotSame(
        "connection caching failed",
        mainTransport.getEstablishedStreams().get(0),
        mainTransport.getEstablishedStreams().get(1));
  }

  @Test
  public void testGetTransferMode() throws Exception {
    mainTransport = new Transport(StreamMode.SOCKS5_DIRECT);

    IConnectionManager dtm = new DataTransferManager(connectionServiceStub, mainTransport, null);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    dtm.connect(new JID("foo@bar.com"));

    assertEquals(
        "wrong transport mode returned",
        StreamMode.SOCKS5_DIRECT,
        dtm.getTransferMode(new JID("foo@bar.com")));

    assertEquals(
        "wrong transport mode returned",
        StreamMode.NONE,
        dtm.getTransferMode(new JID("nothing@all")));
  }

  public void testGetConnectionOnInvalidConnectionIdentifierWithNoConnection() throws Exception {
    mainTransport = new Transport(StreamMode.SOCKS5_DIRECT);

    DataTransferManager dtm = new DataTransferManager(connectionServiceStub, mainTransport, null);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    TransferDescription description = TransferDescription.newDescription();

    assertNull(dtm.getConnection("foo", new JID("foo@bar.com")));
  }

  public void testGetConnectionOnInvalidConnectionIdentifier() throws Exception {
    mainTransport = new Transport(StreamMode.SOCKS5_DIRECT);

    DataTransferManager dtm = new DataTransferManager(connectionServiceStub, mainTransport, null);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    dtm.connect("bar", new JID("foo@bar.com"));
    assertNull(dtm.getConnection("foo", new JID("foo@bar.com")));
  }

  @Test
  public void testGetConnectionOnValidConnectionIdentifier() throws Exception {
    mainTransport = new Transport(StreamMode.SOCKS5_DIRECT);

    DataTransferManager dtm = new DataTransferManager(connectionServiceStub, mainTransport, null);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    dtm.connect("foo", new JID("foo@bar.com"));

    assertNotNull(dtm.getConnection("foo", new JID("foo@bar.com")));
  }

  @Test(timeout = 30000)
  public void testConcurrentConnections() throws Exception {

    final CountDownLatch connectAcknowledge = new CountDownLatch(1);
    final CountDownLatch connectProceed = new CountDownLatch(1);

    Set<JID> nonBlockingConnects = new HashSet<JID>();

    nonBlockingConnects.add(new JID("foo@bar.example"));

    mainTransport =
        new BlockableTransport(
            nonBlockingConnects, StreamMode.SOCKS5_DIRECT, connectAcknowledge, connectProceed);

    fallbackTransport = new Transport(StreamMode.IBB);

    final IConnectionManager dtm =
        new DataTransferManager(connectionServiceStub, mainTransport, fallbackTransport);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    TestThread connectThread0 =
        new TestThread(
            new TestThread.Runnable() {
              @Override
              public void run() throws Exception {
                dtm.connect(new JID("foo@bar.com"));
              }
            });

    TestThread connectThread1 =
        new TestThread(
            new TestThread.Runnable() {
              @Override
              public void run() throws Exception {
                dtm.connect(new JID("foo@bar.com"));
              }
            });

    TestThread connectThread2 =
        new TestThread(
            new TestThread.Runnable() {
              @Override
              public void run() throws Exception {
                dtm.connect(new JID("foo@bar.example"));
              }
            });

    dtm.connect(new JID("foo@bar.example"));

    // side effect ! this JID is no longer ignored
    nonBlockingConnects.clear();

    connectThread0.start();

    if (!connectAcknowledge.await(10000, TimeUnit.MILLISECONDS)) {
      connectThread0.interrupt();
      fail("transport connect method was not called");
    }

    long currentTime = System.currentTimeMillis();

    connectThread1.start();

    // poll thread status
    while ((connectThread1.getState() != Thread.State.BLOCKED
            && connectThread1.getState() != Thread.State.WAITING)
        && (System.currentTimeMillis() - currentTime < 1000)) Thread.yield();

    if (connectThread1.getState() != Thread.State.BLOCKED
        && connectThread1.getState() != Thread.State.WAITING) {
      connectProceed.countDown();
      connectThread0.interrupt();
      connectThread1.interrupt();
      fail("second connection request must be blocked");
    }

    // This MUST not block because the connection is already established
    connectThread2.start();
    connectThread2.join(10000);

    try {
      connectThread2.verify();
    } finally {
      // release lock so the other 2 thread will not idle forever
      connectProceed.countDown();
    }

    connectThread0.join(10000);
    connectThread1.join(10000);

    connectThread0.verify();
    connectThread1.verify();

    assertEquals(
        "connection caching failed during multiple connection requests",
        2,
        mainTransport.getEstablishedStreams().size());
  }

  @Test
  public void connectWithRemoteSideConnectedFirst() throws Exception {
    mainTransport = new Transport(StreamMode.SOCKS5_DIRECT);

    IConnectionManager dtm = new DataTransferManager(connectionServiceStub, mainTransport, null);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    mainTransport.announceIncomingRequest(
        DataTransferManager.DEFAULT_CONNECTION_ID, new JID("fallback@emergency"));
    dtm.connect(new JID("fallback@emergency"));

    assertEquals(
        "established an outgoing connection also the remote side is already connected to the local side",
        1,
        mainTransport.getEstablishedStreams().size());
  }

  @Test(timeout = 30000)
  public void connectToRemoteSideWhileRemoteIsConnectingToLocalSide() throws Exception {

    final CountDownLatch connectAcknowledge = new CountDownLatch(1);
    final CountDownLatch connectProceed = new CountDownLatch(1);

    mainTransport =
        new BlockableTransport(
            new HashSet<JID>(), StreamMode.SOCKS5_DIRECT, connectAcknowledge, connectProceed);

    fallbackTransport = new Transport(StreamMode.IBB);

    final DataTransferManager dtm =
        new DataTransferManager(connectionServiceStub, mainTransport, fallbackTransport);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    TestThread connectThread0 =
        new TestThread(
            new TestThread.Runnable() {
              @Override
              public void run() throws Exception {
                dtm.connect(new JID("foo@bar.com"));
              }
            });

    connectThread0.start();

    if (!connectAcknowledge.await(10000, TimeUnit.MILLISECONDS)) {
      connectThread0.interrupt();
      fail("transport connect method was not called");
    }

    fallbackTransport.announceIncomingRequest(
        DataTransferManager.DEFAULT_CONNECTION_ID, new JID("foo@bar.com"));

    connectProceed.countDown();
    connectThread0.join(10000);
    connectThread0.verify();

    TransferDescription description = TransferDescription.newDescription();

    description.setRecipient(new JID("foo@bar.com"));
    description.setNamespace("http://example,org");
    description.setElementName("dummy");

    ((IPacketConnection) dtm.getConnection(null, new JID("foo@bar.com")))
        .send(description, new byte[1]);

    assertTrue(
        "wrong transport was chosen",
        ((DummyByteStream) mainTransport.getEstablishedStreams().get(0)).wasUsed());

    assertFalse(
        "wrong transport was chosen",
        ((DummyByteStream) fallbackTransport.getEstablishedStreams().get(0)).wasUsed());
  }

  @Test
  public void testConnectionClosureOnManualClose() throws Exception {
    mainTransport = new Transport(StreamMode.SOCKS5_DIRECT);

    IConnectionManager dtm = new DataTransferManager(connectionServiceStub, mainTransport, null);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    dtm.connect(new JID("fallback@emergency"));
    mainTransport.announceIncomingRequest(
        DataTransferManager.DEFAULT_CONNECTION_ID, new JID("fallback@emergency"));

    dtm.closeConnection(new JID("fallback@emergency"));

    assertTrue(
        "outgoing connection was not closed",
        ((DummyByteStream) mainTransport.getEstablishedStreams().get(0)).isClosed());

    assertTrue(
        "incoming connection was not closed",
        ((DummyByteStream) mainTransport.getEstablishedStreams().get(1)).isClosed());

    assertEquals(StreamMode.NONE, dtm.getTransferMode(new JID("fallback@emergency")));
  }

  @Test
  public void testConnectionCloseOnDisconnect() throws Exception {
    Transport mainTransport = new Transport(StreamMode.SOCKS5_DIRECT);

    IConnectionManager dtm = new DataTransferManager(connectionServiceStub, mainTransport, null);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    dtm.connect(new JID("fallback@emergency"));
    mainTransport.announceIncomingRequest(
        DataTransferManager.DEFAULT_CONNECTION_ID, new JID("fallback@emergency"));

    connectionListener
        .getValue()
        .connectionStateChanged(connectionMock, ConnectionState.NOT_CONNECTED);

    assertTrue(
        "outgoing connection was not closed",
        ((DummyByteStream) mainTransport.getEstablishedStreams().get(0)).isClosed());

    assertTrue(
        "incoming connection was not closed",
        ((DummyByteStream) mainTransport.getEstablishedStreams().get(1)).isClosed());

    assertEquals(StreamMode.NONE, dtm.getTransferMode(new JID("fallback@emergency")));
  }
}
