package de.fu_berlin.inf.dpp.net.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionManager;
import de.fu_berlin.inf.dpp.net.stream.IStreamService;
import de.fu_berlin.inf.dpp.net.stream.StreamMode;
import de.fu_berlin.inf.dpp.net.xmpp.IConnectionListener;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.net.xmpp.XMPPConnectionService;
import de.fu_berlin.inf.dpp.test.util.TestThread;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.jivesoftware.smack.Connection;
import org.junit.Before;
import org.junit.Test;

public class DataTransferManagerTest {

  private static class Transport implements IStreamService {

    private List<ChannelConnection> establishedConnections = new ArrayList<ChannelConnection>();

    private IByteStreamConnectionListener listener;

    private StreamMode mode;

    private String connectionID;

    public Transport(StreamMode mode) {
      this.mode = mode;
    }

    @Override
    public synchronized IByteStreamConnection connect(String connectionIdentifier, JID peer)
        throws IOException, InterruptedException {

      connectionID = connectionIdentifier;

      ChannelConnection connection = new ChannelConnection(peer, mode, listener);

      establishedConnections.add(connection);
      return connection;
    }

    public synchronized void announceIncomingRequest(JID peer) {
      ChannelConnection connection = new ChannelConnection(peer, mode, listener);

      establishedConnections.add(connection);
      listener.connectionChanged(connectionID, connection, true);
    }

    @Override
    public void initialize(Connection connection, IByteStreamConnectionListener listener) {
      this.listener = listener;
    }

    @Override
    public void uninitialize() {
      this.listener = null;
    }

    public synchronized List<ChannelConnection> getEstablishedConnections() {
      return establishedConnections;
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
    public IByteStreamConnection connect(String connectionIdentifier, JID peer)
        throws IOException, InterruptedException {

      if (jidsToIgnore.contains(peer)) return super.connect(connectionIdentifier, peer);

      synchronized (this) {
        if (isConnecting)
          throw new IllegalStateException("connect must not be called concurrently");
        isConnecting = true;
      }

      acknowledge.countDown();
      proceed.await();
      IByteStreamConnection connection = super.connect(connectionIdentifier, peer);
      isConnecting = false;
      return connection;
    }
  }

  private static class ChannelConnection implements IByteStreamConnection {

    private JID to;
    private StreamMode mode;
    private IByteStreamConnectionListener listener;
    private volatile boolean closed;
    private volatile int sendPackets;

    public ChannelConnection(JID to, StreamMode mode, IByteStreamConnectionListener listener) {
      this.to = to;
      this.mode = mode;
      this.listener = listener;
    }

    @Override
    public JID getRemoteAddress() {
      return to;
    }

    @Override
    public void close() {
      closed = true;
      listener.connectionClosed(/* FIMXE */ null, this);
    }

    @Override
    public boolean isConnected() {
      return !closed;
    }

    @Override
    public void send(TransferDescription data, byte[] content) throws IOException {
      sendPackets++;
    }

    @Override
    public StreamMode getMode() {
      return mode;
    }

    public int getSendPacketsCount() {
      return sendPackets;
    }

    @Override
    public String getConnectionID() {
      return null;
    }

    @Override
    public void initialize() {
      // NOP
    }
  }

  private XMPPConnectionService connectionServiceStub;

  private Capture<IConnectionListener> connectionListener = new Capture<IConnectionListener>();

  private Connection connectionMock;

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

  @Test(expected = NullPointerException.class)
  public void testEstablishConnectionWithNullPeer() throws Exception {

    IConnectionManager dtm = new DataTransferManager(connectionServiceStub, null, null, null);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    dtm.connect("foo", null);
  }

  @Test(expected = NullPointerException.class)
  public void testEstablishConnectionWithNullConnectionID() throws Exception {

    IConnectionManager dtm = new DataTransferManager(connectionServiceStub, null, null, null);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    dtm.connect(null, new JID("foo@bar.com"));
  }

  @Test(expected = IOException.class)
  public void testEstablishConnectionWithNoTransports() throws Exception {

    IConnectionManager dtm = new DataTransferManager(connectionServiceStub, null, null, null);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    dtm.connect(new JID("foo@bar.com"));
  }

  @Test
  public void testEstablishConnectionWithMainAndFallbackTransport() throws Exception {

    IStreamService mainTransport = new Transport(StreamMode.SOCKS5_DIRECT);
    IStreamService fallbackTransport = new Transport(StreamMode.IBB);

    IConnectionManager dtm =
        new DataTransferManager(connectionServiceStub, null, mainTransport, fallbackTransport);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    dtm.connect(new JID("foo@bar.com"));
    assertEquals(StreamMode.SOCKS5_DIRECT, dtm.getTransferMode(new JID("foo@bar.com")));
  }

  @Test
  public void testEstablishConnectionWithMainAndFallbackTransportAndUsingFallback()
      throws Exception {

    IStreamService mainTransport = EasyMock.createMock(IStreamService.class);

    IStreamService fallbackTransport = new Transport(StreamMode.IBB);

    EasyMock.expect(mainTransport.connect(EasyMock.isA(String.class), EasyMock.isA(JID.class)))
        .andThrow(new IOException())
        .anyTimes();

    mainTransport.initialize(
        EasyMock.isA(Connection.class), EasyMock.isA(IByteStreamConnectionListener.class));

    EasyMock.expectLastCall().once();

    EasyMock.replay(mainTransport);

    IConnectionManager dtm =
        new DataTransferManager(connectionServiceStub, null, mainTransport, fallbackTransport);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    dtm.connect(new JID("foo@bar.com"));

    EasyMock.verify(mainTransport);

    assertEquals(
        "Wrong transport fallback", StreamMode.IBB, dtm.getTransferMode(new JID("foo@bar.com")));
  }

  @Test
  public void testForceIBBOnly() throws Exception {

    IStreamService mainTransport = new Transport(StreamMode.SOCKS5_DIRECT);
    IStreamService fallbackTransport = new Transport(StreamMode.IBB);

    DataTransferManager dtm =
        new DataTransferManager(connectionServiceStub, null, mainTransport, fallbackTransport);

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

    Transport mainTransport = new Transport(StreamMode.SOCKS5_DIRECT);

    IConnectionManager dtm =
        new DataTransferManager(connectionServiceStub, null, mainTransport, null);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    dtm.connect(new JID("foo@bar.com"));
    dtm.connect(new JID("foo@bar.de"));
    dtm.connect(new JID("foo@bar.com"));

    assertEquals("connection caching failed", 2, mainTransport.getEstablishedConnections().size());

    assertNotSame(
        "connection caching failed",
        mainTransport.getEstablishedConnections().get(0),
        mainTransport.getEstablishedConnections().get(1));
  }

  @Test
  public void testGetTransferMode() throws Exception {
    IStreamService mainTransport = new Transport(StreamMode.SOCKS5_DIRECT);

    IConnectionManager dtm =
        new DataTransferManager(connectionServiceStub, null, mainTransport, null);

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

  @Test(expected = IOException.class)
  public void testSendOnInvalidConnectionIdentifierWithNoConnection() throws Exception {
    IStreamService mainTransport = new Transport(StreamMode.SOCKS5_DIRECT);

    DataTransferManager dtm =
        new DataTransferManager(connectionServiceStub, null, mainTransport, null);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    TransferDescription description = TransferDescription.newDescription();

    description.setRecipient(new JID("foo@bar.com"));

    dtm.sendData("foo", description, new byte[0]);
  }

  @Test(expected = IOException.class)
  public void testSendOnInvalidConnectionIdentifier() throws Exception {
    IStreamService mainTransport = new Transport(StreamMode.SOCKS5_DIRECT);

    DataTransferManager dtm =
        new DataTransferManager(connectionServiceStub, null, mainTransport, null);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    TransferDescription description = TransferDescription.newDescription();

    dtm.connect("bar", new JID("foo@bar.com"));

    description.setRecipient(new JID("foo@bar.com"));

    dtm.sendData("foo", description, new byte[0]);
  }

  @Test
  public void testSendOnValidConnectionIdentifier() throws Exception {
    Transport mainTransport = new Transport(StreamMode.SOCKS5_DIRECT);

    DataTransferManager dtm =
        new DataTransferManager(connectionServiceStub, null, mainTransport, null);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    TransferDescription description = TransferDescription.newDescription();

    dtm.connect("foo", new JID("foo@bar.com"));

    description.setRecipient(new JID("foo@bar.com"));

    dtm.sendData("foo", description, new byte[0]);
  }

  @Test(timeout = 30000)
  public void testConcurrentConnections() throws Exception {

    final CountDownLatch connectAcknowledge = new CountDownLatch(1);
    final CountDownLatch connectProceed = new CountDownLatch(1);

    Set<JID> nonBlockingConnects = new HashSet<JID>();

    nonBlockingConnects.add(new JID("foo@bar.example"));

    BlockableTransport mainTransport =
        new BlockableTransport(
            nonBlockingConnects, StreamMode.SOCKS5_DIRECT, connectAcknowledge, connectProceed);

    Transport fallbackTransport = new Transport(StreamMode.IBB);

    final IConnectionManager dtm =
        new DataTransferManager(connectionServiceStub, null, mainTransport, fallbackTransport);

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
        mainTransport.getEstablishedConnections().size());
  }

  @Test
  public void connectWithRemoteSideConnectedFirst() throws Exception {
    Transport mainTransport = new Transport(StreamMode.SOCKS5_DIRECT);

    IConnectionManager dtm =
        new DataTransferManager(connectionServiceStub, null, mainTransport, null);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    mainTransport.announceIncomingRequest(new JID("fallback@emergency"));
    dtm.connect(new JID("fallback@emergency"));

    assertEquals(
        "established an outgoing connection also the remote side is already connected to the local side",
        1,
        mainTransport.getEstablishedConnections().size());
  }

  @Test(timeout = 30000)
  public void connectToRemoteSideWhileRemoteIsConnectingToLocalSide() throws Exception {

    final CountDownLatch connectAcknowledge = new CountDownLatch(1);
    final CountDownLatch connectProceed = new CountDownLatch(1);

    BlockableTransport mainTransport =
        new BlockableTransport(
            new HashSet<JID>(), StreamMode.SOCKS5_DIRECT, connectAcknowledge, connectProceed);

    Transport fallbackTransport = new Transport(StreamMode.IBB);

    final DataTransferManager dtm =
        new DataTransferManager(connectionServiceStub, null, mainTransport, fallbackTransport);

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

    fallbackTransport.announceIncomingRequest(new JID("foo@bar.com"));

    connectProceed.countDown();
    connectThread0.join(10000);
    connectThread0.verify();

    TransferDescription description = TransferDescription.newDescription();

    description.setRecipient(new JID("foo@bar.com"));

    dtm.sendData(description, new byte[0]);

    assertEquals(
        "wrong connection was chosen",
        1,
        mainTransport.getEstablishedConnections().get(0).getSendPacketsCount());

    assertEquals(
        "wrong connection was chosen",
        0,
        fallbackTransport.getEstablishedConnections().get(0).getSendPacketsCount());
  }

  @Test
  public void testConnectionClosureOnManualClose() throws Exception {
    Transport mainTransport = new Transport(StreamMode.SOCKS5_DIRECT);

    IConnectionManager dtm =
        new DataTransferManager(connectionServiceStub, null, mainTransport, null);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    dtm.connect(new JID("fallback@emergency"));
    mainTransport.announceIncomingRequest(new JID("fallback@emergency"));

    dtm.closeConnection(new JID("fallback@emergency"));

    assertFalse(
        "outgoing connection was not closed",
        mainTransport.getEstablishedConnections().get(0).isConnected());

    assertFalse(
        "incoming connection was not closed",
        mainTransport.getEstablishedConnections().get(1).isConnected());

    assertEquals(StreamMode.NONE, dtm.getTransferMode(new JID("fallback@emergency")));
  }

  @Test
  public void testConnectionClosureOnDisconnect() throws Exception {
    Transport mainTransport = new Transport(StreamMode.SOCKS5_DIRECT);

    IConnectionManager dtm =
        new DataTransferManager(connectionServiceStub, null, mainTransport, null);

    connectionListener.getValue().connectionStateChanged(connectionMock, ConnectionState.CONNECTED);

    dtm.connect(new JID("fallback@emergency"));
    mainTransport.announceIncomingRequest(new JID("fallback@emergency"));

    connectionListener
        .getValue()
        .connectionStateChanged(connectionMock, ConnectionState.NOT_CONNECTED);

    assertFalse(
        "outgoing connection was not closed",
        mainTransport.getEstablishedConnections().get(0).isConnected());

    assertFalse(
        "incoming connection was not closed",
        mainTransport.getEstablishedConnections().get(1).isConnected());

    assertEquals(StreamMode.NONE, dtm.getTransferMode(new JID("fallback@emergency")));
  }
}
